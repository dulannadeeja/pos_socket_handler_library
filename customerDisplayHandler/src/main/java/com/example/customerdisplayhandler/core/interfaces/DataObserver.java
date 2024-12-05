package com.example.customerdisplayhandler.core.interfaces;

public interface DataObserver<T> {
    void onDataChanged(T data);
    void onError(Exception e);
}

