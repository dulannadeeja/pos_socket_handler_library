package com.example.customerdisplayhandler.core;

import android.util.Log;
import android.util.Pair;

import com.example.customerdisplayhandler.constants.NetworkConstants;
import com.example.customerdisplayhandler.core.interfaces.IClientInfoManager;
import com.example.customerdisplayhandler.core.interfaces.IConnectedDisplaysRepository;
import com.example.customerdisplayhandler.core.interfaces.ICustomerDisplayUpdatesSender;
import com.example.customerdisplayhandler.core.interfaces.ITcpMessageListener;
import com.example.customerdisplayhandler.core.interfaces.ITcpMessageSender;
import com.example.customerdisplayhandler.helpers.ISocketMessageProcessHelper;
import com.example.customerdisplayhandler.model.ConnectionReq;
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
import java.util.UUID;
import java.util.concurrent.TimeUnit;

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
    private ICustomerDisplayUpdatesSender customerDisplayUpdatesSender;
    private ISocketMessageProcessHelper socketMessageProcessHelper;

    public PairDisplayImpl(ISocketsManager socketsManager,
                           IClientInfoManager clientInfoManager,
                           IJsonUtil jsonUtil,
                           ITcpMessageSender tcpMessageSender,
                           ITcpMessageListener tcpMessageListener,
                           IConnectedDisplaysRepository connectedDisplaysRepository,
                           ICustomerDisplayUpdatesSender customerDisplayUpdatesSender,
                           ISocketMessageProcessHelper socketMessageProcessHelper
    ) {
        this.socketsManager = socketsManager;
        this.clientInfoManager = clientInfoManager;
        this.jsonUtil = jsonUtil;
        this.tcpMessageSender = tcpMessageSender;
        this.tcpMessageListener = tcpMessageListener;
        this.connectedDisplaysRepository = connectedDisplaysRepository;
        this.customerDisplayUpdatesSender = customerDisplayUpdatesSender;
        this.socketMessageProcessHelper = socketMessageProcessHelper;
    }

    // 1. retrieve information about the client
    // 2. send a connection request to the server and wait for a ack
    // 3. if the server approves the connection, save the connection
    public Completable startDisplayPairing(Socket connectedSocket, ServiceInfo serviceInfo, Boolean isDarkMode, OnPairingServerListener listener) {
        return clientInfoManager.getClientInfo()
                .flatMapCompletable(clientInfo -> getConnectionApprovalStatus(serviceInfo, UUID.randomUUID().toString(), clientInfo, isDarkMode, listener)
                        .flatMapCompletable(connectionApproval -> {
                            if (connectionApproval.isConnectionApproved()) {
                                listener.onConnectionRequestApproved(serviceInfo);
                                return onApprovalReceived(serviceInfo,connectionApproval,isDarkMode)
                                        .doOnComplete(() -> listener.onSavedEstablishedConnection(serviceInfo));
                            } else {
                                listener.onConnectionRequestRejected();
                                return Completable.complete();
                            }
                        }))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    private Completable onApprovalReceived(ServiceInfo serviceInfo,ConnectionApproval connectionApproval, Boolean isDarkMode) {
        CustomerDisplay customerDisplay = new CustomerDisplay(
                connectionApproval.getServerID(),
                serviceInfo.getDeviceName(),
                connectionApproval.getServerIpAddress(),
                true,
                isDarkMode
        );
        return  connectedDisplaysRepository.getCustomerDisplayById(connectionApproval.getServerID())
                .switchIfEmpty(connectedDisplaysRepository.addCustomerDisplay(customerDisplay).toSingle(() -> customerDisplay))
                .flatMapCompletable(existingCustomerDisplay -> connectedDisplaysRepository.updateCustomerDisplay(customerDisplay).ignoreElement());
    }

    private Single<ConnectionApproval> getConnectionApprovalStatus(ServiceInfo serviceInfo, String connectionReqMessageId, ClientInfo clientInfo, Boolean isDarkMode, OnPairingServerListener listener) {
        // Send connection approval request and listen for server response
        return sendConnectionReq(serviceInfo, isDarkMode, connectionReqMessageId)
                .doOnComplete(listener::onConnectionRequestSent)
                .andThen(waitForConnectionApprovalResponse(clientInfo.getClientID(), connectionReqMessageId))
                .doOnSuccess(connectionApproval -> Log.i(TAG, "Connection approval received from the " + serviceInfo.getDeviceName() + " with IP: " + serviceInfo.getIpAddress()))
                .subscribeOn(Schedulers.io());
    }

    private Completable sendConnectionReq(ServiceInfo serviceInfo, Boolean isDarkMode, String connectionReqMessageId) {
        return clientInfoManager.getClientInfo()
                .flatMapCompletable(clientInfo -> {
                    String connectionApprovalReq = createConnectionApprovalRequest(serviceInfo, clientInfo, isDarkMode, connectionReqMessageId);
                    return socketsManager.reconnectIfDisconnected(serviceInfo.getServerId(), serviceInfo.getIpAddress())
                            .flatMapCompletable(reconnectedSocket -> tcpMessageSender.sendOneWayMessage(reconnectedSocket, serviceInfo.getServerId(), connectionApprovalReq)
                            );
                })
                .subscribeOn(Schedulers.io());
    }

    private Single<ConnectionApproval> waitForConnectionApprovalResponse(String clientId, String connectionReqMessageId) {
        return tcpMessageListener.getServerMessageSubject()
                .filter(serverMessage -> {
                    ConnectionApproval connectionApproval = socketMessageProcessHelper.getConnectionApproval(serverMessage.second, clientId, connectionReqMessageId);
                    return connectionApproval != null;
                })
                .firstOrError()
                .timeout(NetworkConstants.WAITING_FOR_CONNECTION_APPROVAL_TIMEOUT, TimeUnit.MILLISECONDS)
                .map(serverMessage -> socketMessageProcessHelper.getConnectionApproval(serverMessage.second, clientId, connectionReqMessageId))
                .doOnError(error -> Log.w(TAG, "Connection approval not received from the server within the timeout period"));
    }

    private String createConnectionApprovalRequest(ServiceInfo serviceInfo, ClientInfo clientInfo, Boolean isDarkMode, String messageID) {
        ConnectionReq connectionReq = new ConnectionReq(
                clientInfo.getClientID(),
                clientInfo.getClientIpAddress(),
                clientInfo.getClientDeviceName(),
                isDarkMode
        );
        SocketMessageBase socketMessageBase = new SocketMessageBase(
                connectionReq,
                NetworkConstants.REQUEST_CONNECTION_APPROVAL,
                serviceInfo.getServerId(),
                clientInfo.getClientID(),
                messageID

        );
        return jsonUtil.toJson(socketMessageBase);
    }
}

