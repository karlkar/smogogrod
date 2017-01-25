package pl.kksionek.smogogrod.view;

import android.annotation.TargetApi;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

import pl.kksionek.smogogrod.R;
import pl.kksionek.smogogrod.data.MarkedPlace;
import pl.kksionek.smogogrod.model.Network;
import rx.Observable;
import rx.Single;
import rx.android.schedulers.AndroidSchedulers;
import rx.subscriptions.CompositeSubscription;

public class LwoMapFragment extends MapFragment {

    private static final String TAG = "MAPFRAGMENT";

    private static final LatLng LAT_LNG_LEGIONOWO = new LatLng(52.3998006, 20.934969);

    private static BitmapDescriptor mBitmapDescriptorFactory = null;
    private static BitmapDescriptor mBitmapDescriptorRequest = null;
    private static BitmapDescriptor mBitmapDescriptorMeasurement = null;

    private GoogleMap mMap;
    private Single<GoogleMap> mMapReadySignal;
    private final CompositeSubscription mSubscriptions = new CompositeSubscription();

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setRetainInstance(true);
    }

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

            return mPopup;
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        mMapReadySignal = Single.<GoogleMap>fromEmitter(objectEmitter -> {
            OnMapReadyCallback callback = googleMap -> {
                mMap = googleMap;
                objectEmitter.onSuccess(googleMap);
            };
            getMapAsync(callback);
        }).cache();

        mSubscriptions.add(mMapReadySignal
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((googleMap) -> {
                    mMap.setInfoWindowAdapter(
                            new MyInfoWindowAdapter(LayoutInflater.from(getContext())));
                    mMap.moveCamera(CameraUpdateFactory.zoomTo(13.0f));
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(LAT_LNG_LEGIONOWO));
                }));

        mSubscriptions.add(
                Observable.zip(
                        Network.getMarkedPlaces(getContext()),
                        mMapReadySignal.toObservable(),
                        (markedPlaces, ignore) -> markedPlaces)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                this::showOnMap,
                                Throwable::printStackTrace));
    }

    @TargetApi(android.os.Build.VERSION_CODES.M)
    private boolean isAnyPermissionGranted(String[] strings) {
        for (String permission : strings) {
            if (getActivity().checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED)
                return true;
        }
        return false;
    }

    @Override
    public void onDestroy() {
        mSubscriptions.clear();
        super.onDestroy();
    }
}
