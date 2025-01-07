package com.example.customerdisplayhandler.core.interfaces;

import android.util.Pair;

import com.example.customerdisplayhandler.model.ClientInfo;
import com.example.customerdisplayhandler.model.ServiceInfo;

import java.net.Socket;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.subjects.BehaviorSubject;

public interface IConnectedServerManager {

    Completable sendMessageToServer(String serverId, Socket socket, String message);
    Completable startListening(String serverId, Socket socket);
    Single<Boolean> startPairingServer(ServiceInfo serviceInfo, Socket socket, ClientInfo clientInfo,OnConnectionRequestSendListener onConnectionRequestSendListener);
    BehaviorSubject<Pair<String, String>> getServerMessageSubject();

    interface OnConnectionRequestSendListener {
        void onConnectionRequestSent();
    }
}
