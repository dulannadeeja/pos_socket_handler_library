package com.example.customerdisplayhandler.model;

public class Customer {
    private final String customerId;
    private final String customerFirstName;
    private final String customerLastName;
    private final String phone;
    private final String email;
    private final Float loyaltyPoint;
    private final Float totalLoyaltyPoint;
    private final Float customerOutstanding;
    private final Float loyaltyStatus;
    private final String creditLimit;
    private final String customerCode;

    private Customer(Builder builder) {
        this.customerId = builder.customerId;
        this.customerFirstName = builder.customerFirstName;
        this.customerLastName = builder.customerLastName;
        this.phone = builder.phone;
        this.email = builder.email;
        this.loyaltyPoint = builder.loyaltyPoint;
        this.totalLoyaltyPoint = builder.totalLoyaltyPoint;
        this.customerOutstanding = builder.customerOutstanding;
        this.loyaltyStatus = builder.loyaltyStatus;
        this.creditLimit = builder.creditLimit;
        this.customerCode = builder.customerCode;
    }

    public String getCustomerId() {
        return customerId;
    }

    public String getCustomerFirstName() {
        return customerFirstName;
    }

    public String getCustomerLastName() {
        return customerLastName;
    }

    public String getPhone() {
        return phone;
    }

    public String getEmail() {
        return email;
    }

    public Float getLoyaltyPoint() {
        return loyaltyPoint;
    }

    public Float getTotalLoyaltyPoint() {
        return totalLoyaltyPoint;
    }

    public Float getCustomerOutstanding() {
        return customerOutstanding;
    }

    public Float getLoyaltyStatus() {
        return loyaltyStatus;
    }

    public String getCreditLimit() {
        return creditLimit;
    }

    public String getCustomerCode() {
        return customerCode;
    }

    public static class Builder {
        private String customerId;
        private String customerFirstName;
        private String customerLastName;
        private String phone;
        private String email;
        private Float loyaltyPoint;
        private Float totalLoyaltyPoint;
        private Float customerOutstanding;
        private Float loyaltyStatus;
        private String creditLimit;
        private String customerCode;

        public Builder setCustomerId(String customerId) {
            this.customerId = customerId;
            return this;
        }

        public Builder setCustomerFirstName(String customerFirstName) {
            this.customerFirstName = customerFirstName;
            return this;
        }

        public Builder setCustomerLastName(String customerLastName) {
            this.customerLastName = customerLastName;
            return this;
        }

        public Builder setPhone(String phone) {
            this.phone = phone;
            return this;
        }

        public Builder setEmail(String email) {
            this.email = email;
            return this;
        }

        public Builder setLoyaltyPoint(Float loyaltyPoint) {
            this.loyaltyPoint = loyaltyPoint;
            return this;
        }

        public Builder setTotalLoyaltyPoint(Float totalLoyaltyPoint) {
            this.totalLoyaltyPoint = totalLoyaltyPoint;
            return this;
        }

        public Builder setCustomerOutstanding(Float customerOutstanding) {
            this.customerOutstanding = customerOutstanding;
            return this;
        }

        public Builder setLoyaltyStatus(Float loyaltyStatus) {
            this.loyaltyStatus = loyaltyStatus;
            return this;
        }

        public Builder setCreditLimit(String creditLimit) {
            this.creditLimit = creditLimit;
            return this;
        }

        public Builder setCustomerCode(String customerCode) {
            this.customerCode = customerCode;
            return this;
        }

        public Customer build() {
            return new Customer(this);
        }
    }
}
