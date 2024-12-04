package com.example.pos.network;

import android.util.Log;

import com.example.pos.network.callbacks.OnSendMessageCompleted;
import com.example.pos.network.callbacks.OnServerMessageReceived;
import com.example.pos.network.interfaces.IConnectedServerManager;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ConnectedServerManagerImpl implements IConnectedServerManager {
    private static final String TAG = ConnectedServerManagerImpl.class.getSimpleName();
    private final ExecutorService executorService;

    public ConnectedServerManagerImpl() {
        this.executorService = Executors.newFixedThreadPool(10);
    }

    @Override
    public void sendMessageToServer(Socket socket, String message, OnSendMessageCompleted onSendMessageCompleted) {
        executorService.submit(
                () -> {
                    try {
                        DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
                        dataOutputStream.writeUTF(message);
                        dataOutputStream.flush();
                        Log.i(TAG, "Message sent to server: " + message);
                        onSendMessageCompleted.onMessageSent(message);
                    } catch (Exception e) {
                        onSendMessageCompleted.onError(e);
                    }
                }
        );
    }

    @Override
    public void startListening(Socket socket, OnServerMessageReceived onServerMessageReceived, OnListeningError onListeningError) {
        executorService.submit(
                () -> {
                    Log.i(TAG, "Listening for messages from server:" + socket.getInetAddress().getHostAddress());
                    listenForMessages(socket, onServerMessageReceived, onListeningError);
                }
        );
    }

    private void listenForMessages(Socket socket, OnServerMessageReceived onServerMessageReceived, OnListeningError onListeningError) {
        try (DataInputStream input = new DataInputStream(socket.getInputStream())) {

            String serverMessage;
            while ((serverMessage = input.readUTF()) != null) {
                onServerMessageReceived.onMessageReceived(serverMessage);
                Log.i(TAG, "Message received from server: " + serverMessage);
            }
        } catch (EOFException eof) {
            Log.e(TAG, "Client disconnected: " + eof.getMessage());
            onListeningError.onError(eof);
        } catch (IOException e) {
            Log.e(TAG, "Error reading message: " + e.getMessage());
            onListeningError.onError(e);
        } catch (Exception e) {
            Log.e(TAG, "Unexpected error: " + e.getMessage());
            onListeningError.onError(e);
        }
    }
}
