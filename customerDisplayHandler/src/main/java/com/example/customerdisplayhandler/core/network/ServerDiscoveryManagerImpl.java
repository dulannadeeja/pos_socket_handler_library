package com.example.customerdisplayhandler.core.network;

import android.util.Log;
import android.util.Pair;

import com.example.customerdisplayhandler.model.ServerInfo;
import com.example.customerdisplayhandler.model.SocketMessageBase;
import com.example.customerdisplayhandler.utils.SocketConfigConstants;
import com.example.customerdisplayhandler.core.interfaces.IConnectedServerManager;
import com.example.customerdisplayhandler.utils.IJsonUtil;
import com.example.customerdisplayhandler.core.interfaces.IServerDiscoveryManager;
import com.example.customerdisplayhandler.core.interfaces.ISocketConnectionManager;

import java.net.Socket;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class ServerDiscoveryManagerImpl implements IServerDiscoveryManager {
    private final static String TAG = ServerDiscoveryManagerImpl.class.getSimpleName();
    private final ISocketConnectionManager socketConnectionManager;
    private final IConnectedServerManager connectedServerManager;
    private final IJsonUtil jsonUtil;

    public ServerDiscoveryManagerImpl(ISocketConnectionManager socketConnectionManager, IConnectedServerManager connectedServerManager, IJsonUtil jsonUtil) {
        this.socketConnectionManager = socketConnectionManager;
        this.connectedServerManager = connectedServerManager;
        this.jsonUtil = jsonUtil;
    }

    @Override
    public Flowable<Pair<ServerInfo, Socket>> searchAvailableServersInNetwork(String localIPAddress, int serverPort) {
        return Flowable.range(0, 254) // Create a range of numbers from 0 to 255
                .parallel() // Parallelize the flowable stream
                .runOn(Schedulers.io()) // Assign each task to an IO thread
                .map(i -> baseIP(localIPAddress) + i) // Generate the IP address
                .filter(ip -> !ip.equals(localIPAddress) && !isAlreadyConnected(ip)) // Filter invalid or connected IPs
                .flatMap(ip -> inspectIP(ip, serverPort)
                        .doOnSuccess(pair -> {
                            connectedServerManager.removeUnknownSocket(pair.first.getServerID());
                            connectedServerManager.addConnectedServer(pair.first, pair.second);
                        })
                        .toFlowable()
                ) // Inspect each IP
                .sequential() // Combine results back into a single flowable
                .timeout(SocketConfigConstants.CONNECTION_REQUEST_TIMEOUT, TimeUnit.MILLISECONDS) // Set a timeout of 5 seconds
                .doOnSubscribe(disposable -> Log.i(TAG, "Started scanning the network"))
                .doOnComplete(() -> Log.i(TAG, "Completed scanning the network"));
    }

    private String baseIP(String localIPAddress) {
        String[] ipParts = localIPAddress.split("\\.");
//        return ipParts[0] + "." + ipParts[1] + "." + ipParts[2] + ".";
        return "10.30.100.";
    }

    private boolean isAlreadyConnected(String ip) {
        List<String> connectedServerIPs = new java.util.ArrayList<>();
        connectedServerManager.getDiscoveredServers().forEach(connectedServer -> {
            connectedServerIPs.add(connectedServer.first.getServerIpAddress());
        });
        connectedServerManager.getUnknownSockets().forEach(unknownSocket -> {
            connectedServerIPs.add(unknownSocket.second.getInetAddress().getHostAddress());
        });
        return connectedServerIPs.contains(ip);
    }

    private Single<Pair<ServerInfo, Socket>> inspectIP(String ip, int serverPort) {
        return socketConnectionManager.connectToServer(ip, serverPort)
                .flatMap(socket -> {
                    String socketId = UUID.randomUUID().toString();
                    ServerInfo serverInfo = new ServerInfo(socketId, socket.getInetAddress().getHostAddress(), "Unknown");
                    connectedServerManager.addUnknownSocket(socketId, socket);
                    return Single.just(new Pair<>(serverInfo, socket));
                }).flatMap(pair -> waitingForHandshakeFromServer(pair.first.getServerID())
                        .map(serverInfo -> new Pair<>(serverInfo, pair.second)))
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
