package pl.kksionek.smogogrod.data;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class StationDetails {
    @SerializedName("isError")
    boolean mIsError;

    @SerializedName("errorMessage")
    String mErrorMessage;

    @SerializedName("chartElements")
    ArrayList<ChartElement> mChartElements;

    public boolean isError() {
        return mIsError;
    }

    public String getErrorMessage() {
        return mErrorMessage;
    }

    public ArrayList<ChartElement> getChartElements() {
        return mChartElements;
    }
}
