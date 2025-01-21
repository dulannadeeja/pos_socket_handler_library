package com.example.customerdisplayhandler.core.network;

import android.util.Log;
import android.util.Pair;

import com.example.customerdisplayhandler.core.interfaces.ITcpMessageListener;

import java.io.DataInputStream;
import java.io.EOFException;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.subjects.BehaviorSubject;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.subjects.ReplaySubject;

public class TcpMessageListenerImpl implements ITcpMessageListener {
    private static final String TAG = TcpMessageListenerImpl.class.getSimpleName();
    private volatile ReplaySubject<Pair<String, String>> serverMessageSubject = ReplaySubject.create();
    private final ConcurrentHashMap<Socket, Completable> activeListeners = new ConcurrentHashMap<>();

    @Override
    public Completable startListening(String serverId, Socket socket) {
        if (activeListeners.containsKey(socket)) {
            Log.w(TAG, "Listener is already active for this socket.");
            return Completable.complete();
        }

        Completable listeningTask = Completable.create(emitter -> {
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
                .doFinally(() -> activeListeners.remove(socket)) // Remove listener when complete
                .doOnError(throwable -> Log.e(TAG, "Error reading message: " + throwable.getMessage()))
                .subscribeOn(Schedulers.io());

        activeListeners.put(socket, listeningTask);
        return listeningTask;
    }

    public ReplaySubject<Pair<String, String>> getServerMessageSubject() {
        return serverMessageSubject;
    }
}
