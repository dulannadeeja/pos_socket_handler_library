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
import com.example.customerdisplayhandler.shared.OnSilentTroubleshootListener;
import com.example.customerdisplayhandler.shared.OnTroubleshootListener;

import java.net.Socket;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class TroubleshootDisplayImpl implements ITroubleshootDisplay {
    private final String TAG = TroubleshootDisplayImpl.class.getSimpleName();
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
                .doOnComplete(() -> Log.i(TAG, "looking for connected sockets and disconnection completed"))
                .andThen(handleSocketConnection(customerDisplay, listener)
                        .doOnComplete(listener::onTroubleshootCompleted)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread()));
    }

    @Override
    public Completable startSilentTroubleshooting(CustomerDisplay customerDisplay) {
        return disconnectIfConnected(customerDisplay)
                .doOnComplete(() -> Log.i(TAG, "Disconnected from existing connections"))
                .andThen(handleSocketConnectionWithoutListeners(customerDisplay))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    private Completable handleSocketConnection(CustomerDisplay customerDisplay, OnTroubleshootListener listener) {
        return notifyDevicesToEnableDiscoveryMode()
                .doOnComplete(() -> Log.i(TAG, "Notification sent to all devices to turn on discoverable mode"))
                .andThen(searchForCustomerDisplay(customerDisplay, listener))
                .flatMapCompletable(newServiceInfo -> connectToService(newServiceInfo, customerDisplay, listener));
    }

    private Single<ServiceInfo> searchForCustomerDisplay(CustomerDisplay customerDisplay, OnTroubleshootListener listener) {
        return searchForService(customerDisplay)
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(disposable -> listener.onScanningForCustomerDisplays())
                .doOnSuccess(serviceInfo -> listener.onCustomerDisplayFound());
    }

    private Completable handleSocketConnectionWithoutListeners(CustomerDisplay customerDisplay) {
        return notifyDevicesToEnableDiscoveryMode()
                .andThen(searchForCustomerDisplayWithoutListeners(customerDisplay))
                .flatMapCompletable(nsdServiceInfo -> connectToServiceWithoutListeners(nsdServiceInfo, customerDisplay));
    }

    private Completable connectToServiceWithoutListeners(ServiceInfo serviceInfo, CustomerDisplay customerDisplay) {
        return tcpConnector.connectToServer(serviceInfo.getIpAddress(), NetworkConstants.DEFAULT_SERVER_PORT)
                .flatMapCompletable(socket -> updateCustomerDisplay(socket, serviceInfo, customerDisplay));
    }

    private Single<ServiceInfo> searchForCustomerDisplayWithoutListeners(CustomerDisplay customerDisplay) {
        return searchForService(customerDisplay)
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(AndroidSchedulers.mainThread());
    }

    private Single<ServiceInfo> searchForService(CustomerDisplay customerDisplay) {
        return Single.<ServiceInfo>create(emitter -> {
                    networkServiceDiscoveryManager.startSearchForServices(new INetworkServiceDiscoveryManager.SearchListener() {
                        @Override
                        public void onSearchStarted() {
                            Log.i(TAG, "Scanning for customer displays...");
                        }

                        @Override
                        public void onSearchCompleted() {
                            if (!emitter.isDisposed()) {
                                emitter.onError(new Exception("No customer display found"));
                            }
                        }

                        @Override
                        public void onSearchFailed(String errorMessage) {
                            emitter.onError(new Exception("Search failed: " + errorMessage));
                        }

                        @Override
                        public void onServiceFound(ServiceInfo foundServiceInfo) {
                            Log.i(TAG, "Found service: " + foundServiceInfo.getServerId());
                            Log.i(TAG, "Customer display ID: " + customerDisplay.getCustomerDisplayID());
                            if (foundServiceInfo.getServerId().equals(customerDisplay.getCustomerDisplayID())) {
                                emitter.onSuccess(foundServiceInfo);
                                networkServiceDiscoveryManager.stopSearchForServices();
                            }
                        }
                    }, NetworkConstants.TROUBLESHOOTING_TIMEOUT);
                })
                .doOnSuccess(serviceInfo -> Log.i(TAG, "Found customer display at IP: " + serviceInfo.getIpAddress()));
    }

    private Completable notifyDevicesToEnableDiscoveryMode() {
        return multicastManager.sendMessage("all_devices." + NetworkConstants.TURN_ON_ALL_DEVICES_COMMAND)
                .doOnComplete(() -> Log.i(TAG, "Sent discovery mode notification to all devices"));
    }

    private Completable connectToService(ServiceInfo newServiceInfo, CustomerDisplay customerDisplay, OnTroubleshootListener listener) {
        return tcpConnector.connectToServer(newServiceInfo.getIpAddress(), NetworkConstants.DEFAULT_SERVER_PORT)
                .doOnSuccess(socket -> Log.i(TAG, "TCP connection established with customer display"))
                .doOnSubscribe(disposable -> listener.onAttemptingToConnect())
                .flatMapCompletable((socket) -> updateCustomerDisplay(socket, newServiceInfo, customerDisplay)
                        .doOnSubscribe(d -> listener.onSavingCustomerDisplay())
                );
    }

    private Completable disconnectIfConnected(CustomerDisplay customerDisplay) {
        Socket connectedSocket = socketsManager.findSocketIfConnected(customerDisplay.getCustomerDisplayID());
        if (connectedSocket != null) {
            socketsManager.removeConnectedSocket(customerDisplay.getCustomerDisplayID());
            return tcpConnector.disconnectSafelyFromServer(connectedSocket);
        }
        return Completable.complete();
    }

    private Completable updateCustomerDisplay(Socket socket, ServiceInfo newServiceInfo, CustomerDisplay customerDisplay) {
        CustomerDisplay updatedCustomerDisplay = new CustomerDisplay(
                newServiceInfo.getServerId(),
                customerDisplay.getCustomerDisplayName(),
                newServiceInfo.getIpAddress(),
                customerDisplay.getIsActivated(),
                customerDisplay.getIsDarkModeActivated()
        );
        return connectedDisplaysRepository.updateCustomerDisplay(updatedCustomerDisplay).ignoreElement()
                .doOnComplete(() -> {
                    Log.i(TAG, "Customer display updated successfully with new IP: " + newServiceInfo.getIpAddress());
                    socketsManager.addConnectedSocket(socket, newServiceInfo);
                })
                .subscribeOn(Schedulers.io());

    }


}
