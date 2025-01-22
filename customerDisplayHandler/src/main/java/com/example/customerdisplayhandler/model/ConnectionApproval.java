package com.example.customerdisplayhandler.model;

public class ConnectionApproval {
    private String serverID;
    private String serverIpAddress;
    private String serverDeviceName;
    private Boolean isConnectionApproved;
    private String connectionReqMessageId;

    public ConnectionApproval(String serverID, String serverIpAddress, String serverDeviceName, Boolean isConnectionApproved, String connectionReqMessageId) {
        this.serverID = serverID;
        this.serverIpAddress = serverIpAddress;
        this.serverDeviceName = serverDeviceName;
        this.isConnectionApproved = isConnectionApproved;
        this.connectionReqMessageId = connectionReqMessageId;
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
    public String getConnectionReqMessageId() { return connectionReqMessageId; }
}
