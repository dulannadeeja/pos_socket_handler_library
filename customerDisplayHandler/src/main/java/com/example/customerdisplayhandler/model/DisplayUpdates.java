package com.example.customerdisplayhandler.model;

import java.util.List;

public class DisplayUpdates {
    private int receiptStatus;
    private String customerDisplayHeader;
    private String customerDisplayBg;
    private String customerDisplayLogo;
    private char decimalSeparator;
    private char thousandSeparator;
    private int decimalPlaces;
    private String language;
    private String receiptName;
    private String createdBY;
    private String mainInvoiceNumber;
    private String dateTime;
    private String billNote;
    private String cashier;
    private String orderType;
    private double subTotal;
    private double discount;
    private double tax;
    private double receiptTotal;
    private float numberOfItem;
    private int numberOfQty;
    private double receiptPaidValue;
    private double balance;
    private Customer customer;
    private Terminal terminal;
    private ReceiptPoint receiptPoint;
    private List<ReceiptItem> receiptItemList;
    private String defaultCartHeader;
    private String dualPricingCartHeader;
    private String customerDisplayQR;
    private int dualPricingStatus;
    private boolean dualPricingCardEnable;
    private boolean dualPricingCashEnable;
    private double dualPricingSubTotal;
    private double dualPricingDiscount;
    private double dualPricingTax;
    private double dualPricingReceiptTotal;

    public DisplayUpdates() {

    }

    private DisplayUpdates(Builder builder) {
        this.receiptStatus = builder.receiptStatus;
        this.customerDisplayHeader = builder.customerDisplayHeader;
        this.customerDisplayBg = builder.customerDisplayBg;
        this.customerDisplayLogo = builder.customerDisplayLogo;
        this.decimalSeparator = builder.decimalSeparator;
        this.decimalPlaces = builder.decimalPlaces;
        this.thousandSeparator = builder.thousandSeparator;
        this.language = builder.language;
        this.receiptName = builder.receiptName;
        this.createdBY = builder.createdBY;
        this.mainInvoiceNumber = builder.mainInvoiceNumber;
        this.dateTime = builder.dateTime;
        this.billNote = builder.billNote;
        this.cashier = builder.cashier;
        this.orderType = builder.orderType;
        this.subTotal = builder.subTotal;
        this.discount = builder.discount;
        this.tax = builder.tax;
        this.receiptTotal = builder.receiptTotal;
        this.numberOfItem = builder.numberOfItem;
        this.numberOfQty = builder.numberOfQty;
        this.receiptPaidValue = builder.receiptPaidValue;
        this.balance = builder.balance;
        this.customer = builder.customer;
        this.terminal = builder.terminal;
        this.receiptPoint = builder.receiptPoint;
        this.receiptItemList = builder.receiptItemList;
        this.defaultCartHeader = builder.defaultCartHeader;
        this.dualPricingCartHeader = builder.dualPricingCartHeader;
        this.customerDisplayQR = builder.customerDisplayQR;
        this.dualPricingStatus = builder.dualPricingStatus;
        this.dualPricingCardEnable = builder.dualPricingCardEnable;
        this.dualPricingCashEnable = builder.dualPricingCashEnable;
        this.dualPricingSubTotal = builder.dualPricingSubTotal;
        this.dualPricingDiscount = builder.dualPricingDiscount;
        this.dualPricingTax = builder.dualPricingTax;
        this.dualPricingReceiptTotal = builder.dualPricingReceiptTotal;
    }

