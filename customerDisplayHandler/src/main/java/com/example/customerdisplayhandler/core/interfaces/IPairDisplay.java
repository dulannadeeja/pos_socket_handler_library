package com.example.customerdisplayhandler.core.interfaces;

import com.example.customerdisplayhandler.shared.OnPairingServerListener;
import com.example.customerdisplayhandler.model.ServiceInfo;

import java.net.Socket;

import io.reactivex.rxjava3.core.Completable;

public interface IPairDisplay {
    Completable startDisplayPairing(Socket socket, ServiceInfo serviceInfo, OnPairingServerListener listener);
}
