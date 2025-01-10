package com.example.customerdisplayhandler.api;

import android.content.Context;
import android.util.Log;
import android.util.Pair;

import com.example.customerdisplayhandler.constants.NetworkConstants;
import com.example.customerdisplayhandler.core.PairDisplayImpl;
import com.example.customerdisplayhandler.core.TroubleshootDisplayImpl;
import com.example.customerdisplayhandler.core.interfaces.IClientInfoManager;
import com.example.customerdisplayhandler.core.interfaces.IMulticastManager;
import com.example.customerdisplayhandler.core.interfaces.INetworkServiceDiscoveryManager;
import com.example.customerdisplayhandler.core.interfaces.IPairDisplay;
import com.example.customerdisplayhandler.core.interfaces.ISocketsManager;
import com.example.customerdisplayhandler.core.interfaces.ITcpMessageListener;
import com.example.customerdisplayhandler.core.interfaces.ITcpMessageSender;
import com.example.customerdisplayhandler.core.interfaces.ITroubleshootDisplay;
import com.example.customerdisplayhandler.shared.OnPairingServerListener;
import com.example.customerdisplayhandler.shared.OnTroubleshootListener;
import com.example.customerdisplayhandler.core.network.MulticastManagerImpl;
import com.example.customerdisplayhandler.core.network.NetworkServiceDiscoveryManagerImpl;
import com.example.customerdisplayhandler.core.SocketsManagerImpl;
import com.example.customerdisplayhandler.core.ConnectedDisplaysRepositoryImpl;
import com.example.customerdisplayhandler.core.interfaces.IConnectedDisplaysRepository;
import com.example.customerdisplayhandler.core.network.TcpMessageListenerImpl;
import com.example.customerdisplayhandler.core.network.TcpMessageSenderImpl;
import com.example.customerdisplayhandler.model.CustomerDisplay;
import com.example.customerdisplayhandler.model.ServiceInfo;
import com.example.customerdisplayhandler.utils.IJsonUtil;
import com.example.customerdisplayhandler.core.interfaces.ITcpConnector;
import com.example.customerdisplayhandler.core.IClientInfoManagerImpl;
import com.example.customerdisplayhandler.core.network.TcpConnectorImpl;
import com.example.customerdisplayhandler.helpers.IPManager;
import com.example.customerdisplayhandler.helpers.IPManagerImpl;
import com.example.customerdisplayhandler.helpers.ISharedPrefManager;
import com.example.customerdisplayhandler.helpers.ISharedPrefManagerImpl;
import com.example.customerdisplayhandler.utils.JsonUtilImpl;

