package com.example.customerdisplayhandler.model;

public class CustomerDisplay {
    private String customerDisplayID;
    private String customerDisplayName;
    private String customerDisplayIpAddress;
    private Boolean isActivated;

    public CustomerDisplay(String customerDisplayID, String customerDisplayName, String customerDisplayIpAddress, Boolean isActivated) {
        this.customerDisplayID = customerDisplayID;
        this.customerDisplayName = customerDisplayName;
        this.customerDisplayIpAddress = customerDisplayIpAddress;
        this.isActivated = isActivated;
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
}