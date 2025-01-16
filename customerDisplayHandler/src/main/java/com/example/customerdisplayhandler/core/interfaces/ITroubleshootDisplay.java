package com.example.customerdisplayhandler.core.interfaces;

import com.example.customerdisplayhandler.model.CustomerDisplay;
import com.example.customerdisplayhandler.shared.OnSilentTroubleshootListener;
import com.example.customerdisplayhandler.shared.OnTroubleshootListener;

import io.reactivex.rxjava3.core.Completable;

public interface ITroubleshootDisplay {
    Completable startManualTroubleshooting(CustomerDisplay customerDisplay, OnTroubleshootListener listener);
    Completable startSilentTroubleshooting(CustomerDisplay customerDisplay);
}
