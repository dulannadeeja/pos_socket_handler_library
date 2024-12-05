package com.example.customerdisplayhandler.core.interfaces;
import com.example.customerdisplayhandler.core.callbacks.OnSearchServerCompleted;

public interface IServerDiscoveryManager {
    void searchAvailableServersInNetwork(String localIPAddress, int serverPort, OnSearchServerCompleted onSearchServerCompleted,ISocketDataSource socketDataSource);
}
