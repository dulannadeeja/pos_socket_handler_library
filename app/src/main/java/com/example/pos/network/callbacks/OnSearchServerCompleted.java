package com.example.pos.network.callbacks;

import com.example.pos.model.ServerInfo;

import java.net.Socket;
import java.util.List;
import java.util.Map;

public interface OnSearchServerCompleted {
    void serversFound(Map<ServerInfo,Socket> servers);
}
