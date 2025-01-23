package com.example.customerdisplayhandler.core.interfaces;
import android.util.Pair;
import com.example.customerdisplayhandler.model.ServiceInfo;
import java.net.Socket;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.subjects.ReplaySubject;

public interface ISocketsManager {
    Single<Socket> reconnectIfDisconnected(String serverId, String serverIpAddress);
    Single<Socket> reconnect(String serverId, String serverIpAddress);
    Completable disconnectIfConnected(String serverId);
    PublishSubject<Pair<String, Socket>> getSocketConnectionSubject();
}
