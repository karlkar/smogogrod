package pl.kksionek.smogogrod.data;

import com.google.android.gms.maps.model.LatLng;

public class MarkedPlace {
    LatLng mPosition;
    String mType;
    String mTitle;
    String mLabel;

    public MarkedPlace(String latlng, String type, String title, String label) {
//        mPosition =
        mType = type;
        mTitle = title;
        mLabel = label;
    }
}
