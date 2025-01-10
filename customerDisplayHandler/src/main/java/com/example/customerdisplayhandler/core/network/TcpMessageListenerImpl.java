package com.example.customerdisplayhandler.core.network;

import android.util.Log;
import android.util.Pair;

import com.example.customerdisplayhandler.core.interfaces.ITcpMessageListener;

import java.io.DataInputStream;
import java.io.EOFException;
import java.net.Socket;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.subjects.PublishSubject;

public class TcpMessageListenerImpl implements ITcpMessageListener {
    private static final String TAG = TcpMessageListenerImpl.class.getSimpleName();
    private volatile PublishSubject<Pair<String, String>> serverMessageSubject = PublishSubject.create();

    @Override
    public Completable startListening(String serverId, Socket socket) {
        return Completable.create((emitter) -> {
                    try {
                        Log.i(TAG, "Listening for messages from server: " + socket.getInetAddress().getHostAddress());
                        DataInputStream input = new DataInputStream(socket.getInputStream());
                        String serverMessage;
                        while ((serverMessage = input.readUTF()) != null) {
                            serverMessageSubject.onNext(new Pair<>(serverId, serverMessage));
                            Log.i(TAG, "Message received from server: " + serverMessage);
                        }
                    } catch (Exception e) {
                        if (!emitter.isDisposed()) {
                            emitter.onError(e);
                        }
                    }
                })
                .doOnError(throwable -> {
                    if (throwable instanceof EOFException) {
                        Log.e(TAG, "Client disconnected from server, IP: " + socket.getInetAddress().getHostAddress() + ", Server ID: " + serverId);
                    } else {
                        Log.e(TAG, "Error reading message: " + throwable.getMessage());
                    }
                })
                .subscribeOn(Schedulers.io()); // Run the task on an IO thread
    }

    public PublishSubject<Pair<String, String>> getServerMessageSubject() {
        return serverMessageSubject;
    }
}
