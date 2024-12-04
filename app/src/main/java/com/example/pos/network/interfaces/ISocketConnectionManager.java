package com.example.pos.network.interfaces;

import com.example.pos.network.callbacks.OnConnectToServerCompleted;

import java.net.Socket;

public interface ISocketConnectionManager {
    public void connectToServer(String ip, int port, OnConnectToServerCompleted onConnectToServerCompleted);
    public void disconnectSafelyFromServer(Socket serverSocket);
}
