package pl.kksionek.smogogrod.view;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import pl.kksionek.smogogrod.R;
import pl.kksionek.smogogrod.data.Station;
import pl.kksionek.smogogrod.data.StationDetails;
import pl.kksionek.smogogrod.model.Network;
import pl.kksionek.smogogrod.model.StatusAdapter;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class StatusFragment extends Fragment {

    private static final String TAG = "StatusFragment";

    RecyclerView mRecyclerView;
    private Subscription mSubscription;
    private StatusAdapter mStatusAdapter = null;
//    private HashMap<Integer, Station> mStationHashMap = new HashMap<>();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
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
            mSubscription = Network.getStations(getActivity())
                    .subscribeOn(Schedulers.io())
//                    .doOnNext(this::save)
                    .flatMapIterable(stations -> stations)
                    .filter(this::stationNeedDetails)
                    .flatMap(station -> Observable.zip(
                            Observable.just(station),
                            Network.getStationDetails(getActivity(), station.getStationId()),
                            (station1, stationDetails) -> new Pair(station1, stationDetails))
                            .subscribeOn(Schedulers.io()))
                    .doOnError(Throwable::printStackTrace)
                    .retryWhen(errors ->
                            errors
                                    .zipWith(
                                            Observable.range(1, 3), (n, i) -> i)
                                    .flatMap(
                                            retryCount -> Observable.timer(5L * retryCount, TimeUnit.SECONDS)))
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

    private void save(ArrayList<Station> stations) {
//        mStationHashMap.clear();
//        for (Station station : stations)
//            mStationHashMap.put(station.getStationId(), station);
    }

    @Override
    public void onDestroyView() {
        if (mSubscription != null && !mSubscription.isUnsubscribed())
            mSubscription.unsubscribe();
        mRecyclerView = null;
        super.onDestroyView();
    }
}
