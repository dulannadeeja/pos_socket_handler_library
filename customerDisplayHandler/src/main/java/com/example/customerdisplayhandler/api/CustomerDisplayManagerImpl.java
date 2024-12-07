package com.example.customerdisplayhandler.api;

import android.content.Context;

import com.example.customerdisplayhandler.core.interfaces.IConnectedServerManager;
import com.example.customerdisplayhandler.core.interfaces.IJsonUtil;
import com.example.customerdisplayhandler.core.interfaces.IServerDiscoveryManager;
import com.example.customerdisplayhandler.core.interfaces.ISocketConnectionManager;
import com.example.customerdisplayhandler.core.interfaces.ISocketDataSource;
import com.example.customerdisplayhandler.core.network.ConnectedServerManagerImpl;
import com.example.customerdisplayhandler.core.network.ServerDiscoveryManagerImpl;
import com.example.customerdisplayhandler.core.network.SocketConnectionManagerImpl;
import com.example.customerdisplayhandler.helpers.IPManager;
import com.example.customerdisplayhandler.helpers.IPManagerImpl;
import com.example.customerdisplayhandler.utils.JsonUtilImpl;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class CustomerDisplayManagerImpl implements CustomerDisplayManager{
    private static CustomerDisplayManagerImpl INSTANCE;
    private Context context;
    private int serverPort;
    private IJsonUtil jsonUtil;
    private IPManager ipManager;
    private ISocketDataSource socketDataSource;
    private ISocketConnectionManager socketConnectionManager;
    private IServerDiscoveryManager serverDiscoveryManager;
    private IConnectedServerManager connectedServerManager;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();
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
        connectedServerManager = new ConnectedServerManagerImpl();
        serverDiscoveryManager = new ServerDiscoveryManagerImpl(socketConnectionManager,connectedServerManager,jsonUtil);
    }

    @Override
    public void startSearchForCustomerDisplays(SearchListener searchListener) {
        searchListener.onSearchStarted();
        compositeDisposable.add(
                ipManager.getDeviceLocalIPAddress()
                        .flatMapCompletable(localIPAddress ->{
                            return  serverDiscoveryManager.searchAvailableServersInNetwork(localIPAddress,serverPort)
                                    .doOnNext(pair -> {
                                        searchListener.onCustomerDisplayFound(pair.first);
                                    })
                                    .ignoreElements();
                        })
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(() -> {
                            searchListener.onSearchCompleted();
                        }, throwable -> {
                            searchListener.onSearchFailed(throwable.getMessage());
                        })


        );
    }
}
