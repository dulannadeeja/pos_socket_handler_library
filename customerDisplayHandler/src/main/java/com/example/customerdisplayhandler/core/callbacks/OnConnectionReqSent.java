package com.example.customerdisplayhandler.core.callbacks;

import com.example.customerdisplayhandler.model.ServerInfo;

public interface OnConnectionReqSent {
    void onSuccess(ServerInfo serverInfo);
    void onFailed(Exception e);
}
