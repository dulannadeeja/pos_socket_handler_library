package com.example.customerdisplayhandler.api;

import android.content.Context;
import android.util.Log;
import android.util.Pair;

import com.example.customerdisplayhandler.core.interfaces.ClientInfoManager;
import com.example.customerdisplayhandler.core.interfaces.IConnectedServerManager;
import com.example.customerdisplayhandler.utils.IJsonUtil;
import com.example.customerdisplayhandler.core.interfaces.IServerDiscoveryManager;
import com.example.customerdisplayhandler.core.interfaces.ISocketConnectionManager;
import com.example.customerdisplayhandler.core.interfaces.ISocketDataSource;
import com.example.customerdisplayhandler.core.network.ClientInfoManagerImpl;
import com.example.customerdisplayhandler.core.network.ConnectedServerManagerImpl;
import com.example.customerdisplayhandler.core.network.ServerDiscoveryManagerImpl;
import com.example.customerdisplayhandler.core.network.SocketConnectionManagerImpl;
import com.example.customerdisplayhandler.helpers.IPManager;
import com.example.customerdisplayhandler.helpers.IPManagerImpl;
import com.example.customerdisplayhandler.helpers.SharedPrefManager;
import com.example.customerdisplayhandler.helpers.SharedPrefManagerImpl;
import com.example.customerdisplayhandler.model.ServerInfo;
import com.example.customerdisplayhandler.utils.JsonUtilImpl;

import java.net.Socket;
import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class CustomerDisplayManagerImpl implements CustomerDisplayManager {
    private static CustomerDisplayManagerImpl INSTANCE;
    private Context context;
    private int serverPort;
    private IJsonUtil jsonUtil;
    private IPManager ipManager;
    private ISocketDataSource socketDataSource;
    private ISocketConnectionManager socketConnectionManager;
    private IServerDiscoveryManager serverDiscoveryManager;
    private IConnectedServerManager connectedServerManager;
    private SharedPrefManager sharedPrefManager;
    private ClientInfoManager clientInfoManager;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private Disposable searchCustomerDisplaysDisposable;

    public static CustomerDisplayManagerImpl newInstance(Context context, int serverPort) {
        if (INSTANCE == null) {
            INSTANCE = new CustomerDisplayManagerImpl(context, serverPort);
        }
        return INSTANCE;
    }

    private CustomerDisplayManagerImpl(Context context, int serverPort) {
        this.context = context;
        this.serverPort = serverPort;
        jsonUtil = new JsonUtilImpl();
        ipManager = new IPManagerImpl(context);
        socketConnectionManager = new SocketConnectionManagerImpl();
        connectedServerManager = new ConnectedServerManagerImpl(jsonUtil);
        serverDiscoveryManager = new ServerDiscoveryManagerImpl(socketConnectionManager, connectedServerManager, jsonUtil);
        sharedPrefManager = SharedPrefManagerImpl.getInstance(context);
        ipManager = new IPManagerImpl(context);
        clientInfoManager = new ClientInfoManagerImpl(ipManager, sharedPrefManager, jsonUtil);
    }

    @Override
    public void startSearchForCustomerDisplays(SearchListener searchListener) {
        searchCustomerDisplaysDisposable =
                connectedServerManager.clearInactiveServers()
                        .doOnComplete(searchListener::onSearchStarted)
                        .andThen(
                                ipManager.getDeviceLocalIPAddress()
                                        .flatMapCompletable(localIPAddress -> serverDiscoveryManager.searchAvailableServersInNetwork(localIPAddress, serverPort)
                                                .doOnNext(pair -> {
                                                    searchListener.onCustomerDisplayFound(pair.first);
                                                }).ignoreElements())
                        ).subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(searchListener::onSearchCompleted, throwable -> {
                            searchListener.onSearchFailed(throwable.getMessage());
                        });
    }

    @Override
    public void stopSearchForCustomerDisplays() {
        if (searchCustomerDisplaysDisposable != null && !searchCustomerDisplaysDisposable.isDisposed()) {
            searchCustomerDisplaysDisposable.dispose();
        }
    }

    @Override
    public void startPairingServer(ServerInfo serverInfo, IConnectedServerManager.OnPairingServerListener listener) {
        compositeDisposable.add(
                clientInfoManager.getClientInfo()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe((clientInfo) -> {
                            connectedServerManager.startPairingServer(serverInfo, clientInfo, listener);
                        }, throwable -> {
                            listener.onConnectionRequestFailed();
                            Log.e("CustomerDisplayManager", "Error getting client info: " + throwable.getMessage());
                        })
        );
    }

    @Override
    public List<ServerInfo> getAvailableCustomerDisplays() {
        List<Pair<ServerInfo,Socket>> availableCustomerDisplays = connectedServerManager.getDiscoveredServers();
        List<ServerInfo> availableCustomerDisplayList = new java.util.ArrayList<>();
        for (Pair<ServerInfo,Socket> pair : availableCustomerDisplays) {
            availableCustomerDisplayList.add(pair.first);
        }
        return availableCustomerDisplayList;
    }

    @Override
    public List<ServerInfo> getPairedCustomerDisplays() {
        List<Pair<ServerInfo,Socket>> pairedCustomerDisplays = connectedServerManager.getEstablishedConnections();
        List<ServerInfo> pairedCustomerDisplayList = new java.util.ArrayList<>();
        for (Pair<ServerInfo,Socket> pair : pairedCustomerDisplays) {
            pairedCustomerDisplayList.add(pair.first);
        }
        return pairedCustomerDisplayList;
    }

    @Override
    public void disposeCustomerDisplayManager() {
        compositeDisposable.clear();

    }
}
