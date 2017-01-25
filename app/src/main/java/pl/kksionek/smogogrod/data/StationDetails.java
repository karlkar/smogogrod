package pl.kksionek.smogogrod.data;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class StationDetails {
    @SerializedName("isError")
    private boolean mIsError;

    @SerializedName("errorMessage")
    private String mErrorMessage;

    @SerializedName("chartElements")
    private ArrayList<ChartElement> mChartElements;

    public boolean isError() {
        return mIsError;
    }

    public String getErrorMessage() {
        return mErrorMessage;
    }

    public ArrayList<ChartElement> getChartElements() {
        return mChartElements;
    }

    public long getLastTimestamp() {
        return mChartElements.get(0).getLastTimestamp();
    }
}
