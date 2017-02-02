package pl.kksionek.smogogrod.model;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.support.annotation.DrawableRes;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.Pair;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import pl.kksionek.smogogrod.R;
import pl.kksionek.smogogrod.data.ChartElement;
import pl.kksionek.smogogrod.data.Station;
import pl.kksionek.smogogrod.data.StationDetails;

public class StatusAdapter extends RecyclerView.Adapter<StatusViewHolder> {

    private static final String TAG = "StatusAdapter";

    private static final float sPM10Limit = 50.0f;
    private static final float sPM25Limit = 25.0f;
    private static final float sNO2Limit = 200.0f;
    private static final float sSO2Limit = 125.0f;
    private static final float sCOLimit = 10000.0f;
    private static final float sO3Limit = 120.0f;
    private static final float sC6H6Limit = 5.0f;

    private static final int sCardBackgroundColor0 = Color.rgb(123, 217, 41);
    private static final int sCardBackgroundColor1 = Color.rgb(195, 234, 54);
    private static final int sCardBackgroundColor2 = Color.rgb(255, 230, 99);
    private static final int sCardBackgroundColor3 = Color.rgb(244, 153, 36);
    private static final int sCardBackgroundColor4 = Color.rgb(255, 93, 93);
    private static final int sCardBackgroundColor5 = Color.rgb(206, 88, 88);

    private static final SimpleDateFormat sDateFormatter = new SimpleDateFormat(
            "dd-MM-yyyy HH:mm",
            Locale.getDefault());
    private static final int ANIM_DURATION = 1000;

    private final ArrayList<Integer> mIdentifiers = new ArrayList<>();
    private final SparseArray<Pair<Station, StationDetails>> mStations = new SparseArray<>();

    public StatusAdapter() {
        setHasStableIds(true);
    }

    public void add(Pair<Station, StationDetails> station) {
        Pair<Station, StationDetails> pair = mStations.get(station.first.getStationId());
        mStations.put(station.first.getStationId(), station);
        if (pair == null)
            mIdentifiers.add(station.first.getStationId());
        notifyDataSetChanged();
    }

    public void remove(Pair<Station, StationDetails> station) {
        mIdentifiers.remove(station.first.getStationId());
        mStations.remove(station.first.getStationId());
        notifyDataSetChanged();
    }

    public int remove(int adapterPosition) {
        Integer id = mIdentifiers.get(adapterPosition);
        mStations.remove(id);
        mIdentifiers.remove(adapterPosition);
        return id;
    }

