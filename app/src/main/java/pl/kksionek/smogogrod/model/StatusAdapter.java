package pl.kksionek.smogogrod.model;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
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

    private static final SimpleDateFormat sDateFormatter = new SimpleDateFormat("DD-MM-yyyy hh:mm", Locale.ROOT);

    private ArrayList<Integer> mIdentifiers = new ArrayList<>();
    private HashMap<Integer, Pair<Station, StationDetails>> mStations = new HashMap<>();

    public void add(Pair<Station, StationDetails> station) {
        mIdentifiers.add(station.first.getStationId());
        mStations.put(station.first.getStationId(), station);
        notifyDataSetChanged();
    }

    public void remove(Pair<Station, StationDetails> station) {
        mIdentifiers.remove(station.first.getStationId());
        mStations.remove(station.first.getStationId());
        notifyDataSetChanged();
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

        holder.cardTitle.setText(station.first.getStationName());
        holder.timestamp.setText(sDateFormatter.format(station.second.getChartElements().get(0).getLastTimestamp()));
        holder.pm10.setText("N/A");
        holder.pm25.setText("N/A");
        holder.no2.setText("N/A");
        holder.so2.setText("N/A");
        holder.o3.setText("N/A");
        holder.c6h6.setText("N/A");
        holder.co.setText("N/A");

        Context ctx = holder.cardTitle.getContext();

        for (ChartElement element : station.second.getChartElements()) {
            switch (element.getKey()) {
                case "PM10":
                    holder.pm10.setText(ctx.getString(R.string.adapter_status_percentage, (element.getLastValue() / sPM10Limit) * 100));
                    holder.pm10_row.setVisibility(View.VISIBLE);
                    break;
                case "PM2.5":
                    holder.pm25.setText(ctx.getString(R.string.adapter_status_percentage, (element.getLastValue() / sPM25Limit) * 100));
                    holder.pm25_row.setVisibility(View.VISIBLE);
                    break;
                case "NO2":
                    holder.no2.setText(ctx.getString(R.string.adapter_status_percentage, (element.getLastValue() / sNO2Limit) * 100));
                    holder.no2_row.setVisibility(View.VISIBLE);
                    break;
                case "SO2":
                    holder.so2.setText(ctx.getString(R.string.adapter_status_percentage, (element.getLastValue()/ sSO2Limit) * 100));
                    holder.so2_row.setVisibility(View.VISIBLE);
                    break;
                case "O3":
                    holder.o3.setText(ctx.getString(R.string.adapter_status_percentage, (element.getLastValue() / sO3Limit) * 100));
                    holder.o3_row.setVisibility(View.VISIBLE);
                    break;
                case "C6H6":
                    holder.c6h6.setText(ctx.getString(R.string.adapter_status_percentage, (element.getLastValue() / sC6H6Limit) * 100));
                    holder.c6h6_row.setVisibility(View.VISIBLE);
                    break;
                case "CO":
                    holder.co.setText(ctx.getString(R.string.adapter_status_percentage, (element.getLastValue() / sCOLimit) * 100));
                    holder.co_row.setVisibility(View.VISIBLE);
                    break;
            }
        }

        if (holder.pm10.getText().equals("N/A"))
            holder.pm10_row.setVisibility(View.GONE);
        if (holder.pm25.getText().equals("N/A"))
            holder.pm25_row.setVisibility(View.GONE);
        if (holder.no2.getText().equals("N/A"))
            holder.no2_row.setVisibility(View.GONE);
        if (holder.so2.getText().equals("N/A"))
            holder.so2_row.setVisibility(View.GONE);
        if (holder.o3.getText().equals("N/A"))
            holder.o3_row.setVisibility(View.GONE);
        if (holder.c6h6.getText().equals("N/A"))
            holder.c6h6_row.setVisibility(View.GONE);
        if (holder.co.getText().equals("N/A"))
            holder.co_row.setVisibility(View.GONE);
    }

    @Override
    public int getItemCount() {
        return mStations.size();
    }
}
