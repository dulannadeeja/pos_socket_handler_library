package com.example.customerdisplayhandler.model;

public class CustomerDisplay {
    private String customerDisplayID;
    private String customerDisplayName;
    private String customerDisplayIpAddress;

    public CustomerDisplay(String customerDisplayID, String customerDisplayName, String customerDisplayIpAddress) {
        this.customerDisplayID = customerDisplayID;
        this.customerDisplayName = customerDisplayName;
        this.customerDisplayIpAddress = customerDisplayIpAddress;
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
}