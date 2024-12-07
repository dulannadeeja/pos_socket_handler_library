package com.example.customerdisplayhandler.helpers;

import io.reactivex.rxjava3.core.Single;

public interface IPManager {
    Single<String> getDeviceLocalIPAddress();
}
