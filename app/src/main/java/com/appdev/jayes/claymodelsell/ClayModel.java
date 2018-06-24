package com.appdev.jayes.claymodelsell;

public class ClayModel {
    private String modelName;
    private String modelPrice;
    private String key;

    public ClayModel(String key, String modelName, String modelPrice) {
        this.key = key;
        this.modelName = modelName;
        this.modelPrice = modelPrice;
    }

    public ClayModel(String modelName, String modelPrice) {
        this.modelName = modelName;
        this.modelPrice = modelPrice;
    }

    public ClayModel() {
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public String getModelPrice() {
        return modelPrice;
    }

    public void setModelPrice(String modelPrice) {
        this.modelPrice = modelPrice;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}
