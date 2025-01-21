package com.example.customerdisplayhandler.helpers;

import com.example.customerdisplayhandler.model.SocketMessageBase;

public interface ISocketMessageProcessHelper {
    String getAcknowledgeMessageId(String message);
    SocketMessageBase getBaseMessage(String message);
}
