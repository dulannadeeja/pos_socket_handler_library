package com.example.customerdisplayhandler.helpers;

import android.util.Log;
import android.util.Pair;

import com.example.customerdisplayhandler.constants.NetworkConstants;
import com.example.customerdisplayhandler.model.ConnectionApproval;
import com.example.customerdisplayhandler.model.SocketMessageBase;
import com.example.customerdisplayhandler.utils.IJsonUtil;
import com.google.gson.Gson;

public class SocketMessageProcessHelperImpl implements ISocketMessageProcessHelper {
    private static final String TAG = SocketMessageProcessHelperImpl.class.getSimpleName();
    private IJsonUtil jsonUtil;

    public SocketMessageProcessHelperImpl(IJsonUtil jsonUtil) {
        this.jsonUtil = jsonUtil;
    }

    @Override
    public SocketMessageBase getBaseMessage(String message,String clientId) {
        try {
            SocketMessageBase socketMessageBase = jsonUtil.toObj(message, SocketMessageBase.class);
            if (socketMessageBase.getCommand() == null || socketMessageBase.getCommand().isEmpty()) {
                Log.w(TAG, "Message dropped when processing, command is empty");
                return null;
            } else if (socketMessageBase.getSenderId() == null || socketMessageBase.getSenderId().isEmpty()) {
                Log.w(TAG, "Message dropped when processing, senderId is empty");
                return null;
            } else if (socketMessageBase.getData() == null) {
                Log.w(TAG, "Message dropped when processing, data is empty");
                return null;
            } else if (socketMessageBase.getMessageId() == null || socketMessageBase.getMessageId().isEmpty()) {
                Log.w(TAG, "Message dropped when processing, messageId is empty");
                return null;
            } else if (socketMessageBase.getReceiverId() == null || socketMessageBase.getReceiverId().isEmpty()) {
                Log.w(TAG, "Message dropped when processing, receiverId is empty");
                return null;
            } else if (!clientId.equals(socketMessageBase.getReceiverId())) {
                Log.w(TAG, "Message dropped when processing, receiverId is not matching");
                return null;
            }
            return socketMessageBase;
        } catch (Exception e) {
            Log.wtf(TAG, "Error processing message: " + e.getMessage(), e);
            return null;
        }
    }

    @Override
    public String getAcknowledgeMessageId(String message, String clientId) {
        SocketMessageBase baseMessage = getBaseMessage(message,clientId);
        if (baseMessage == null) {
            return null;
        }
        if(!baseMessage.getCommand().equals(NetworkConstants.MESSAGE_ACKNOWLEDGEMENT)) {
            Log.w(TAG, "Message dropped when processing, command is not an acknowledgement");
            return null;
        }
        String messageId = baseMessage.getData().toString();
        if (messageId.isEmpty()) {
            Log.w(TAG, "Message dropped when processing, acknowledgement message id is empty");
            return null;
        }
        return messageId;
    }

    @Override
    public ConnectionApproval getConnectionApproval(String message, String clientId, String connectionReqMessageId) {
        try {
            SocketMessageBase socketMessageBase = getBaseMessage(message,clientId);

            if (socketMessageBase == null) {
                return null;
            }

            if (!NetworkConstants.RESPONSE_CONNECTION_APPROVAL.equals(socketMessageBase.getCommand())) {
                Log.w(TAG, "Message dropped when processing, command is not a connection approval");
                return null;
            }

            ConnectionApproval connectionApproval = jsonUtil.toObj(jsonUtil.toJson(socketMessageBase.getData()), ConnectionApproval.class);

            if (connectionApproval == null || connectionApproval.isConnectionApproved() == null) {
                Log.w(TAG, "Message dropped when processing, connection approval is empty");
                return null;
            }

            if(!connectionApproval.getConnectionReqMessageId().equals(connectionReqMessageId)){
                Log.w(TAG, "Message dropped when processing, connectionReqMessageId is not matching");
                return null;
            }

            return connectionApproval;

        } catch (Exception e) {
            Log.wtf(TAG, "Error processing connection approval: " + e.getMessage(), e);
            return null;
        }
    }
}
