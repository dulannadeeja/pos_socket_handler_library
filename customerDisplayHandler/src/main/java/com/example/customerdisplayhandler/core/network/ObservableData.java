package com.example.customerdisplayhandler.core.network;

import com.example.customerdisplayhandler.core.interfaces.DataObserver;

import java.util.ArrayList;
import java.util.List;

public class ObservableData<T> {
    private final List<DataObserver<T>> observers = new ArrayList<>();
    private T data;

    public void addObserver(DataObserver<T> observer) {
        synchronized (observers) {
            observers.add(observer);
        }
    }

    public void removeObserver(DataObserver<T> observer) {
        synchronized (observers) {
            observers.remove(observer);
        }
    }

    public void setData(T data) {
        this.data = data;
        notifyObservers();
    }

    public T getData() {
        return data;
    }

    private void notifyObservers() {
        synchronized (observers) {
            for (DataObserver<T> observer : observers) {
                observer.onDataChanged(data);
            }
        }
    }

    public void notifyError(Exception e) {
        synchronized (observers) {
            for (DataObserver<T> observer : observers) {
                observer.onError(e);
            }
        }
    }
}

