package pl.kksionek.smogogrod.view;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import pl.kksionek.smogogrod.R;
import pl.kksionek.smogogrod.model.Network;
import pl.kksionek.smogogrod.model.StatusAdapter;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class StatusFragment extends Fragment {

    RecyclerView mRecyclerView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_status, null);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        StatusAdapter statusAdapter = new StatusAdapter();
        mRecyclerView.setAdapter(statusAdapter);

        Network.getLegionowoStationDetails(getContext())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(statusAdapter::add,
                        Throwable::printStackTrace);

        return view;
    }
}
