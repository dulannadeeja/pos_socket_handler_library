package com.example.customerdisplayhandler.api;

import android.util.Pair;

import com.example.customerdisplayhandler.core.interfaces.IConnectedServerManager;
import com.example.customerdisplayhandler.model.ClientInfo;
import com.example.customerdisplayhandler.model.ServerInfo;

import java.net.Socket;
import java.util.List;

public interface CustomerDisplayManager {
    void startSearchForCustomerDisplays(SearchListener searchListener);
    void stopSearchForCustomerDisplays();
    List<ServerInfo> getAvailableCustomerDisplays();
    List<ServerInfo> getPairedCustomerDisplays();
    void startPairingServer(ServerInfo serverInfo, IConnectedServerManager.OnPairingServerListener listener);
    void disposeCustomerDisplayManager();
    interface SearchListener {
        void onSearchStarted();
        void onSearchCompleted();
        void onSearchFailed(String errorMessage);
        void onCustomerDisplayFound(ServerInfo serverInfo);
    }
}
