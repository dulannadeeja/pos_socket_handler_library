package com.example.customerdisplayhandler.core.interfaces;

import android.util.Pair;

import com.example.customerdisplayhandler.model.ClientInfo;
import com.example.customerdisplayhandler.model.ServerInfo;

import java.net.Socket;
import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.subjects.BehaviorSubject;

public interface IConnectedServerManager {
    void addUnknownSocket(String socketId,Socket socket);
    void removeUnknownSocket(String socketId);
    void addConnectedServer(ServerInfo serverInfo, Socket socket);
    void closeAllConnections();
    Completable sendMessageToServer(String serverId, String message);
    Completable startListening(String serverId, Socket socket);
    Completable safelyStopListening(String serverId, Socket socket);
    BehaviorSubject<Pair<String,String>> getServerMessageSubject();
    List<Pair<ServerInfo, Socket>> getDiscoveredServers();
    List<Pair<String, Socket>> getUnknownSockets();
    List<Pair<ServerInfo, Socket>> getEstablishedConnections();
    void startPairingServer(ServerInfo serverInfo, ClientInfo clientInfo, OnPairingServerListener listener);
    Completable clearInactiveServers();
    interface OnPairingServerListener{
        void onPairingServerStarted();
        void onConnectionRequestSent();
        void onConnectionRequestFailed();
        void onConnectionRequestApproved();
        void onConnectionRequestRejected();
    }
}
