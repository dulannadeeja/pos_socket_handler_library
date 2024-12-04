package com.example.pos.helpers;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import java.lang.ref.WeakReference;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class IPManager {
    private static final String TAG = IPManager.class.getSimpleName();
    private final WeakReference<Context> contextRef;
    private final ExecutorService executorService;

    public IPManager(Context context) {
        this.contextRef = new WeakReference<>(context);
        this.executorService = Executors.newSingleThreadExecutor();
    }

    // Method to get device IP address and return result via callback
    public void getDeviceLocalIPAddress(IPAddressCallback callback) {
        executorService.submit(
                () -> {
                    Context context = contextRef.get();
                    try {
                        if (context != null) {
                            WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                            if (wifiManager != null) {
                                WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                                int ipAddress = wifiInfo.getIpAddress();
                                String formattedIP = String.format(
                                        Locale.ROOT,
                                        "%d.%d.%d.%d",
                                        (ipAddress & 0xff),
                                        (ipAddress >> 8 & 0xff),
                                        (ipAddress >> 16 & 0xff),
                                        (ipAddress >> 24 & 0xff)
                                );
                                // Return result on the main thread
                                if (callback != null) {
                                    callback.onResult(formattedIP);
                                }
                            }
                        }
                    } catch (Exception e) {
                        if (callback != null) {
                            callback.onError(e);  // Notify callback about error
                        }
                    }
                }
        );
    }
}
