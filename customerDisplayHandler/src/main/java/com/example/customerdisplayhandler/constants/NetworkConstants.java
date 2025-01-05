package com.example.customerdisplayhandler.constants;

public class NetworkConstants {
    public static final String MULTICAST_GROUP_ADDRESS = "224.0.0.1";
    public static final int MULTICAST_PORT = 49201;
    public static final String TURN_ON_NSD_COMMAND = "TURN_ON_NSD";
    public static final String TURN_ON_ALL_DEVICES_COMMAND = "TURN_ON_ALL_DEVICES";
    public static final String DEVICE_NAME_LABEL = "deviceName";
    public static final String SERVER_ID_LABEL = "serverId";
    public static final String CONNECTED_CLIENT_ID_LABEL = "connectedClientID";
    public static final String IP_ADDRESS_LABEL = "ipAddress";
    public static final int SERVICE_DISCOVERY_TIMEOUT = 60000;
    public static final int CONNECTION_APPROVAL_TIMEOUT = 60000;
    public static final String REQUEST_CONNECTION_APPROVAL = "REQUEST_CONNECTION_APPROVAL_FROM_CLIENT";
    public static final String RESPONSE_CONNECTION_APPROVAL = "RESPONSE_CONNECTION_APPROVAL_FROM_SERVER";
    public static final int DEFAULT_SERVER_PORT = 49200;
}
