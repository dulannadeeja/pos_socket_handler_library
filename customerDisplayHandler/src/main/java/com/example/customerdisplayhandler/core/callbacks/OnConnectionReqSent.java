package com.example.customerdisplayhandler.core.callbacks;

import com.example.customerdisplayhandler.model.CustomerDisplay;

public interface OnConnectionReqSent {
    void onSuccess(CustomerDisplay customerDisplay);
    void onFailed(Exception e);
}
