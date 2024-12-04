package com.example.pos.network.interfaces;

public interface DataObserver<T> {
    void onDataChanged(T data);
    void onError(Exception e);
}

