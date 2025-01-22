package com.example.customerdisplayhandler.core.network;

import android.util.Log;
import android.util.Pair;

import com.example.customerdisplayhandler.core.interfaces.ISocketsManager;
import com.example.customerdisplayhandler.core.interfaces.ITcpConnector;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.subjects.PublishSubject;

public class TcpConnectorImpl implements ITcpConnector {
    private static final String TAG = TcpConnectorImpl.class.getSimpleName();
    private final PublishSubject<Pair<String,Socket>> serverConnectionSubject = PublishSubject.create();

    public TcpConnectorImpl() {
    }

    @Override
    public Single<Socket> connectToServer(String serverIPAddress, int serverPort) {
        return Single.<Socket>create(emitter -> {
                    try {
                        Socket socket = new Socket(serverIPAddress, serverPort);
                        socket.setKeepAlive(true);
                        Log.i(TAG, "Connected to server: " + serverIPAddress + ":" + serverPort);
                        serverConnectionSubject.onNext(new Pair<>(serverIPAddress, socket));
                        emitter.onSuccess(socket);
                    } catch (Exception e) {
                        Log.e(TAG, "Error connecting to server: " + e.getMessage());
                        Exception newException = new Exception("Error occurred while connecting to customer display");
                        if (!emitter.isDisposed()) {
                            emitter.onError(newException);
                        }
                    }
                })
                .subscribeOn(Schedulers.io()) // Perform the connection on an IO thread
                .observeOn(Schedulers.io()); // Observe the result on the same IO thread
    }

    @Override
    public Single<Socket> tryToConnectWithingTimeout(String serverIPAddress, int serverPort, int timeoutInMillis) {
        return Single.<Socket>create(emitter -> {
                    try {
                        Socket socket = new Socket();
                        SocketAddress socketAddress = new InetSocketAddress(serverIPAddress, serverPort);
                        socket.connect(socketAddress, timeoutInMillis);
                        socket.setKeepAlive(true);
                        Log.i(TAG, "Connected to server: " + serverIPAddress + ":" + serverPort);
                        serverConnectionSubject.onNext(new Pair<>(serverIPAddress, socket));
                        emitter.onSuccess(socket);
                    } catch (Exception e) {
                        Log.e(TAG, "Error connecting to server: " + e.getMessage());
                        Exception newException = new Exception("Error occurred while connecting to customer display");
                        if (!emitter.isDisposed()) {
                            emitter.onError(newException);
                        }
                    }
                })
                .subscribeOn(Schedulers.io()) // Perform the connection on an IO thread
                .observeOn(Schedulers.io()); // Observe the result on the same IO thread

    }

    @Override
    public Completable disconnectSafelyFromServer(Socket serverSocket) {
        return Completable.fromAction(() -> {
            try {
                if (serverSocket != null && serverSocket.isConnected()) {
                    serverSocket.close();
                    Log.i(TAG, "Disconnected from server: " + serverSocket.getInetAddress().getHostAddress());
                }
            } catch (Exception e) {
                Log.e(TAG, "Error disconnecting from server: " + e.getMessage());
            }
        }).subscribeOn(Schedulers.io()); // Perform operation on IO thread
    }
}
