package com.example.pos.model;

import java.util.UUID;

public class ClientInfo {
    private UUID clientID;
    private String clientIpAddress;
    private String clientDeviceName;

    public ClientInfo(UUID clientID, String clientIpAddress, String clientDeviceName) {
        this.clientID = clientID;
        this.clientIpAddress = clientIpAddress;
        this.clientDeviceName = clientDeviceName;
    }

    public UUID getClientID() {
        return clientID;
    }

    public String getClientIpAddress() {
        return clientIpAddress;
    }

    public String getClientDeviceName() {
        return clientDeviceName;
    }
}
