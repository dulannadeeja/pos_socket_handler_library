package com.example.customerdisplayhandler.core.interfaces;

import com.example.customerdisplayhandler.model.ClientInfo;

import io.reactivex.rxjava3.core.Single;

public interface IClientInfoManager {
    Single<ClientInfo> getClientInfo();
    void setTerminalID(String terminalID);
}
