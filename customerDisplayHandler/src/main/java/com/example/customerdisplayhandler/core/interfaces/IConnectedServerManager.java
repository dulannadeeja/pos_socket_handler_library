package com.example.customerdisplayhandler.core.interfaces;

import android.util.Pair;

import com.example.customerdisplayhandler.core.callbacks.OnSendMessageCompleted;
import com.example.customerdisplayhandler.core.callbacks.OnServerMessageReceived;
import com.example.customerdisplayhandler.model.ServerInfo;

import java.net.Socket;
import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.subjects.PublishSubject;

public interface IConnectedServerManager {
    public void addConnectedServer(Socket socket, ServerInfo serverInfo);
    public Completable sendMessageToServer(String serverId, String message);
    public Completable startListening(String serverId, Socket socket);
    public Completable safelyStopListening(String serverId, Socket socket);
    public PublishSubject<Pair<String,String>> getServerMessageSubject();
    public PublishSubject<List<String>> getListeningServersSubject();
}
