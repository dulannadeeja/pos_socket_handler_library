package com.example.customerdisplayhandler.model;

public class ItemTax {
    private final int taxMapTableId;
    private final String taxCode;
    private final String taxName;
    private final String productCode;
    private final int taxMode;
    private final int isVat;
    private final int isAttached;
    private final float taxPercentage;
    private final String taxValueType;

    private ItemTax(Builder builder) {
        this.taxMapTableId = builder.taxMapTableId;
        this.taxCode = builder.taxCode;
        this.taxName = builder.taxName;
        this.productCode = builder.productCode;
        this.taxMode = builder.taxMode;
        this.isVat = builder.isVat;
        this.isAttached = builder.isAttached;
        this.taxPercentage = builder.taxPercentage;
        this.taxValueType = builder.taxValueType;
    }

    public int getTaxMapTableId() {
        return taxMapTableId;
    }

    public String getTaxCode() {
        return taxCode;
    }

    public String getTaxName() {
        return taxName;
    }

    public String getProductCode() {
        return productCode;
    }

    public int getTaxMode() {
        return taxMode;
    }

    public int getIsVat() {
        return isVat;
    }

    public int getIsAttached() {
        return isAttached;
    }

    public float getTaxPercentage() {
        return taxPercentage;
    }

    public String getTaxValueType() {
        return taxValueType;
    }

    public static class Builder {
        private int taxMapTableId;
        private String taxCode;
        private String taxName;
        private String productCode;
        private int taxMode;
        private int isVat;
        private int isAttached;
        private float taxPercentage;
        private String taxValueType;

        public Builder setTaxMapTableId(int taxMapTableId) {
            this.taxMapTableId = taxMapTableId;
            return this;
        }

        public Builder setTaxCode(String taxCode) {
            this.taxCode = taxCode;
            return this;
        }

        public Builder setTaxName(String taxName) {
            this.taxName = taxName;
            return this;
        }

        public Builder setProductCode(String productCode) {
            this.productCode = productCode;
            return this;
        }

        public Builder setTaxMode(int taxMode) {
            this.taxMode = taxMode;
            return this;
        }

        public Builder setIsVat(int isVat) {
            this.isVat = isVat;
            return this;
        }

        public Builder setIsAttached(int isAttached) {
            this.isAttached = isAttached;
            return this;
        }

        public Builder setTaxPercentage(float taxPercentage) {
            this.taxPercentage = taxPercentage;
            return this;
        }

        public Builder setTaxValueType(String taxValueType) {
            this.taxValueType = taxValueType;
            return this;
        }

        public ItemTax build() {
            return new ItemTax(this);
        }
    }
}
