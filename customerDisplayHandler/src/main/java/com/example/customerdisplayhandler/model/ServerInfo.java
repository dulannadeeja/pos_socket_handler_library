package com.example.customerdisplayhandler.model;

import java.io.Serializable;

public class ServerInfo implements Serializable {
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

    public void setServerID(String serverID) {
        this.serverID = serverID;
    }

    public void setServerIpAddress(String serverIpAddress) {
        this.serverIpAddress = serverIpAddress;
    }

    public void setServerDeviceName(String serverDeviceName) {
        this.serverDeviceName = serverDeviceName;
    }
}