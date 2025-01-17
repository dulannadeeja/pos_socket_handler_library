package com.example.customerdisplayhandler.core;

import android.util.Log;

import com.example.customerdisplayhandler.core.interfaces.IConnectedDisplaysRepository;
import com.example.customerdisplayhandler.helpers.ISharedPrefManager;
import com.example.customerdisplayhandler.model.CustomerDisplay;
import com.example.customerdisplayhandler.utils.IJsonUtil;
import com.example.customerdisplayhandler.utils.SharedPrefLabels;
import com.google.gson.internal.LinkedTreeMap;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class ConnectedDisplaysRepositoryImpl implements IConnectedDisplaysRepository {
    private static ConnectedDisplaysRepositoryImpl instance;
    private ISharedPrefManager sharedPrefManager;
    private IJsonUtil jsonUtil;
    private String sharedPrefKey = SharedPrefLabels.CUSTOMER_DISPLAY_LIST;

    public static ConnectedDisplaysRepositoryImpl getInstance(ISharedPrefManager sharedPrefManager, IJsonUtil jsonUtil) {
        if (instance == null) {
            instance = new ConnectedDisplaysRepositoryImpl(sharedPrefManager, jsonUtil);
        }
        return instance;
    }

    private ConnectedDisplaysRepositoryImpl(ISharedPrefManager sharedPrefManager, IJsonUtil jsonUtil) {
        this.sharedPrefManager = sharedPrefManager;
        this.jsonUtil = jsonUtil;
    }

    @Override
    public Completable addCustomerDisplay(CustomerDisplay customerDisplay) {
        return getListOfConnectedDisplays()
                .flatMapCompletable((customerDisplayList) -> {
                    if(customerDisplay == null ||
                            customerDisplay.getCustomerDisplayID() == null ||
                            customerDisplay.getCustomerDisplayName() == null ||
                            customerDisplay.getCustomerDisplayIpAddress() == null

                    ){
                        return Completable.error(new Exception("Customer display is null or has null fields"));
                    }
                    customerDisplayList.add(customerDisplay);
                    Log.e("ConnectedDisplaysRepository", "Customer display added to repo: " + jsonUtil.toJson(customerDisplay));
                    return saveListOnSharedPref(customerDisplayList);
                })
                .subscribeOn(Schedulers.io());
    }

    @Override
    public Completable removeCustomerDisplay(String customerDisplayId) {
        return getListOfConnectedDisplays()
                .flatMapCompletable((customerDisplayList) -> {
                    for (int i = 0; i < customerDisplayList.size(); i++) {
                        if (customerDisplayList.get(i).getCustomerDisplayID().equals(customerDisplayId)) {
                            customerDisplayList.remove(i);
                            return saveListOnSharedPref(customerDisplayList);
                        }
                    }
                    return Completable.error(new Exception("Customer display already removed"));
                })
                .subscribeOn(Schedulers.io());
    }

    @Override
    public Maybe<CustomerDisplay> getCustomerDisplayById(String customerDisplayId) {
        return getListOfConnectedDisplays()
                .doOnSuccess((customerDisplays -> Log.d("ConnectedDisplaysRepository", "Customer displays: " + customerDisplays)))
                .flatMapMaybe((customerDisplayList) -> {
                    for (CustomerDisplay customerDisplay : customerDisplayList) {
                        if (customerDisplay != null && customerDisplay.getCustomerDisplayID() != null && customerDisplay.getCustomerDisplayID().equals(customerDisplayId)) {
                            return Maybe.just(customerDisplay);
                        }
                    }
                    return Maybe.empty();
                })
                .subscribeOn(Schedulers.io());
    }

    @Override
    public Single<CustomerDisplay> updateCustomerDisplay(CustomerDisplay customerDisplay) {
        return getListOfConnectedDisplays()
                .flatMap((customerDisplayList) -> {
                    for (int i = 0; i < customerDisplayList.size(); i++) {
                        if (customerDisplayList.get(i).getCustomerDisplayID().equals(customerDisplay.getCustomerDisplayID())) {
                            customerDisplayList.set(i, customerDisplay);
                            return saveListOnSharedPref(customerDisplayList)
                                    .andThen(Single.just(customerDisplay));
                        }
                    }
                    return Single.error(new Exception("Customer display not found"));
                })
                .subscribeOn(Schedulers.io());
    }

    @Override
    public Single<List<CustomerDisplay>> getListOfConnectedDisplays() {
        return Single.<List<CustomerDisplay>>create((emitter) -> {
                    try {
                        String json = sharedPrefManager.getString(sharedPrefKey, "");
                        if (json.isEmpty()) {
                            emitter.onSuccess(new ArrayList<>());
                            return;
                        }
                        // Deserialize JSON into a generic ArrayList
                        ArrayList<?> rawList = jsonUtil.toObj(json, ArrayList.class);

                        // Convert LinkedTreeMap objects to CustomerDisplay objects
                        List<CustomerDisplay> customerDisplayList = new ArrayList<>();
                        for (Object obj : rawList) {
                            if (obj instanceof LinkedTreeMap) {
                                String jsonString = jsonUtil.toJson(obj); // Convert LinkedTreeMap back to JSON
                                CustomerDisplay customerDisplay = jsonUtil.toObj(jsonString, CustomerDisplay.class); // Deserialize JSON to CustomerDisplay
                                customerDisplayList.add(customerDisplay);
                            }else{
                                Log.e("ConnectedDisplaysRepository", "Error deserializing customer display: " + obj);
                                emitter.onError(new Exception("Error deserializing customer display"));
                            }
                        }
                        emitter.onSuccess(customerDisplayList);
                    } catch (Exception e) {
                        Log.e("ConnectedDisplaysRepository", "Error getting connected displays: " + e.getMessage());
                        emitter.onError(e);
                    }
                })
                .subscribeOn(Schedulers.io());
    }

    private Completable saveListOnSharedPref(List<CustomerDisplay> customerDisplayList) {
        return Completable.create((emitter) -> {
            try {
                String json = jsonUtil.toJson(customerDisplayList);
                sharedPrefManager.putString(sharedPrefKey, json);
                emitter.onComplete();
            } catch (Exception e) {
                emitter.onError(e);
            }
        });
    }
}
