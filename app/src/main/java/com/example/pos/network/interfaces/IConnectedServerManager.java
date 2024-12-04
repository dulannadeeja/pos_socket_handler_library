package com.example.pos.network.interfaces;

import com.example.pos.network.callbacks.OnSendMessageCompleted;
import com.example.pos.network.callbacks.OnServerMessageReceived;

import java.net.Socket;

public interface IConnectedServerManager {
    void sendMessageToServer(Socket socket, String message, OnSendMessageCompleted onSendMessageCompleted);
    void startListening(Socket socket, OnServerMessageReceived onServerMessageReceived, OnListeningError onListeningError);

    public interface OnListeningError {
        void onError(Exception e);
    }
}
