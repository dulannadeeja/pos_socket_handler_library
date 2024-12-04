package com.example.pos.network;

import android.util.Log;
import android.util.Pair;

import com.example.pos.constants.SocketConfigConstants;
import com.example.pos.model.ServerInfo;
import com.example.pos.model.SocketMessageBase;
import com.example.pos.network.callbacks.OnConnectToServerCompleted;
import com.example.pos.network.callbacks.OnSearchServerCompleted;
import com.example.pos.network.interfaces.DataObserver;
import com.example.pos.network.interfaces.IConnectedServerManager;
import com.example.pos.network.interfaces.IJsonUtil;
import com.example.pos.network.interfaces.IServerDiscoveryManager;
import com.example.pos.network.interfaces.ISocketConnectionManager;
import com.example.pos.network.interfaces.ISocketDataSource;

import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServerDiscoveryManagerImpl implements IServerDiscoveryManager {
    private final static String TAG = ServerDiscoveryManagerImpl.class.getSimpleName();
    private final ISocketConnectionManager socketConnectionManager;
    private final IConnectedServerManager connectedServerManager;
    private final IJsonUtil jsonUtil;
    private final ExecutorService executorService;

    public ServerDiscoveryManagerImpl(ISocketConnectionManager socketConnectionManager, IConnectedServerManager connectedServerManager, IJsonUtil jsonUtil) {
        this.executorService = Executors.newCachedThreadPool();
        this.socketConnectionManager = socketConnectionManager;
        this.connectedServerManager = connectedServerManager;
        this.jsonUtil = jsonUtil;
    }

    @Override
    public void searchAvailableServersInNetwork(String localIPAddress, int serverPort, OnSearchServerCompleted onSearchServerCompleted, ISocketDataSource socketDataSource) {
        executorService.submit(
                () -> {
                    Map<ServerInfo, Socket> connectedServers = new HashMap<>();
                    String[] ipParts = localIPAddress.split("\\.");
                    String baseIP = ipParts[0] + "." + ipParts[1] + "." + ipParts[2] + ".";
                    // TODO: Remove hardcoded IP address, use the baseIP instead
//                    baseIP = "172.20.10.";
                    for (int i = 0; i < 256; i++) {
                        String ip = baseIP + i;
                        Log.i(TAG, "Searching for server: " + ip);
                        if(!ip.equals(localIPAddress)) {
                            socketConnectionManager.connectToServer(ip, serverPort, new OnConnectToServerCompleted() {
                                @Override
                                public void onServerConnectionSuccess(Socket socket) {
                                    Log.i(TAG, "Connected to server: " + ip);
                                    // give temporary serverId to the server,
                                    // this will be replaced by the serverId received from the server
                                    UUID serverID = UUID.randomUUID();
                                    ServerInfo serverInfo = new ServerInfo(serverID, ip, "Unknown");
                                    waitingForHandshakeFromServer(socket, serverInfo, serverInfoReceived -> {
                                                connectedServers.put(serverInfoReceived, socket);
                                                onSearchServerCompleted.serversFound(connectedServers);
                                            },
                                            () -> {
                                                // If handshake times out, simply ignore it
                                                Log.e(TAG, "Handshake timed out for server: " + serverInfo.getServerIpAddress());
                                            },
                                            socketDataSource);
                                }

                                @Override
                                public void onServerConnectionFailure(Exception e) {
                                    // If connection to server fails, simply ignore it
                                    // Log.e(TAG, "Connection to server failed: " + e.getMessage());
                                }
                            });
                        }
//                        try {
//                            Thread.sleep(100);
//                        } catch (InterruptedException e) {
//                            throw new RuntimeException(e);
//                        }
                    }
                }
        );
    }

    private void waitingForHandshakeFromServer(Socket socket, ServerInfo serverInfo, OnHandshakeMessageReceived onHandshakeMessageReceived, OnHandshakeTimeout onHandshakeTimeout, ISocketDataSource socketDataSource) {
        executorService.submit(() -> {
            try {
                Timer timer = new Timer();
                Log.i(TAG, "Waiting for handshake from server: " + serverInfo.getServerIpAddress());

                // Start listening for server messages
                socketDataSource.listenForUnknownServers(socket, serverInfo);
                socketDataSource.getUnknownServerMessageStreamObservable().addObserver(new DataObserver<>() {
                    @Override
                    public void onDataChanged(Pair<ServerInfo, String> data) {
                        synchronized (timer) {
                            try {
                                ServerInfo serverInfoReceived = processHandshake(data.second);
                                // we have received the valid handshake message,
                                // now we can stop the timer and proceed
                                onHandshakeMessageReceived.handshakeMessageReceived(serverInfoReceived);
                                timer.cancel();
                            } catch (Exception e) {
                                Log.e(TAG, "Error processing handshake message: " + e.getMessage());
                            }
                        }
                    }

                    @Override
                    public void onError(Exception e) {
                        Log.e(TAG, "Error listening for server messages: " + e.getMessage());
                    }
                });

                // Schedule a timeout task
                scheduleSocketTimeout(timer, onHandshakeTimeout);
            } catch (Exception e) {
                Log.e(TAG, "Error waiting for handshake from server: " ,e);
            }
        });
    }

    private void scheduleSocketTimeout(Timer timer, OnHandshakeTimeout onHandshakeTimeout) {
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                synchronized (timer) {
                    onHandshakeTimeout.onTimeout();
                }
            }
        }, SocketConfigConstants.CONNECTION_REQUEST_TIMEOUT);
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

    private interface OnHandshakeTimeout {
        void onTimeout();
    }

    private interface OnHandshakeMessageReceived {
        void handshakeMessageReceived(ServerInfo serverInfo);
    }
}