    @Override
    public StatusViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.fragment_status_item,
                parent,
                false);
        return new StatusViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(StatusViewHolder holder, int position) {
        Pair<Station, StationDetails> station = mStations.get(mIdentifiers.get(position));

        setCardBackground(holder.cardView, station.first);

        holder.cardTitle.setText(station.first.getStationName());

        setTimestamp(holder.timestamp, station.first, station.second);

        holder.pm10_row.setVisibility(View.GONE);
        holder.pm25_row.setVisibility(View.GONE);
        holder.no2_row.setVisibility(View.GONE);
        holder.so2_row.setVisibility(View.GONE);
        holder.o3_row.setVisibility(View.GONE);
        holder.c6h6_row.setVisibility(View.GONE);
        holder.co_row.setVisibility(View.GONE);

        @DrawableRes int drawable;

        for (ChartElement element : station.second.getChartElements()) {
            float lastVal = element.getLastValue();
            drawable = (element.getPreLastValue() < lastVal) ?
                    R.drawable.ic_increase_negative : R.drawable.ic_decrease_positive;
            switch (element.getKey()) {
                case "PM10": {
                    setFieldText(holder.pm10, station.first, lastVal, sPM10Limit);
                    holder.pm10_image.setImageResource(drawable);
                    holder.pm10_row.setVisibility(View.VISIBLE);
                    break;
                }
                case "PM2.5": {
                    setFieldText(holder.pm25, station.first, lastVal, sPM25Limit);
                    holder.pm25_image.setImageResource(drawable);
                    holder.pm25_row.setVisibility(View.VISIBLE);
                    break;
                }
                case "NO2":
                    setFieldText(holder.no2, station.first, lastVal, sNO2Limit);
                    holder.no2_image.setImageResource(drawable);
                    holder.no2_row.setVisibility(View.VISIBLE);
                    break;
                case "SO2":
                    setFieldText(holder.so2, station.first, lastVal, sSO2Limit);
                    holder.so2_image.setImageResource(drawable);
                    holder.so2_row.setVisibility(View.VISIBLE);
                    break;
                case "O3":
                    setFieldText(holder.o3, station.first, lastVal, sO3Limit);
                    holder.o3_image.setImageResource(drawable);
                    holder.o3_row.setVisibility(View.VISIBLE);
                    break;
                case "C6H6":
                    setFieldText(holder.c6h6, station.first, lastVal, sC6H6Limit);
                    holder.c6h6_image.setImageResource(drawable);
                    holder.c6h6_row.setVisibility(View.VISIBLE);
                    break;
                case "CO":
                    setFieldText(holder.co, station.first, lastVal, sCOLimit);
                    holder.co_image.setImageResource(drawable);
                    holder.co_row.setVisibility(View.VISIBLE);
                    break;
            }
        }
        station.first.markDataAsOld();
    }

    private int getTargetColor(int aqIndex) {
        int targetColor = sCardBackgroundColor0;
        switch (aqIndex) {
            case 0:
                targetColor = sCardBackgroundColor0;
                break;
            case 1:
                targetColor = sCardBackgroundColor1;
                break;
            case 2:
                targetColor = sCardBackgroundColor2;
                break;
            case 3:
                targetColor = sCardBackgroundColor3;
                break;
            case 4:
                targetColor = sCardBackgroundColor4;
                break;
            case 5:
                targetColor = sCardBackgroundColor5;
                break;
        }
        return targetColor;
    }

    private void setCardBackground(CardView cardView, Station station) {
        int targetColor = getTargetColor(station.getAqIndex());
        if (station.isNewData()) {
            Log.d(TAG, "setCardBackground: " + cardView.getCardBackgroundColor().getDefaultColor() + ", target = " + targetColor);
            ValueAnimator valueAnimator = ValueAnimator.ofObject(
                    new ArgbEvaluator(),
                    cardView.getCardBackgroundColor().getDefaultColor(),
                    targetColor);
            valueAnimator.setDuration(ANIM_DURATION);
            valueAnimator.addUpdateListener(
                    animation -> cardView.setCardBackgroundColor(
                            (int) animation.getAnimatedValue()));
            valueAnimator.start();
        } else
            cardView.setCardBackgroundColor(targetColor);
    }

    private void setTimestamp(TextView timestamp, Station station, StationDetails stationDetails) {
        if (station.isNewData()) {
            try {
                Date date = sDateFormatter.parse(timestamp.getText().toString());
                ValueAnimator animator = ValueAnimator.ofFloat(0.0f, 1.0f);
                animator.setDuration(ANIM_DURATION);
                long diff = stationDetails.getLastTimestamp() - date.getTime();
                animator.addUpdateListener(
                        animation -> timestamp.setText(
                                sDateFormatter.format(
                                        date.getTime()
                                                + (long) (diff * (float) animation.getAnimatedValue()))));
                animator.start();
            } catch (ParseException e) {
                timestamp.setText(sDateFormatter.format(stationDetails.getLastTimestamp()));
            }
        } else
            timestamp.setText(sDateFormatter.format(stationDetails.getLastTimestamp()));
    }

    private void setFieldText(TextView field, Station station, float lastValue, float limit) {
        float newValue = (lastValue / limit) * 100.0f;
        Context context = field.getContext();
        if (station.isNewData()) {
            CharSequence prevText = field.getText();
            ValueAnimator animator = new ValueAnimator();
            animator.setDuration(ANIM_DURATION);
            float prevVal;
            try {
                prevVal = Float.parseFloat(prevText.toString().substring(0, prevText.length() - 1));
            } catch (NumberFormatException | StringIndexOutOfBoundsException ex) {
                prevVal = 0;
            }
            animator.setObjectValues(prevVal, newValue);
            animator.addUpdateListener(animation -> field.setText(
                    context.getString(
                            R.string.adapter_status_percentage,
                            (float) animation.getAnimatedValue())));
            animator.start();
        } else {
            field.setText(context.getString(R.string.adapter_status_percentage, newValue));
        }
    }

    @Override
    public int getItemCount() {
        return mIdentifiers.size();
    }

    @Override
    public long getItemId(int position) {
        return mIdentifiers.get(position);
    }

    public boolean isRemovable(int adapterPosition) {
        Pair<Station, StationDetails> pair = mStations.get(mIdentifiers.get(adapterPosition));
        return pair != null && !pair.first.getStationName().toLowerCase().contains("legionowo");
    }
}
