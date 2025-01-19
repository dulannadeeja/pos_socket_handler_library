package com.example.customerdisplayhandler.model;

import java.io.Serializable;

public class CustomerDisplay implements Serializable {
    private String customerDisplayID;
    private String customerDisplayName;
    private String customerDisplayIpAddress;
    private Boolean isDarkModeActivated;
    private Boolean isActivated;

    public CustomerDisplay(String customerDisplayID, String customerDisplayName, String customerDisplayIpAddress, Boolean isActivated, Boolean isDarkModeActivated) {
        this.customerDisplayID = customerDisplayID;
        this.customerDisplayName = customerDisplayName;
        this.customerDisplayIpAddress = customerDisplayIpAddress;
        this.isActivated = isActivated;
        this.isDarkModeActivated = isDarkModeActivated;
    }

    public String getCustomerDisplayID() {
        return customerDisplayID;
    }

    public String getCustomerDisplayName() {
        return customerDisplayName;
    }

    public String getCustomerDisplayIpAddress() {
        return customerDisplayIpAddress;
    }

    public Boolean getIsActivated() {
        return isActivated;
    }

    public Boolean getIsDarkModeActivated() {
        return isDarkModeActivated;
    }
}