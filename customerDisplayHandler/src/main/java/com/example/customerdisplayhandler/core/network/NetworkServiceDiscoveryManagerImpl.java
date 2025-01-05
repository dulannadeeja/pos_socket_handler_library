package com.example.customerdisplayhandler.core.network;

import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.os.CountDownTimer;
import android.util.Log;
import com.example.customerdisplayhandler.constants.NetworkConstants;
import com.example.customerdisplayhandler.core.interfaces.INetworkServiceDiscoveryManager;
import com.example.customerdisplayhandler.model.ServiceInfo;
import java.net.InetAddress;
import java.util.Map;

public class NetworkServiceDiscoveryManagerImpl implements INetworkServiceDiscoveryManager {
    private static final String TAG = NetworkServiceDiscoveryManagerImpl.class.getSimpleName();
    private static final String SERVICE_TYPE = "_customerDisplay._tcp.";
    private final NsdManager nsdManager;
    private NsdManager.DiscoveryListener discoveryListener;
    private CountDownTimer searchTimer;
    private Boolean isSearchInProgress = false;

    public NetworkServiceDiscoveryManagerImpl(Context context) {
        this.nsdManager = (NsdManager) context.getSystemService(Context.NSD_SERVICE);
    }


    @Override
    public void startSearchForServices(SearchListener searchListener, int timeout) {
        if (isSearchInProgress) {
            Log.w(TAG, "Service discovery is already in progress. Ignoring start request.");
            return;
        }

        stopSearchForServices();

        searchTimer = new CountDownTimer(timeout, 1000) {
            private boolean isTimerTriggered = false;

            @Override
            public void onTick(long millisUntilFinished) {
                if (!isTimerTriggered) {
                    isTimerTriggered = true;
                    startDiscovery(searchListener);
                }
            }

            @Override
            public void onFinish() {
                stopSearchForServices();
                searchListener.onSearchCompleted();
            }
        };
        searchTimer.start();
    }

    private void startDiscovery(SearchListener searchListener) {
        discoveryListener = new NsdManager.DiscoveryListener() {
            @Override
            public void onDiscoveryStarted(String regType) {
                isSearchInProgress = true;
                searchListener.onSearchStarted();
                Log.i(TAG, "Service discovery started.");
            }

            @Override
            public void onServiceFound(NsdServiceInfo serviceInfo) {
                Log.i(TAG, "Service found: " + serviceInfo);
                resolveService(searchListener,serviceInfo);
            }

            @Override
            public void onServiceLost(NsdServiceInfo serviceInfo) {
                Log.w(TAG, "Service lost: " + serviceInfo);
            }

            @Override
            public void onDiscoveryStopped(String serviceType) {
                isSearchInProgress = false;
                searchListener.onSearchCompleted();
                Log.i(TAG, "Service discovery stopped.");
                stopSearchForServices();
            }

            @Override
            public void onStartDiscoveryFailed(String serviceType, int errorCode) {
                Log.e(TAG, "Discovery start failed. Error code: " + errorCode);
                searchListener.onSearchFailed("Discovery start failed. Error code: " + errorCode);
                stopSearchForServices();
            }

            @Override
            public void onStopDiscoveryFailed(String serviceType, int errorCode) {
                Log.e(TAG, "Discovery stop failed. Error code: " + errorCode);
                searchListener.onSearchFailed("Discovery stop failed. Error code: " + errorCode);
                stopSearchForServices();
            }
        };

        nsdManager.discoverServices(SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, discoveryListener);
    }

    private void resolveService(SearchListener searchListener,NsdServiceInfo serviceInfo) {
        nsdManager.resolveService(serviceInfo, new NsdManager.ResolveListener() {
            @Override
            public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode) {
                Log.e(TAG, "Resolve failed for service: " + serviceInfo + ", Error code: " + errorCode);
            }

            @Override
            public void onServiceResolved(NsdServiceInfo serviceInfo) {
                Log.i(TAG, "Service resolved: " + serviceInfo);
                processResolvedService(searchListener,serviceInfo);
            }
        });
    }

    private void processResolvedService(SearchListener searchListener, NsdServiceInfo serviceInfo) {
        InetAddress host = serviceInfo.getHost();
        int port = serviceInfo.getPort();

        // Validate host and port
        if (host == null || port == 0) {
            Log.e(TAG, "Invalid service information: " + serviceInfo);
            return;
        }

        String serviceName = serviceInfo.getServiceName();
        Map<String, byte[]> attributes = serviceInfo.getAttributes();

        // Validate attributes
        if (!areAttributesValid(attributes, serviceName)) {
            return;
        }

        // Extract attribute values
        String serverId = extractAttributeValue(attributes, NetworkConstants.SERVER_ID_LABEL);
        String deviceName = extractAttributeValue(attributes, NetworkConstants.DEVICE_NAME_LABEL);
        String connectedClientId = extractAttributeValue(attributes, NetworkConstants.CONNECTED_CLIENT_ID_LABEL);
        String ipAddress = extractAttributeValue(attributes, NetworkConstants.IP_ADDRESS_LABEL);

        // Create ServiceInfo and notify listener
        ServiceInfo service = new ServiceInfo(serverId, deviceName, ipAddress, connectedClientId);
        searchListener.onServiceFound(service);
    }

    /**
     * Validates the required attributes for the service.
     */
    private boolean areAttributesValid(Map<String, byte[]> attributes, String serviceName) {
        if (attributes == null) {
            Log.e(TAG, "No attributes found for service: " + serviceName);
            return false;
        }

        String[] requiredKeys = {
                NetworkConstants.SERVER_ID_LABEL,
                NetworkConstants.DEVICE_NAME_LABEL,
                NetworkConstants.IP_ADDRESS_LABEL,
                NetworkConstants.CONNECTED_CLIENT_ID_LABEL
        };

        for (String key : requiredKeys) {
            if (!attributes.containsKey(key)) {
                return false;
            } else if (!key.equals(NetworkConstants.CONNECTED_CLIENT_ID_LABEL) && attributes.get(key) == null) {
                return false;
            }
        }
        return true;
    }

    /**
     * Safely extracts the value of an attribute as a String.
     */
    private String extractAttributeValue(Map<String, byte[]> attributes, String key) {
        byte[] value = attributes.get(key);
        return value != null ? new String(value) : "";
    }

    @Override
    public void stopSearchForServices() {
        if (discoveryListener != null) {
            try {
                nsdManager.stopServiceDiscovery(discoveryListener);
                Log.i(TAG, "Service discovery stopped.");
            } catch (IllegalArgumentException e) {
                Log.w(TAG, "Discovery was already stopped or not started: " + e.getMessage());
            }
            discoveryListener = null;
        }
        isSearchInProgress = false;

        if (searchTimer != null) {
            searchTimer.cancel();
            searchTimer = null;
        }
    }
}
