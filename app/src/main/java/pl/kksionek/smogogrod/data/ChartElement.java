package pl.kksionek.smogogrod.data;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class ChartElement {
    @SerializedName("key")
    String mKey;

    @SerializedName("values")
    ArrayList<ArrayList<Double>> mChartValues;

    @SerializedName("color")
    String mColor;

    @SerializedName("unapproved")
    boolean mUnapproved;

    @SerializedName("unit")
    String mUnit;

    public String getKey() {
        return mKey;
    }

    public float getLastValue() {
        for (ArrayList<Double> value : mChartValues) {
            if (value.get(1) != null)
                return value.get(1).floatValue();
        }
        return 0;
    }

    public float getPreLastValue() {
        int notNullCount = 0;
        for (ArrayList<Double> value : mChartValues) {
            if (value.get(1) != null) {
                if (++notNullCount > 1)
                    return value.get(1).floatValue();
            }
        }
        return 0;
    }

    public long getLastTimestamp() {
        for (ArrayList<Double> value : mChartValues) {
            if (value.get(1) != null)
                return value.get(0).longValue();
        }
        return 0;
    }

    public int getChartValuesSize() {
        return mChartValues.size();
    }

    public ChartValue getChartValueAt(int index) {
        if (index > mChartValues.size())
            throw new IndexOutOfBoundsException("Chart value doesn't exist.");
        return new ChartValue(mChartValues.get(index));
    }

    public ArrayList<ArrayList<Double>> getChartValues() {
        return mChartValues;
    }

    public String getColor() {
        return mColor;
    }

    public void setColor(String color) {
        mColor = color;
    }

    public boolean isUnapproved() {
        return mUnapproved;
    }

    public String getUnit() {
        return mUnit;
    }
}
