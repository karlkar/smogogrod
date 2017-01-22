package pl.kksionek.smogogrod.view;

import android.app.Fragment;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import pl.kksionek.smogogrod.R;
import pl.kksionek.smogogrod.data.Station;
import pl.kksionek.smogogrod.data.StationDetails;
import pl.kksionek.smogogrod.model.Network;
import pl.kksionek.smogogrod.model.StatusAdapter;
import rx.Emitter;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.schedulers.Schedulers;

public class StatusFragment extends Fragment {

    private static final String TAG = "StatusFragment";

    RecyclerView mRecyclerView;
    private Subscription mSubscription;
    private StatusAdapter mStatusAdapter = null;
    private Gson mGson;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        mGson = new Gson();
    }

    @Nullable
    @Override
    public View onCreateView(
            LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_status, null);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        boolean redownload = false;
        if (mStatusAdapter == null) {
            Log.d(TAG, "onCreateView: ");
            mStatusAdapter = new StatusAdapter();
            redownload = true;
        }
        mRecyclerView.setAdapter(mStatusAdapter);

        if (redownload) {
            mSubscription = Observable.concat(
                    Observable.fromEmitter(emitter -> {
                        for (Station station : getStationsFromCache()) {
                            StationDetails stationDetails = getStationDetailsFromCache(station.getStationId());
                            if (stationDetails != null)
                                emitter.onNext(new Pair<>(station, stationDetails));
                        }
                        emitter.onCompleted();
                    }, Emitter.BackpressureMode.DROP),
                    Network.getStations(getActivity())
                    .subscribeOn(Schedulers.io())
//                    .doOnNext(this::save)
                    .flatMapIterable(stations -> stations)
                    .filter(this::stationNeedDetails)
                    .flatMap(station -> Network.getStationDetails(getActivity(), station.getStationId())
                            .map(stationDetails -> new Pair<>(station, stationDetails))
                            .subscribeOn(Schedulers.io()))
                    .doOnError(Throwable::printStackTrace)
                    .doOnNext(this::save)
                    .retryWhen(errors ->
                            errors
                                    .zipWith(
                                            Observable.range(1, 3), (n, i) -> i)
                                    .flatMap(
                                            retryCount -> Observable.timer(5L * retryCount, TimeUnit.SECONDS))))
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            pair -> mStatusAdapter.add(pair),
                            Throwable::printStackTrace);
        }
        return view;
    }

    private boolean stationNeedDetails(Station station) {
        //TODO: get others
        return station.getStationName().toLowerCase().contains("legionowo");
    }

    private void save(Pair<Station, StationDetails> stations) {
        String stationJson = mGson.toJson(stations.first);
        String stationDetailsJson = mGson.toJson(stations.second);
        PreferenceManager.getDefaultSharedPreferences(getActivity())
                .edit()
                .putString("STATION_" + stations.first.getStationId(), stationJson)
                .putString("STATION_" + stations.first.getStationId() + "_DETAILS", stationDetailsJson)
                .apply();
    }

    private List<Station> getStationsFromCache() {
        List<Station> stations = new ArrayList<>();
        Map<String, ?> all = PreferenceManager.getDefaultSharedPreferences(getActivity()).getAll();
        for (String key : all.keySet()) {
            if (key.startsWith("STATION_") && !key.endsWith("_DETAILS")) {
                stations.add(mGson.fromJson((String) all.get(key), Station.class));
            }
        }
        Log.d(TAG, "getStationsFromCache: Got " + stations.size());
        return stations;
    }

    private StationDetails getStationDetailsFromCache(int id) {
        String string = PreferenceManager.getDefaultSharedPreferences(getActivity()).getString("STATION_" + id + "_DETAILS", "");
        if (string.isEmpty())
            return null;
        return mGson.fromJson(string, StationDetails.class);
    }

    @Override
    public void onDestroyView() {
        if (mSubscription != null && !mSubscription.isUnsubscribed())
            mSubscription.unsubscribe();
        mRecyclerView = null;
        super.onDestroyView();
    }
}
