package com.example.customerdisplayhandler.core.interfaces;

import java.net.Socket;

import io.reactivex.rxjava3.core.Single;

public interface ITcpConnectionManager {
    Single<Socket> connectToServer(String serverIPAddress, int serverPort);
    void disconnectSafelyFromServer(Socket serverSocket);
}
