package com.example.customerdisplayhandler.core.interfaces;

import android.util.Pair;
import com.example.customerdisplayhandler.core.network.ObservableData;
import com.example.customerdisplayhandler.model.ClientInfo;
import com.example.customerdisplayhandler.model.ServerInfo;

import java.net.Socket;
import java.util.List;
import java.util.Map;

public interface ISocketDataSource {
    void getAvailableServers(String localIpAddress, int serverPort, OnServerScanCompleted onServerScanCompleted);
    void sendConnectionRequest(ServerInfo serverInfo, ClientInfo clientInfo);
    void listenForServerMessages(ServerInfo serverInfo);
    void listenForUnknownServers(Socket socket,ServerInfo serverInfo);
    void getClientInfo(OnClientInfoRetrieved onClientInfoRetrieved);
    void saveClientInfo(ClientInfo clientInfo, OnClientInfoSaved onClientInfoSaved);
    void addActiveServer(ServerInfo serverInfo);
    List<ServerInfo> getActiveServers();
    ObservableData<Pair<ServerInfo, String>> getUnknownServerMessageStreamObservable();
    ObservableData<Pair<ServerInfo, String>> getServerMessageStreamObservable();
    ObservableData<Map<ServerInfo, Socket>> getActiveServerConnections();
    interface OnClientInfoRetrieved {
        void onSucess(ClientInfo serverInfo);
        void onError(Exception e);
    }

    interface OnClientInfoSaved {
        void onSucess();
        void onError(Exception e);
    }

    interface OnMessageReceived {
        void onMessageReceived(ServerInfo serverInfo,String message);
    }

    interface OnServerScanCompleted {
        void onSucess(Map<ServerInfo, Socket> serverInfoList);
    }

    interface OnConnectionApprovalReceived{
        void isApproved(Boolean isApproved);
    }

}
