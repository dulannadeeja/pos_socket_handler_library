package com.example.pos.network.callbacks;

public interface OnSendMessageCompleted {
    void onMessageSent(String message);
    void onError(Exception e);
}
