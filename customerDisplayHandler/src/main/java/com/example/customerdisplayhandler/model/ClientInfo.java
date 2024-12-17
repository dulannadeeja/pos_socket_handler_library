package com.example.customerdisplayhandler.model;

import java.util.UUID;

public class ClientInfo {
    private String clientID;
    private String clientIpAddress;
    private String clientDeviceName;

    public ClientInfo(String clientID, String clientIpAddress, String clientDeviceName) {
        this.clientID = clientID;
        this.clientIpAddress = clientIpAddress;
        this.clientDeviceName = clientDeviceName;
    }

    public String getClientID() {
        return clientID;
    }

    public String getClientIpAddress() {
        return clientIpAddress;
    }

    public String getClientDeviceName() {
        return clientDeviceName;
    }
}
