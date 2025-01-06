package com.example.customerdisplayhandler.api;

import android.content.Context;
import android.util.Log;
import android.util.Pair;

import com.example.customerdisplayhandler.constants.NetworkConstants;
import com.example.customerdisplayhandler.core.interfaces.ClientInfoManager;
import com.example.customerdisplayhandler.core.interfaces.IConnectedServerManager;
import com.example.customerdisplayhandler.core.interfaces.IMulticastManager;
import com.example.customerdisplayhandler.core.interfaces.INetworkServiceDiscoveryManager;
import com.example.customerdisplayhandler.core.network.MulticastManagerImpl;
import com.example.customerdisplayhandler.core.network.NetworkServiceDiscoveryManagerImpl;
import com.example.customerdisplayhandler.helpers.ConnectedDisplaysRepositoryImpl;
import com.example.customerdisplayhandler.helpers.IConnectedDisplaysRepository;
import com.example.customerdisplayhandler.model.CustomerDisplay;
import com.example.customerdisplayhandler.model.ServiceInfo;
import com.example.customerdisplayhandler.utils.IJsonUtil;
import com.example.customerdisplayhandler.core.interfaces.ITcpConnectionManager;
import com.example.customerdisplayhandler.core.network.ClientInfoManagerImpl;
import com.example.customerdisplayhandler.core.network.ConnectedServerManagerImpl;
import com.example.customerdisplayhandler.core.network.TcpConnectionManagerImpl;
import com.example.customerdisplayhandler.helpers.IPManager;
import com.example.customerdisplayhandler.helpers.IPManagerImpl;
import com.example.customerdisplayhandler.helpers.ISharedPrefManager;
import com.example.customerdisplayhandler.helpers.ISharedPrefManagerImpl;
import com.example.customerdisplayhandler.utils.JsonUtilImpl;

import java.net.Socket;
import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class CustomerDisplayManagerImpl implements ICustomerDisplayManager {
    private static CustomerDisplayManagerImpl INSTANCE;
    private Context context;
    private int serverPort;
    private IJsonUtil jsonUtil;
    private IPManager ipManager;
    private ITcpConnectionManager socketConnectionManager;
    private INetworkServiceDiscoveryManager networkServiceDiscoveryManager;
    private IConnectedServerManager connectedServerManager;
    private IMulticastManager multicastManager;
    private ISharedPrefManager sharedPrefManager;
    private ClientInfoManager clientInfoManager;
    private IConnectedDisplaysRepository connectedDisplaysManager;
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
        socketConnectionManager = new TcpConnectionManagerImpl();
        connectedServerManager = new ConnectedServerManagerImpl(jsonUtil);
        networkServiceDiscoveryManager = new NetworkServiceDiscoveryManagerImpl(context);
        sharedPrefManager = ISharedPrefManagerImpl.getInstance(context);
        ipManager = new IPManagerImpl(context);
        multicastManager = new MulticastManagerImpl(NetworkConstants.MULTICAST_GROUP_ADDRESS, NetworkConstants.MULTICAST_PORT);
        clientInfoManager = new ClientInfoManagerImpl(ipManager, sharedPrefManager, jsonUtil);
        connectedDisplaysManager = ConnectedDisplaysRepositoryImpl.getInstance(sharedPrefManager, jsonUtil);
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
    public Completable startListeningForServerMessages(String serverId, Socket socket) {
        return connectedServerManager.startListening(serverId, socket);
    }

    @Override
    public void startPairingServer(ServiceInfo serviceInfo, IConnectedServerManager.OnPairingServerListener listener) {
        compositeDisposable.add(
                socketConnectionManager.connectToServer(serviceInfo.getIpAddress(), serverPort)
                        .flatMap(socket -> clientInfoManager.getClientInfo().map(clientInfo -> new Pair<>(socket, clientInfo)))
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe((pair) -> {
                            connectedServerManager.startPairingServer(serviceInfo, pair.first, pair.second, listener);
                        }, throwable -> {
                            Log.e("CustomerDisplayManager", "Error getting client info: " + throwable.getMessage());
                            listener.onPairingServerFailed(throwable.getMessage());
                        })
        );
    }

    @Override
    public void stopPairingServer() {
        connectedServerManager.stopPairingServer();
    }

    @Override
    public void addConnectedDisplay(String customerDisplayId, String customerDisplayName, String customerDisplayIpAddress, AddCustomerDisplayListener listener) {
        CustomerDisplay customerDisplayNew = new CustomerDisplay(customerDisplayId, customerDisplayName, customerDisplayIpAddress);
        Disposable disposable = connectedDisplaysManager.getCustomerDisplayById(customerDisplayId)
                .doOnComplete(() -> Log.d("CustomerDisplayManager", "Customer display not found: " + customerDisplayId))
                .switchIfEmpty(Single.just(new CustomerDisplay(null, null, null)))
                .flatMapCompletable(customerDisplay -> {
                    if (customerDisplay == null || customerDisplay.getCustomerDisplayID() == null) {
                        return connectedDisplaysManager.addCustomerDisplay(customerDisplayNew)
                                .doOnComplete(() -> Log.d("CustomerDisplayManager", "Customer display added: " + customerDisplayNew.getCustomerDisplayID()));
                    } else {
                        return connectedDisplaysManager.updateCustomerDisplay(customerDisplayNew)
                                .doOnSuccess(updatedCustomerDisplay -> Log.d("CustomerDisplayManager", "Customer display updated: " + updatedCustomerDisplay.getCustomerDisplayID()))
                                .ignoreElement();
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> {
                    listener.onCustomerDisplayAdded(customerDisplayNew);
                }, throwable -> {
                    Log.e("CustomerDisplayManager", "Error adding customer display: " + throwable.getMessage());
                    listener.onCustomerDisplayAddFailed("Error occurred while saving customer display");
                });
        compositeDisposable.add(disposable);
    }

    @Override
    public void removeConnectedDisplay(String customerDisplayId, RemoveCustomerDisplayListener listener) {
        Disposable disposable = connectedDisplaysManager.removeCustomerDisplay(customerDisplayId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> {
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
        Disposable disposable = connectedDisplaysManager.getListOfConnectedDisplays()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(listener::onConnectedDisplaysReceived, throwable -> {
                    Log.e("CustomerDisplayManager", "Error getting connected displays: " + throwable.getMessage());
                    listener.onConnectedDisplaysReceiveFailed("Error occurred while getting connected displays");
                });
        compositeDisposable.add(disposable);
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
    }
}