    public static class Builder {
        private int receiptStatus;
        private String customerDisplayHeader;
        private String customerDisplayBg;
        private String customerDisplayLogo;
        private char decimalSeparator;
        private int decimalPlaces;
        private char thousandSeparator;
        private String language;
        private String receiptName;
        private String createdBY;
        private String mainInvoiceNumber;
        private String dateTime;
        private String billNote;
        private String cashier;
        private String orderType;
        private double subTotal;
        private double discount;
        private double tax;
        private double receiptTotal;
        private float numberOfItem;
        private int numberOfQty;
        private double receiptPaidValue;
        private double balance;
        private Customer customer;
        private Terminal terminal;
        private ReceiptPoint receiptPoint;
        private List<ReceiptItem> receiptItemList;
        private String defaultCartHeader;
        private String dualPricingCartHeader;
        private String customerDisplayQR;
        private int dualPricingStatus;
        private boolean dualPricingCardEnable;
        private boolean dualPricingCashEnable;
        private double dualPricingSubTotal;
        private double dualPricingDiscount;
        private double dualPricingTax;
        private double dualPricingReceiptTotal;

        public Builder setReceiptStatus(int receiptStatus) {
            this.receiptStatus = receiptStatus;
            return this;
        }

        public Builder setCustomerDisplayHeader(String customerDisplayHeader) {
            this.customerDisplayHeader = customerDisplayHeader;
            return this;
        }

        public Builder setCustomerDisplayBg(String customerDisplayBg) {
            this.customerDisplayBg = customerDisplayBg;
            return this;
        }

        public Builder setCustomerDisplayLogo(String customerDisplayLogo) {
            this.customerDisplayLogo = customerDisplayLogo;
            return this;
        }

        public Builder setDecimalSeparator(char decimalSeparator) {
            this.decimalSeparator = decimalSeparator;
            return this;
        }

        public Builder setDecimalPlaces(int decimalPlaces) {
            this.decimalPlaces = decimalPlaces;
            return this;
        }

        public Builder setThousandSeparator(char thousandSeparator) {
            this.thousandSeparator = thousandSeparator;
            return this;
        }

        public Builder setLanguage(String language) {
            this.language = language;
            return this;
        }

        public Builder setReceiptName(String receiptName) {
            this.receiptName = receiptName;
            return this;
        }

        public Builder setCreatedBY(String createdBY) {
            this.createdBY = createdBY;
            return this;
        }

        public Builder setMainInvoiceNumber(String mainInvoiceNumber) {
            this.mainInvoiceNumber = mainInvoiceNumber;
            return this;
        }

        public Builder setDateTime(String dateTime) {
            this.dateTime = dateTime;
            return this;
        }

        public Builder setBillNote(String billNote) {
            this.billNote = billNote;
            return this;
        }

        public Builder setCashier(String cashier) {
            this.cashier = cashier;
            return this;
        }

        public Builder setOrderType(String orderType) {
            this.orderType = orderType;
            return this;
        }

        public Builder setSubTotal(double subTotal) {
            this.subTotal = subTotal;
            return this;
        }

        public Builder setDiscount(double discount) {
            this.discount = discount;
            return this;
        }

        public Builder setTax(double tax) {
            this.tax = tax;
            return this;
        }

        public Builder setReceiptTotal(double receiptTotal) {
            this.receiptTotal = receiptTotal;
            return this;
        }

        public Builder setNumberOfItem(float numberOfItem) {
            this.numberOfItem = numberOfItem;
            return this;
        }

        public Builder setNumberOfQty(int numberOfQty) {
            this.numberOfQty = numberOfQty;
            return this;
        }

        public Builder setReceiptPaidValue(double receiptPaidValue) {
            this.receiptPaidValue = receiptPaidValue;
            return this;
        }

        public Builder setBalance(double balance) {
            this.balance = balance;
            return this;
        }

        public Builder setCustomer(Customer customer) {
            this.customer = customer;
            return this;
        }

        public Builder setTerminal(Terminal terminal) {
            this.terminal = terminal;
            return this;
        }

        public Builder setReceiptPoint(ReceiptPoint receiptPoint) {
            this.receiptPoint = receiptPoint;
            return this;
        }

        public Builder setReceiptItemList(List<ReceiptItem> receiptItemList) {
            this.receiptItemList = receiptItemList;
            return this;
        }

        public Builder setDefaultCartHeader(String defaultCartHeader) {
            this.defaultCartHeader = defaultCartHeader;
            return this;
        }

