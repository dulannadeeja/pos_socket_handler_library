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

    public void setServerId(String serverId) {
        this.serverId = serverId;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public void setConnectedClientID(String connectedClientID) {
        this.connectedClientID = connectedClientID;
    }
}
