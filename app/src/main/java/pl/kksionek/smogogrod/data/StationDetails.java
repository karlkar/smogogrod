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

    public void setError(boolean error) {
        mIsError = error;
    }

    public String getErrorMessage() {
        return mErrorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        mErrorMessage = errorMessage;
    }

    public ArrayList<ChartElement> getChartElements() {
        return mChartElements;
    }

    public void setChartElements(ArrayList<ChartElement> chartElements) {
        mChartElements = chartElements;
    }
}
