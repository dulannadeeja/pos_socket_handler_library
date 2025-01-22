package com.example.customerdisplayhandler.core.interfaces;

import java.net.Socket;

import io.reactivex.rxjava3.core.Completable;

public interface ITcpMessageSender{
    Completable sendMessageAndCatchAcknowledgement(String serverId, Socket socket, String message, String messageId, String clientId);
    Completable sendOneWayMessage(Socket socket,String serverId, String message);
}