        public Builder setDualPricingCartHeader(String dualPricingCartHeader) {
            this.dualPricingCartHeader = dualPricingCartHeader;
            return this;
        }

        public Builder setCustomerDisplayQR(String customerDisplayQR) {
            this.customerDisplayQR = customerDisplayQR;
            return this;
        }

        public Builder setDualPricingStatus(int dualPricingStatus) {
            this.dualPricingStatus = dualPricingStatus;
            return this;
        }

        public Builder setDualPricingCardEnable(boolean dualPricingCardEnable) {
            this.dualPricingCardEnable = dualPricingCardEnable;
            return this;
        }

        public Builder setDualPricingCashEnable(boolean dualPricingCashEnable) {
            this.dualPricingCashEnable = dualPricingCashEnable;
            return this;
        }

        public Builder setDualPricingSubTotal(double dualPricingSubTotal) {
            this.dualPricingSubTotal = dualPricingSubTotal;
            return this;
        }

        public Builder setDualPricingDiscount(double dualPricingDiscount) {
            this.dualPricingDiscount = dualPricingDiscount;
            return this;
        }

        public Builder setDualPricingTax(double dualPricingTax) {
            this.dualPricingTax = dualPricingTax;
            return this;
        }

        public Builder setDualPricingReceiptTotal(double dualPricingReceiptTotal) {
            this.dualPricingReceiptTotal = dualPricingReceiptTotal;
            return this;
        }

        public DisplayUpdates build() {
            return new DisplayUpdates(this);
        }
    }

    // Getters and Setters for serializing and deserializing the object

    public int getReceiptStatus() {
        return receiptStatus;
    }

    public String getCustomerDisplayHeader() {
        return customerDisplayHeader;
    }

    public String getCustomerDisplayBg() {
        return customerDisplayBg;
    }

    public String getCustomerDisplayLogo() {
        return customerDisplayLogo;
    }

    public char getDecimalSeparator() {
        return decimalSeparator;
    }

    public int getDecimalPlaces() {
        return decimalPlaces;
    }

    public char getThousandSeparator() {
        return thousandSeparator;
    }

    public String getLanguage() {
        return language;
    }

    public String getReceiptName() {
        return receiptName;
    }

    public String getCreatedBY() {
        return createdBY;
    }

    public String getMainInvoiceNumber() {
        return mainInvoiceNumber;
    }

    public String getDateTime() {
        return dateTime;
    }

    public String getBillNote() {
        return billNote;
    }

    public String getCashier() {
        return cashier;
    }

    public String getOrderType() {
        return orderType;
    }

    public double getSubTotal() {
        return subTotal;
    }

    public double getDiscount() {
        return discount;
    }

    public double getTax() {
        return tax;
    }

    public double getReceiptTotal() {
        return receiptTotal;
    }

    public float getNumberOfItem() {
        return numberOfItem;
    }

    public int getNumberOfQty() {
        return numberOfQty;
    }

    public double getReceiptPaidValue() {
        return receiptPaidValue;
    }

    public double getBalance() {
        return balance;
    }

    public Customer getCustomer() {
        return customer;
    }

    public Terminal getTerminal() {
        return terminal;
    }

    public ReceiptPoint getReceiptPoint() {
        return receiptPoint;
    }

    public List<ReceiptItem> getReceiptItemList() {
        return receiptItemList;
    }

    public String getDefaultCartHeader() {
        return defaultCartHeader;
    }

    public String getDualPricingCartHeader() {
        return dualPricingCartHeader;
    }

    public String getCustomerDisplayQR() {
        return customerDisplayQR;
    }

    public int getDualPricingStatus() {
        return dualPricingStatus;
    }

    public boolean isDualPricingCardEnable() {
        return dualPricingCardEnable;
    }

    public boolean isDualPricingCashEnable() {
        return dualPricingCashEnable;
    }

    public double getDualPricingSubTotal() {
        return dualPricingSubTotal;
    }

    public double getDualPricingDiscount() {
        return dualPricingDiscount;
    }

