package com.example.customerdisplayhandler.core.interfaces;

import android.util.Pair;

import java.net.Socket;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.subjects.PublishSubject;

public interface ITcpConnector {
    Single<Socket> connectToServer(String serverIPAddress, int serverPort);
    Single<Socket> tryToConnectWithingTimeout(String serverIPAddress, int serverPort, int timeoutInMillis);
    Completable disconnectSafelyFromServer(Socket serverSocket);
    PublishSubject<Pair<String, Socket>> getServerConnectionSubject();
}
