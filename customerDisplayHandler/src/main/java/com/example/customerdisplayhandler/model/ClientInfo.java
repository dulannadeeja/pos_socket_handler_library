package com.example.customerdisplayhandler.model;

import java.util.UUID;

public class ClientInfo {
    private String clientID;
    private String clientIpAddress;
    private String clientDeviceName;
    private String terminalID;

    public ClientInfo(String clientID, String clientIpAddress, String clientDeviceName, String terminalID) {
        this.clientID = clientID;
        this.clientIpAddress = clientIpAddress;
        this.clientDeviceName = clientDeviceName;
        this.terminalID = terminalID;
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

    public String getTerminalID() { return terminalID;}
}
