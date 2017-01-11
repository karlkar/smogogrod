package pl.kksionek.smogogrod.data;

import java.util.ArrayList;

public class ChartValue {
    private long mTimestamp;
    private Double mValue;

    public ChartValue(ArrayList<Double> value) {
        mTimestamp = value.get(0).longValue();
        mValue = value.get(1);
    }

    public long getTimestamp() {
        return mTimestamp;
    }

    public void setTimestamp(long timestamp) {
        mTimestamp = timestamp;
    }

    public Double getValue() {
        return mValue;
    }

    public void setValue(Double value) {
        mValue = value;
    }
}
