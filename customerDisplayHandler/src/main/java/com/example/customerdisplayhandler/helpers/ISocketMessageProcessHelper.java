package com.example.customerdisplayhandler.helpers;

import com.example.customerdisplayhandler.model.ConnectionApproval;
import com.example.customerdisplayhandler.model.SocketMessageBase;

public interface ISocketMessageProcessHelper {
    String getAcknowledgeMessageId(String message, String clientId);
    SocketMessageBase getBaseMessage(String message, String clientId);
    ConnectionApproval getConnectionApproval(String message, String clientId, String connectionReqMessageId);
}
