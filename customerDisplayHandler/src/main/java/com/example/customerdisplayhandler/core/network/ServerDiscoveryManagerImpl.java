package com.example.customerdisplayhandler.core.network;

import android.util.Log;
import android.util.Pair;

import com.example.customerdisplayhandler.model.ServerInfo;
import com.example.customerdisplayhandler.model.SocketMessageBase;
import com.example.customerdisplayhandler.utils.SocketConfigConstants;
import com.example.customerdisplayhandler.core.callbacks.OnConnectToServerCompleted;
import com.example.customerdisplayhandler.core.callbacks.OnSearchServerCompleted;
import com.example.customerdisplayhandler.core.interfaces.DataObserver;
import com.example.customerdisplayhandler.core.interfaces.IConnectedServerManager;
import com.example.customerdisplayhandler.core.interfaces.IJsonUtil;
import com.example.customerdisplayhandler.core.interfaces.IServerDiscoveryManager;
import com.example.customerdisplayhandler.core.interfaces.ISocketConnectionManager;
import com.example.customerdisplayhandler.core.interfaces.ISocketDataSource;

import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.core.BackpressureStrategy;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class ServerDiscoveryManagerImpl implements IServerDiscoveryManager {
    private final static String TAG = ServerDiscoveryManagerImpl.class.getSimpleName();
    private final ISocketConnectionManager socketConnectionManager;
    private final IConnectedServerManager connectedServerManager;
    private final IJsonUtil jsonUtil;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    public ServerDiscoveryManagerImpl(ISocketConnectionManager socketConnectionManager, IConnectedServerManager connectedServerManager, IJsonUtil jsonUtil) {
        this.socketConnectionManager = socketConnectionManager;
        this.connectedServerManager = connectedServerManager;
        this.jsonUtil = jsonUtil;
    }

    @Override
    public Flowable<Pair<ServerInfo, Socket>> searchAvailableServersInNetwork(String localIPAddress, int serverPort) {
        return Flowable.<Pair<ServerInfo, Socket>>create(emitter -> {
                    try {
                        String[] ipParts = localIPAddress.split("\\.");
                        String baseIP = ipParts[0] + "." + ipParts[1] + "." + ipParts[2] + ".";
                        // TODO: Remove hardcoded IP address, use the baseIP instead
                        baseIP = "10.30.100.";

                        // Use a CountDownLatch to wait for all IP checks to finish
                        CountDownLatch latch = new CountDownLatch(256);


                        for (int i = 0; i < 256; i++) {
                            String ip = baseIP + i;
                            if (!ip.equals(localIPAddress)) {
                                compositeDisposable.add(
                                        inspectIP(ip, serverPort)
                                                .doOnTerminate(() -> latch.countDown())
                                                .subscribe(pair -> {
                                                    emitter.onNext(pair);
                                                }, error -> {
                                                    // ignore errors
                                                })
                                );
                            }
                        }

                        // Wait for all IP checks to finish
                        latch.await();
                        emitter.onComplete();
                    } catch (Exception e) {
                        Log.e(TAG, "Error searching for servers in network: " + e.getMessage());
                    }
                }, BackpressureStrategy.BUFFER)
                .doOnSubscribe(disposable -> Log.i(TAG, "Searching for servers in network"))
                .doOnComplete(() -> Log.i(TAG, "Search for servers in network completed"))
                .subscribeOn(Schedulers.io());
    }

    private Single<Pair<ServerInfo, Socket>> inspectIP(String ip, int serverPort) {
        return socketConnectionManager.connectToServer(ip, serverPort)
                .flatMap(socket -> {
                    String serverID = UUID.randomUUID().toString();
                    ServerInfo serverInfo = new ServerInfo(serverID, ip, "Unknown");
                    connectedServerManager.addConnectedServer(socket, serverInfo);
                    return Single.just(new Pair<>(serverInfo, socket));
                }).flatMap(pair -> waitingForHandshakeFromServer(pair.first.getServerID())
                        .map(serverInfo -> new Pair<>(serverInfo, pair.second)))
                .timeout(SocketConfigConstants.CONNECTION_REQUEST_TIMEOUT, TimeUnit.MILLISECONDS) // Set a timeout for the handshake
                .doOnSuccess(pair -> Log.i(TAG, "IP inspected: " + ip))
                .subscribeOn(Schedulers.io());
    }

    private Single<ServerInfo> waitingForHandshakeFromServer(String serverId) {
        return connectedServerManager.getServerMessageSubject()
                .filter(pair -> pair.first.equals(serverId)) // Filter messages from the specified server
                .take(1) // Take only the first matching message
                .singleOrError()
                .flatMap(messagePair -> {
                    try {
                        ServerInfo serverInfo = processHandshake(messagePair.second);
                        return Single.just(serverInfo);
                    } catch (Exception e) {
                        return Single.error(e);
                    }
                })
                .doOnSubscribe(disposable -> Log.i(TAG, "Waiting for handshake from server: " + serverId))
                .doOnError(error -> Log.e(TAG, "Handshake timed out or failed for server: " + serverId + ", " + error.getMessage()))
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io());
    }

    private ServerInfo processHandshake(String message) throws Exception {
        if (message == null || message.isEmpty()) {
            throw new Exception("Invalid handshake message received: " + message);
        }
        SocketMessageBase socketMessageBase = jsonUtil.toObj(message, SocketMessageBase.class);
        if (socketMessageBase.getCommand() == null || socketMessageBase.getSenderId() == null || socketMessageBase.getData() == null) {
            throw new Exception("Invalid handshake message received: " + message);
        }

        if (!socketMessageBase.getCommand().equals(SocketConfigConstants.RESPONSE_COMMAND_HANDSHAKE)) {
            throw new Exception("Invalid handshake message received: " + message);
        }

        Object obj = socketMessageBase.getData();
        Log.i(TAG, "Handshake message received: " + jsonUtil.toJson(obj));
        ServerInfo serverInfo = jsonUtil.toObj(jsonUtil.toJson(obj), ServerInfo.class);
        if (serverInfo.getServerID() == null || serverInfo.getServerIpAddress() == null || serverInfo.getServerDeviceName() == null) {
            throw new Exception("Invalid handshake message received: " + message);
        }
        return serverInfo;
    }
}
