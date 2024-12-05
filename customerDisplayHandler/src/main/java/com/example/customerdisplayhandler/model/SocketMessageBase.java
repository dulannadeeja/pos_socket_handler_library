package com.example.customerdisplayhandler.model;

import java.util.UUID;

public class SocketMessageBase {
    private Object data;
    private String command;
    private UUID receiverId;
    private UUID senderId;

    public SocketMessageBase(Object data, String command, UUID receiverId, UUID senderId) {
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

    public UUID getReceiverId() {
        return receiverId;
    }

    public UUID getSenderId() {
        return senderId;
    }
}
