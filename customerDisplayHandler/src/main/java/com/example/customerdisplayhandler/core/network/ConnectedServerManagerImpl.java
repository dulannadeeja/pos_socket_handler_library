package com.example.customerdisplayhandler.core.network;

import android.util.Log;
import android.util.Pair;

import com.example.customerdisplayhandler.core.callbacks.OnSendMessageCompleted;
import com.example.customerdisplayhandler.core.callbacks.OnServerMessageReceived;
import com.example.customerdisplayhandler.core.interfaces.IConnectedServerManager;
import com.example.customerdisplayhandler.model.ServerInfo;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.subjects.PublishSubject;

public class ConnectedServerManagerImpl implements IConnectedServerManager {
    private static final String TAG = ConnectedServerManagerImpl.class.getSimpleName();

    private PublishSubject<Pair<String, String>> serverMessageSubject = PublishSubject.create();
    private PublishSubject<List<String>> listeningServersSubject = PublishSubject.create();
    private List<String> listeningServers = new ArrayList<>();
    private List<Pair<ServerInfo, Socket>> connectedServers = new ArrayList<>();
    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    public ConnectedServerManagerImpl() {
    }

    @Override
    public void addConnectedServer(Socket socket, ServerInfo serverInfo) {
        Log.d(TAG, "Adding connected server: " + serverInfo.getServerID());
        Pair<ServerInfo, Socket> connectedServer = new Pair<>(serverInfo, socket);
        compositeDisposable.add(
                startListening(serverInfo.getServerID(), socket)
                        .subscribe(()->{
                            connectedServers.add(connectedServer);
                        }, error -> {
                            connectedServers.remove(connectedServer);
                        })
        );
    }

    @Override
    public Completable sendMessageToServer(String serverId, String message) {
        return Completable.fromAction(() -> {
            Socket socket = connectedServers.stream()
                    .filter(server -> server.first.getServerID().equals(serverId))
                    .findFirst()
                    .map(server -> server.second)
                    .orElseThrow(() -> new Exception("Server not found in connected servers, cannot send message"));

            try (DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream())) {
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
            listeningServers.add(serverId);
            listeningServersSubject.onNext(listeningServers);

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
                // Clean up after disconnection or error
                listeningServers.remove(serverId);
                listeningServersSubject.onNext(listeningServers);
            }
        }).subscribeOn(Schedulers.io()); // Run the task on an IO thread
    }

    @Override
    public Completable safelyStopListening(String serverId, Socket socket) {
        return Completable.fromAction(() -> {
            try {
                // Remove serverId from the listeningServers list and notify observers
                listeningServers.remove(serverId);
                listeningServersSubject.onNext(listeningServers);

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
    public PublishSubject<Pair<String, String>> getServerMessageSubject() {
        return serverMessageSubject;
    }

    @Override
    public PublishSubject<List<String>> getListeningServersSubject() {
        return listeningServersSubject;
    }
}
