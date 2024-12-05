package com.example.customerdisplayhandler.api;

import com.example.customerdisplayhandler.ui.CustomerDisplaySettingsChildFragment;

public class CustomerDisplayManager {
    private static CustomerDisplayManager INSTANCE;
    public static CustomerDisplayManager newInstance() {
        if (INSTANCE == null) {
            INSTANCE = new CustomerDisplayManager();
        }
        return INSTANCE;
    }

    private CustomerDisplayManager() {
    }

    public CustomerDisplaySettingsChildFragment getCustomerDisplaySettingsChildFragment() {
        return new CustomerDisplaySettingsChildFragment();
    }
}
