package com.example.customerdisplayhandler.core.interfaces;

import android.util.Pair;

import java.net.Socket;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.subjects.BehaviorSubject;
import io.reactivex.rxjava3.subjects.PublishSubject;

public interface ITcpMessageListener {
    Completable startListening(String serverId, Socket socket);
    PublishSubject<Pair<String, String>> getServerMessageSubject();
}
