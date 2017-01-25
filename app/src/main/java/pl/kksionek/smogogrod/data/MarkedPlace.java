package pl.kksionek.smogogrod.data;

import com.google.android.gms.maps.model.LatLng;

public class MarkedPlace {
    private LatLng mPosition;
    private String mType;
    private String mTitle;
    private String mLabel;

    public MarkedPlace(String latlng, String type, String title, String label) {
        String[] strings = latlng.split(",");
        if (strings.length != 2) {
            throw new IllegalArgumentException(
                    "latlng string must contain two double values separated with a coma");
        }
        mPosition = new LatLng(Double.parseDouble(strings[0]), Double.parseDouble(strings[1]));
        mType = type;
        mTitle = title;
        mLabel = label;
    }

    public LatLng getLatLng() {
        return mPosition;
    }

    public String getType() {
        return mType;
    }

    public String getDescription() {
        return mLabel;
    }

    public String getTitle() {
        return mTitle;
    }
}
