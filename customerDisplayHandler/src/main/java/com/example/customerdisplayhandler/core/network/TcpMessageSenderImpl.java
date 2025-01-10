package com.example.customerdisplayhandler.core.network;

import android.util.Log;

import com.example.customerdisplayhandler.core.interfaces.ITcpMessageSender;

import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class TcpMessageSenderImpl implements ITcpMessageSender {
    private static final String TAG = TcpMessageSenderImpl.class.getSimpleName();
    @Override
    public Completable sendMessageToServer(String serverId, Socket socket, String message) {
        return Completable.create((emitter) -> {
            try {
                DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
                dataOutputStream.writeUTF(message);
                dataOutputStream.flush();
                Log.i(TAG, "Message sent to server: " + message);
                emitter.onComplete();
            } catch (EOFException eof) {
                Log.e(TAG, "Client disconnected from server, IP: " + socket.getInetAddress().getHostAddress() + ", Server ID: " + serverId);
                if (!emitter.isDisposed()){
                    emitter.onError(new Exception("Customer display disconnected, cannot send message."));
                }
            } catch (IOException e) {
                Log.e(TAG, "Error sending message: " + e.getMessage());
                if (!emitter.isDisposed()) {
                    emitter.onError(new Exception("Error occurred while sending message to customer display"));
                }
            }
        }).subscribeOn(Schedulers.io()); // Perform operation on IO thread
    }
}