    public double getDualPricingTax() {
        return dualPricingTax;
    }

    public double getDualPricingReceiptTotal() {
        return dualPricingReceiptTotal;
    }

    public void setReceiptStatus(int receiptStatus) {
        this.receiptStatus = receiptStatus;
    }

    public void setCustomerDisplayHeader(String customerDisplayHeader) {
        this.customerDisplayHeader = customerDisplayHeader;
    }

    public void setCustomerDisplayBg(String customerDisplayBg) {
        this.customerDisplayBg = customerDisplayBg;
    }

    public void setCustomerDisplayLogo(String customerDisplayLogo) {
        this.customerDisplayLogo = customerDisplayLogo;
    }

    public void setDecimalSeparator(char decimalSeparator) {
        this.decimalSeparator = decimalSeparator;
    }

    public void setDecimalPlaces(int decimalPlaces) {
        this.decimalPlaces = decimalPlaces;
    }

    public void setThousandSeparator(char thousandSeparator) {
        this.thousandSeparator = thousandSeparator;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public void setReceiptName(String receiptName) {
        this.receiptName = receiptName;
    }

    public void setCreatedBY(String createdBY) {
        this.createdBY = createdBY;
    }

    public void setMainInvoiceNumber(String mainInvoiceNumber) {
        this.mainInvoiceNumber = mainInvoiceNumber;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public void setBillNote(String billNote) {
        this.billNote = billNote;
    }

    public void setCashier(String cashier) {
        this.cashier = cashier;
    }

    public void setOrderType(String orderType) {
        this.orderType = orderType;
    }

    public void setSubTotal(double subTotal) {
        this.subTotal = subTotal;
    }

    public void setDiscount(double discount) {
        this.discount = discount;
    }

    public void setTax(double tax) {
        this.tax = tax;
    }

    public void setReceiptTotal(double receiptTotal) {
        this.receiptTotal = receiptTotal;
    }

    public void setNumberOfItem(float numberOfItem) {
        this.numberOfItem = numberOfItem;
    }

    public void setNumberOfQty(int numberOfQty) {
        this.numberOfQty = numberOfQty;
    }

    public void setReceiptPaidValue(double receiptPaidValue) {
        this.receiptPaidValue = receiptPaidValue;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public void setTerminal(Terminal terminal) {
        this.terminal = terminal;
    }

    public void setReceiptPoint(ReceiptPoint receiptPoint) {
        this.receiptPoint = receiptPoint;
    }

    public void setReceiptItemList(List<ReceiptItem> receiptItemList) {
        this.receiptItemList = receiptItemList;
    }

    public void setDefaultCartHeader(String defaultCartHeader) {
        this.defaultCartHeader = defaultCartHeader;
    }

    public void setDualPricingCartHeader(String dualPricingCartHeader) {
        this.dualPricingCartHeader = dualPricingCartHeader;
    }

    public void setCustomerDisplayQR(String customerDisplayQR) {
        this.customerDisplayQR = customerDisplayQR;
    }

    public void setDualPricingStatus(int dualPricingStatus) {
        this.dualPricingStatus = dualPricingStatus;
    }

    public void setDualPricingCardEnable(boolean dualPricingCardEnable) {
        this.dualPricingCardEnable = dualPricingCardEnable;
    }

    public void setDualPricingCashEnable(boolean dualPricingCashEnable) {
        this.dualPricingCashEnable = dualPricingCashEnable;
    }

    public void setDualPricingSubTotal(double dualPricingSubTotal) {
        this.dualPricingSubTotal = dualPricingSubTotal;
    }

    public void setDualPricingDiscount(double dualPricingDiscount) {
        this.dualPricingDiscount = dualPricingDiscount;
    }

    public void setDualPricingTax(double dualPricingTax) {
        this.dualPricingTax = dualPricingTax;
    }

    public void setDualPricingReceiptTotal(double dualPricingReceiptTotal) {
        this.dualPricingReceiptTotal = dualPricingReceiptTotal;
    }
}
