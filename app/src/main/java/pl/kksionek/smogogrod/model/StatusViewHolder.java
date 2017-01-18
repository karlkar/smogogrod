package pl.kksionek.smogogrod.model;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import pl.kksionek.smogogrod.R;

public class StatusViewHolder extends RecyclerView.ViewHolder {

    public TextView cardTitle;
    public TextView pm10;
    public TextView pm25;
    public TextView no2;
    public TextView so2;
    public TextView o3;
    public TextView co;
    public TextView c6h6;

    public StatusViewHolder(View itemView) {
        super(itemView);
        cardTitle = (TextView) itemView.findViewById(R.id.station_name);
        pm10 = (TextView) itemView.findViewById(R.id.item_pm10);
        pm25 = (TextView) itemView.findViewById(R.id.item_pm25);
        no2 = (TextView) itemView.findViewById(R.id.item_no2);
        so2 = (TextView) itemView.findViewById(R.id.item_so2);
        o3 = (TextView) itemView.findViewById(R.id.item_o3);
        co = (TextView) itemView.findViewById(R.id.item_co);
        c6h6 = (TextView) itemView.findViewById(R.id.item_c6h6);
    }
}
