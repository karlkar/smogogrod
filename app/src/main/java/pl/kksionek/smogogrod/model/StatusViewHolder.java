package pl.kksionek.smogogrod.model;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TableRow;
import android.widget.TextView;

import pl.kksionek.smogogrod.R;

public class StatusViewHolder extends RecyclerView.ViewHolder {

    public CardView cardView;

    public TextView cardTitle;
    public TextView timestamp;
    public TextView pm10;
    public TextView pm25;
    public TextView no2;
    public TextView so2;
    public TextView o3;
    public TextView co;
    public TextView c6h6;

    public TableRow pm10_row;
    public TableRow pm25_row;
    public TableRow no2_row;
    public TableRow so2_row;
    public TableRow o3_row;
    public TableRow co_row;
    public TableRow c6h6_row;

    public ImageView pm10_image;
    public ImageView pm25_image;
    public ImageView no2_image;
    public ImageView so2_image;
    public ImageView o3_image;
    public ImageView co_image;
    public ImageView c6h6_image;

    public StatusViewHolder(View itemView) {
        super(itemView);
        cardView = (CardView) itemView.findViewById(R.id.card_view);

        cardTitle = (TextView) itemView.findViewById(R.id.station_name);
        timestamp = (TextView) itemView.findViewById(R.id.station_time);
        pm10 = (TextView) itemView.findViewById(R.id.item_pm10);
        pm25 = (TextView) itemView.findViewById(R.id.item_pm25);
        no2 = (TextView) itemView.findViewById(R.id.item_no2);
        so2 = (TextView) itemView.findViewById(R.id.item_so2);
        o3 = (TextView) itemView.findViewById(R.id.item_o3);
        co = (TextView) itemView.findViewById(R.id.item_co);
        c6h6 = (TextView) itemView.findViewById(R.id.item_c6h6);

        pm10_row = (TableRow) itemView.findViewById(R.id.item_pm10_row);
        pm25_row = (TableRow) itemView.findViewById(R.id.item_pm25_row);
        no2_row = (TableRow) itemView.findViewById(R.id.item_no2_row);
        so2_row = (TableRow) itemView.findViewById(R.id.item_so2_row);
        o3_row = (TableRow) itemView.findViewById(R.id.item_o3_row);
        co_row = (TableRow) itemView.findViewById(R.id.item_co_row);
        c6h6_row = (TableRow) itemView.findViewById(R.id.item_c6h6_row);

        pm10_image = (ImageView) itemView.findViewById(R.id.item_pm10_image);
        pm25_image = (ImageView) itemView.findViewById(R.id.item_pm25_image);
        no2_image = (ImageView) itemView.findViewById(R.id.item_no2_image);
        so2_image = (ImageView) itemView.findViewById(R.id.item_so2_image);
        o3_image = (ImageView) itemView.findViewById(R.id.item_o3_image);
        co_image = (ImageView) itemView.findViewById(R.id.item_co_image);
        c6h6_image = (ImageView) itemView.findViewById(R.id.item_c6h6_image);
    }
}
