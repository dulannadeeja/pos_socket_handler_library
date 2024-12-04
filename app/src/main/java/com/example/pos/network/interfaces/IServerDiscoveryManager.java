package com.example.pos.network.interfaces;

import com.example.pos.model.ServerInfo;
import com.example.pos.network.callbacks.OnConnectionReqSent;
import com.example.pos.network.callbacks.OnSearchServerCompleted;

import java.net.Socket;
import java.util.Map;

public interface IServerDiscoveryManager {
    void searchAvailableServersInNetwork(String localIPAddress, int serverPort, OnSearchServerCompleted onSearchServerCompleted,ISocketDataSource socketDataSource);
}
