package com.example.customerdisplayhandler.core.interfaces;

import java.net.Socket;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;

public interface ITcpConnector {
    Single<Socket> connectToServer(String serverIPAddress, int serverPort);
    Completable disconnectSafelyFromServer(Socket serverSocket);
}
