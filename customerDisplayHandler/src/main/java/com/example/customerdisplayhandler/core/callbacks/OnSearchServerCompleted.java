package com.example.customerdisplayhandler.core.callbacks;

import com.example.customerdisplayhandler.model.CustomerDisplay;

import java.net.Socket;
import java.util.Map;

public interface OnSearchServerCompleted {
    void serversFound(Map<CustomerDisplay,Socket> servers);
}
