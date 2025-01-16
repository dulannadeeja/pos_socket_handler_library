package com.example.customerdisplayhandler.model;

public class Modifier {
    private final int id;
    private final int isSelect;
    private final String code;
    private final String name;
    private final float cost;
    private final float price;
    private final float qty;
    private final int status;
    private final int isBackup;
    private final String uniqueID;

    private Modifier(Builder builder) {
        this.id = builder.id;
        this.isSelect = builder.isSelect;
        this.code = builder.code;
        this.name = builder.name;
        this.cost = builder.cost;
        this.price = builder.price;
        this.qty = builder.qty;
        this.status = builder.status;
        this.isBackup = builder.isBackup;
        this.uniqueID = builder.uniqueID;
    }

    public int getId() {
        return id;
    }

    public int getIsSelect() {
        return isSelect;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public float getCost() {
        return cost;
    }

    public float getPrice() {
        return price;
    }

    public float getQty() {
        return qty;
    }

    public int getStatus() {
        return status;
    }

    public int getIsBackup() {
        return isBackup;
    }

    public String getUniqueID() {
        return uniqueID;
    }

    public static class Builder {
        private int id;
        private int isSelect;
        private String code;
        private String name;
        private float cost;
        private float price;
        private float qty;
        private int status;
        private int isBackup;
        private String uniqueID;

        public Builder setId(int id) {
            this.id = id;
            return this;
        }

        public Builder setIsSelect(int isSelect) {
            this.isSelect = isSelect;
            return this;
        }

        public Builder setCode(String code) {
            this.code = code;
            return this;
        }

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public Builder setCost(float cost) {
            this.cost = cost;
            return this;
        }

        public Builder setPrice(float price) {
            this.price = price;
            return this;
        }

        public Builder setQty(float qty) {
            this.qty = qty;
            return this;
        }

        public Builder setStatus(int status) {
            this.status = status;
            return this;
        }

        public Builder setIsBackup(int isBackup) {
            this.isBackup = isBackup;
            return this;
        }

        public Builder setUniqueID(String uniqueID) {
            this.uniqueID = uniqueID;
            return this;
        }

        public Modifier build() {
            return new Modifier(this);
        }
    }
}
