package com.example.customerdisplayhandler.api;

import com.example.customerdisplayhandler.core.interfaces.IConnectedServerManager;
import com.example.customerdisplayhandler.core.interfaces.INetworkServiceDiscoveryManager;
import com.example.customerdisplayhandler.model.CustomerDisplay;
import com.example.customerdisplayhandler.model.ServiceInfo;

import java.net.Socket;
import java.util.List;

import io.reactivex.rxjava3.core.Completable;

public interface ICustomerDisplayManager {
    void startSearchForCustomerDisplays(INetworkServiceDiscoveryManager.SearchListener searchListener);
    void stopSearchForCustomerDisplays();
    Completable startListeningForServerMessages(String serverId, Socket socket);
    void startPairingServer(ServiceInfo serviceInfo, IConnectedServerManager.OnPairingServerListener listener);
    void stopPairingServer();
    void sendMulticastMessage(String message);
    void addConnectedDisplay(String customerDisplayId, String customerDisplayName, String customerDisplayIpAddress,AddCustomerDisplayListener listener);
    void removeConnectedDisplay(String customerDisplayId, RemoveCustomerDisplayListener listener);
    void getConnectedDisplays(GetConnectedDisplaysListener listener);
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
}
