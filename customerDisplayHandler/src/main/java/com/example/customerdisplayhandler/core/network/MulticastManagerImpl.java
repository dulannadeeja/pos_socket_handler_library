package com.example.customerdisplayhandler.core.network;

import android.util.Log;

import com.example.customerdisplayhandler.core.interfaces.IMulticastManager;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import io.reactivex.rxjava3.core.Completable;

public class MulticastManagerImpl implements IMulticastManager {

    private static final String TAG = MulticastManagerImpl.class.getSimpleName();
    private final int multicastPort;
    private final String multicastGroupAddress;

    public MulticastManagerImpl(String multicastGroupAddress, int multicastPort) {
        this.multicastGroupAddress = multicastGroupAddress;
        this.multicastPort = multicastPort;
    }

    public Completable sendMessage(String message) {
        return Completable.create(emitter -> {
            DatagramSocket socket = null;
            try {
                InetAddress group = InetAddress.getByName(multicastGroupAddress);
                socket = new DatagramSocket();
                byte[] buffer = message.getBytes();
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length, group, multicastPort);

                socket.send(packet);
                Log.d(TAG, "Message sent: " + message);

                if (!emitter.isDisposed()) {
                    emitter.onComplete();
                }
            } catch (IOException e) {
                Log.e(TAG, "Error sending message: " + e.getMessage());
                if (!emitter.isDisposed()) {
                    emitter.onError(e);
                }
            } finally {
                if (socket != null && !socket.isClosed()) {
                    socket.close();
                }
            }
        });
    }
}
