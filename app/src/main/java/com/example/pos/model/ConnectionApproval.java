package com.example.pos.model;

public class ConnectionApproval {
    private String serverID;
    private String serverIpAddress;
    private String serverDeviceName;
    private Boolean isConnectionApproved;

    public ConnectionApproval(String serverID, String serverIpAddress, String serverDeviceName, Boolean isConnectionApproved) {
        this.serverID = serverID;
        this.serverIpAddress = serverIpAddress;
        this.serverDeviceName = serverDeviceName;
        this.isConnectionApproved = isConnectionApproved;
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

    public Boolean isConnectionApproved() {
        return isConnectionApproved;
    }

    public void setConnectionApproved(Boolean connectionApproved) {
        isConnectionApproved = connectionApproved;
    }
}
