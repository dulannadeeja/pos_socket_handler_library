package com.example.customerdisplayhandler.core.callbacks;

public interface OnHandshakeMessageReceived {
    void handshakeMessageReceived(String message);
    void handshakeMessageFailed();
}
