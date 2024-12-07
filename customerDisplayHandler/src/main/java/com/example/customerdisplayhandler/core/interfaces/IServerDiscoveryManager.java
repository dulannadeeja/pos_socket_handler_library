package com.example.customerdisplayhandler.core.interfaces;
import android.util.Pair;
import com.example.customerdisplayhandler.model.ServerInfo;
import java.net.Socket;
import io.reactivex.rxjava3.core.Flowable;

public interface IServerDiscoveryManager {
    Flowable<Pair<ServerInfo, Socket>> searchAvailableServersInNetwork(String localIPAddress, int serverPort);
}
