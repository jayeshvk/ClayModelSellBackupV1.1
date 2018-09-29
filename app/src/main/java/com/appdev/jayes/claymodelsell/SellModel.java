package com.appdev.jayes.claymodelsell;

public class SellModel {
    private String key;
    private String receiptNo;
    private String date;

    private String name;
    private String mobile;
    private String city;
    private String comments;

    private String price;
    private String advance;
    private String balance;
    private String modelName;
    private String location;
    private String settled;

    public SellModel() {
    }

    public SellModel(String receiptNo,
                     String date,

                     String name,
                     String mobile,
                     String city,
                     String comments,

                     String price,
                     String advance,
                     String balance,
                     String modelName,
                     String location,
                     String settled

    ) {
        this.receiptNo = receiptNo;
        this.date = date;

        this.name = name;
        this.mobile = mobile;
        this.city = city;
        this.comments = comments;

        this.price = price;
        this.advance = advance;
        this.balance = balance;
        this.modelName = modelName;
        this.location = location;
        this.settled = settled;
    }

    public SellModel(String key,
                     String receiptNo,
                     String date,

                     String name,
                     String mobile,
                     String city,
                     String comments,

                     String price,
                     String advance,
                     String balance,
                     String modelName,
                     String location,
                     String settled

    ) {
        this.key = key;
        this.receiptNo = receiptNo;
        this.date = date;

        this.name = name;
        this.mobile = mobile;
        this.city = city;
        this.comments = comments;

        this.price = price;
        this.advance = advance;
        this.balance = balance;
        this.modelName = modelName;
        this.location = location;
        this.settled = settled;

    }

    public String getSettled() {
        return settled;
    }

    public void setSettled(String settled) {
        this.settled = settled;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getReceiptNo() {
        return receiptNo;
    }

    public void setReceiptNo(String receiptNo) {
        this.receiptNo = receiptNo;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getAdvance() {
        return advance;
    }

    public void setAdvance(String advance) {
        this.advance = advance;
    }

    public String getBalance() {
        return balance;
    }

    public void setBalance(String balance) {
        this.balance = balance;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

}
