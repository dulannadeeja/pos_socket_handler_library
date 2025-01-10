package com.example.customerdisplayhandler.core.interfaces;

import java.net.Socket;

import io.reactivex.rxjava3.core.Completable;

public interface ITcpMessageSender{
    public Completable sendMessageToServer(String serverId, Socket socket, String message);
}
