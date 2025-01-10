package com.example.customerdisplayhandler.core;

import android.util.Log;

import com.example.customerdisplayhandler.core.interfaces.IClientInfoManager;
import com.example.customerdisplayhandler.helpers.IPManager;
import com.example.customerdisplayhandler.utils.IJsonUtil;
import com.example.customerdisplayhandler.helpers.ISharedPrefManager;
import com.example.customerdisplayhandler.model.ClientInfo;
import com.example.customerdisplayhandler.utils.SharedPrefLabels;

import java.util.UUID;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class IClientInfoManagerImpl implements IClientInfoManager {
    private final ISharedPrefManager ISharedPrefManager;
    private final IPManager ipManager;
    private final IJsonUtil jsonUtil;

    public IClientInfoManagerImpl(IPManager ipManager, ISharedPrefManager ISharedPrefManager, IJsonUtil jsonUtil) {
        this.ipManager = ipManager;
        this.ISharedPrefManager = ISharedPrefManager;
        this.jsonUtil = jsonUtil;
    }

    @Override
    public Single<ClientInfo> getClientInfo() {
        return ipManager.getDeviceLocalIPAddress()
                .doOnSuccess(ipAddress -> Log.i("ClientInfoManager", "Device IP Address: " + ipAddress))
                .flatMap(this::getOrUpdateClientInfo)
                .onErrorResumeNext(this::createAndSaveNewClientInfo);
    }

    private Single<ClientInfo> getOrUpdateClientInfo(String ipAddress) {
        return retrieveClientInfo()
                .flatMap(clientInfo -> {
                    if (isSameIPAddress(clientInfo, ipAddress)) {
                        return Single.just(clientInfo);
                    } else {
                        return updateAndSaveClientInfo(clientInfo, ipAddress);
                    }
                })
                .subscribeOn(Schedulers.io())
                .doOnSuccess(clientInfo -> Log.i("ClientInfoManager", "Client Info: " + clientInfo.toString()));
    }

    private Single<ClientInfo> createAndSaveNewClientInfo(Throwable throwable) {
        return ipManager.getDeviceLocalIPAddress()
                .flatMap(this::createNewClientInfo)
                .doOnSuccess(clientInfo -> Log.i("ClientInfoManager", "New Client Info: " + clientInfo.toString()))
                .subscribeOn(Schedulers.io());
    }

    private Single<ClientInfo> createNewClientInfo(String ipAddress) {
        String clientID = UUID.randomUUID().toString();
        String deviceName = android.os.Build.MODEL;
        ClientInfo newClientInfo = new ClientInfo(clientID, ipAddress, deviceName);
        return saveClientInfo(newClientInfo)
                .andThen(Single.just(newClientInfo))
                .subscribeOn(Schedulers.io())
                .doOnSuccess(clientInfo -> Log.i("ClientInfoManager", "New Client Info: " + clientInfo.toString()));
    }

    private boolean isSameIPAddress(ClientInfo clientInfo, String ipAddress) {
        return clientInfo.getClientIpAddress().equals(ipAddress);
    }

    private Single<ClientInfo> updateAndSaveClientInfo(ClientInfo clientInfo, String ipAddress) {
        ClientInfo updatedClientInfo = new ClientInfo(
                clientInfo.getClientID(),
                ipAddress,
                clientInfo.getClientDeviceName()
        );
        return saveClientInfo(updatedClientInfo)
                .andThen(Single.just(updatedClientInfo))
                .subscribeOn(Schedulers.io())
                .doOnSuccess(clientInfo1 -> Log.i("ClientInfoManager", "Updated Client Info: " + clientInfo1.toString()));
    }

    private Single<ClientInfo> retrieveClientInfo() {
        return Single.<ClientInfo>create(emitter -> {
                    try {
                        String clientInfoString = ISharedPrefManager.getString(SharedPrefLabels.CLIENT_INFO_LABEL, "");
                        if (clientInfoString.isEmpty()) {
                            emitter.onError(new Exception("Error occurred while retrieving POS info"));
                        } else {
                            ClientInfo clientInfo = jsonUtil.toObj(clientInfoString, ClientInfo.class);
                            emitter.onSuccess(clientInfo);
                        }
                    } catch (Exception e) {
                        emitter.onError(e);
                    }
                })
                .subscribeOn(Schedulers.io())
                .doOnError(throwable -> Log.e("ClientInfoManager", "Error retrieving POS info: " + throwable.getMessage()));
    }

    private Completable saveClientInfo(ClientInfo clientInfo) {
        return Completable.create(emitter -> {
                    try {
                        String clientInfoString = jsonUtil.toJson(clientInfo);
                        ISharedPrefManager.putString(SharedPrefLabels.CLIENT_INFO_LABEL, clientInfoString);
                        emitter.onComplete();
                    } catch (Exception e) {
                        Exception newException = new Exception("Error occurred while saving POS info");
                        emitter.onError(newException);
                    }
                })
                .subscribeOn(Schedulers.io())
                .doOnError(throwable -> Log.e("ClientInfoManager", "Error saving POS info: " + throwable.getMessage()));
    }
}
