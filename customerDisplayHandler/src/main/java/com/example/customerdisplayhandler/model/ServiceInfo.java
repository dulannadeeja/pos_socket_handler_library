package com.example.customerdisplayhandler.model;

import java.io.Serializable;

public class ServiceInfo implements Serializable {
    String serverId;
    String deviceName;
    String ipAddress;
    String connectedClientID;

    public ServiceInfo(String serverId, String deviceName, String ipAddress, String connectedClientID) {
        this.serverId = serverId;
        this.deviceName = deviceName;
        this.ipAddress = ipAddress;
        this.connectedClientID = connectedClientID;
    }

    public String getServerId() {
        return serverId;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public String getConnectedClientID() {
        return connectedClientID;
    }
}
