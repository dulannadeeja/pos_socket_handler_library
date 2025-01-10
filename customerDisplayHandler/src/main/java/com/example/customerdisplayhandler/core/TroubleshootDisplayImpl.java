package com.example.customerdisplayhandler.core;

import android.util.Log;

import com.example.customerdisplayhandler.constants.NetworkConstants;
import com.example.customerdisplayhandler.core.interfaces.IConnectedDisplaysRepository;
import com.example.customerdisplayhandler.core.interfaces.IMulticastManager;
import com.example.customerdisplayhandler.core.interfaces.INetworkServiceDiscoveryManager;
import com.example.customerdisplayhandler.core.interfaces.ISocketsManager;
import com.example.customerdisplayhandler.core.interfaces.ITcpConnector;
import com.example.customerdisplayhandler.core.interfaces.ITroubleshootDisplay;
import com.example.customerdisplayhandler.model.CustomerDisplay;
import com.example.customerdisplayhandler.model.ServiceInfo;
import com.example.customerdisplayhandler.shared.OnTroubleshootListener;

import java.net.Socket;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class TroubleshootDisplayImpl implements ITroubleshootDisplay {

    ITcpConnector tcpConnector;
    ISocketsManager socketsManager;
    IMulticastManager multicastManager;
    INetworkServiceDiscoveryManager networkServiceDiscoveryManager;
    IConnectedDisplaysRepository connectedDisplaysRepository;

    public TroubleshootDisplayImpl(ITcpConnector tcpConnector, ISocketsManager socketsManager, IMulticastManager multicastManager, INetworkServiceDiscoveryManager networkServiceDiscoveryManager, IConnectedDisplaysRepository connectedDisplaysRepository) {
        this.tcpConnector = tcpConnector;
        this.socketsManager = socketsManager;
        this.multicastManager = multicastManager;
        this.networkServiceDiscoveryManager = networkServiceDiscoveryManager;
        this.connectedDisplaysRepository = connectedDisplaysRepository;
    }

    @Override
    public Completable startManualTroubleshooting(CustomerDisplay customerDisplay, OnTroubleshootListener listener) {
        return disconnectIfConnected(customerDisplay)
                .andThen(handleSocketConnection(customerDisplay, listener)
                        .doOnComplete(listener::onTroubleshootCompleted)
                        .doOnError(throwable -> handleTroubleshootError(throwable, listener))
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread()));
    }

    @Override
    public Completable startSilentTroubleshooting(CustomerDisplay customerDisplay, OnSilentTroubleshootListener listener) {
        OnTroubleshootListener manualTroubleshootListener = new OnTroubleshootListener() {
            @Override
            public void onScanningForCustomerDisplays() {
                Log.d("CustomerDisplayManager", "Scanning for customer displays");
            }

            @Override
            public void onCustomerDisplayFound() {
                Log.d("CustomerDisplayManager", "Customer display found");
            }

            @Override
            public void onAttemptingToConnect() {
                Log.d("CustomerDisplayManager", "Attempting to connect");
            }

            @Override
            public void onSavingCustomerDisplay() {
                Log.d("CustomerDisplayManager", "Saving customer display");
            }

            @Override
            public void onTroubleshootCompleted() {
                listener.onTroubleshootCompleted();
            }

            @Override
            public void onTroubleshootFailed(String errorMessage) {
                listener.onTroubleshootFailed(errorMessage);
            }
        };
        return disconnectIfConnected(customerDisplay)
                .andThen(handleSocketConnection(customerDisplay, manualTroubleshootListener))
                .doOnComplete(listener::onTroubleshootCompleted)
                .doOnError(throwable -> listener.onTroubleshootFailed(throwable.getMessage()))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    private Completable disconnectIfConnected(CustomerDisplay customerDisplay) {
        Socket connectedSocket = socketsManager.findSocketIfConnected(customerDisplay.getCustomerDisplayID());
        if (connectedSocket != null) {
            socketsManager.removeConnectedSocket(customerDisplay.getCustomerDisplayID());
            return tcpConnector.disconnectSafelyFromServer(connectedSocket);
        }
        return Completable.complete();
    }

    private Single<ServiceInfo> searchForCustomerDisplay(CustomerDisplay customerDisplay, OnTroubleshootListener listener) {
        return Single.<ServiceInfo>create(emitter -> {
            networkServiceDiscoveryManager.startSearchForServices(new INetworkServiceDiscoveryManager.SearchListener() {
                @Override
                public void onSearchStarted() {
                    listener.onScanningForCustomerDisplays();
                }

                @Override
                public void onSearchCompleted() {
                    if (!emitter.isDisposed()) {
                        emitter.onError(new Exception("Sorry, we couldn't find the customer display"));
                    }
                    listener.onTroubleshootFailed("Cannot find customer display");
                }

                @Override
                public void onSearchFailed(String errorMessage) {
                    emitter.onError(new Exception("Search failed due to: " + errorMessage));
                    listener.onTroubleshootFailed("Error occurred while searching for customer display");
                }

                @Override
                public void onServiceFound(ServiceInfo foundServiceInfo) {
                    Log.d("CustomerDisplayManager", "Service found: " + foundServiceInfo.getServerId());
                    if (foundServiceInfo.getServerId().equals(customerDisplay.getCustomerDisplayID())) {
                        emitter.onSuccess(foundServiceInfo);
                        listener.onCustomerDisplayFound();
                        networkServiceDiscoveryManager.stopSearchForServices();
                    }
                }
            }, NetworkConstants.TROUBLESHOOTING_TIMEOUT);
        }).subscribeOn(AndroidSchedulers.mainThread());
    }

    private Completable handleSocketConnection(CustomerDisplay customerDisplay, OnTroubleshootListener listener) {
        return multicastManager.sendMessage("all_devices." + NetworkConstants.TURN_ON_ALL_DEVICES_COMMAND)
                .andThen(searchForCustomerDisplay(customerDisplay, listener))
                .flatMapCompletable(newServiceInfo -> connectToService(newServiceInfo, customerDisplay, listener));
    }

    private Completable connectToService(ServiceInfo newServiceInfo, CustomerDisplay customerDisplay, OnTroubleshootListener listener) {
        return tcpConnector.connectToServer(newServiceInfo.getIpAddress(), NetworkConstants.DEFAULT_SERVER_PORT)
                .doOnSubscribe(disposable -> listener.onAttemptingToConnect())
                .flatMapCompletable((socket) -> updateCustomerDisplay(socket, newServiceInfo, customerDisplay)
                        .doOnSubscribe(d -> listener.onSavingCustomerDisplay())
                );
    }

    private Completable updateCustomerDisplay(Socket socket, ServiceInfo newServiceInfo, CustomerDisplay customerDisplay) {
        CustomerDisplay updatedCustomerDisplay = new CustomerDisplay(
                newServiceInfo.getServerId(),
                newServiceInfo.getDeviceName(),
                newServiceInfo.getIpAddress(),
                customerDisplay.getIsActivated()
        );
        return connectedDisplaysRepository.updateCustomerDisplay(updatedCustomerDisplay).ignoreElement()
                .doOnComplete(() -> {
                    Log.d("CustomerDisplayManager", "Customer display updated: " + updatedCustomerDisplay.getCustomerDisplayID());
                    Log.d("CustomerDisplayManager", "Customer display updated: " + updatedCustomerDisplay.getCustomerDisplayName());
                    Log.d("CustomerDisplayManager", "Customer display updated: " + updatedCustomerDisplay.getCustomerDisplayIpAddress());
                    socketsManager.addConnectedSocket(socket, newServiceInfo);
                });

    }

    private void handleTroubleshootError(Throwable throwable, OnTroubleshootListener listener) {
        Log.e("CustomerDisplayManager", "Error starting troubleshooting: " + throwable);
        listener.onTroubleshootFailed(throwable.getMessage());
    }


}
