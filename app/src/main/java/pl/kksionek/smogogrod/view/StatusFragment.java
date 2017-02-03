package pl.kksionek.smogogrod.view;

import android.app.Fragment;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.gson.Gson;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import pl.kksionek.smogogrod.R;
import pl.kksionek.smogogrod.data.Station;
import pl.kksionek.smogogrod.data.StationDetails;
import pl.kksionek.smogogrod.model.Network;
import pl.kksionek.smogogrod.model.StatusAdapter;
import rx.Emitter;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class StatusFragment extends Fragment {

    private static final String TAG = "StatusFragment";
    private static final String PREF_FILTER = "FILTER";

    private static final Collator COLLATOR = Collator.getInstance(new Locale("pl", "PL"));

    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView mRecyclerView;
    private Subscription mSubscription;
    private StatusAdapter mStatusAdapter = null;
    private SharedPreferences mSharedPreferences;

    private Gson mGson;
    private ArrayList<Pair<String, Integer>> mAvailableStations;
    private final Lock mAvailableStationsLock = new ReentrantLock();
    private final SortedSet<Integer> mFilterConditions = new TreeSet<>();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        mGson = new Gson();

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        Set<String> filter = mSharedPreferences.getStringSet(PREF_FILTER, null);
        if (filter != null) {
            for (String str : filter)
                mFilterConditions.add(Integer.parseInt(str));
        }
    }

    @Nullable
    @Override
    public View onCreateView(
            LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_status, container, false);

        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.fragment_status_swipe_layout);
        int mDistanceToTriggerSync = (int) (getResources().getDisplayMetrics().heightPixels * 0.3f);
        mSwipeRefreshLayout.setDistanceToTriggerSync(mDistanceToTriggerSync);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        ItemTouchHelper.Callback cb = new ItemTouchHelper.Callback() {
            @Override
            public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                int swipeFlags;
                if (((StatusAdapter) recyclerView.getAdapter()).isRemovable(viewHolder.getAdapterPosition()))
                    swipeFlags = ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT;
                else
                    swipeFlags = 0;
                return makeMovementFlags(0, swipeFlags);
            }

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                int removedId = mStatusAdapter.remove(viewHolder.getAdapterPosition());
                mStatusAdapter.notifyItemRemoved(viewHolder.getLayoutPosition());
                removeFilter(removedId);
            }
        };
        ItemTouchHelper helper = new ItemTouchHelper(cb);
        helper.attachToRecyclerView(mRecyclerView);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        if (mStatusAdapter == null) {
            mStatusAdapter = new StatusAdapter();
            mRecyclerView.setAdapter(mStatusAdapter);
            mSwipeRefreshLayout.setRefreshing(true);
            refreshData(true);
        } else
            mRecyclerView.setAdapter(mStatusAdapter);

        mSwipeRefreshLayout.setOnRefreshListener(() -> refreshData(false));

        FloatingActionButton floatingActionButton = (FloatingActionButton) view.findViewById(R.id.fragment_status_fab);
        floatingActionButton.setOnClickListener(v -> {
            mAvailableStationsLock.lock();
            if (mAvailableStations == null) {
                Log.d(TAG, "onCreateView: No available stations");
                Toast.makeText(
                        getActivity(),
                        R.string.fragment_status_please_refresh_list,
                        Toast.LENGTH_SHORT).show();
                mAvailableStationsLock.unlock();
                return;
            }

            List<Pair<String, Integer>> options = new ArrayList<>();
            List<String> optionStrings = new ArrayList<>();
            for (Pair<String, Integer> station : mAvailableStations) {
                if (!mFilterConditions.contains(station.second)) {
                    options.add(station);
                    optionStrings.add(station.first);
                }
            }
            boolean checkedItems[] = new boolean[optionStrings.size()];
            new AlertDialog.Builder(getActivity())
                    .setMultiChoiceItems(
                            optionStrings.toArray(new String[0]),
                            checkedItems,
                            (dialog, which, isChecked) -> {})
                    .setTitle(R.string.fragment_status_add_station)
                    .setOnDismissListener(dialog -> mAvailableStationsLock.unlock())
                    .setNegativeButton(android.R.string.cancel, (dialog, which) -> dialog.dismiss())
                    .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                        boolean added = false;
                        for (int i = 0; i < checkedItems.length; ++i) {
                            if (checkedItems[i]) {
                                addNewFilter(options.get(i));
                                added = true;
                            }
                        }
                        if (added) {
                            mSwipeRefreshLayout.setRefreshing(true);
                            refreshData(false);
                        }
                        dialog.dismiss();
                    })
                    .show();
        });

        return view;
    }

    private void addNewFilter(Pair<String, Integer> station) {
        mFilterConditions.add(station.second);
        Set<String> newFilter = new TreeSet<>();
        Set<String> filter = mSharedPreferences.getStringSet(PREF_FILTER, null);
        if (filter != null)
            newFilter.addAll(filter);
        newFilter.add(String.valueOf(station.second));
        mSharedPreferences.edit().putStringSet(PREF_FILTER, newFilter).apply();
    }

    private void removeFilter(int id) {
        mFilterConditions.remove((Integer) id);
        Set<String> newFilter = new TreeSet<>();
        Set<String> filter = mSharedPreferences.getStringSet(PREF_FILTER, null);
        if (filter != null) {
            for (String str : filter) {
                if (!str.equals(String.valueOf(id)))
                    newFilter.add(str);
            }
        }
        mSharedPreferences.edit()
                .putStringSet(PREF_FILTER, newFilter)
                .remove("STATION_" + id)
                .remove("STATION_" + id + "_DETAILS")
                .apply();
    }

    private void refreshData(boolean useCache) {
        if (mSubscription != null && !mSubscription.isUnsubscribed())
            mSubscription.unsubscribe();
        mSubscription = Observable.concat(
                Observable.fromEmitter(emitter -> {
                    if (!useCache) {
                        emitter.onCompleted();
                        return;
                    }
                    for (Station station : getStationsFromCache()) {
                        StationDetails stationDetails = getStationDetailsFromCache(station.getStationId());
                        if (stationDetails != null)
                            emitter.onNext(new Pair<>(station, stationDetails));
                    }
                    emitter.onCompleted();
                }, Emitter.BackpressureMode.DROP),
                Network.getStations(getActivity())
                        .subscribeOn(Schedulers.io())
                        .doOnNext(this::saveAvailableStations)
                        .flatMapIterable(stations -> stations)
                        .filter(this::stationNeedDetails)
                        .flatMap(station -> Network.getStationDetails(getActivity(), station.getStationId())
                                .map(stationDetails -> new Pair<>(station, stationDetails))
                                .subscribeOn(Schedulers.io()))
                        .doOnNext(this::save))
                .onErrorResumeNext(throwable -> {
                    throwable.printStackTrace();
                    return null;
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        pair -> {
                            if (pair == null) {
                                Toast.makeText(
                                        getActivity(),
                                        R.string.fragment_status_connection_problem_message,
                                        Toast.LENGTH_SHORT).show();
                            } else
                                mStatusAdapter.add(pair);
                        },
                        throwable -> {
                            throwable.printStackTrace();
                            mSwipeRefreshLayout.setRefreshing(false);
                        },
                        () -> mSwipeRefreshLayout.setRefreshing(false));
    }

    private void saveAvailableStations(ArrayList<Station> stations) {
        mAvailableStationsLock.lock();
        mAvailableStations = new ArrayList<>();
        String name;
        for (Station station : stations) {
            name = station.getStationName().trim();
            if (name.endsWith(","))
                name = name.substring(0, name.length() - 1);
            mAvailableStations.add(Pair.create(name, station.getStationId()));
        }
        Collections.sort(mAvailableStations, (o1, o2) -> COLLATOR.compare(o1.first, o2.first));
        mAvailableStationsLock.unlock();
    }

    private boolean stationNeedDetails(Station station) {
        return station.getStationName().toLowerCase().contains("legionowo")
                || mFilterConditions.contains(station.getStationId());
    }

    private void save(Pair<Station, StationDetails> stations) {
        String stationJson = mGson.toJson(stations.first);
        String stationDetailsJson = mGson.toJson(stations.second);
        mSharedPreferences
                .edit()
                .putString("STATION_" + stations.first.getStationId(), stationJson)
                .putString("STATION_" + stations.first.getStationId() + "_DETAILS", stationDetailsJson)
                .apply();
    }

    private List<Station> getStationsFromCache() {
        List<Station> stations = new ArrayList<>();
        Map<String, ?> all = mSharedPreferences.getAll();
        for (String key : all.keySet()) {
            if (key.startsWith("STATION_") && !key.endsWith("_DETAILS")) {
                stations.add(mGson.fromJson((String) all.get(key), Station.class));
            }
        }
        Log.d(TAG, "getStationsFromCache: Got " + stations.size());
        return stations;
    }

    private StationDetails getStationDetailsFromCache(int id) {
        String string = mSharedPreferences.getString("STATION_" + id + "_DETAILS", "");
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
