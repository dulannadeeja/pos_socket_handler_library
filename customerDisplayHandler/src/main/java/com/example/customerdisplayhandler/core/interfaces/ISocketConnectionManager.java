package com.example.customerdisplayhandler.core.interfaces;

import com.example.customerdisplayhandler.core.callbacks.OnConnectToServerCompleted;

import java.net.Socket;

import io.reactivex.rxjava3.core.Single;

public interface ISocketConnectionManager {
    public Single<Socket> connectToServer(String serverIPAddress, int serverPort);
    public void disconnectSafelyFromServer(Socket serverSocket);
}
