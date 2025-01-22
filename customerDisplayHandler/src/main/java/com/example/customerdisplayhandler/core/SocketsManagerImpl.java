package com.example.customerdisplayhandler.core;

import android.util.Log;
import android.util.Pair;

import com.example.customerdisplayhandler.constants.NetworkConstants;
import com.example.customerdisplayhandler.core.interfaces.ISocketsManager;
import com.example.customerdisplayhandler.core.interfaces.ITcpConnector;
import com.example.customerdisplayhandler.core.interfaces.ITcpMessageListener;
import com.example.customerdisplayhandler.model.CustomerDisplay;
import com.example.customerdisplayhandler.model.ServiceInfo;

import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.subjects.ReplaySubject;

public class SocketsManagerImpl implements ISocketsManager {
    private static final String TAG = SocketsManagerImpl.class.getSimpleName();
    private static SocketsManagerImpl instance;
    private ITcpConnector tcpConnector;
    private ITcpMessageListener tcpMessageListener;
    private final int serverPort = NetworkConstants.DEFAULT_SERVER_PORT;
    private final List<Pair<String, Socket>> connectedSockets = new ArrayList<>();
    private PublishSubject<Pair<String, Socket>> socketConnectionSubject = PublishSubject.create();

    private SocketsManagerImpl(ITcpConnector tcpConnector, ITcpMessageListener tcpMessageListener) {
        this.tcpConnector = tcpConnector;
        this.tcpMessageListener = tcpMessageListener;
    }

    public synchronized static SocketsManagerImpl getInstance(ITcpConnector tcpConnector, ITcpMessageListener tcpMessageListener) {
        if (instance == null) {
            instance = new SocketsManagerImpl(tcpConnector, tcpMessageListener);
        }
        return instance;
    }

    public Single<Socket> reconnectIfDisconnected(String serverId, String serverIpAddress) {

        if (serverId == null || serverId.isEmpty() || serverIpAddress == null || serverIpAddress.isEmpty()) {
            Log.wtf(TAG, "serverId: " + serverId + " serverIpAddress: " + serverIpAddress);
            throw new IllegalArgumentException("serverId and serverIpAddress must not be null or empty");
        }

        Socket existingSocket = findSocket(serverId);
        if (isSocketInvalid(existingSocket)) {
            if (existingSocket != null) {
                removeSocket(serverId);
            }
            return connectAndStoreSocket(serverId, serverIpAddress);
        }
        return Single.just(existingSocket);
    }

    public Single<Socket> reconnect(String serverId, String serverIpAddress) {

        if (serverId == null || serverId.isEmpty() || serverIpAddress == null || serverIpAddress.isEmpty()) {
            Log.wtf(TAG, "serverId: " + serverId + " serverIpAddress: " + serverIpAddress);
            throw new IllegalArgumentException("serverId and serverIpAddress must not be null or empty");
        }

        Socket existingSocket = findSocket(serverId);
        boolean isSocketInvalid = isSocketInvalid(existingSocket);
        removeSocket(serverId);
        if (isSocketInvalid) {
            return connectAndStoreSocket(serverId, serverIpAddress);
        } else {
            return tcpConnector.disconnectSafelyFromServer(existingSocket)
                    .andThen(connectAndStoreSocket(serverId, serverIpAddress));
        }
    }

    public Single<Socket> tryToReconnect(String serverId, String serverIpAddress) {
        if (serverId == null || serverId.isEmpty() || serverIpAddress == null || serverIpAddress.isEmpty()) {
            Log.wtf(TAG, "serverId: " + serverId + " serverIpAddress: " + serverIpAddress);
            throw new IllegalArgumentException("serverId and serverIpAddress must not be null or empty");
        }
        Socket existingSocket = findSocket(serverId);
        boolean isSocketInvalid = isSocketInvalid(existingSocket);
        removeSocket(serverId);
        if (isSocketInvalid) {
            return tcpConnector.tryToConnectWithingTimeout(serverIpAddress, serverPort, NetworkConstants.WAITING_FOR_SOCKET_CONNECTION_TIMEOUT)
                    .doOnSuccess(socket -> addSocket(serverId, socket));
        } else {
            return tcpConnector.disconnectSafelyFromServer(existingSocket)
                    .andThen(tcpConnector.tryToConnectWithingTimeout(serverIpAddress, serverPort, NetworkConstants.WAITING_FOR_SOCKET_CONNECTION_TIMEOUT))
                    .doOnSuccess(socket -> addSocket(serverId, socket));
        }
    }

    public Completable disconnectIfConnected(String serverId) {
        Socket existingSocket = findSocket(serverId);
        boolean isSocketInvalid = isSocketInvalid(existingSocket);
        if (isSocketInvalid) {
            return tcpConnector.disconnectSafelyFromServer(existingSocket)
                    .doOnComplete(() -> removeSocket(serverId));
        }else {
            removeSocket(serverId);
        }
        return Completable.complete();
    }

    private boolean isSocketInvalid(Socket socket) {
        return socket == null || socket.isClosed() || !socket.isConnected();
    }

    private Single<Socket> connectAndStoreSocket(String serverId, String serverIpAddress) {
        return tcpConnector.tryToConnectWithingTimeout(serverIpAddress, serverPort, NetworkConstants.WAITING_FOR_SOCKET_CONNECTION_TIMEOUT)
                .doOnSuccess(newSocket -> addSocket(serverId, newSocket));
    }

    private synchronized Socket findSocket(String serverId) {
        synchronized (connectedSockets) {
            for (Pair<String, Socket> connectedSocket : connectedSockets) {
                if (connectedSocket.first != null && connectedSocket.first.equals(serverId)) {
                    return connectedSocket.second;
                }
            }
            return null;
        }
    }

    private void addSocket(String serverId, Socket socket) {
        synchronized (connectedSockets) {
            if(socket == null || socket.isClosed() || !socket.isConnected() || serverId == null || serverId.isEmpty()){
                Log.wtf(TAG, "Cant add invalid socket to connectedSockets list");
                return;
            }
            connectedSockets.add(new Pair<>(serverId, socket));
            socketConnectionSubject.onNext(new Pair<>(serverId, socket));
        }
    }

    private void removeSocket(String serverId) {
        synchronized (connectedSockets) {
            for (Pair<String, Socket> connectedSocket : connectedSockets) {
                if (connectedSocket.first.equals(serverId)) {
                    connectedSockets.remove(connectedSocket);
                    break;
                }
            }
        }
    }

    public PublishSubject<Pair<String, Socket>> getSocketConnectionSubject() {
        return socketConnectionSubject;
    }

}
