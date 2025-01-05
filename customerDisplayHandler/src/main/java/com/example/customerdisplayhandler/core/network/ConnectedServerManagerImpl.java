package com.example.customerdisplayhandler.core.network;

import android.util.Log;
import android.util.Pair;

import com.example.customerdisplayhandler.constants.NetworkConstants;
import com.example.customerdisplayhandler.core.interfaces.IConnectedServerManager;
import com.example.customerdisplayhandler.model.ServiceInfo;
import com.example.customerdisplayhandler.utils.IJsonUtil;
import com.example.customerdisplayhandler.model.ClientInfo;
import com.example.customerdisplayhandler.model.ConnectionApproval;
import com.example.customerdisplayhandler.model.ServerInfo;
import com.example.customerdisplayhandler.model.SocketMessageBase;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.subjects.BehaviorSubject;

public class ConnectedServerManagerImpl implements IConnectedServerManager {
    private static final String TAG = ConnectedServerManagerImpl.class.getSimpleName();
    private final IJsonUtil jsonUtil;
    private final BehaviorSubject<Pair<String, String>> serverMessageSubject = BehaviorSubject.create();
    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    public ConnectedServerManagerImpl(IJsonUtil jsonUtil) {
        this.jsonUtil = jsonUtil;
    }

    @Override
    public Completable sendMessageToServer(String serverId, Socket socket, String message) {
        return Completable.fromAction(() -> {
            try {
                DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
                dataOutputStream.writeUTF(message);
                dataOutputStream.flush();
                Log.i(TAG, "Message sent to server: " + message);
            } catch (Exception e) {
                Log.e(TAG, "Failed to send message to server: " + serverId, e);
                throw new Exception("Error sending message to server: " + serverId, e);
            }
        }).subscribeOn(Schedulers.io()); // Perform operation on IO thread
    }

    @Override
    public Completable startListening(String serverId, Socket socket) {
        return Completable.fromAction(() -> {
            Log.i(TAG, "Listening for messages from server: " + socket.getInetAddress().getHostAddress());
            try (DataInputStream input = new DataInputStream(socket.getInputStream())) {
                String serverMessage;
                while ((serverMessage = input.readUTF()) != null) {
                    serverMessageSubject.onNext(new Pair<>(serverId, serverMessage));
                    Log.i(TAG, "Message received from server: " + serverMessage);
                }
            } catch (EOFException eof) {
                Log.e(TAG, "Client disconnected from server, IP: " + socket.getInetAddress().getHostAddress() + ", Server ID: " + serverId);
            } catch (IOException e) {
                Log.e(TAG, "Error reading message: " + e.getMessage());
            }
        }).subscribeOn(Schedulers.io()); // Run the task on an IO thread
    }

    @Override
    public void startPairingServer(ServiceInfo serviceInfo, Socket socket, ClientInfo clientInfo, OnPairingServerListener onPairingServerListener) {
        if (socket == null || !socket.isConnected()) {
            onPairingServerListener.onPairingServerFailed("Connection error occurred while pairing with customer display.");
            Log.e(TAG, "Socket is null or not connected.");
            return;
        }

        onPairingServerListener.onPairingServerStarted();

        SocketMessageBase socketMessageBase = new SocketMessageBase(
                clientInfo,
                NetworkConstants.REQUEST_CONNECTION_APPROVAL,
                serviceInfo.getServerId(),
                clientInfo.getClientID()
        );

        String message = jsonUtil.toJson(socketMessageBase);

        Disposable disposable = sendMessageToServer(serviceInfo.getServerId(), socket, message)
                .doOnComplete(onPairingServerListener::onConnectionRequestSent)
                .andThen(catchConnectionApprovalResponse(serviceInfo))
                .subscribe(
                        messageFromServer -> {
                            String response = processConnectionApproval(messageFromServer);
                            switch (response) {
                                case "approved":
                                    onPairingServerListener.onConnectionRequestApproved();
                                    break;
                                case "rejected":
                                    onPairingServerListener.onConnectionRequestRejected();
                                    break;
                                default:
                                    onPairingServerListener.onPairingServerFailed(
                                            "Error occurred while approving connection request from customer display."
                                    );
                                    break;
                            }
                        },
                        error -> {
                            onPairingServerListener.onPairingServerFailed(
                                    "Unexpected error occurred while pairing with customer display."
                            );
                            Log.e(TAG, "Error pairing with customer display: " + error.getMessage(), error);
                        }
                );

        compositeDisposable.add(disposable);
    }

    @Override
    public void stopPairingServer() {
        compositeDisposable.clear();
    }

    private Single<String> catchConnectionApprovalResponse(ServiceInfo serviceInfo) {
        return serverMessageSubject
                .filter(pair -> pair.first.equals(serviceInfo.getServerId())) // Filter by server ID
                .map((Pair<String, String> pair) -> pair.second)
                .singleOrError();
    }

    private String processConnectionApproval(String message) {
        try {
            if (message == null || message.isEmpty()) {
                Log.e(TAG, "Invalid connection approval message received: " + message);
                return "invalid";
            }

            SocketMessageBase socketMessageBase = jsonUtil.toObj(message, SocketMessageBase.class);
            if (!isValidSocketMessage(socketMessageBase)) {
                Log.e(TAG, "Invalid connection approval message structure: " + message);
                return "invalid";
            }

            if (!NetworkConstants.RESPONSE_CONNECTION_APPROVAL.equals(socketMessageBase.getCommand())) {
                Log.e(TAG, "Unexpected command received: " + message);
                return "invalid";
            }

            ConnectionApproval connectionApproval = parseConnectionApproval(socketMessageBase.getData());
            if (connectionApproval != null && connectionApproval.isConnectionApproved() != null) {
                return connectionApproval.isConnectionApproved() ? "approved" : "rejected";
            }

            Log.e(TAG, "Connection approval data is null or invalid.");
            return "invalid";

        } catch (Exception e) {
            Log.e(TAG, "Error processing connection approval: " + e.getMessage(), e);
            return "invalid";
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

    private ConnectionApproval parseConnectionApproval(Object data) {
        try {
            String jsonData = jsonUtil.toJson(data);
            return jsonUtil.toObj(jsonData, ConnectionApproval.class);
        } catch (Exception e) {
            Log.e(TAG, "Error parsing connection approval data: " + e.getMessage(), e);
            return null;
        }
    }


    public BehaviorSubject<Pair<String, String>> getServerMessageSubject() {
        return serverMessageSubject;
    }
}
