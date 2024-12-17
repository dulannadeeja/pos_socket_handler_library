package com.example.customerdisplayhandler.core.network;

import android.util.Log;
import android.util.Pair;

import com.example.customerdisplayhandler.helpers.SharedPrefManager;
import com.example.customerdisplayhandler.model.ClientInfo;
import com.example.customerdisplayhandler.model.ConnectionApproval;
import com.example.customerdisplayhandler.model.ServerInfo;
import com.example.customerdisplayhandler.model.SocketMessageBase;
import com.example.customerdisplayhandler.utils.SharedPrefLabels;
import com.example.customerdisplayhandler.utils.SocketConfigConstants;
import com.example.customerdisplayhandler.core.interfaces.DataObserver;
import com.example.customerdisplayhandler.core.interfaces.IConnectedServerManager;
import com.example.customerdisplayhandler.utils.IJsonUtil;
import com.example.customerdisplayhandler.core.interfaces.IServerDiscoveryManager;
import com.example.customerdisplayhandler.core.interfaces.ISocketConnectionManager;
import com.example.customerdisplayhandler.core.interfaces.ISocketDataSource;

import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SocketDataSourceImpl implements ISocketDataSource {
    private static final String TAG = SocketDataSourceImpl.class.getSimpleName();
    private final ISocketConnectionManager socketConnectionManager;
    private final IServerDiscoveryManager serverDiscoveryManager;
    private final IConnectedServerManager connectedServerManager;
    private final SharedPrefManager sharedPrefManager;
    private final IJsonUtil jsonUtil;
    private Map<ServerInfo, Socket> discoveredServerConnections = new HashMap<>();
    private Map<ServerInfo, Socket> unknownServerConnections = new HashMap<>();
    private final ObservableData<Map<ServerInfo, Socket>> activeServerConnections = new ObservableData<>();
    private final ObservableData<Pair<ServerInfo, String>> serverMessageStreamObservable = new ObservableData<>();
    private final ObservableData<Pair<ServerInfo, String>> unknownServerMessageStreamObservable = new ObservableData<>();
    private ExecutorService executorService;

    public SocketDataSourceImpl(ISocketConnectionManager socketConnectionManager, IServerDiscoveryManager serverDiscoveryManager, IConnectedServerManager connectedServerManager, IJsonUtil jsonUtil, SharedPrefManager sharedPrefManager) {
        this.socketConnectionManager = socketConnectionManager;
        this.serverDiscoveryManager = serverDiscoveryManager;
        this.connectedServerManager = connectedServerManager;
        this.sharedPrefManager = sharedPrefManager;
        this.jsonUtil = jsonUtil;
        this.executorService = Executors.newCachedThreadPool();
    }

    @Override
    public void getAvailableServers(String localIpAddress, int serverPort, OnServerScanCompleted onServerScanCompleted) {
//        serverDiscoveryManager.searchAvailableServersInNetwork(localIpAddress, serverPort, servers -> {
//            discoveredServerConnections = servers;
//            discoveredServerConnections.forEach(
//                    (serverInfo, socket) -> {
//                        AtomicReference<Boolean> isInPending = new AtomicReference<>(false);
//                        unknownServerConnections.forEach((serverInfoTemp, socketTemp) -> {
//                            if (Objects.equals(socketTemp.getInetAddress().getHostAddress(), socket.getInetAddress().getHostAddress())) {
//                                isInPending.set(true);
//                            }
//                        });
//                        if (isInPending.get()) {
//                            Boolean isRemoved = unknownServerConnections.remove(serverInfo, socket);
//                        }
//                    }
//            );
//            onServerScanCompleted.onSucess(servers);
//        },this);
    }

    @Override
    public void listenForServerMessages(ServerInfo serverInfo) {
        Socket socket;
        if (discoveredServerConnections.containsKey(serverInfo)) {
            socket = discoveredServerConnections.get(serverInfo);
        } else {
            Log.e(TAG, "Server not found, cannot listen for messages: " + serverInfo.getServerID());
            return;
        }

        if (activeServerConnections.getData().containsKey(serverInfo)) {
            Log.d(TAG, "Server already connected and listening: " + serverInfo.getServerDeviceName());
            return;
        }

        activeServerConnections.getData().put(serverInfo, socket);
//        connectedServerManager.startListening(
//                socket,
//                (message) -> {
//                    Pair<ServerInfo, String> messagePair = new Pair<>(serverInfo, message);
//                    serverMessageStreamObservable.setData(messagePair);
//                },
//                (e) -> {
//                    Log.e(TAG, "Error listening for server: " + serverInfo.getServerID());
//                    Boolean isRemoved = activeServerConnections.getData().remove(serverInfo, socket);
//                }
//        );
    }

    @Override
    public void sendConnectionRequest(ServerInfo serverInfo, ClientInfo clientInfo) {
//        if (discoveredServerConnections.containsKey(serverInfo)) {
//            Socket socket = discoveredServerConnections.get(serverInfo);
//            SocketMessageBase socketMessageBase = new SocketMessageBase(clientInfo, SocketConfigConstants.REQUEST_CONNECTION_APPROVAL, serverInfo.getServerID(), clientInfo.getClientID());
//            String message = jsonUtil.toJson(socketMessageBase);
//            connectedServerManager.sendMessageToServer(socket, message, new OnSendMessageCompleted() {
//                @Override
//                public void onMessageSent(String message) {
//                    Log.i(TAG, "Message sent to server: " + message);
//                    catchConnectionApprovalResponse(serverInfo, isApproved -> {
//                        if (isApproved) {
//                            addActiveServer(serverInfo);
//                            activeServerConnections.setData(discoveredServerConnections);
//                            Log.i(TAG, "Connection approved by server: " + serverInfo.getServerDeviceName());
//                        }else {
//                            Map<ServerInfo,Socket> connections = activeServerConnections.getData();
//                            Log.i(TAG, "Connection not approved by server: " + serverInfo.getServerDeviceName());
//                            connections.remove(serverInfo);
//                        }
//                    });
//                }
//
//                @Override
//                public void onError(Exception e) {
//                    Log.e(TAG, "Error sending message to server: " + e.getMessage());
//                }
//            });
//        }
    }

    private void catchConnectionApprovalResponse(ServerInfo serverInfo, OnConnectionApprovalReceived onConnectionApprovalReceived) {
        executorService.submit(() -> {
            try {
                unknownServerMessageStreamObservable.addObserver(new DataObserver<>() {
                    @Override
                    public void onDataChanged(Pair<ServerInfo, String> data) {
                        try {
                            processConnectionApproval(serverInfo, data.second);
                            onConnectionApprovalReceived.isApproved(true);
                        } catch (Exception e) {
                            Log.e("CatchConnectionApproval", "Error processing connection approval response: " + e.getMessage());
                            onConnectionApprovalReceived.isApproved(false);
                        }
                    }

                    @Override
                    public void onError(Exception e) {

                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "Error listening for connection approval response: " + e.getMessage());
            }
        });
    }

    private void processConnectionApproval(ServerInfo serverInfo, String message) throws Exception {
        if (message == null || message.isEmpty()) {
            throw new Exception("Invalid connection approval message received: " + message);
        }
        SocketMessageBase socketMessageBase = jsonUtil.toObj(message, SocketMessageBase.class);
        if (socketMessageBase.getCommand() == null || socketMessageBase.getCommand().isEmpty() || socketMessageBase.getData() == null || socketMessageBase.getSenderId() == null || socketMessageBase.getReceiverId() == null) {
            throw new Exception("Invalid connection approval message received: " + message);
        }
        if (!socketMessageBase.getCommand().equals(SocketConfigConstants.RESPONSE_CONNECTION_APPROVAL)) {
            throw new Exception("Invalid connection approval message received: " + message);
        }
        Object data = socketMessageBase.getData();
        ConnectionApproval connectionApproval = jsonUtil.toObj(jsonUtil.toJson(data), ConnectionApproval.class);
        if (connectionApproval != null && connectionApproval.isConnectionApproved() != null && !connectionApproval.isConnectionApproved()) {
            throw new Exception("Connection not approved by server: " + serverInfo.getServerDeviceName());
        }
    }

    @Override
    public void getClientInfo(OnClientInfoRetrieved onClientInfoRetrieved) {
        try {
            String clientInfoString = sharedPrefManager.getString(SharedPrefLabels.CLIENT_INFO_LABEL, "");
            if (clientInfoString.isEmpty()) {
                onClientInfoRetrieved.onError(new Exception("Client info not found"));
            }
            ClientInfo clientInfo = jsonUtil.toObj(clientInfoString, ClientInfo.class);
            onClientInfoRetrieved.onSucess(clientInfo);
        } catch (Exception e) {
            onClientInfoRetrieved.onError(e);
        }
    }

    @Override
    public void saveClientInfo(ClientInfo clientInfo, OnClientInfoSaved onClientInfoSaved) {
        try {
            String clientInfoString = jsonUtil.toJson(clientInfo);
            sharedPrefManager.putString(SharedPrefLabels.CLIENT_INFO_LABEL, clientInfoString);
            onClientInfoSaved.onSucess();
        } catch (Exception e) {

            onClientInfoSaved.onError(e);
        }
    }

    @Override
    public void addActiveServer(ServerInfo serverInfo) {
        try {
            List<ServerInfo> activeServers = getActiveServers();
            if (activeServers == null) {
                activeServers = List.of(serverInfo);
            } else {
                activeServers.add(serverInfo);
            }
            ActiveServersList activeServersList = new ActiveServersList();
            activeServersList.setActiveServers(activeServers);
            String activeServersListString = jsonUtil.toJson(activeServersList);
            sharedPrefManager.putString(SharedPrefLabels.ACTIVE_SERVER_LIST_LABEL, activeServersListString);
        } catch (Exception e) {
            Log.e(TAG, "Error setting active server: " + e.getMessage());
        }
    }

    @Override
    public List<ServerInfo> getActiveServers() {
        try {
            String serverInfoString = sharedPrefManager.getString(SharedPrefLabels.ACTIVE_SERVER_LIST_LABEL, "");
            if (serverInfoString.isEmpty()) {
                return null;
            }
            ActiveServersList activeServersList = jsonUtil.toObj(serverInfoString, ActiveServersList.class);
            return activeServersList.getActiveServers();
        } catch (Exception e) {
            Log.e(TAG, "Error getting active server: " + e.getMessage());
            return null;
        }
    }

    @Override
    public ObservableData<Pair<ServerInfo, String>> getServerMessageStreamObservable() {
        return serverMessageStreamObservable;
    }

    @Override
    public ObservableData<Pair<ServerInfo, String>> getUnknownServerMessageStreamObservable() {
        return unknownServerMessageStreamObservable;
    }

    @Override
    public ObservableData<Map<ServerInfo, Socket>> getActiveServerConnections() {
        return activeServerConnections;
    }

    private static class ActiveServersList {
        private List<ServerInfo> activeServers;

        public List<ServerInfo> getActiveServers() {
            return activeServers;
        }

        public void setActiveServers(List<ServerInfo> activeServers) {
            this.activeServers = activeServers;
        }
    }
}
