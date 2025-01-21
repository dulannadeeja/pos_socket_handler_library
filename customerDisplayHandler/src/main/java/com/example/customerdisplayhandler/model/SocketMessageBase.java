package com.example.customerdisplayhandler.model;

public class SocketMessageBase {
    private Object data;
    private String command;
    private String receiverId;
    private String senderId;
    private String messageId;

    public SocketMessageBase(Object data, String command, String receiverId, String senderId, String messageId) {
        this.data = data;
        this.command = command;
        this.receiverId = receiverId;
        this.senderId = senderId;
        this.messageId = messageId;
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

    public String getMessageId() {
        return messageId;
    }
}
