package com.example.customerdisplayhandler.helpers;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.lang.ref.WeakReference;
import java.util.Locale;

import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class IPManagerImpl implements IPManager {
    private static final String TAG = IPManager.class.getSimpleName();
    private final WeakReference<Context> contextRef;

    public IPManagerImpl(Context context) {
        this.contextRef = new WeakReference<>(context);
    }

    @Override
    public Single<String> getDeviceLocalIPAddress() {
        return Single.<String>create(emitter -> {
                    Context context = contextRef.get();
                    if (context == null) {
                        emitter.onError(new NullPointerException("Context is null"));
                        return;
                    }

                    try {
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
                            emitter.onSuccess(formattedIP);  // Emit the IP address
                        } else {
                            emitter.onError(new NullPointerException("WifiManager is null"));
                        }
                    } catch (Exception e) {
                        emitter.onError(e);  // Emit the error if something fails
                    }
                })
                .doOnError(throwable -> {
                    Log.wtf(TAG, "Error while getting device IP address: " + throwable.getMessage(), throwable);
                })
                .subscribeOn(Schedulers.io()) // Execute the task on a background thread
                .observeOn(Schedulers.single()); // Observe on a single thread (main thread or UI)
    }
}