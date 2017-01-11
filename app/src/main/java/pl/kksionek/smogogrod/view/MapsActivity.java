package pl.kksionek.smogogrod.view;

import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import pl.kksionek.smogogrod.R;
import pl.kksionek.smogogrod.SmogApplication;
import pl.kksionek.smogogrod.data.MarkedPlace;
import pl.kksionek.smogogrod.data.Station;
import rx.Emitter;
import rx.Observable;
import rx.functions.Action1;
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

//        mStationSource
//                .subscribe(stations -> Log.d(TAG, "onMapReady: size " + stations.size()));
//
//        SmogApplication.getAirRetrofitService(getApplicationContext())
//                .getStationDetails(1, 471)
//                .subscribeOn(Schedulers.io())
//                .subscribe(stationDetails -> {
//                    Log.d(TAG, "onMapReady: ok... " + stationDetails.getChartElements().get(0).getChartValueAt(0).getTimestamp());
//                });

        getMarkedPlaces()
                .subscribeOn(Schedulers.io())
                .subscribe(markedPlaces -> Log.d(TAG, "onMapReady: size = " + markedPlaces.size()));

//        LatLng sydney = new LatLng(-34, 151);
//        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }

    private Observable<ArrayList<MarkedPlace>> getMarkedPlaces() {
        return Observable.fromEmitter(new Action1<Emitter<ArrayList<MarkedPlace>>>() {
            @Override
            public void call(Emitter<ArrayList<MarkedPlace>> emitter) {
                OkHttpClient okHttpClient = SmogApplication.getOkHttpClient(getApplicationContext());

                Request req = new Request.Builder()
                        .url("http://alarm.legionowo.info.pl/")
                        .build();

                String responseStr = null;
                try {
                    Response response = okHttpClient.newCall(req).execute();
                    responseStr = response.body().string();
                } catch (IOException e) {
                    emitter.onError(e);
                    return;
                }

                if (responseStr == null) {
                    emitter.onError(new IOException("Empty response received."));
                    return;
                }

                Pattern featurePattern = Pattern.compile("var features = \\[([\\w\\W]*?)\\}\\];", Pattern.MULTILINE);
                Pattern positionPattern = Pattern.compile("google\\.maps\\.LatLng\\(([0-9\\.,]+)\\)");
                Pattern typePattern = Pattern.compile("type: '(.*?)'");
                Pattern titlePattern = Pattern.compile("title: '(.*?)'");
                Pattern labelPattern = Pattern.compile("label: '(.*?)'");
                Matcher featureMatcher = featurePattern.matcher(responseStr);
                ArrayList<MarkedPlace> places = new ArrayList<>();
                if (featureMatcher.find()) {
                    boolean arrayEnded = false;
                    Matcher positionMatcher = positionPattern.matcher(featureMatcher.group(1));
                    Matcher typeMatcher = typePattern.matcher(featureMatcher.group(1));
                    Matcher titleMatcher = titlePattern.matcher(featureMatcher.group(1));
                    Matcher labelMatcher = labelPattern.matcher(featureMatcher.group(1));
                    while (!arrayEnded) {
                        if (!positionMatcher.find()) {
                            if (places.isEmpty()) {
                                emitter.onError(new ParseException("No matches found for position.", 0));
                                return;
                            }
                            arrayEnded = true;
                            continue;
                        }
                        if (!typeMatcher.find()) {
                            emitter.onError(new ParseException("No matches found for type.", 0));
                            return;
                        }
                        if (!titleMatcher.find()) {
                            emitter.onError(new ParseException("No matches found for title.", 0));
                            return;
                        }
                        if (!labelMatcher.find()) {
                            emitter.onError(new ParseException("No matches found for label.", 0));
                            return;
                        }
                        places.add(new MarkedPlace(
                                positionMatcher.group(1),
                                typeMatcher.group(1),
                                titleMatcher.group(1),
                                labelMatcher.group(1)));
                    }
                    emitter.onNext(places);
                    emitter.onCompleted();
                } else {
                    emitter.onError(new ParseException("No matches found for all features.", 0));
                    return;
                }
            }
        }, Emitter.BackpressureMode.DROP);
    }
}
