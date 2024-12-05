package com.example.customerdisplayhandler.core.callbacks;

public interface OnSendMessageCompleted {
    void onMessageSent(String message);
    void onError(Exception e);
}
