package com.example.customerdisplayhandler.core.network;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.text.format.Formatter;
import android.util.Log;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Locale;

public class NetworkDiscovery {
    private final Context context;

    public NetworkDiscovery(Context context) {
        this.context = context;
    }

    public void discoverDevices() {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);

        // Get the device's current IP address
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int ipAddress = wifiInfo.getIpAddress();
        String localIPAddress = String.format(
                Locale.ROOT,
                "%d.%d.%d.%d",
                (ipAddress & 0xff),
                (ipAddress >> 8 & 0xff),
                (ipAddress >> 16 & 0xff),
                (ipAddress >> 24 & 0xff)
        );

        // Calculate the subnet from the device's IP address
        String subnet = localIPAddress.substring(0, localIPAddress.lastIndexOf(".") + 1);
        subnet = "10.30.100.";

        Log.d("NetworkDiscovery", "Local IP address: " + localIPAddress);
        Log.d("NetworkDiscovery", "Subnet: " + subnet);

        // Scan subnet for devices
        for (int i = 1; i < 255; i++) {
            String host = subnet + i;
            new Thread(() -> {
                try {
                    // Test if the host is reachable
                    if (InetAddress.getByName(host).isReachable(200)) {
                        Log.d("NetworkDiscovery", "Host " + host + " is reachable");
                    }
                } catch (IOException ignored) {
                    Log.d("NetworkDiscovery", "Host " + host + " is not reachable");
                }
            }).start();
        }
    }
}
