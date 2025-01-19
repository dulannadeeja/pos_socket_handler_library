package com.example.customerdisplayhandler.core.interfaces;

import android.util.Pair;

import com.example.customerdisplayhandler.model.CustomerDisplay;
import com.example.customerdisplayhandler.model.DisplayUpdates;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;

public interface ICustomerDisplayUpdatesSender {
    Single<List<Pair<CustomerDisplay, Boolean>>> sendUpdatesToCustomerDisplays(DisplayUpdates displayUpdates);
    Completable sendThemeUpdateToCustomerDisplay(CustomerDisplay customerDisplay);
}
