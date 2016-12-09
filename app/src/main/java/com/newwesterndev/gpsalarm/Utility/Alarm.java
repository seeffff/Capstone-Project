package com.newwesterndev.gpsalarm.Utility;

public class Alarm {
    private String mDestination;
    private int mIsActive;
    private int mVolume;
    private int mVibrate;
    private double mLon;
    private double mLat;
    private double mRange;
    private String mRangeType;
    private long mId;

    public Alarm(String destination, int active, int volume, int vibrate, double lon, double lat, double range, String rangeType){
        mDestination = destination;
        mIsActive = active;
        mVolume = volume;
        mVibrate = vibrate;
        mLon = lon;
        mLat = lat;
        mRange = range;
        mRangeType = rangeType;
    }

    public Alarm(String destination, int active, int volume, int vibrate, double lon, double lat, double range, String rangeType,long id){
        mDestination = destination;
        mIsActive = active;
        mVolume = volume;
        mVibrate = vibrate;
        mLon = lon;
        mLat = lat;
        mRange = range;
        mRangeType = rangeType;
        mId = id;
    }

    public String getDestination(){
        return mDestination;
    }

    public int getIsActive(){
        return mIsActive;
    }

    public int getVolume(){
        return mVolume;
    }

    public int getVibrate(){
        return mVibrate;
    }

    public double getLon(){
        return mLon;
    }

    public double getLat(){
        return mLat;
    }

    public double getRange(){
        return mRange;
    }

    public String getRangeType(){
        return mRangeType;
    }

    public long getId(){
        return mId;
    }
}
