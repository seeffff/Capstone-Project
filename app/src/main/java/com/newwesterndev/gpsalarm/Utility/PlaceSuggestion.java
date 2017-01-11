package com.newwesterndev.gpsalarm.utility;

public class PlaceSuggestion {

    private String mName;
    private String mAddress;
    private double mLat;
    private double mLon;
    private String mIconUrl;

    public PlaceSuggestion(String name, String address, double lat, double lon, String icon){
        mName = name;
        mLat = lat;
        mLon = lon;
        mAddress = address;
        mIconUrl = icon;
    }

    public String getPlaceName(){
        return mName;
    }

    public String getAddress(){
        return mAddress;
    }

    public double getLat(){
        return mLat;
    }

    public double getLon(){
        return mLon;
    }

    public String getIconUrl(){
        return mIconUrl;
    }

}