import java.io.IOException;
import java.net.Socket;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class CustomerDisplayManagerImpl implements ICustomerDisplayManager {
    private static volatile CustomerDisplayManagerImpl INSTANCE;
    private int serverPort;
    private IJsonUtil jsonUtil;
    private IPManager ipManager;
    private INetworkServiceDiscoveryManager networkServiceDiscoveryManager;
    private IMulticastManager multicastManager;
    private ISharedPrefManager sharedPrefManager;
    private IClientInfoManager clientInfoManager;
    private IConnectedDisplaysRepository connectedDisplaysRepository;
    private ISocketsManager socketsManager;
    private ITcpConnector tcpConnector;
    private IPairDisplay pairDisplay;
    private ITcpMessageListener tcpMessageListener;
    private ITcpMessageSender tcpMessageSender;
    private ITroubleshootDisplay troubleshootDisplay;
    private volatile CompositeDisposable pairingCompositeDisposable = new CompositeDisposable();
    private volatile CompositeDisposable compositeDisposable = new CompositeDisposable();

    public static synchronized CustomerDisplayManagerImpl newInstance(Context context, int serverPort) {
        if (INSTANCE == null) {
            INSTANCE = new CustomerDisplayManagerImpl(context, serverPort);
        }
        return INSTANCE;
    }

    private CustomerDisplayManagerImpl(Context context, int serverPort) {
        this.serverPort = serverPort;
        jsonUtil = new JsonUtilImpl();
        ipManager = new IPManagerImpl(context);
        networkServiceDiscoveryManager = new NetworkServiceDiscoveryManagerImpl(context);
        sharedPrefManager = ISharedPrefManagerImpl.getInstance(context);
        ipManager = new IPManagerImpl(context);
        multicastManager = new MulticastManagerImpl(NetworkConstants.MULTICAST_GROUP_ADDRESS, NetworkConstants.MULTICAST_PORT);
        clientInfoManager = new IClientInfoManagerImpl(ipManager, sharedPrefManager, jsonUtil);
        connectedDisplaysRepository = ConnectedDisplaysRepositoryImpl.getInstance(sharedPrefManager, jsonUtil);
        socketsManager = SocketsManagerImpl.getInstance();
        tcpConnector = new TcpConnectorImpl();
        tcpMessageListener = new TcpMessageListenerImpl();
        tcpMessageSender = new TcpMessageSenderImpl();
        pairDisplay = new PairDisplayImpl(socketsManager, clientInfoManager, jsonUtil, tcpMessageSender, tcpMessageListener, connectedDisplaysRepository);
        troubleshootDisplay = new TroubleshootDisplayImpl(tcpConnector, socketsManager, multicastManager, networkServiceDiscoveryManager, connectedDisplaysRepository);
    }

    @Override
    public void startSearchForCustomerDisplays(INetworkServiceDiscoveryManager.SearchListener searchListener) {
        // send multicast message to all devices to turn on their network service discovery
        sendMulticastMessage("all_devices." + NetworkConstants.TURN_ON_ALL_DEVICES_COMMAND);
        // start searching for customer displays
        networkServiceDiscoveryManager.startSearchForServices(searchListener, NetworkConstants.SERVICE_DISCOVERY_TIMEOUT);
    }

    @Override
    public void stopSearchForCustomerDisplays() {
        networkServiceDiscoveryManager.stopSearchForServices();
    }

    @Override
    public void startListeningForServerMessages(String serverId, Socket socket) {
        compositeDisposable.add(tcpMessageListener.startListening(serverId, socket)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> {
                    Log.d("CustomerDisplayManager", "Listening Stopped for server: " + serverId);
                }, throwable -> {
                    Log.e("CustomerDisplayManager", "Error receiving message: " + throwable.getMessage());
                }));

    }

    @Override
    public void startPairingCustomerDisplay(ServiceInfo serviceInfo, OnPairingServerListener listener) {
        pairingCompositeDisposable.add(
                reconnect(serviceInfo)
                        .doOnSuccess(p -> listener.onCustomerDisplayFound())
                        .flatMapCompletable(socketServiceInfoPair ->
                                pairDisplay.startDisplayPairing(socketServiceInfoPair.first, socketServiceInfoPair.second, listener)
                        )
                        .doOnSubscribe(disposable -> listener.onPairingServerStarted())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(() -> {
                            Log.d("CustomerDisplayManager", "Pairing completed with customer display: " + serviceInfo.getIpAddress());
                        }, throwable -> {
                            Log.e("CustomerDisplayManager", "Error pairing with customer display: " + throwable.getMessage());
                            listener.onPairingServerFailed(throwable.getMessage());
                        })
        );
    }

    @Override
    public void stopPairingServer() {
        pairingCompositeDisposable.clear();
    }

    @Override
    public void addConnectedDisplay(String customerDisplayId, String customerDisplayName, String customerDisplayIpAddress, AddCustomerDisplayListener listener) {
        CustomerDisplay customerDisplayNew = new CustomerDisplay(customerDisplayId, customerDisplayName, customerDisplayIpAddress, true);
        Disposable disposable = connectedDisplaysRepository.getCustomerDisplayById(customerDisplayId)
                .switchIfEmpty(Single.just(new CustomerDisplay(null, null, null, false)))
                .flatMapCompletable(customerDisplay -> {
                    if (customerDisplay == null || customerDisplay.getCustomerDisplayID() == null) {
                        return connectedDisplaysRepository.addCustomerDisplay(customerDisplayNew);
                    } else {
                        return connectedDisplaysRepository.updateCustomerDisplay(customerDisplayNew)
                                .ignoreElement();
                    }
                }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> {
                    listener.onCustomerDisplayAdded(customerDisplayNew);
                }, throwable -> {
                    Log.e("CustomerDisplayManager", "Error adding customer display: " + throwable.getMessage());
                    listener.onCustomerDisplayAddFailed("Error occurred while saving customer display");
                });
        pairingCompositeDisposable.add(disposable);
    }

    @Override
    public void removeConnectedDisplay(String customerDisplayId, RemoveCustomerDisplayListener listener) {
        Disposable disposable = connectedDisplaysRepository.removeCustomerDisplay(customerDisplayId).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(() -> {
            Log.d("CustomerDisplayManager", "Customer display removed: " + customerDisplayId);
            listener.onCustomerDisplayRemoved();
        }, throwable -> {
            Log.e("CustomerDisplayManager", "Error removing customer display: " + throwable.getMessage());
            listener.onCustomerDisplayRemoveFailed("Error occurred while removing customer display");
        });
        compositeDisposable.add(disposable);
    }

    @Override
    public void getConnectedDisplays(GetConnectedDisplaysListener listener) {
        Disposable disposable = connectedDisplaysRepository.getListOfConnectedDisplays().subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(listener::onConnectedDisplaysReceived, throwable -> {
            Log.e("CustomerDisplayManager", "Error getting connected displays: " + throwable.getMessage());
            listener.onConnectedDisplaysReceiveFailed("Error occurred while getting connected displays");
        });
        compositeDisposable.add(disposable);
    }

    @Override
    public synchronized void toggleCustomerDisplayActivation(String customerDisplayId, OnCustomerDisplayActivationToggleListener listener) {
        Disposable disposable = connectedDisplaysRepository
                .getCustomerDisplayById(customerDisplayId)
                .defaultIfEmpty(new CustomerDisplay(null, null, null, false))
                .flatMap(customerDisplay -> {
                    if (customerDisplay != null && customerDisplay.getCustomerDisplayID() != null) {
                        CustomerDisplay updatedCustomerDisplay = new CustomerDisplay(customerDisplay.getCustomerDisplayID(), customerDisplay.getCustomerDisplayName(), customerDisplay.getCustomerDisplayIpAddress(), !customerDisplay.getIsActivated());
                        return connectedDisplaysRepository.updateCustomerDisplay(updatedCustomerDisplay)
                                .map(CustomerDisplay::getIsActivated);
                    } else {
                        return Single.error(new IOException("Customer display not found"));
                    }
                }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((isActivated) -> {
                    Log.d("CustomerDisplayManager", "Customer display activation toggled: " + customerDisplayId);
                    if (isActivated) {
                        listener.onCustomerDisplayActivated();
                    } else {
                        listener.onCustomerDisplayDeactivated();
                    }
                }, throwable -> {
                    Log.e("CustomerDisplayManager", "Error toggling customer display activation: " + throwable.getMessage());
                    listener.onCustomerDisplayActivationToggleFailed("Operation failed, please try again");
                });
        compositeDisposable.add(disposable);
    }

    @Override
    public void startManualTroubleshooting(CustomerDisplay customerDisplay, OnTroubleshootListener listener) {

    }

    @Override
    public void sendUpdatesToCustomerDisplays(String data, OnSendUpdatesListener listener) {

    }

    @Override
    public void sendMulticastMessage(String message) {
        Disposable disposable = multicastManager.sendMessage(message).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(() -> {
            Log.d("CustomerDisplayManager", "Multicast message sent: " + message);
        }, throwable -> {
            Log.e("CustomerDisplayManager", "Error sending multicast message: " + throwable.getMessage());
        });
        compositeDisposable.add(disposable);
    }


    @Override
    public void disposeCustomerDisplayManager() {
        compositeDisposable.clear();
    }

    private Single<Pair<Socket, ServiceInfo>> reconnectIfDisconnected(ServiceInfo serviceInfo) {
        Socket socket = socketsManager.findSocketIfConnected(serviceInfo.getServerId());
        if (socket == null) {
            return tcpConnector.connectToServer(serviceInfo.getIpAddress(), serverPort)
                    .doOnSuccess(newSocket -> socketsManager.addConnectedSocket(newSocket, serviceInfo))
                    .map(updatedSocket -> new Pair<>(updatedSocket, serviceInfo));
        } else {
            return Single.just(new Pair<>(socket, serviceInfo));
        }
    }

    private Single<Pair<Socket, ServiceInfo>> reconnect(ServiceInfo serviceInfo) {
        Socket socket = socketsManager.findSocketIfConnected(serviceInfo.getServerId());
        if (socket != null) {
            socketsManager.removeConnectedSocket(serviceInfo.getServerId());
            return tcpConnector.disconnectSafelyFromServer(socket)
                    .andThen(tcpConnector.connectToServer(serviceInfo.getIpAddress(), serverPort))
                    .doOnSuccess(newSocket -> socketsManager.addConnectedSocket(newSocket, serviceInfo))
                    .map(updatedSocket -> new Pair<>(updatedSocket, serviceInfo));
        }
        return tcpConnector.connectToServer(serviceInfo.getIpAddress(), serverPort)
                .doOnSuccess(newSocket -> socketsManager.addConnectedSocket(newSocket, serviceInfo))
                .map(updatedSocket -> new Pair<>(updatedSocket, serviceInfo));
    }
}
