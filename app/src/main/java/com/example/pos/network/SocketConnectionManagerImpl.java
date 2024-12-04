package com.example.pos.network;

import android.util.Log;

import com.example.pos.network.callbacks.OnConnectToServerCompleted;
import com.example.pos.network.interfaces.ISocketConnectionManager;

import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SocketConnectionManagerImpl implements ISocketConnectionManager {
    private static final String TAG = "ClientSocketManager";
    private final ExecutorService executorService;

    public SocketConnectionManagerImpl() {
        this.executorService = Executors.newCachedThreadPool();
    }

    @Override
    public void connectToServer(String serverIPAddress, int serverPort,OnConnectToServerCompleted onConnectToServerCompleted) {
        executorService.submit(
                () -> {
                    try {
                        Socket socket = new Socket(serverIPAddress, serverPort);
                        Log.d(TAG, "Connected to server: " + serverIPAddress + ":" + serverPort);
                        socket.setKeepAlive(true);
                        onConnectToServerCompleted.onServerConnectionSuccess(socket);
                    } catch (Exception e) {
                        Log.e(TAG, "Error connecting to server: " + e.getMessage() + " " + serverIPAddress + ":" + serverPort);
                        onConnectToServerCompleted.onServerConnectionFailure(e);
                    }
                }
        );
    }

    @Override
    public void disconnectSafelyFromServer(Socket serverSocket) {
        executorService.submit(() ->{
            try {
                serverSocket.close();
            } catch (Exception e) {
                Log.e(TAG, "Error disconnecting from server: " + e.getMessage());
            }
        });
    }
}
