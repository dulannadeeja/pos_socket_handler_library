package com.example.customerdisplayhandler.core.interfaces;

import com.example.customerdisplayhandler.core.callbacks.OnSendMessageCompleted;
import com.example.customerdisplayhandler.core.callbacks.OnServerMessageReceived;

import java.net.Socket;

public interface IConnectedServerManager {
    void sendMessageToServer(Socket socket, String message, OnSendMessageCompleted onSendMessageCompleted);
    void startListening(Socket socket, OnServerMessageReceived onServerMessageReceived, OnListeningError onListeningError);

    public interface OnListeningError {
        void onError(Exception e);
    }
}
