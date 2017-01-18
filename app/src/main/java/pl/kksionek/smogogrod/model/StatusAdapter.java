package pl.kksionek.smogogrod.model;

import android.support.v7.widget.RecyclerView;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import pl.kksionek.smogogrod.R;
import pl.kksionek.smogogrod.data.ChartElement;
import pl.kksionek.smogogrod.data.Station;
import pl.kksionek.smogogrod.data.StationDetails;

public class StatusAdapter extends RecyclerView.Adapter<StatusViewHolder> {

    private ArrayList<Pair<Station,StationDetails>> mStations = new ArrayList<>();

    public void add(Pair<Station, StationDetails> station) {
        mStations.add(station);
        notifyDataSetChanged();
    }

    public void remove(Pair<Station, StationDetails> station) {
        mStations.remove(station);
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
        Pair<Station,StationDetails> station = mStations.get(position);

        holder.cardTitle.setText(station.first.getStationName());
        holder.pm10.setText("N/A");
        holder.pm25.setText("N/A");
        holder.no2.setText("N/A");
        holder.so2.setText("N/A");
        holder.o3.setText("N/A");
        holder.c6h6.setText("N/A");
        holder.co.setText("N/A");

        for (ChartElement element : station.second.getChartElements()) {
            switch (element.getKey()) {
                case "PM10":
                    holder.pm10.setText(String.valueOf(element.getLastValue()));
                    holder.pm10_row.setVisibility(View.VISIBLE);
                    break;
                case "PM25":
                    holder.pm25.setText(String.valueOf(element.getLastValue()));
                    holder.pm25_row.setVisibility(View.VISIBLE);
                    break;
                case "NO2":
                    holder.no2.setText(String.valueOf(element.getLastValue()));
                    holder.no2_row.setVisibility(View.VISIBLE);
                    break;
                case "SO2":
                    holder.so2.setText(String.valueOf(element.getLastValue()));
                    holder.so2_row.setVisibility(View.VISIBLE);
                    break;
                case "O3":
                    holder.o3.setText(String.valueOf(element.getLastValue()));
                    holder.o3_row.setVisibility(View.VISIBLE);
                    break;
                case "C6H6":
                    holder.c6h6.setText(String.valueOf(element.getLastValue()));
                    holder.c6h6_row.setVisibility(View.VISIBLE);
                    break;
                case "CO":
                    holder.co.setText(String.valueOf(element.getLastValue()));
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
