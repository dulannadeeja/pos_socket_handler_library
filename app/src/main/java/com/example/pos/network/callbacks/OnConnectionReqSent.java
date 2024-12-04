package com.example.pos.network.callbacks;

import com.example.pos.model.ServerInfo;

public interface OnConnectionReqSent {
    void onSuccess(ServerInfo serverInfo);
    void onFailed(Exception e);
}
