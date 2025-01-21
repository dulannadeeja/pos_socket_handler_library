package com.example.customerdisplayhandler.helpers;

import android.util.Log;
import android.util.Pair;

import com.example.customerdisplayhandler.constants.NetworkConstants;
import com.example.customerdisplayhandler.model.SocketMessageBase;
import com.example.customerdisplayhandler.utils.IJsonUtil;
import com.google.gson.Gson;

public class SocketMessageProcessHelperImpl implements ISocketMessageProcessHelper {
    private static final String TAG = SocketMessageProcessHelperImpl.class.getSimpleName();
    private IJsonUtil jsonUtil;

    public SocketMessageProcessHelperImpl(IJsonUtil jsonUtil) {
        this.jsonUtil = jsonUtil;
    }

    public SocketMessageBase getBaseMessage(String message) {
        try {
            SocketMessageBase socketMessageBase = jsonUtil.toObj(message, SocketMessageBase.class);
            if (socketMessageBase.getCommand() == null || socketMessageBase.getCommand().isEmpty()) {
                Log.i(TAG, "Message arrives to the data source (ignored), command is empty");
                return null;
            } else if (socketMessageBase.getSenderId() == null || socketMessageBase.getSenderId().isEmpty()) {
                Log.i(TAG, "Message arrives to the data source (ignored), senderId is empty");
                return null;
            } else if (socketMessageBase.getData() == null) {
                Log.i(TAG, "Message arrives to the data source (ignored), data is empty");
                return null;
            } else if (socketMessageBase.getMessageId() == null || socketMessageBase.getMessageId().isEmpty()) {
                Log.i(TAG, "Message arrives to the data source (ignored), messageId is empty");
                return null;
            }
            return socketMessageBase;
        } catch (Exception e) {
            Log.e(TAG, "Error processing message: " + e.getMessage(), e);
            return null;
        }
    }

    public String getAcknowledgeMessageId(String message) {
        SocketMessageBase baseMessage = getBaseMessage(message);
        if (baseMessage == null) {
            return null;
        }
        if(!baseMessage.getCommand().equals(NetworkConstants.MESSAGE_ACKNOWLEDGEMENT)) {
            Log.i(TAG, "Message arrives to the data source (ignored), command is not an acknowledgement");
            return null;
        }
        String messageId = baseMessage.getData().toString();
        if (messageId.isEmpty()) {
            Log.i(TAG, "Message arrives to the data source (ignored), acknowledgement message is empty");
            return null;
        }
        return messageId;
    }
}
