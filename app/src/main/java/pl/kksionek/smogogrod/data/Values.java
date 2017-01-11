package pl.kksionek.smogogrod.data;

import com.google.gson.annotations.SerializedName;

//":{"PM10":72.7009,"SO2":9.59907,"NO2":22.9599,"CO":0.60759,"PM2.5":49.4451,"O3":59.0839}}
public class Values {
    @SerializedName("PM10")
    private float mPm10;

    @SerializedName("SO2")
    private float mSo2;

    @SerializedName("NO2")
    private float mNo2;

    @SerializedName("CO")
    private float mCo;

    @SerializedName("PM2.5")
    private float mPm25;

    @SerializedName("O3")
    private float mO3;

    public float getPm10() {
        return mPm10;
    }

    public void setPm10(float pm10) {
        mPm10 = pm10;
    }

    public float getSo2() {
        return mSo2;
    }

    public void setSo2(float so2) {
        mSo2 = so2;
    }

    public float getNo2() {
        return mNo2;
    }

    public void setNo2(float no2) {
        mNo2 = no2;
    }

    public float getCo() {
        return mCo;
    }

    public void setCo(float co) {
        mCo = co;
    }

    public float getPm25() {
        return mPm25;
    }

    public void setPm25(float pm25) {
        mPm25 = pm25;
    }

    public float getO3() {
        return mO3;
    }

    public void setO3(float o3) {
        mO3 = o3;
    }
}
