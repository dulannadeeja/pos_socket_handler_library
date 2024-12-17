package com.example.customerdisplayhandler.model;

import java.util.UUID;

public class SocketMessageBase {
    private Object data;
    private String command;
    private String receiverId;
    private String senderId;

    public SocketMessageBase(Object data, String command, String receiverId, String senderId) {
        this.data = data;
        this.command = command;
        this.receiverId = receiverId;
        this.senderId = senderId;
    }

    public Object getData() {
        return data;
    }

    public String getCommand() {
        return command;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public String getSenderId() {
        return senderId;
    }
}
