package com.example.pos;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pos.adaptors.ListAdapter;
import com.example.pos.constants.SocketConfigConstants;
import com.example.pos.helpers.IPAddressCallback;
import com.example.pos.helpers.IPManager;
import com.example.pos.helpers.SharedPrefManagerImpl;
import com.example.pos.model.ClientInfo;
import com.example.pos.model.ConnectionApproval;
import com.example.pos.model.ServerInfo;
import com.example.pos.model.SocketMessageBase;
import com.example.pos.network.ConnectedServerManagerImpl;
import com.example.pos.network.ServerDiscoveryManagerImpl;
import com.example.pos.network.SocketDataSourceImpl;
import com.example.pos.network.SocketConnectionManagerImpl;
import com.example.pos.network.interfaces.DataObserver;
import com.example.pos.network.interfaces.IJsonUtil;
import com.example.pos.utils.JsonUtilImpl;

import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    private IPManager ipManager;
    private SocketDataSourceImpl socketDataSource;
    private ListAdapter availableListAdapter;
    private ListAdapter connectedListAdapter;
    private IJsonUtil jsonUtil;
    private RecyclerView availableRecyclerView;
    private RecyclerView connectedRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Button searchButton = findViewById(R.id.button);
        availableRecyclerView = findViewById(R.id.search_list_recycler_view);

        availableListAdapter = new ListAdapter(new ArrayList<>(),
                serverInfo -> {
                    Log.d("SearchForCustomerDisplays", "Server selected - device name: " + serverInfo.getServerDeviceName() + " server ID: " + serverInfo.getServerID() + " server IP: " + serverInfo.getServerIpAddress());
                    ipManager.getDeviceLocalIPAddress(new IPAddressCallback() {
                        @Override
                        public void onResult(String ipAddress) {
                            socketDataSource.getClientInfo(new SocketDataSourceImpl.OnClientInfoRetrieved() {
                                @Override
                                public void onSucess(ClientInfo clientInfo) {
                                    socketDataSource.sendConnectionRequest(serverInfo, clientInfo);
                                }

                                @Override
                                public void onError(Exception e) {
                                    ClientInfo clientInfoUpdated = new ClientInfo(UUID.randomUUID(), ipAddress, getDeviceModelName());
                                    socketDataSource.saveClientInfo(clientInfoUpdated, new SocketDataSourceImpl.OnClientInfoSaved() {
                                        @Override
                                        public void onSucess() {
                                            socketDataSource.sendConnectionRequest(serverInfo, clientInfoUpdated);
                                        }

                                        @Override
                                        public void onError(Exception e) {
                                            Log.e("SearchForCustomerDisplays", "Error saving server info: " + e.getMessage());
                                        }
                                    });
                                }
                            });
                        }

                        @Override
                        public void onError(Exception e) {
                            Log.e("SearchForCustomerDisplays", "Error getting local IP address: " + e.getMessage());
                        }
                    });
                });

        availableRecyclerView.setAdapter(availableListAdapter);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        availableRecyclerView.setLayoutManager(linearLayoutManager);

        connectedRecyclerView = findViewById(R.id.active_list_recycler_view);
        connectedListAdapter = new ListAdapter(new ArrayList<>(), serverInfo -> {
            Log.d("SearchForCustomerDisplays", "Server selected - device name: " + serverInfo.getServerDeviceName() + " server ID: " + serverInfo.getServerID() + " server IP: " + serverInfo.getServerIpAddress());
        });
        connectedRecyclerView.setAdapter(connectedListAdapter);
        LinearLayoutManager connectedLinearLayoutManager = new LinearLayoutManager(this);
        connectedRecyclerView.setLayoutManager(connectedLinearLayoutManager);


        ipManager = new IPManager(this);
        jsonUtil = new JsonUtilImpl();
        SharedPrefManagerImpl sharedPrefManager = SharedPrefManagerImpl.getInstance(getApplicationContext());
        SocketConnectionManagerImpl socketConnectionManager = new SocketConnectionManagerImpl();
        ConnectedServerManagerImpl connectedServerManager = new ConnectedServerManagerImpl();
        ServerDiscoveryManagerImpl serverDiscoveryManager = new ServerDiscoveryManagerImpl(socketConnectionManager, connectedServerManager, jsonUtil);
        socketDataSource = new SocketDataSourceImpl(socketConnectionManager, serverDiscoveryManager, connectedServerManager, jsonUtil,sharedPrefManager );

        searchButton.setOnClickListener(v -> {
            searchForCustomerDisplays();
        });

        socketDataSource.getActiveServerConnections().addObserver(
                new DataObserver<Map<ServerInfo, Socket>>() {
                    @Override
                    public void onDataChanged(Map<ServerInfo, Socket> data) {
                        if(data == null) {
                            return;
                        }
                        List<ServerInfo> serverInfoArrayList = Collections.synchronizedList(new ArrayList<>());
                        for (Map.Entry<ServerInfo, Socket> entry : data.entrySet()) {
                            ServerInfo serverInfo = entry.getKey();
                            serverInfoArrayList.add(serverInfo);
                            Log.d("SearchForCustomerDisplays", "Server found - device name: " + serverInfo.getServerDeviceName() + " server ID: " + serverInfo.getServerID() + " server IP: " + serverInfo.getServerIpAddress());
                        }
                        runOnUiThread(() -> {
                            synchronized (serverInfoArrayList) {
                                connectedListAdapter.setServerInfoList(serverInfoArrayList);
                            }
                        });
                    }

                    @Override
                    public void onError(Exception e) {
                        Log.e("SearchForCustomerDisplays", "Error getting active server connections: " + e.getMessage());
                    }
                }
        );
    }

    private String getDeviceModelName() {
        return android.os.Build.MODEL;
    }

    private void searchForCustomerDisplays() {
        ipManager.getDeviceLocalIPAddress(new IPAddressCallback() {

            @Override
            public void onResult(String ipAddress) {
                socketDataSource.getAvailableServers(ipAddress, SocketConfigConstants.DEFAULT_SERVER_PORT, serverInfoList -> {
                    List<ServerInfo> serverInfoArrayList = Collections.synchronizedList(new ArrayList<>());
                    for (Map.Entry<ServerInfo, Socket> entry : serverInfoList.entrySet()) {
                        ServerInfo serverInfo = entry.getKey();
                        Socket socket = entry.getValue();
                        serverInfoArrayList.add(serverInfo);
                        Log.d("SearchForCustomerDisplays", "Server found - device name: " + serverInfo.getServerDeviceName() + " server ID: " + serverInfo.getServerID() + " server IP: " + serverInfo.getServerIpAddress());
                    }
                    runOnUiThread(() -> {
                        synchronized (serverInfoArrayList) {
                            availableListAdapter.setServerInfoList(serverInfoArrayList);
                        }
                    });
                });
            }

            @Override
            public void onError(Exception e) {
                Log.e("SearchForCustomerDisplays", "Error getting local IP address: " + e.getMessage());
            }
        });
    }
}