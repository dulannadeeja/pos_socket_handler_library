package com.example.pos;

import android.app.Application;

import com.example.customerdisplayhandler.api.CustomerDisplayManagerImpl;
import com.example.customerdisplayhandler.api.ICustomerDisplayManager;
import com.example.customerdisplayhandler.constants.NetworkConstants;

public class App extends Application {
    private static final String TAG = App.class.getSimpleName();
    private ICustomerDisplayManager ICustomerDisplayManager;

    @Override
    public void onCreate() {
        super.onCreate();
        ICustomerDisplayManager = CustomerDisplayManagerImpl.newInstance(this, NetworkConstants.DEFAULT_SERVER_PORT);
    }

    public ICustomerDisplayManager getCustomerDisplayManager() {
        return ICustomerDisplayManager;
    }
}
