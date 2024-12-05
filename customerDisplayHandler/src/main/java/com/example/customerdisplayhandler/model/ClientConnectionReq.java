package com.example.customerdisplayhandler.model;

public class ClientConnectionReq {
    private ClientInfo clientInfo;
    private String messageForServer;
    private String typeOfMessage;
    private String receiverIpAddress;

    public ClientConnectionReq(ClientInfo clientInfo, String messageForServer, String typeOfMessage, String receiverIpAddress) {
        this.clientInfo = clientInfo;
        this.messageForServer = messageForServer;
        this.typeOfMessage = typeOfMessage;
        this.receiverIpAddress = receiverIpAddress;
    }
}
