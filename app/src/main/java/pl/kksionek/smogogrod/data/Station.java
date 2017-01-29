package pl.kksionek.smogogrod.data;

import com.google.gson.annotations.SerializedName;

public class Station {
    @SerializedName("stationId")
    private int mStationId;

    @SerializedName("stationName")
    private String mStationName;

    @SerializedName("aqIndex")
    private int mAqIndex;

    @SerializedName("values")
    private Values mValues;

    private boolean mNewData = true;

    public int getStationId() {
        return mStationId;
    }

    public String getStationName() {
        return mStationName;
    }

    public int getAqIndex() {
        return mAqIndex;
    }

    public boolean isNewData() {
        return mNewData;
    }

    public void markDataAsOld() {
        mNewData = false;
    }
}
