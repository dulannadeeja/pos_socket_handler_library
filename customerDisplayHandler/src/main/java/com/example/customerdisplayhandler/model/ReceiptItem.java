package com.example.customerdisplayhandler.model;

import java.util.List;

public class ReceiptItem {
    private final String itemName;
    private final float itemQty;
    private final double itemPrice;
    private final int creditNote;
    private final int itemOrder;
    private final float creditNoteQty;
    private final float creditNoteValue;
    private final String crnId;
    private final String originalLineNo;
    private final String kotNote;
    private final String itemRemark;
    private final int uniqueId;
    private final List<Modifier> modifierList;
    private final List<ItemTax> itemTaxList;
    private final double dualItemPrice;
    private final List<ComboGroup> comboList;

    private ReceiptItem(Builder builder) {
        this.itemName = builder.itemName;
        this.itemQty = builder.itemQty;
        this.itemPrice = builder.itemPrice;
        this.creditNote = builder.creditNote;
        this.itemOrder = builder.itemOrder;
        this.creditNoteQty = builder.creditNoteQty;
        this.creditNoteValue = builder.creditNoteValue;
        this.crnId = builder.crnId;
        this.originalLineNo = builder.originalLineNo;
        this.kotNote = builder.kotNote;
        this.itemRemark = builder.itemRemark;
        this.uniqueId = builder.uniqueId;
        this.modifierList = builder.modifierList;
        this.itemTaxList = builder.itemTaxList;
        this.dualItemPrice = builder.dualItemPrice;
        this.comboList = builder.comboList;
    }

    // Getters
    public String getItemName() {
        return itemName;
    }

    public float getItemQty() {
        return itemQty;
    }

    public double getItemPrice() {
        return itemPrice;
    }

    public int getCreditNote() {
        return creditNote;
    }

    public int getItemOrder() {
        return itemOrder;
    }

    public float getCreditNoteQty() {
        return creditNoteQty;
    }

    public float getCreditNoteValue() {
        return creditNoteValue;
    }

    public String getCrnId() {
        return crnId;
    }

    public String getOriginalLineNo() {
        return originalLineNo;
    }

    public String getKotNote() {
        return kotNote;
    }

    public String getItemRemark() {
        return itemRemark;
    }

    public int getUniqueId() {
        return uniqueId;
    }

    public List<Modifier> getModifierList() {
        return modifierList;
    }

    public List<ItemTax> getItemTaxList() {
        return itemTaxList;
    }

    public double getDualItemPrice() {
        return dualItemPrice;
    }

    public List<ComboGroup> getComboList(){
        return comboList;
    }

    public static class Builder {
        private String itemName;
        private float itemQty;
        private double itemPrice;
        private int creditNote;
        private int itemOrder;
        private float creditNoteQty;
        private float creditNoteValue;
        private String crnId;
        private String originalLineNo;
        private String kotNote;
        private String itemRemark;
        private int uniqueId;
        private List<Modifier> modifierList;
        private List<ItemTax> itemTaxList;
        private double dualItemPrice;
        private List<ComboGroup> comboList;

        public Builder setItemName(String itemName) {
            this.itemName = itemName;
            return this;
        }

        public Builder setItemQty(float itemQty) {
            this.itemQty = itemQty;
            return this;
        }

        public Builder setItemPrice(double itemPrice) {
            this.itemPrice = itemPrice;
            return this;
        }

        public Builder setCreditNote(int creditNote) {
            this.creditNote = creditNote;
            return this;
        }

        public Builder setItemOrder(int itemOrder) {
            this.itemOrder = itemOrder;
            return this;
        }

        public Builder setCreditNoteQty(float creditNoteQty) {
            this.creditNoteQty = creditNoteQty;
            return this;
        }

        public Builder setCreditNoteValue(float creditNoteValue) {
            this.creditNoteValue = creditNoteValue;
            return this;
        }

        public Builder setCrnId(String crnId) {
            this.crnId = crnId;
            return this;
        }

        public Builder setOriginalLineNo(String originalLineNo) {
            this.originalLineNo = originalLineNo;
            return this;
        }

        public Builder setKotNote(String kotNote) {
            this.kotNote = kotNote;
            return this;
        }

        public Builder setItemRemark(String itemRemark) {
            this.itemRemark = itemRemark;
            return this;
        }

        public Builder setUniqueId(int uniqueId) {
            this.uniqueId = uniqueId;
            return this;
        }

        public Builder setModifierList(List<Modifier> modifierList) {
            this.modifierList = modifierList;
            return this;
        }

        public Builder setItemTaxList(List<ItemTax> itemTaxList) {
            this.itemTaxList = itemTaxList;
            return this;
        }

        public Builder setDualItemPrice(double dualItemPrice) {
            this.dualItemPrice = dualItemPrice;
            return this;
        }

        public Builder setComboList(List<ComboGroup> comboList){
            this.comboList = comboList;
            return this;
        }

        public ReceiptItem build() {
            return new ReceiptItem(this);
        }
    }
}
