package com.example.customerdisplayhandler.shared;

import com.example.customerdisplayhandler.model.ServiceInfo;

public interface OnPairingServerListener {
    void onPairingServerStarted();
    void onConnectionRequestSent();
    void onConnectionRequestApproved(ServiceInfo serviceInfo);
    void onConnectionRequestRejected();
    void onPairingServerFailed(String message);
}
