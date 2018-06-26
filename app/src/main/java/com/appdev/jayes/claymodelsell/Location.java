package com.appdev.jayes.claymodelsell;

public class Location {
    private String locationName;
    private String guid;

    public Location(String guid, String locationName) {
        this.guid = guid;
        this.locationName = locationName;
    }

    public Location(String locationName) {
        this.locationName = locationName;
    }

    public Location() {

    }

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }


}
