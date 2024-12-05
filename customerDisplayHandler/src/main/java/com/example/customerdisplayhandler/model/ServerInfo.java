package com.example.customerdisplayhandler.model;

import java.util.UUID;

public class ServerInfo {
    private UUID serverID;
    private String serverIpAddress;
    private String serverDeviceName;

    public ServerInfo(UUID serverID, String serverIpAddress, String serverDeviceName) {
        this.serverID = serverID;
        this.serverIpAddress = serverIpAddress;
        this.serverDeviceName = serverDeviceName;
    }

    public UUID getServerID() {
        return serverID;
    }

    public String getServerIpAddress() {
        return serverIpAddress;
    }

    public String getServerDeviceName() {
        return serverDeviceName;
    }
}