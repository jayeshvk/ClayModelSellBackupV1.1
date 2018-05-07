package com.appdev.jayes.claymodelsell;

public class ClayModel {
    private String modelName;
    private String modelPrice;
    private String guid;

    public ClayModel(String guid, String modelName, String modelPrice) {
        this.guid = guid;
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

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }
}
