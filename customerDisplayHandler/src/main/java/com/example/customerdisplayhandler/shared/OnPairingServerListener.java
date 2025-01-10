package com.example.customerdisplayhandler.shared;

import com.example.customerdisplayhandler.model.ServiceInfo;

public interface OnPairingServerListener {
    void onPairingServerStarted();
    void onCustomerDisplayFound();
    void onConnectionRequestSent();
    void onConnectionRequestRejected();
    void onConnectionRequestApproved(ServiceInfo serviceInfo);
    void onSavedEstablishedConnection(ServiceInfo serviceInfo);
    void onPairingServerFailed(String message);
}
