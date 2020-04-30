package com.eukaytek.duplicatephotocleaner.ui.searchresults;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.eukaytek.duplicatephotocleaner.R;
import com.eukaytek.duplicatephotocleaner.events.FileDeleteCompleteEvent;
import com.eukaytek.duplicatephotocleaner.events.ItemClickEvent;
import com.eukaytek.duplicatephotocleaner.events.SelectAllEvent;
import com.eukaytek.duplicatephotocleaner.events.UpdateAdapterEvent;
import com.eukaytek.duplicatephotocleaner.service.ImageData;
import com.eukaytek.duplicatephotocleaner.ui.ActivityInterface;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.List;

import timber.log.Timber;

public class ResultsFragment extends Fragment {

    private ActivityInterface activityInterface;
    private ResultsAdapter adapter;
    private boolean isLongClickRunning = false;
    private RecyclerView recyclerView;

    public static ResultsFragment newInstance() {
        return new ResultsFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.results_fragment, container, false);
        recyclerView = view.findViewById(R.id.resultsListView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(),
                LinearLayoutManager.VERTICAL, false));
        adapter = new ResultsAdapter(activityInterface.getResultViewModel());
        recyclerView.setAdapter(adapter);
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof ActivityInterface) {
            activityInterface = (ActivityInterface) context;
        } else {
            throw new IllegalArgumentException("Hosting activity must implement ActivityInterface");
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        Timber.d("Setting Results Fragment to current");
        activityInterface.getResultViewModel().setCurrentFragment(ResultsViewModel.FRAGMENT_SEARCH_RESULTS);
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe
    public void onItemClickEvent(ItemClickEvent event) {
        if (event.clickType == ItemClickEvent.LONG_CLICK) {
            getActivity().invalidateOptionsMenu();
            if (event.clickKey == -1 && event.clickPos == -1) {
                adapter.notifyItemRangeChanged(0, adapter.getItemCount());
            } else {
                activityInterface.getResultViewModel()
                        .selectItemAtPos(event.clickKey, event.clickPos);
                adapter.selectItem(event.clickPos);
            }
        }
    }

    @Subscribe
    public void onFileDeletedEvent(FileDeleteCompleteEvent event) {
        adapter.notifyDataSetChanged();
    }

    @Subscribe
    public void selectAllEvent(SelectAllEvent event) {
        ResultsViewModel model = activityInterface.getResultViewModel();
        SparseArray<List<ImageData>> searchResults = model.getSearchResults();
        for (int i = 0; i < searchResults.size(); i++) {
            int key = searchResults.keyAt(i);
            model.selectItemAtPos(key, i, false);
        }
        adapter.notifyDataSetChanged();
    }

    @Subscribe
    public void updateAdapter(UpdateAdapterEvent event) {
        adapter.notifyDataSetChanged();
    }
}
