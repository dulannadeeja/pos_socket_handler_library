package com.example.customerdisplayhandler.core.network;

import android.util.Log;
import android.util.Pair;

import com.example.customerdisplayhandler.constants.NetworkConstants;
import com.example.customerdisplayhandler.core.interfaces.IConnectedServerManager;
import com.example.customerdisplayhandler.model.ServiceInfo;
import com.example.customerdisplayhandler.utils.IJsonUtil;
import com.example.customerdisplayhandler.model.ClientInfo;
import com.example.customerdisplayhandler.model.ConnectionApproval;
import com.example.customerdisplayhandler.model.SocketMessageBase;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;

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

    public ConnectedServerManagerImpl(IJsonUtil jsonUtil) {
        this.jsonUtil = jsonUtil;
    }

    @Override
    public Completable sendMessageToServer(String serverId, Socket socket, String message) {
        return Completable.create((emitter) -> {
            try {
                DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
                dataOutputStream.writeUTF(message);
                dataOutputStream.flush();
                Log.i(TAG, "Message sent to server: " + message);
                emitter.onComplete();
            } catch (EOFException eof) {
                Log.e(TAG, "Client disconnected from server, IP: " + socket.getInetAddress().getHostAddress() + ", Server ID: " + serverId);
                emitter.onError(new Exception("Customer display disconnected, cannot send message."));
            } catch (IOException e) {
                Log.e(TAG, "Error sending message: " + e.getMessage());
                emitter.onError(new Exception("Error occurred while sending message to customer display"));
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
    public Single<Boolean> startPairingServer(ServiceInfo serviceInfo, Socket socket, ClientInfo clientInfo, OnConnectionRequestSendListener onConnectionRequestSendListener) {
        return Single.<Boolean>create(emitter -> {
                    try {
                        // Notify the listener that the pairing server has started
                        SocketMessageBase socketMessageBase = new SocketMessageBase(clientInfo, NetworkConstants.REQUEST_CONNECTION_APPROVAL, serviceInfo.getServerId(), clientInfo.getClientID());
                        String message = jsonUtil.toJson(socketMessageBase);

                        Log.i(TAG, "Sending connection request to customer display.");

                        sendMessageToServer(serviceInfo.getServerId(), socket, message)
                                .doOnComplete(onConnectionRequestSendListener::onConnectionRequestSent)
                                .doOnError((e)->{
                                    if(!emitter.isDisposed()){
                                        emitter.onError(e);
                                    }
                                })
                                .blockingAwait(); // Block until the message is sent

                        Log.i(TAG, "Waiting for connection approval from customer display.");
                        try {
                            DataInputStream input = new DataInputStream(socket.getInputStream());
                            Log.i(TAG, "Listening for connection approval from customer display.");
                            String serverMessage;
                            while ((serverMessage = input.readUTF()) != null) {
                                Log.i(TAG, "Message received from server: " + serverMessage);
                                String approvalStatus = processConnectionApproval(serverMessage);
                                switch (approvalStatus) {
                                    case "approved":
                                        Log.i(TAG, "Connection approved.");
                                        emitter.onSuccess(true);
                                        return;
                                    case "rejected":
                                        Log.e(TAG, "Connection rejected.");
                                        emitter.onSuccess(false);
                                        return;
                                }
                            }
                        } catch (EOFException eof) {
                            Log.e(TAG, "Connection lost with customer display.");
                            emitter.onError(new Exception("Connection lost with customer display."));
                        } catch (IOException e) {
                            Log.e(TAG, "Error reading message: " + e.getMessage());
                            emitter.onError(new Exception("Error during connection approval process."));
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error starting pairing server: " + e.getMessage());
                        emitter.onError(e);
                    }
                })
                .subscribeOn(Schedulers.io()); // Perform the operation on an IO thread
    }

    @Override
    public BehaviorSubject<Pair<String, String>> getServerMessageSubject() {
        return serverMessageSubject;
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
