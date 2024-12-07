package com.example.customerdisplayhandler.api;

import com.example.customerdisplayhandler.model.ServerInfo;

public interface CustomerDisplayManager {
    void startSearchForCustomerDisplays(SearchListener searchListener);
    interface SearchListener {
        void onSearchStarted();
        void onSearchCompleted();
        void onSearchFailed(String errorMessage);
        void onCustomerDisplayFound(ServerInfo serverInfo);
    }
}
