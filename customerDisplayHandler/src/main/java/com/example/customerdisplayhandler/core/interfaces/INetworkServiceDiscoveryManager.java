package com.example.customerdisplayhandler.core.interfaces;

import com.example.customerdisplayhandler.model.ServiceInfo;

public interface INetworkServiceDiscoveryManager {

    void startSearchForServices(SearchListener searchListener, int timeout);
    void stopSearchForServices();

    interface SearchListener {
        void onSearchStarted();
        void onSearchCompleted();
        void onSearchFailed(String errorMessage);
        void onServiceFound(ServiceInfo serviceInfo);
    }
}
