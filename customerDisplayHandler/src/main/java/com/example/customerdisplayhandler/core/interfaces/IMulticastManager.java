package com.example.customerdisplayhandler.core.interfaces;

import io.reactivex.rxjava3.core.Completable;

public interface IMulticastManager {
    Completable sendMessage(String message);
}
