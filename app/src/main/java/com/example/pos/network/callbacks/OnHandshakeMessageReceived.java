package com.example.pos.network.callbacks;

public interface OnHandshakeMessageReceived {
    void handshakeMessageReceived(String message);
    void handshakeMessageFailed();
}
