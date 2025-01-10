package com.example.customerdisplayhandler.core.interfaces;
import android.util.Pair;
import com.example.customerdisplayhandler.model.ServiceInfo;
import java.net.Socket;
import io.reactivex.rxjava3.subjects.ReplaySubject;

public interface ISocketsManager {
    void addConnectedSocket(Socket socket, ServiceInfo serviceInfo);
    Pair<Socket, ServiceInfo> getConnectedSocket(String serverId);
    void removeConnectedSocket(String serverId);
    Socket findSocketIfConnected(String serverId);
    ReplaySubject<Pair<Socket, ServiceInfo>> getConnectedSocketsSubject();
}
