package com.example.customerdisplayhandler.core.network;

import android.util.Log;
import android.util.Pair;

import com.example.customerdisplayhandler.core.interfaces.IConnectedServerManager;
import com.example.customerdisplayhandler.utils.IJsonUtil;
import com.example.customerdisplayhandler.model.ClientInfo;
import com.example.customerdisplayhandler.model.ConnectionApproval;
import com.example.customerdisplayhandler.model.ServerInfo;
import com.example.customerdisplayhandler.model.SocketMessageBase;
import com.example.customerdisplayhandler.utils.SocketConfigConstants;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

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
    private final List<Pair<String,Socket>> unknownSockets = new ArrayList<>();
    private final List<Pair<ServerInfo, Socket>> discoveredServers = new ArrayList<>();
    private final List<Pair<ServerInfo,Socket>> establishedConnections = new ArrayList<>();
    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    public ConnectedServerManagerImpl(IJsonUtil jsonUtil) {
        this.jsonUtil = jsonUtil;
    }

    @Override
    public void addUnknownSocket(String socketId,Socket socket) {
        Pair<String,Socket> unknownSocket = new Pair<>(socketId,socket);
        unknownSockets.add(unknownSocket);
        compositeDisposable.add(
                startListening(socketId, socket)
                        .subscribe(()->{
                        }, error -> {
                            unknownSockets.remove(unknownSocket);
                            Log.e(TAG, "Error starting to listen for messages from server: " + socketId, error);
                        })
        );
    }

    @Override
    public Completable clearInactiveServers() {
        return Completable.fromAction(() -> {
            discoveredServers.removeIf(serverInfoSocketPair -> serverInfoSocketPair.second.isClosed());
        }).subscribeOn(Schedulers.io());
    }

    @Override
    public void removeUnknownSocket(String socketId) {
        unknownSockets.removeIf(unknownSocket -> unknownSocket.first.equals(socketId));
    }

    @Override
    public void addConnectedServer(ServerInfo serverInfo, Socket socket) {
       try {
           Pair<ServerInfo,Socket> connectedServer = new Pair<>(serverInfo,socket);
           discoveredServers.add(connectedServer);
           socket.setKeepAlive(true);
       }catch (Exception e){
           Log.e(TAG, "Error adding connected server: " + serverInfo.getServerID(), e);
       }
    }

    @Override
    public void closeAllConnections() {
        unknownSockets.forEach(unknownSocket -> {
            try {
                unknownSocket.second.close();
            } catch (IOException e) {
                Log.e(TAG, "Error closing connection to unknown server: " + unknownSocket.first, e);
            }
        });
        unknownSockets.clear();
        discoveredServers.forEach(server -> {
            try {
                server.second.close();
            } catch (IOException e) {
                Log.e(TAG, "Error closing connection to server: " + server.first.getServerID(), e);
            }
        });
        discoveredServers.clear();
    }


    @Override
    public Completable sendMessageToServer(String serverId, String message) {
        return Completable.fromAction(() -> {
            try {
                Socket socket = discoveredServers.stream()
                        .filter(server -> server.first.getServerID().equals(serverId))
                        .findFirst()
                        .map(server -> server.second)
                        .orElseThrow(() -> new Exception("Server not found in connected servers, cannot send message"));

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
            } finally {
//                safelyStopListening(serverId, socket).subscribe();
            }
        }).subscribeOn(Schedulers.io()); // Run the task on an IO thread
    }

    @Override
    public Completable safelyStopListening(String serverId, Socket socket) {
        return Completable.fromAction(() -> {
            try {
                // Remove serverId from the listeningServers list and notify observers
                discoveredServers.removeIf(server -> server.first.getServerID().equals(serverId));

                // Close the socket connection
                socket.close();

                Log.i(TAG, "Stopped listening for messages from server: " + serverId);
            } catch (Exception e) {
                Log.e(TAG, "Error stopping listening for messages from server: " + serverId + ", " + e.getMessage());
                throw e; // Propagate the exception to the Completable
            }
        }).subscribeOn(Schedulers.io()); // Execute on IO thread
    }

    @Override
    public void startPairingServer(ServerInfo serverInfo,ClientInfo clientInfo, OnPairingServerListener onPairingServerListener){
        if(serverInfo != null && discoveredServers.stream().anyMatch(server -> server.first.getServerID().equals(serverInfo.getServerID()))){
                onPairingServerListener.onPairingServerStarted();
                SocketMessageBase socketMessageBase = new SocketMessageBase(clientInfo, SocketConfigConstants.REQUEST_CONNECTION_APPROVAL, serverInfo.getServerID(), clientInfo.getClientID());
                String message = jsonUtil.toJson(socketMessageBase);
                Disposable disposable = sendMessageToServer(serverInfo.getServerID(),message)
                        .doOnComplete(onPairingServerListener::onConnectionRequestSent)
                        .andThen(catchConnectionApprovalResponse(serverInfo))
                        .subscribe(
                                isApproved -> {
                                    if(isApproved){
                                        establishedConnections.add(new Pair<>(serverInfo,discoveredServers.stream().filter(server -> server.first.getServerID().equals(serverInfo.getServerID())).findFirst().map(server -> server.second).orElse(null)));
                                        discoveredServers.removeIf(server -> server.first.getServerID().equals(serverInfo.getServerID()));
                                        onPairingServerListener.onConnectionRequestApproved();
                                    }else{
                                        onPairingServerListener.onConnectionRequestRejected();
                                    }
                                },
                                error -> {
                                    onPairingServerListener.onConnectionRequestFailed();
                                    Log.e(TAG, "Error pairing with server: " + serverInfo.getServerID(), error);
                                }
                        );
                compositeDisposable.add(disposable);
        }else{
            onPairingServerListener.onConnectionRequestFailed();
            Log.e(TAG, "Server not found in connected servers, cannot pair with server: " + serverInfo.getServerID());
        }
    }
    private Single<Boolean> catchConnectionApprovalResponse(ServerInfo serverInfo) {
        return Single.<Boolean>create(emitter -> {
            Disposable disposable = serverMessageSubject
//                    .filter(pair -> pair.first.equals(serverInfo.getServerID())) // Filter by server ID
                    .map(pair -> pair.second) // Extract the message
                    .takeUntil(message -> {
                        try {
                            Log.i("ConnectionApproval", "Received message: " + message);
                            // Process and check if the message matches the approval criteria
                            return processConnectionApproval(message);
                        } catch (Exception e) {
                            Log.e("ConnectionApproval", "Error processing message: " + e.getMessage());
                            return false; // Continue processing if not valid
                        }
                    })
                    .timeout(30, TimeUnit.SECONDS) // Limit the time to wait for messages
                    .subscribe(
                            message -> {
                                try {
                                    // Final check to approve the connection
                                    Boolean isApproved = processConnectionApproval(message);
                                    if (isApproved != null && isApproved) {
                                        emitter.onSuccess(true); // Emit success
                                    }
                                } catch (Exception e) {
                                    emitter.onError(e);
                                }
                            },
                            throwable -> {
                                if (!emitter.isDisposed()) {
                                    emitter.onError(throwable); // Emit an error if timed out or failed
                                }
                            }
                    );

            // Clean up the disposable when the emitter is disposed
            emitter.setDisposable(disposable);
        }).subscribeOn(Schedulers.io());
    }

    private Boolean processConnectionApproval(String message) {
        try {
            if (message == null || message.isEmpty()) {
                Log.e("ConnectionApproval", "Invalid connection approval message received: " + message);
                return false;
            }

            SocketMessageBase socketMessageBase = jsonUtil.toObj(message, SocketMessageBase.class);

            if (socketMessageBase.getCommand() == null ||
                    socketMessageBase.getCommand().isEmpty() ||
                    socketMessageBase.getData() == null ||
                    socketMessageBase.getSenderId() == null ||
                    socketMessageBase.getReceiverId() == null) {
                Log.e("ConnectionApproval", "Invalid connection approval message structure: " + message);
                return false;
            }

            if (!socketMessageBase.getCommand().equals(SocketConfigConstants.RESPONSE_CONNECTION_APPROVAL)) {
                Log.e("ConnectionApproval", "Unexpected command received: " + message);
                return false;
            }

            Object data = socketMessageBase.getData();
            ConnectionApproval connectionApproval = jsonUtil.toObj(jsonUtil.toJson(data), ConnectionApproval.class);

            if (connectionApproval != null && connectionApproval.isConnectionApproved() != null) {
                return connectionApproval.isConnectionApproved();
            }

            Log.e("ConnectionApproval", "Connection approval data is null or invalid.");
            return false;

        } catch (Exception e) {
            Log.e("ConnectionApproval", "Error processing connection approval: " + e.getMessage(), e);
            return false;
        }
    }

    @Override
    public BehaviorSubject<Pair<String, String>> getServerMessageSubject() {
        return serverMessageSubject;
    }

    @Override
    public List<Pair<ServerInfo, Socket>> getDiscoveredServers() {
        return discoveredServers;
    }

    @Override
    public List<Pair<String, Socket>> getUnknownSockets() {
        return unknownSockets;
    }

    @Override
    public List<Pair<ServerInfo, Socket>> getEstablishedConnections() {
        return establishedConnections;
    }
}
