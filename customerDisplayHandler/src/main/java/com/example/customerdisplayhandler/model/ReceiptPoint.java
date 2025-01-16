package com.example.customerdisplayhandler.model;

public class ReceiptPoint {
    private String mainInvoiceNumber;
    private String date;
    private String customerId;
    private Float receiptAmount;
    private Float addedPoint;
    private String loyaltyLevel;
    private Float earnPoint;
    private Float redeemPoint;

    // Private constructor to enforce the use of the builder
    private ReceiptPoint(Builder builder) {
        this.mainInvoiceNumber = builder.mainInvoiceNumber;
        this.date = builder.date;
        this.customerId = builder.customerId;
        this.receiptAmount = builder.receiptAmount;
        this.addedPoint = builder.addedPoint;
        this.loyaltyLevel = builder.loyaltyLevel;
        this.earnPoint = builder.earnPoint;
        this.redeemPoint = builder.redeemPoint;
    }

    // Getters
    public String getMainInvoiceNumber() {
        return mainInvoiceNumber;
    }

    public String getDate() {
        return date;
    }

    public String getCustomerId() {
        return customerId;
    }

    public Float getReceiptAmount() {
        return receiptAmount;
    }

    public Float getAddedPoint() {
        return addedPoint;
    }

    public String getLoyaltyLevel() {
        return loyaltyLevel;
    }

    public Float getEarnPoint() {
        return earnPoint;
    }

    public Float getRedeemPoint() {
        return redeemPoint;
    }

    // Builder Class
    public static class Builder {
        private String mainInvoiceNumber;
        private String date;
        private String customerId;
        private Float receiptAmount;
        private Float addedPoint;
        private String loyaltyLevel;
        private Float earnPoint;
        private Float redeemPoint;

        public Builder setMainInvoiceNumber(String mainInvoiceNumber) {
            this.mainInvoiceNumber = mainInvoiceNumber;
            return this;
        }

        public Builder setDate(String date) {
            this.date = date;
            return this;
        }

        public Builder setCustomerId(String customerId) {
            this.customerId = customerId;
            return this;
        }

        public Builder setReceiptAmount(Float receiptAmount) {
            this.receiptAmount = receiptAmount;
            return this;
        }

        public Builder setAddedPoint(Float addedPoint) {
            this.addedPoint = addedPoint;
            return this;
        }

        public Builder setLoyaltyLevel(String loyaltyLevel) {
            this.loyaltyLevel = loyaltyLevel;
            return this;
        }

        public Builder setEarnPoint(Float earnPoint) {
            this.earnPoint = earnPoint;
            return this;
        }

        public Builder setRedeemPoint(Float redeemPoint) {
            this.redeemPoint = redeemPoint;
            return this;
        }

        public ReceiptPoint build() {
            return new ReceiptPoint(this);
        }
    }
}
