package com.example.customerdisplayhandler.core.interfaces;

import com.example.customerdisplayhandler.core.callbacks.OnConnectToServerCompleted;

import java.net.Socket;

public interface ISocketConnectionManager {
    public void connectToServer(String ip, int port, OnConnectToServerCompleted onConnectToServerCompleted);
    public void disconnectSafelyFromServer(Socket serverSocket);
}
