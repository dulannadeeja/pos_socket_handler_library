package com.example.customerdisplayhandler.helpers;

public interface IPAddressCallback {
    void onResult(String ipAddress);
    void onError(Exception e);
}
