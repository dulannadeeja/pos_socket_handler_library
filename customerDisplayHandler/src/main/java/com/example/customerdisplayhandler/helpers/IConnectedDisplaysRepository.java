package com.example.customerdisplayhandler.helpers;

import com.example.customerdisplayhandler.model.CustomerDisplay;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;

public interface IConnectedDisplaysRepository {
    Completable addCustomerDisplay(CustomerDisplay customerDisplay);
    Completable removeCustomerDisplay(String customerDisplayId);
    Maybe<CustomerDisplay> getCustomerDisplayById(String customerDisplayId);
    Single<CustomerDisplay> updateCustomerDisplay(CustomerDisplay customerDisplay);
    Single<List<CustomerDisplay>> getListOfConnectedDisplays();
}
