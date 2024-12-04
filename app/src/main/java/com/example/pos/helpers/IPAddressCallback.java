package com.example.pos.helpers;

public interface IPAddressCallback {
    void onResult(String ipAddress);
    void onError(Exception e);
}
