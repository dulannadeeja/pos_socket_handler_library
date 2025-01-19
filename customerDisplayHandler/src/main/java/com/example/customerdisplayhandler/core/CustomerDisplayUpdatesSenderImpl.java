package com.example.customerdisplayhandler.core;

import android.util.Log;
import android.util.Pair;

import com.example.customerdisplayhandler.constants.NetworkConstants;
import com.example.customerdisplayhandler.core.interfaces.IClientInfoManager;
import com.example.customerdisplayhandler.core.interfaces.IConnectedDisplaysRepository;
import com.example.customerdisplayhandler.core.interfaces.ICustomerDisplayUpdatesSender;
import com.example.customerdisplayhandler.core.interfaces.ISocketsManager;
import com.example.customerdisplayhandler.core.interfaces.ITcpConnector;
import com.example.customerdisplayhandler.core.interfaces.ITcpMessageSender;
import com.example.customerdisplayhandler.core.interfaces.ITroubleshootDisplay;
import com.example.customerdisplayhandler.model.ClientInfo;
import com.example.customerdisplayhandler.model.CustomerDisplay;
import com.example.customerdisplayhandler.model.DisplayUpdates;
import com.example.customerdisplayhandler.model.ServiceInfo;
import com.example.customerdisplayhandler.model.SocketMessageBase;
import com.example.customerdisplayhandler.utils.IJsonUtil;

import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;

public class CustomerDisplayUpdatesSenderImpl implements ICustomerDisplayUpdatesSender {
    private static final String TAG = "CustomerDisplayUpdatesSender";

    private final ITroubleshootDisplay troubleshootDisplay;
    private final ISocketsManager socketsManager;
    private final int serverPort;
    private final IConnectedDisplaysRepository connectedDisplaysRepository;
    private final ITcpMessageSender tcpMessageSender;
    private final ITcpConnector tcpConnector;
    private final IClientInfoManager clientInfoManager;
    private final IJsonUtil jsonUtil;

    public CustomerDisplayUpdatesSenderImpl(
            ITroubleshootDisplay troubleshootDisplay,
            ISocketsManager socketsManager,
            int serverPort,
            IConnectedDisplaysRepository connectedDisplaysRepository,
            ITcpMessageSender tcpMessageSender,
            ITcpConnector tcpConnector,
            IClientInfoManager clientInfoManager,
            IJsonUtil jsonUtil
    ) {
        this.troubleshootDisplay = troubleshootDisplay;
        this.socketsManager = socketsManager;
        this.serverPort = serverPort;
        this.connectedDisplaysRepository = connectedDisplaysRepository;
        this.tcpMessageSender = tcpMessageSender;
        this.tcpConnector = tcpConnector;
        this.clientInfoManager = clientInfoManager;
        this.jsonUtil = jsonUtil;
    }

    @Override
    public Completable sendThemeUpdateToCustomerDisplay(CustomerDisplay updatedCustomerDisplay) {
        return Single.just(updatedCustomerDisplay)
                .flatMapCompletable(customerDisplay -> sendUpdateToDisplay(customerDisplay, customerDisplay.getIsDarkModeActivated(), NetworkConstants.UPDATE_THEME_COMMAND)
                        .doOnComplete(() -> Log.i(TAG, "Successfully sent theme update to display: " + customerDisplay.getCustomerDisplayName()))
                        .onErrorComplete(error -> {
                            Log.e(TAG, "Failed to send theme update to display: " + customerDisplay.getCustomerDisplayID(), error);
                            return true;
                        }));
    }

    public Single<List<Pair<CustomerDisplay, Boolean>>> sendUpdatesToCustomerDisplays(DisplayUpdates displayUpdates) {
        List<Pair<CustomerDisplay, Boolean>> customerDisplaysWithResults = new ArrayList<>();

        return getActivatedCustomerDisplays()
                .flatMapCompletable(customerDisplays -> {
                    if (customerDisplays.isEmpty()) {
                        Log.i(TAG, "No activated customer displays found.");
                        return Completable.complete();
                    }
                    Log.i(TAG, "Found " + customerDisplays.size() + " activated customer displays.");
                    return sendUpdatesToDisplays(customerDisplays, displayUpdates, customerDisplaysWithResults, NetworkConstants.UPDATE_DISPLAY_COMMAND);
                })
                .andThen(Single.just(customerDisplaysWithResults));
    }

