package com.example.customerdisplayhandler.core.interfaces;

import com.example.customerdisplayhandler.model.ClientInfo;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;

public interface ClientInfoManager {
    Single<ClientInfo> getClientInfo();
}
