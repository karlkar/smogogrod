package pl.kksionek.smogogrod.view;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

import pl.kksionek.smogogrod.R;
import pl.kksionek.smogogrod.data.MarkedPlace;
import pl.kksionek.smogogrod.model.Network;
import rx.Emitter;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;

public class MapFragment extends SupportMapFragment {

    private static final String TAG = "MAPFRAGMENT";

    public static BitmapDescriptor mBitmapDescriptorFactory = null;
    public static BitmapDescriptor mBitmapDescriptorRequest = null;
    public static BitmapDescriptor mBitmapDescriptorMeasurement = null;

//    private MapFragmentListener mMapFragmentListener;
//    interface MapFragmentListener {
//        void onMapReady();
//    }

    private GoogleMap mMap;
    private Subscription mSubscription;
    private Observable<GoogleMap> mMapReadyObservable;

    private void showOnMap(ArrayList<MarkedPlace> markedPlaces) {
        for (MarkedPlace place : markedPlaces) {
            mMap.addMarker(new MarkerOptions()
                    .icon(getIcon(place.getType()))
                    .position(place.getLatLng())
                    .draggable(false)
                    .title(place.getTitle())
                    .snippet(place.getDescription()));
        }
    }

    private BitmapDescriptor getIcon(String type) {
        switch (type) {
            case "truciciel_dom":
                if (mBitmapDescriptorFactory == null)
                    mBitmapDescriptorFactory = BitmapDescriptorFactory.fromResource(
                            R.drawable.factory);
                return mBitmapDescriptorFactory;
            case "pomiar":
                if (mBitmapDescriptorMeasurement == null)
                    mBitmapDescriptorMeasurement = BitmapDescriptorFactory.fromResource(
                            R.drawable.measurement);
                return mBitmapDescriptorMeasurement;
            case "prosba_o_pomiar":
                if (mBitmapDescriptorRequest == null)
                    mBitmapDescriptorRequest = BitmapDescriptorFactory.fromResource(
                            R.drawable.request);
                return mBitmapDescriptorRequest;
        }
        return null;
    }

    class MyInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

        private final LayoutInflater mInflater;
        private View mPopup;

        MyInfoWindowAdapter(LayoutInflater inflater) {
            mInflater = inflater;
        }

        @Override
        public View getInfoWindow(Marker marker) {
            return null;
        }

        @Override
        public View getInfoContents(Marker marker) {
            if (mPopup == null) {
                mPopup = mInflater.inflate(R.layout.popup, null);
            }

            TextView tv = (TextView) mPopup.findViewById(R.id.title);

            tv.setText(marker.getTitle());
            tv = (TextView) mPopup.findViewById(R.id.snippet);
            tv.setText(marker.getSnippet());

            return(mPopup);
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mMapReadyObservable = Observable.<GoogleMap>fromEmitter(objectEmitter -> {
            OnMapReadyCallback callback = googleMap -> {
                objectEmitter.onNext(googleMap);
                objectEmitter.onCompleted();
            };
            getMapAsync(callback);
        }, Emitter.BackpressureMode.DROP).cache();

        mMapReadyObservable
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(googleMap -> {
                    mMap = googleMap;
                    mMap.setInfoWindowAdapter(new MyInfoWindowAdapter(LayoutInflater.from(getContext())));
                    LatLng legionowo = new LatLng(52.3998006, 20.934969);
                    mMap.moveCamera(CameraUpdateFactory.zoomTo(13.0f));
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(legionowo));
                });

        mSubscription = Network.getMarkedPlaces(getContext())
                .zipWith(mMapReadyObservable, (markedPlaces, googleMap) -> {
                    mMap = googleMap;
                    return markedPlaces;
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        markedPlaces -> showOnMap(markedPlaces),
                        Throwable::printStackTrace);
    }

    @Override
    public void onDestroy() {
        mSubscription.unsubscribe();
        super.onDestroy();
    }
}
