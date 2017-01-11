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

    public int getStationId() {
        return mStationId;
    }

    public void setStationId(int stationId) {
        mStationId = stationId;
    }

    public String getStationName() {
        return mStationName;
    }

    public void setStationName(String stationName) {
        mStationName = stationName;
    }

    public int getAqIndex() {
        return mAqIndex;
    }

    public void setAqIndex(int aqIndex) {
        mAqIndex = aqIndex;
    }

    public Values getValues() {
        return mValues;
    }

    public void setValues(Values values) {
        mValues = values;
    }
}
