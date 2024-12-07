package com.example.customerdisplayhandler.model;

import java.util.UUID;

public class ServerInfo {
    private String serverID;
    private String serverIpAddress;
    private String serverDeviceName;

    public ServerInfo(String serverID, String serverIpAddress, String serverDeviceName) {
        this.serverID = serverID;
        this.serverIpAddress = serverIpAddress;
        this.serverDeviceName = serverDeviceName;
    }

    public String getServerID() {
        return serverID;
    }

    public String getServerIpAddress() {
        return serverIpAddress;
    }

    public String getServerDeviceName() {
        return serverDeviceName;
    }
}