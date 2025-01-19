package com.example.pos;

import android.app.Application;

import com.example.customerdisplayhandler.api.CustomerDisplayManagerImpl;
import com.example.customerdisplayhandler.api.ICustomerDisplayManager;
import com.example.customerdisplayhandler.constants.NetworkConstants;

public class App extends Application {
    private static final String TAG = App.class.getSimpleName();
    private ICustomerDisplayManager customerDisplayManager;

    @Override
    public void onCreate() {
        super.onCreate();
        customerDisplayManager = CustomerDisplayManagerImpl.newInstance(this, NetworkConstants.DEFAULT_SERVER_PORT);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        customerDisplayManager.disposeCustomerDisplayManager();
    }

    public ICustomerDisplayManager getCustomerDisplayManager() {
        return customerDisplayManager;
    }
}
