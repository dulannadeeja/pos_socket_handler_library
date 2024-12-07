package com.example.customerdisplayhandler.core.network;

import android.util.Log;

import com.example.customerdisplayhandler.core.callbacks.OnConnectToServerCompleted;
import com.example.customerdisplayhandler.core.interfaces.ISocketConnectionManager;

import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class SocketConnectionManagerImpl implements ISocketConnectionManager {
    private static final String TAG = "ClientSocketManager";
    private final ExecutorService executorService;

    public SocketConnectionManagerImpl() {
        this.executorService = Executors.newCachedThreadPool();
    }

    @Override
    public Single<Socket> connectToServer(String serverIPAddress, int serverPort) {
        return Single.<Socket>create(emitter -> {
                    try {
                        Socket socket = new Socket(serverIPAddress, serverPort);
                        socket.setKeepAlive(true);
                        Log.d(TAG, "Connected to server: " + serverIPAddress + ":" + serverPort);
                        emitter.onSuccess(socket);
                    }catch (Exception e) {
                        Log.e(TAG, "Error connecting to server: " + e.getMessage());
                    }
                })
                .subscribeOn(Schedulers.io()) // Perform the connection on an IO thread
                .observeOn(Schedulers.io()); // Observe the result on the same IO thread
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
