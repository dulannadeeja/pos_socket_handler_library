package com.example.customerdisplayhandler.api;

import android.util.Pair;

import com.example.customerdisplayhandler.core.interfaces.INetworkServiceDiscoveryManager;
import com.example.customerdisplayhandler.shared.OnPairingServerListener;
import com.example.customerdisplayhandler.shared.OnTroubleshootListener;
import com.example.customerdisplayhandler.model.CustomerDisplay;
import com.example.customerdisplayhandler.model.ServiceInfo;

import java.net.Socket;
import java.util.List;

import io.reactivex.rxjava3.core.Completable;

public interface ICustomerDisplayManager {
    void startSearchForCustomerDisplays(INetworkServiceDiscoveryManager.SearchListener searchListener);
    void stopSearchForCustomerDisplays();
    void startListeningForServerMessages(String serverId, Socket socket);
    void startPairingCustomerDisplay(ServiceInfo serviceInfo, OnPairingServerListener listener);
    void stopPairingServer();
    void sendMulticastMessage(String message);
    void addConnectedDisplay(String customerDisplayId, String customerDisplayName, String customerDisplayIpAddress,AddCustomerDisplayListener listener);
    void removeConnectedDisplay(String customerDisplayId, RemoveCustomerDisplayListener listener);
    void getConnectedDisplays(GetConnectedDisplaysListener listener);
    void toggleCustomerDisplayActivation(String customerDisplayId,OnCustomerDisplayActivationToggleListener listener);
    void startManualTroubleshooting(CustomerDisplay customerDisplay, OnTroubleshootListener listener);
    void sendUpdatesToCustomerDisplays(String data,OnSendUpdatesListener listener);
    void disposeCustomerDisplayManager();


    interface AddCustomerDisplayListener {
        void onCustomerDisplayAdded(CustomerDisplay customerDisplay);
        void onCustomerDisplayAddFailed(String errorMessage);
    }
    interface RemoveCustomerDisplayListener {
        void onCustomerDisplayRemoved();
        void onCustomerDisplayRemoveFailed(String errorMessage);
    }
    interface GetConnectedDisplaysListener {
        void onConnectedDisplaysReceived(List<CustomerDisplay> customerDisplays);
        void onConnectedDisplaysReceiveFailed(String errorMessage);
    }
    interface OnCustomerDisplayActivationToggleListener {
        void onCustomerDisplayActivated();

        void onCustomerDisplayDeactivated();

        void onCustomerDisplayActivationToggleFailed(String errorMessage);
    }

    interface OnSendUpdatesListener{
        void onUpdatesSent();
        void onUpdatesSendFailed(List<Pair<CustomerDisplay,String>> errors);
    }
}
