package com.example.customerdisplayhandler.core.network;

import com.example.customerdisplayhandler.core.interfaces.ClientInfoManager;
import com.example.customerdisplayhandler.helpers.IPManager;
import com.example.customerdisplayhandler.utils.IJsonUtil;
import com.example.customerdisplayhandler.helpers.SharedPrefManager;
import com.example.customerdisplayhandler.model.ClientInfo;
import com.example.customerdisplayhandler.utils.SharedPrefLabels;

import java.util.UUID;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;

public class ClientInfoManagerImpl implements ClientInfoManager {
    private final SharedPrefManager sharedPrefManager;
    private final IPManager ipManager;
    private final IJsonUtil jsonUtil;

    public ClientInfoManagerImpl(IPManager ipManager,SharedPrefManager sharedPrefManager, IJsonUtil jsonUtil) {
        this.ipManager = ipManager;
        this.sharedPrefManager = sharedPrefManager;
        this.jsonUtil = jsonUtil;
    }
    @Override
    public Single<ClientInfo> getClientInfo() {
        return ipManager.getDeviceLocalIPAddress()
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
                });
    }

    private Single<ClientInfo> createAndSaveNewClientInfo(Throwable throwable) {
        return ipManager.getDeviceLocalIPAddress()
                .flatMap(this::createNewClientInfo);
    }

    private Single<ClientInfo> createNewClientInfo(String ipAddress) {
        String clientID = UUID.randomUUID().toString();
        String deviceName = android.os.Build.MODEL;
        ClientInfo newClientInfo = new ClientInfo(clientID, ipAddress, deviceName);
        return saveClientInfo(newClientInfo)
                .andThen(Single.just(newClientInfo));
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
                .andThen(Single.just(updatedClientInfo));
    }

    private Single<ClientInfo> retrieveClientInfo() {
        return Single.create(emitter -> {
            try {
                String clientInfoString = sharedPrefManager.getString(SharedPrefLabels.CLIENT_INFO_LABEL, "");
                if (clientInfoString.isEmpty()) {
                    emitter.onError(new Exception("Client info not found"));
                } else {
                    ClientInfo clientInfo = jsonUtil.toObj(clientInfoString, ClientInfo.class);
                    emitter.onSuccess(clientInfo);
                }
            } catch (Exception e) {
                emitter.onError(e);
            }
        });
    }
    private Completable saveClientInfo(ClientInfo clientInfo) {
        return Completable.create(emitter -> {
            try {
                String clientInfoString = jsonUtil.toJson(clientInfo);
                sharedPrefManager.putString(SharedPrefLabels.CLIENT_INFO_LABEL, clientInfoString);
                emitter.onComplete();
            } catch (Exception e) {
                emitter.onError(e);
            }
        });
    }
}
