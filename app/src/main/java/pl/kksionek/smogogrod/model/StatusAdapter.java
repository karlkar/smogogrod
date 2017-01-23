package pl.kksionek.smogogrod.model;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.support.annotation.DrawableRes;
import android.support.v7.widget.RecyclerView;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import pl.kksionek.smogogrod.R;
import pl.kksionek.smogogrod.data.ChartElement;
import pl.kksionek.smogogrod.data.Station;
import pl.kksionek.smogogrod.data.StationDetails;

public class StatusAdapter extends RecyclerView.Adapter<StatusViewHolder> {

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

    private static final SimpleDateFormat sDateFormatter = new SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault());
    private static final int ANIM_DURATION = 1000;

    private ArrayList<Integer> mIdentifiers = new ArrayList<>();
    private HashMap<Integer, Pair<Station, StationDetails>> mStations = new HashMap<>();

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

    private void animateIfNeeded(ChartElement element, float limit, TextView view) {
        float newValue = (element.getLastValue() / limit) * 100.0f;
        Context context = view.getContext();
        CharSequence prevText = view.getText();
        if (!prevText.equals(context.getString(
                R.string.adapter_status_percentage,
                newValue))) {
            ValueAnimator animator = new ValueAnimator();
            animator.setDuration(ANIM_DURATION);
            float prevVal;
            try {
                prevVal = Float.parseFloat(prevText.toString().substring(0, prevText.length() - 1));
            } catch (NumberFormatException | StringIndexOutOfBoundsException ex) {
                prevVal = 0;
            }
            animator.setObjectValues(prevVal, newValue);
            animator.addUpdateListener(animation -> view.setText(
                    context.getString(
                            R.string.adapter_status_percentage,
                            (float) animation.getAnimatedValue())));
            animator.start();
        }
    }

    @Override
    public void onBindViewHolder(StatusViewHolder holder, int position) {
        Pair<Station, StationDetails> station = mStations.get(mIdentifiers.get(position));

        switch (station.first.getAqIndex()) {
            case 0:
                holder.cardView.setCardBackgroundColor(sCardBackgroundColor0);
                break;
            case 1:
                holder.cardView.setCardBackgroundColor(sCardBackgroundColor1);
                break;
            case 2:
                holder.cardView.setCardBackgroundColor(sCardBackgroundColor2);
                break;
            case 3:
                holder.cardView.setCardBackgroundColor(sCardBackgroundColor3);
                break;
            case 4:
                holder.cardView.setCardBackgroundColor(sCardBackgroundColor4);
                break;
            case 5:
                holder.cardView.setCardBackgroundColor(sCardBackgroundColor5);
                break;
        }

        holder.cardTitle.setText(station.first.getStationName());

        if (!holder.timestamp.getText().toString().isEmpty()) {
            try {
                Date date = sDateFormatter.parse(holder.timestamp.getText().toString());
                ValueAnimator animator = ValueAnimator.ofFloat(0.0f, 1.0f);
                animator.setDuration(ANIM_DURATION);
                long diff = station.second.getLastTimestamp() - date.getTime();
                animator.addUpdateListener(
                        animation -> holder.timestamp.setText(
                                sDateFormatter.format(
                                        date.getTime()
                                                + (long) (diff * (float) animation.getAnimatedValue()))));
                animator.start();
            } catch (ParseException e) {
                holder.timestamp.setText(sDateFormatter.format(station.second.getLastTimestamp()));
                e.printStackTrace();
            }
        } else
            holder.timestamp.setText(sDateFormatter.format(station.second.getLastTimestamp()));

        holder.pm10_row.setVisibility(View.GONE);
        holder.pm25_row.setVisibility(View.GONE);
        holder.no2_row.setVisibility(View.GONE);
        holder.so2_row.setVisibility(View.GONE);
        holder.o3_row.setVisibility(View.GONE);
        holder.c6h6_row.setVisibility(View.GONE);
        holder.co_row.setVisibility(View.GONE);

        @DrawableRes int drawable;

        for (ChartElement element : station.second.getChartElements()) {
            switch (element.getKey()) {
                case "PM10": {
                    animateIfNeeded(element, sPM10Limit, holder.pm10);
                    drawable = (element.getPreLastValue() > element.getLastValue()) ?
                            R.drawable.ic_increase_negative : R.drawable.ic_decrease_positive;
                    holder.pm10_image.setImageResource(drawable);
                    holder.pm10_row.setVisibility(View.VISIBLE);
                    break;
                }
                case "PM2.5": {
                    animateIfNeeded(element, sPM25Limit, holder.pm25);
                    drawable = (element.getPreLastValue() > element.getLastValue()) ?
                            R.drawable.ic_increase_negative : R.drawable.ic_decrease_positive;
                    holder.pm25_image.setImageResource(drawable);
                    holder.pm25_row.setVisibility(View.VISIBLE);
                    break;
                }
                case "NO2":
                    animateIfNeeded(element, sNO2Limit, holder.no2);
                    drawable = (element.getPreLastValue() > element.getLastValue()) ?
                            R.drawable.ic_increase_negative : R.drawable.ic_decrease_positive;
                    holder.no2_image.setImageResource(drawable);
                    holder.no2_row.setVisibility(View.VISIBLE);
                    break;
                case "SO2":
                    animateIfNeeded(element, sSO2Limit, holder.so2);
                    drawable = (element.getPreLastValue() > element.getLastValue()) ?
                            R.drawable.ic_increase_negative : R.drawable.ic_decrease_positive;
                    holder.so2_image.setImageResource(drawable);
                    holder.so2_row.setVisibility(View.VISIBLE);
                    break;
                case "O3":
                    animateIfNeeded(element, sO3Limit, holder.o3);
                    drawable = (element.getPreLastValue() > element.getLastValue()) ?
                            R.drawable.ic_increase_negative : R.drawable.ic_decrease_positive;
                    holder.o3_image.setImageResource(drawable);
                    holder.o3_row.setVisibility(View.VISIBLE);
                    break;
                case "C6H6":
                    animateIfNeeded(element, sC6H6Limit, holder.c6h6);
                    drawable = (element.getPreLastValue() > element.getLastValue()) ?
                            R.drawable.ic_increase_negative : R.drawable.ic_decrease_positive;
                    holder.c6h6_image.setImageResource(drawable);
                    holder.c6h6_row.setVisibility(View.VISIBLE);
                    break;
                case "CO":
                    animateIfNeeded(element, sCOLimit, holder.co);
                    drawable = (element.getPreLastValue() > element.getLastValue()) ?
                            R.drawable.ic_increase_negative : R.drawable.ic_decrease_positive;
                    holder.co_image.setImageResource(drawable);
                    holder.co_row.setVisibility(View.VISIBLE);
                    break;
            }
        }
    }

    @Override
    public int getItemCount() {
        return mIdentifiers.size();
    }

    public boolean isRemovable(int adapterPosition) {
        Pair<Station, StationDetails> pair = mStations.get(mIdentifiers.get(adapterPosition));
        return pair != null && !pair.first.getStationName().toLowerCase().contains("legionowo");
    }
}
