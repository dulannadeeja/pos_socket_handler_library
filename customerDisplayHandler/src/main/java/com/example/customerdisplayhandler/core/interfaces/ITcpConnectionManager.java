package com.example.customerdisplayhandler.core.interfaces;

import java.net.Socket;

import io.reactivex.rxjava3.core.Single;

public interface ITcpConnectionManager {
    public Single<Socket> connectToServer(String serverIPAddress, int serverPort);
    public void disconnectSafelyFromServer(Socket serverSocket);
}
