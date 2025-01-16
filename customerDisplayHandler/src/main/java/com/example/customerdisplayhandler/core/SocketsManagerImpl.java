package com.example.customerdisplayhandler.core;

import android.util.Pair;

import com.example.customerdisplayhandler.core.interfaces.ISocketsManager;
import com.example.customerdisplayhandler.model.ServiceInfo;

import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.subjects.ReplaySubject;

public class SocketsManagerImpl implements ISocketsManager {
    private static final String TAG = SocketsManagerImpl.class.getSimpleName();
    private static SocketsManagerImpl instance;
    private List<Pair<Socket, ServiceInfo>> connectedSockets = new ArrayList<>();
    private ReplaySubject<Pair<Socket, ServiceInfo>> connectedSocketsSubject = ReplaySubject.create();

    private SocketsManagerImpl() {
    }

    public synchronized static SocketsManagerImpl getInstance() {
        if (instance == null) {
            instance = new SocketsManagerImpl();
        }
        return instance;
    }

    @Override
    public void addConnectedSocket(Socket socket, ServiceInfo serviceInfo) {
        if (getConnectedSocket(serviceInfo.getServerId()) != null) {
            removeConnectedSocket(serviceInfo.getServerId());
        }
        connectedSockets.add(new Pair<>(socket, serviceInfo));
        connectedSocketsSubject.onNext(new Pair<>(socket, serviceInfo));
    }
@Override
    public Pair<Socket, ServiceInfo> getConnectedSocket(String serverId) {
        for (Pair<Socket, ServiceInfo> connectedSocket : connectedSockets) {
            if (connectedSocket.second.getServerId().equals(serverId)) {
                return connectedSocket;
            }
        }
        return null;
    }
@Override
    public void removeConnectedSocket(String serverId) {
        Pair<Socket, ServiceInfo> connectedSocket = getConnectedSocket(serverId);
        if (connectedSocket != null) {
            connectedSockets.remove(connectedSocket);
        }
    }
@Override
    public Socket findSocketIfConnected(String serverId) {
        for (Pair<Socket, ServiceInfo> connectedSocket : connectedSockets) {
            if (connectedSocket.second.getServerId() != null && connectedSocket.second.getServerId().equals(serverId)) {
                if (connectedSocket.first.isConnected() && !connectedSocket.first.isClosed()) {
                    return connectedSocket.first;
                } else{
                    removeConnectedSocket(serverId);
                }
            }
        }
        return null;
    }

    public ReplaySubject<Pair<Socket, ServiceInfo>> getConnectedSocketsSubject() {
        return connectedSocketsSubject;
    }

}
