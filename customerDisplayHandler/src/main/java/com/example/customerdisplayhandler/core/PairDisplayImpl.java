package com.example.customerdisplayhandler.core;

import android.graphics.drawable.Icon;
import android.util.Log;

import com.example.customerdisplayhandler.api.ICustomerDisplayManager;
import com.example.customerdisplayhandler.constants.NetworkConstants;
import com.example.customerdisplayhandler.core.interfaces.IClientInfoManager;
import com.example.customerdisplayhandler.core.interfaces.IConnectedDisplaysRepository;
import com.example.customerdisplayhandler.core.interfaces.ITcpMessageListener;
import com.example.customerdisplayhandler.core.interfaces.ITcpMessageSender;
import com.example.customerdisplayhandler.model.CustomerDisplay;
import com.example.customerdisplayhandler.shared.OnPairingServerListener;
import com.example.customerdisplayhandler.core.interfaces.IPairDisplay;
import com.example.customerdisplayhandler.core.interfaces.ISocketsManager;
import com.example.customerdisplayhandler.model.ClientInfo;
import com.example.customerdisplayhandler.model.ConnectionApproval;
import com.example.customerdisplayhandler.model.ServiceInfo;
import com.example.customerdisplayhandler.model.SocketMessageBase;
import com.example.customerdisplayhandler.utils.IJsonUtil;
import java.net.Socket;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class PairDisplayImpl implements IPairDisplay {

    private static final String TAG = PairDisplayImpl.class.getSimpleName();
    private ISocketsManager socketsManager;
    private IClientInfoManager clientInfoManager;
    private IJsonUtil jsonUtil;
    private ITcpMessageSender tcpMessageSender;
    private ITcpMessageListener tcpMessageListener;
    private IConnectedDisplaysRepository connectedDisplaysRepository;

    public PairDisplayImpl(ISocketsManager socketsManager,
                           IClientInfoManager clientInfoManager,
                           IJsonUtil jsonUtil,
                           ITcpMessageSender tcpMessageSender,
                           ITcpMessageListener tcpMessageListener,
                           IConnectedDisplaysRepository connectedDisplaysRepository
    ) {
        this.socketsManager = socketsManager;
        this.clientInfoManager = clientInfoManager;
        this.jsonUtil = jsonUtil;
        this.tcpMessageSender = tcpMessageSender;
        this.tcpMessageListener = tcpMessageListener;
        this.connectedDisplaysRepository = connectedDisplaysRepository;
    }

    public Completable startDisplayPairing(Socket connectedSocket, ServiceInfo serviceInfo, OnPairingServerListener listener) {
        Log.d(TAG, "Connecting to server: " + serviceInfo.getIpAddress());
        return Completable.mergeArray(
                tcpMessageListener.startListening(serviceInfo.getServerId(), connectedSocket),
                clientInfoManager.getClientInfo()
                .flatMapCompletable(clientInfo -> getConnectionApprovalStatus(serviceInfo, connectedSocket, clientInfo, listener)
                        .flatMapCompletable(connectionApproval ->{
                            if (connectionApproval != null && connectionApproval.isConnectionApproved() != null && connectionApproval.isConnectionApproved()) {
                                listener.onConnectionRequestApproved(serviceInfo);
                                CustomerDisplay customerDisplay = new CustomerDisplay(connectionApproval.getServerID(),serviceInfo.getDeviceName(),connectionApproval.getServerIpAddress(),true);
                                return connectedDisplaysRepository.getCustomerDisplayById(connectionApproval.getServerID())
                                        .defaultIfEmpty(new CustomerDisplay(null,null,null,false))
                                        .flatMapCompletable(existingCustomerDisplay -> {
                                            if (existingCustomerDisplay != null && existingCustomerDisplay.getCustomerDisplayID() != null) {
                                                return connectedDisplaysRepository.updateCustomerDisplay(customerDisplay)
                                                        .ignoreElement();
                                            } else {
                                                return connectedDisplaysRepository.addCustomerDisplay(customerDisplay);
                                            }
                                        })
                                        .doOnComplete(() -> listener.onSavedEstablishedConnection(serviceInfo));
                            }else{
                                listener.onConnectionRequestRejected();
                                return Completable.complete();
                            }
                        })))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    private Single<ConnectionApproval> getConnectionApprovalStatus(ServiceInfo serviceInfo, Socket socket, ClientInfo clientInfo, OnPairingServerListener listener) {
        Log.i(TAG, "Starting pairing process with customer display.");

        // Send connection approval request and listen for server response
        return tcpMessageSender
                .sendMessageToServer(serviceInfo.getServerId(), socket, createConnectionApprovalMessage(serviceInfo, clientInfo))
                .doOnSubscribe(disposable -> listener.onConnectionRequestSent())
                .andThen(tcpMessageListener.getServerMessageSubject().firstOrError())
                .map(serverMessage -> {
                    Log.i(TAG, "Message received from server: " + serverMessage.second);
                    return processConnectionApproval(serverMessage.second);
                })
                .doOnError(e -> Log.e(TAG, "Error during pairing process: " + e.getMessage(), e))
                .subscribeOn(Schedulers.io());
    }

    private String createConnectionApprovalMessage(ServiceInfo serviceInfo, ClientInfo clientInfo) {
        SocketMessageBase socketMessageBase = new SocketMessageBase(
                clientInfo,
                NetworkConstants.REQUEST_CONNECTION_APPROVAL,
                serviceInfo.getServerId(),
                clientInfo.getClientID()
        );
        return jsonUtil.toJson(socketMessageBase);
    }

    private ConnectionApproval processConnectionApproval(String rawMessage) {
        try {
            if (rawMessage == null || rawMessage.isEmpty()) {
                Log.e(TAG, "Invalid connection approval message received: " + rawMessage);
                return createConnectionApproval();
            }

            String message = rawMessage.trim();
            if (rawMessage.startsWith(";")) {
                return createConnectionApproval();
            }

            SocketMessageBase socketMessageBase = jsonUtil.toObj(message, SocketMessageBase.class);

            if (!isValidSocketMessage(socketMessageBase)) {
                Log.e(TAG, "Invalid connection approval message structure: " + message);
                return createConnectionApproval();
            }

            if (!NetworkConstants.RESPONSE_CONNECTION_APPROVAL.equals(socketMessageBase.getCommand())) {
                Log.e(TAG, "Unexpected command received: " + message);
                return createConnectionApproval();
            }

            ConnectionApproval connectionApproval = parseConnectionApproval(socketMessageBase.getData());
            if (connectionApproval != null && connectionApproval.isConnectionApproved() != null) {
                return connectionApproval;
            }

            Log.e(TAG, "Connection approval data is null or invalid.");
            return createConnectionApproval();

        } catch (Exception e) {
            Log.e(TAG, "Error processing connection approval: " + e.getMessage(), e);
            return createConnectionApproval();
        }
    }

    private ConnectionApproval createConnectionApproval() {
        return new ConnectionApproval(null, null, null, null);
    }

    private ConnectionApproval parseConnectionApproval(Object data) {
        try {
            String jsonData = jsonUtil.toJson(data);
            return jsonUtil.toObj(jsonData, ConnectionApproval.class);
        } catch (Exception e) {
            Log.e(TAG, "Error parsing connection approval data: " + e.getMessage(), e);
            return null;
        }
    }

    private boolean isValidSocketMessage(SocketMessageBase socketMessageBase) {
        return socketMessageBase != null &&
                socketMessageBase.getCommand() != null &&
                !socketMessageBase.getCommand().isEmpty() &&
                socketMessageBase.getData() != null &&
                socketMessageBase.getSenderId() != null &&
                socketMessageBase.getReceiverId() != null;
    }
}

