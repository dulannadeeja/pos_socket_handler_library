package com.example.customerdisplayhandler.model;

public class ConnectionReq {
    private String clientID;
    private String clientIpAddress;
    private String clientDeviceName;
    private Boolean isDarkMode;
    private String terminalID;

    public ConnectionReq(String clientID, String clientIpAddress, String clientDeviceName, Boolean isDarkMode, String terminalID) {
        this.clientID = clientID;
        this.clientIpAddress = clientIpAddress;
        this.clientDeviceName = clientDeviceName;
        this.isDarkMode = isDarkMode;
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

    public Boolean isDarkMode() {
        return isDarkMode;
    }
    public String getTerminalID() { return terminalID; }
}
