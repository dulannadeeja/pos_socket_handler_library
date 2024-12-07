package com.example.pos;

import android.app.Application;

import com.example.customerdisplayhandler.api.CustomerDisplayManager;
import com.example.customerdisplayhandler.api.CustomerDisplayManagerImpl;
import com.example.customerdisplayhandler.utils.SocketConfigConstants;

public class App extends Application {
    private static final String TAG = App.class.getSimpleName();
    private CustomerDisplayManager customerDisplayManager;

    @Override
    public void onCreate() {
        super.onCreate();
        customerDisplayManager = CustomerDisplayManagerImpl.newInstance(this, SocketConfigConstants.DEFAULT_SERVER_PORT);
    }

    public CustomerDisplayManager getCustomerDisplayManager() {
        return customerDisplayManager;
    }
}
