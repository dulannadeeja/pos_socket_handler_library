package com.example.customerdisplayhandler.api;

import com.example.customerdisplayhandler.core.interfaces.IConnectedServerManager;
import com.example.customerdisplayhandler.core.interfaces.INetworkServiceDiscoveryManager;
import com.example.customerdisplayhandler.model.ServerInfo;
import com.example.customerdisplayhandler.model.ServiceInfo;

public interface ICustomerDisplayManager {
    void startSearchForCustomerDisplays(INetworkServiceDiscoveryManager.SearchListener searchListener);
    void stopSearchForCustomerDisplays();
    void startPairingServer(ServiceInfo serviceInfo, IConnectedServerManager.OnPairingServerListener listener);
    void stopPairingServer();
    void sendMulticastMessage(String message);
    void disposeCustomerDisplayManager();
}
