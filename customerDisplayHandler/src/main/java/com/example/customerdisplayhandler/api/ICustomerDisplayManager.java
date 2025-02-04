package com.example.customerdisplayhandler.api;

import android.util.Pair;

import com.example.customerdisplayhandler.core.interfaces.INetworkServiceDiscoveryManager;
import com.example.customerdisplayhandler.model.DisplayUpdates;
import com.example.customerdisplayhandler.shared.OnPairingServerListener;
import com.example.customerdisplayhandler.shared.OnTroubleshootListener;
import com.example.customerdisplayhandler.model.CustomerDisplay;
import com.example.customerdisplayhandler.model.ServiceInfo;

import java.net.Socket;
import java.util.List;

public interface ICustomerDisplayManager {
    void startSearchForCustomerDisplays(INetworkServiceDiscoveryManager.SearchListener searchListener);
    void stopSearchForCustomerDisplays();
    void startListeningForServerMessages(String serverId, Socket socket);
    void startPairingCustomerDisplay(ServiceInfo serviceInfo,Boolean isDarkMode, OnPairingServerListener listener);
    void stopPairingServer();
    void sendMulticastMessage(String message);
    void removeConnectedDisplay(String customerDisplayId, RemoveCustomerDisplayListener listener);
    void getConnectedDisplays(GetConnectedDisplaysListener listener);
    void toggleCustomerDisplayActivation(String customerDisplayId,OnCustomerDisplayActivationToggleListener listener);
    void startManualTroubleshooting(CustomerDisplay customerDisplay, OnTroubleshootListener listener);
    void stopManualTroubleshooting();
    void sendUpdatesToCustomerDisplays(DisplayUpdates displayUpdates, OnSendUpdatesListener listener);
    void updateCustomerDisplay(CustomerDisplay updatedCustomerDisplay, OnUpdateDisplayListener listener);
    void stopSendingUpdatesToCustomerDisplays();
    void disposeCustomerDisplayManager();
    void setTerminalID(String terminalID);


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
        void onAllUpdatesSentWithSuccess();
        void onSomeUpdatesFailed(List<Pair<CustomerDisplay,Boolean>> failedCustomerDisplays);
        void onSystemError(String errorMessage);
    }

    interface OnUpdateDisplayListener {
        void onDisplayUpdated();
        void onUpdateDisplayFailed(String errorMessage);
    }
}