    private Completable sendUpdatesToDisplays(List<CustomerDisplay> displays, DisplayUpdates displayUpdates, List<Pair<CustomerDisplay, Boolean>> results,String command) {
        return Observable.fromIterable(displays)
                .flatMapCompletable(display -> sendUpdateToDisplay(display, displayUpdates, command)
                        .doOnComplete(() -> {
                            Log.i(TAG, "Successfully sent updates to display: " + display.getCustomerDisplayName());
                            results.add(new Pair<>(display, true));
                        })
                        .onErrorResumeWith(handleFailedMessage(display, displayUpdates, results,command))
                )
                .onErrorComplete();
    }

    private Completable handleFailedMessage(CustomerDisplay display, DisplayUpdates displayUpdates, List<Pair<CustomerDisplay, Boolean>> results, String command) {
        return resendFailedMessage(display, displayUpdates,command)
                .doOnComplete(() -> {
                    Log.i(TAG, "Successfully resent updates to display: " + display.getCustomerDisplayID());
                    results.add(new Pair<>(display, true));
                })
                .doOnError(error -> {
                    Log.e(TAG, "Failed to send updates to display: " + display.getCustomerDisplayID(), error);
                    results.add(new Pair<>(display, false));
                });
    }

    private Completable resendFailedMessage(CustomerDisplay display, DisplayUpdates displayUpdates, String command) {
        Log.i(TAG, "Resending updates to failed display: " + display.getCustomerDisplayName());
        return troubleshootDisplay.startSilentTroubleshooting(display)
                .andThen(sendUpdateToDisplay(display, displayUpdates,command));
    }

    private Single<List<CustomerDisplay>> getActivatedCustomerDisplays() {
        return connectedDisplaysRepository.getListOfConnectedDisplays()
                .flatMap(displays -> {
                    if (displays.isEmpty()) {
                        Log.i(TAG, "No connected displays found.");
                        return Single.just(new ArrayList<>());
                    }
                    List<CustomerDisplay> activatedDisplays = displays.stream()
                            .filter(CustomerDisplay::getIsActivated)
                            .collect(Collectors.toList());
                    Log.i(TAG, "Activated displays count: " + activatedDisplays.size());
                    return Single.just(activatedDisplays);
                });
    }

    private Completable sendUpdateToDisplay(CustomerDisplay display, Object displayUpdates, String command) {
        return Single.just(display)
                .flatMap(d -> clientInfoManager.getClientInfo().map(clientInfo -> new Pair<>(d, clientInfo)))
                .flatMapCompletable(pair -> {
                    Log.i(TAG, "Sending updates to display: " + pair.first.getCustomerDisplayName());
                    Socket socket = socketsManager.findSocketIfConnected(pair.first.getCustomerDisplayID());

                    // Prepare the message to be sent, according to the protocol
                    SocketMessageBase socketMessageBase = new SocketMessageBase(displayUpdates, command, pair.first.getCustomerDisplayID(), pair.second.getClientID());
                    String jsonMessage = jsonUtil.toJson(socketMessageBase);

                    if (socket != null) {
                        Log.i(TAG, "Socket found for display: " + pair.first.getCustomerDisplayName());
                        return tcpMessageSender.sendMessageToServer(pair.first.getCustomerDisplayID(), socket, jsonMessage);
                    } else {
                        Log.i(TAG, "No socket found. Connecting to server for display: " + pair.first.getCustomerDisplayID());
                        return establishNewConnection(pair.first)
                                .flatMapCompletable(newSocket -> tcpMessageSender.sendMessageToServer(pair.first.getCustomerDisplayID(), newSocket, jsonMessage));
                    }
                });
    }

    private Single<Socket> establishNewConnection(CustomerDisplay display) {
        return tcpConnector.tryToConnectWithingTimeout(display.getCustomerDisplayIpAddress(), serverPort, 2000)
                .doOnSuccess(socket -> {
                    Log.i(TAG, "Successfully connected to server for display: " + display.getCustomerDisplayName());
                    socketsManager.addConnectedSocket(socket, new ServiceInfo(
                            display.getCustomerDisplayID(),
                            display.getCustomerDisplayName(),
                            display.getCustomerDisplayIpAddress(),
                            null
                    ));
                });
    }
}
