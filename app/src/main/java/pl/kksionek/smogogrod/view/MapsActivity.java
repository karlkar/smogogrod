package pl.kksionek.smogogrod.view;

import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

import pl.kksionek.smogogrod.R;
import pl.kksionek.smogogrod.SmogApplication;
import pl.kksionek.smogogrod.data.Station;
import rx.Observable;
import rx.schedulers.Schedulers;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    public static final String TAG = "MAPSACTIVITY";
    private GoogleMap mMap;
    private Observable<ArrayList<Station>> mStationSource;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mStationSource = SmogApplication.getAirRetrofitService(getApplicationContext())
                .getStations("AQI")
                .subscribeOn(Schedulers.io());


    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mStationSource
                .subscribe(stations -> Log.d(TAG, "onMapReady: size " + stations.size()));

        SmogApplication.getAirRetrofitService(getApplicationContext())
                .getStationDetails(1, 471)
                .subscribeOn(Schedulers.io())
                .subscribe(stationDetails -> {
                    Log.d(TAG, "onMapReady: ok... " + stationDetails.getChartElements().get(0).getChartValueAt(0).getTimestamp());
                });

//        LatLng sydney = new LatLng(-34, 151);
//        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }
}
