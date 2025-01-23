package com.example.customerdisplayhandler.api;

import android.content.Context;
import android.util.Log;
import android.util.Pair;

import com.example.customerdisplayhandler.constants.NetworkConstants;
import com.example.customerdisplayhandler.core.CustomerDisplayUpdatesSenderImpl;
import com.example.customerdisplayhandler.core.PairDisplayImpl;
import com.example.customerdisplayhandler.core.TroubleshootDisplayImpl;
import com.example.customerdisplayhandler.core.interfaces.IClientInfoManager;
import com.example.customerdisplayhandler.core.interfaces.ICustomerDisplayUpdatesSender;
import com.example.customerdisplayhandler.core.interfaces.IMulticastManager;
import com.example.customerdisplayhandler.core.interfaces.INetworkServiceDiscoveryManager;
import com.example.customerdisplayhandler.core.interfaces.IPairDisplay;
import com.example.customerdisplayhandler.core.interfaces.ISocketsManager;
import com.example.customerdisplayhandler.core.interfaces.ITcpMessageListener;
import com.example.customerdisplayhandler.core.interfaces.ITcpMessageSender;
import com.example.customerdisplayhandler.core.interfaces.ITroubleshootDisplay;
import com.example.customerdisplayhandler.helpers.ISocketMessageProcessHelper;
import com.example.customerdisplayhandler.helpers.SocketMessageProcessHelperImpl;
import com.example.customerdisplayhandler.model.DisplayUpdates;
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
import java.util.UUID;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class CustomerDisplayManagerImpl implements ICustomerDisplayManager {
    private static final String TAG = CustomerDisplayManagerImpl.class.getSimpleName();
    private static volatile CustomerDisplayManagerImpl INSTANCE;
    private int serverPort;
    private IJsonUtil jsonUtil;
    private IPManager ipManager;
    private INetworkServiceDiscoveryManager networkServiceDiscoveryManager;
    private ISocketMessageProcessHelper socketMessageProcessHelper;
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
    private ICustomerDisplayUpdatesSender customerDisplayUpdatesSender;
    private volatile CompositeDisposable pairingCompositeDisposable = new CompositeDisposable();
    private volatile CompositeDisposable troubleshootingCompositeDisposable = new CompositeDisposable();
    private volatile CompositeDisposable sendUpdatesCompositeDisposable = new CompositeDisposable();
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
        socketMessageProcessHelper = new SocketMessageProcessHelperImpl(jsonUtil);
        multicastManager = new MulticastManagerImpl(NetworkConstants.MULTICAST_GROUP_ADDRESS, NetworkConstants.MULTICAST_PORT);
        clientInfoManager = new IClientInfoManagerImpl(ipManager, sharedPrefManager, jsonUtil);
        connectedDisplaysRepository = ConnectedDisplaysRepositoryImpl.getInstance(sharedPrefManager, jsonUtil);
        tcpMessageListener = new TcpMessageListenerImpl();
        tcpMessageSender = new TcpMessageSenderImpl(tcpMessageListener,socketMessageProcessHelper);
        socketsManager = SocketsManagerImpl.getInstance(new TcpConnectorImpl(), tcpMessageListener);
        pairDisplay = new PairDisplayImpl(socketsManager, clientInfoManager, jsonUtil, tcpMessageSender, tcpMessageListener, connectedDisplaysRepository, customerDisplayUpdatesSender, socketMessageProcessHelper);
        troubleshootDisplay = new TroubleshootDisplayImpl(tcpConnector, socketsManager, multicastManager, networkServiceDiscoveryManager, connectedDisplaysRepository);
        customerDisplayUpdatesSender = new CustomerDisplayUpdatesSenderImpl(troubleshootDisplay, socketsManager, connectedDisplaysRepository,tcpMessageSender,clientInfoManager,jsonUtil,tcpMessageListener,socketMessageProcessHelper);

        observeNewServerConnections();
    }

    private void observeNewServerConnections() {
        compositeDisposable.add(
                socketsManager.getSocketConnectionSubject()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(pair -> {
                            startListeningForServerMessages(pair.first, pair.second);
                        }, throwable -> {
                            Log.e(TAG, "Error observing new server connections: ", throwable);
                        })
        );
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
        compositeDisposable.add(
                tcpMessageListener.startListening(serverId, socket)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(() -> {
                            Log.d("CustomerDisplayManager", "Listening Stopped for server: " + serverId);
                        }, throwable -> {
                            Log.e("CustomerDisplayManager", "Error receiving message: ", throwable);
                        })
        );

    }

    @Override
    public void startPairingCustomerDisplay(ServiceInfo serviceInfo, Boolean isDarkMode, OnPairingServerListener listener) {
        // when pairing manually, serviceInfo won't have a serverId, so we set it to the ipAddress
        // after successful pairing, the serverId will be updated to the actual serverId
        if(serviceInfo.getServerId() == null){
            serviceInfo.setServerId(serviceInfo.getIpAddress());
        }

        pairingCompositeDisposable.add(
                socketsManager.reconnect(serviceInfo.getServerId(), serviceInfo.getIpAddress())
                        .doOnSuccess(p -> listener.onCustomerDisplayFound())
                        .flatMapCompletable(socket ->
                                pairDisplay.startDisplayPairing(socket, serviceInfo, isDarkMode, listener)
                        )
                        .doOnSubscribe(disposable -> listener.onPairingServerStarted())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(() -> {
                            Log.d("CustomerDisplayManager", "Pairing completed with customer display: " + serviceInfo.getIpAddress());
                        }, throwable -> {
                            Log.e("CustomerDisplayManager", "Error pairing with customer display: ", throwable);
                            listener.onPairingServerFailed(throwable.getMessage());
                        })
        );
    }

    @Override
    public void stopPairingServer() {
        pairingCompositeDisposable.clear();
    }

    @Override
    public void removeConnectedDisplay(String customerDisplayId, RemoveCustomerDisplayListener listener) {
        Disposable disposable = connectedDisplaysRepository.removeCustomerDisplay(customerDisplayId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe(() -> {
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
        Disposable disposable = connectedDisplaysRepository.getListOfConnectedDisplays()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe(listener::onConnectedDisplaysReceived, throwable -> {
                    Log.e("CustomerDisplayManager", "Error getting connected displays: " + throwable.getMessage());
                    listener.onConnectedDisplaysReceiveFailed("Error occurred while getting connected displays");
                });
        compositeDisposable.add(disposable);
    }

    @Override
    public synchronized void toggleCustomerDisplayActivation(String customerDisplayId, OnCustomerDisplayActivationToggleListener listener) {
        Disposable disposable = connectedDisplaysRepository
                .getCustomerDisplayById(customerDisplayId)
                .defaultIfEmpty(new CustomerDisplay(null, null, null, false, false))
                .flatMap(customerDisplay -> {
                    if (customerDisplay != null && customerDisplay.getCustomerDisplayID() != null) {
                        CustomerDisplay updatedCustomerDisplay = new CustomerDisplay(
                                customerDisplay.getCustomerDisplayID(),
                                customerDisplay.getCustomerDisplayName(),
                                customerDisplay.getCustomerDisplayIpAddress(),
                                !customerDisplay.getIsActivated(),
                                customerDisplay.getIsDarkModeActivated()
                        );
                        return connectedDisplaysRepository.updateCustomerDisplay(updatedCustomerDisplay)
                                .map(CustomerDisplay::getIsActivated);
                    } else {
                        return Single.error(new IOException("Customer display not found"));
                    }
                })
                .subscribeOn(Schedulers.io())
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
        troubleshootingCompositeDisposable.add(
                troubleshootDisplay.startManualTroubleshooting(customerDisplay, listener)
                        .doOnSubscribe(disposable -> Log.i(TAG, customerDisplay.getCustomerDisplayName() + " troubleshooting started"))
                        .doOnDispose(() -> Log.i(TAG, customerDisplay.getCustomerDisplayName() + " troubleshooting subscription disposed"))
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(() -> {
                            Log.i(TAG, customerDisplay.getCustomerDisplayName() + " troubleshooting completed");
                        }, throwable -> {
                            Log.e(TAG, customerDisplay.getCustomerDisplayName() + " troubleshooting failed: ", throwable);
                            listener.onTroubleshootFailed(throwable.getMessage());
                        })
        );
    }

    @Override
    public void stopManualTroubleshooting() {
        troubleshootingCompositeDisposable.clear();
        networkServiceDiscoveryManager.stopSearchForServices();
    }

    @Override
    public void updateCustomerDisplay(CustomerDisplay updatedCustomerDisplay, OnUpdateDisplayListener listener) {
        sendUpdatesCompositeDisposable.add(
                connectedDisplaysRepository.getCustomerDisplayById(updatedCustomerDisplay.getCustomerDisplayID())
                        .switchIfEmpty(Single.error(new IOException("Customer display not found")))
                        .flatMapCompletable(display -> {
                            boolean isDarkModeChanged = display.getIsDarkModeActivated() != updatedCustomerDisplay.getIsDarkModeActivated();
                            if (isDarkModeChanged) {
                                return customerDisplayUpdatesSender.sendThemeUpdateToCustomerDisplay(updatedCustomerDisplay)
                                        .doOnError(throwable -> {
                                            updatedCustomerDisplay.setIsDarkModeActivated(!updatedCustomerDisplay.getIsDarkModeActivated());
                                        })
                                        .andThen(connectedDisplaysRepository.updateCustomerDisplay(updatedCustomerDisplay).ignoreElement());
                            } else {
                                return connectedDisplaysRepository.updateCustomerDisplay(updatedCustomerDisplay).ignoreElement();
                            }
                        })
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                listener::onDisplayUpdated,
                                throwable -> {
                                    Log.e(TAG, "Error sending theme update to customer display: " + throwable.getMessage());
                                    listener.onUpdateDisplayFailed(updatedCustomerDisplay.getCustomerDisplayName() + " error occurred while updating.");
                                }
                        )
        );
    }

    @Override
    public void stopSendingUpdatesToCustomerDisplays() {
        sendUpdatesCompositeDisposable.clear();
    }

    @Override
    public void sendUpdatesToCustomerDisplays(DisplayUpdates displayUpdates, OnSendUpdatesListener listener) {
        sendUpdatesCompositeDisposable.add(
                customerDisplayUpdatesSender.sendUpdatesToCustomerDisplays(displayUpdates, UUID.randomUUID().toString())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                customerDisplaysWithResults -> {
                                    Log.i(TAG, "Updates sent to customer displays: " + jsonUtil.toJson(customerDisplaysWithResults));
                                    if (customerDisplaysWithResults.isEmpty()) {
                                        listener.onSystemError("No customer displays found");
                                    } else {
                                        boolean allUpdatesSent = customerDisplaysWithResults.stream().allMatch(Pair -> Pair.second);
                                        if (allUpdatesSent) {
                                            listener.onAllUpdatesSentWithSuccess();
                                        } else {
                                            listener.onSomeUpdatesFailed(customerDisplaysWithResults);
                                        }
                                    }
                                },
                                throwable -> {
                                    Log.e(TAG, "Error sending updates to customer displays: " + throwable.getMessage());
                                    listener.onSystemError(throwable.getMessage());
                                }
                        )
        );
    }

    @Override
    public void sendMulticastMessage(String message) {
        Disposable disposable = multicastManager.sendMessage(message)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> {
                    Log.d("CustomerDisplayManager", "Multicast message sent: " + message);
                }, throwable -> {
                    Log.e("CustomerDisplayManager", "Error sending multicast message: " + throwable.getMessage());
                });
        compositeDisposable.add(disposable);
    }


    @Override
    public void disposeCustomerDisplayManager() {
        compositeDisposable.clear();
        pairingCompositeDisposable.clear();
        troubleshootingCompositeDisposable.clear();
        sendUpdatesCompositeDisposable.clear();
    }
}
