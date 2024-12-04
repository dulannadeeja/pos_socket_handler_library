package com.example.pos.network.callbacks;

import java.net.Socket;

public interface OnConnectToServerCompleted {
    void onServerConnectionSuccess(Socket serverSocket);
    void onServerConnectionFailure(Exception e);
}
