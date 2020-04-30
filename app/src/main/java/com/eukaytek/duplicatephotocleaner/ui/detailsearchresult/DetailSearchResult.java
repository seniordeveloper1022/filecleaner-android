package com.eukaytek.duplicatephotocleaner.ui.detailsearchresult;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PagerSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.eukaytek.duplicatephotocleaner.R;
import com.eukaytek.duplicatephotocleaner.events.ItemClickEvent;
import com.eukaytek.duplicatephotocleaner.events.SelectAllEvent;
import com.eukaytek.duplicatephotocleaner.events.UpdateAdapterEvent;
import com.eukaytek.duplicatephotocleaner.service.ImageData;
import com.eukaytek.duplicatephotocleaner.ui.ActivityInterface;
import com.eukaytek.duplicatephotocleaner.ui.Constants;
import com.eukaytek.duplicatephotocleaner.ui.searchresults.ResultsViewModel;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.List;

import timber.log.Timber;

public class DetailSearchResult extends Fragment {

    DetailResultAdapter adapter = null;
    private ActivityInterface activityInterface;

    public static DetailSearchResult newInstance(int selectionKey) {
        DetailSearchResult fragment = new DetailSearchResult();
        Bundle args = new Bundle();
        args.putInt(Constants.LAUNCH_SELECTED_KEY, selectionKey);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        int selectedKey = getArguments().getInt(Constants.LAUNCH_SELECTED_KEY);
        View view = inflater.inflate(R.layout.detail_search_result_fragment, container, false);
        RecyclerView recyclerView = view.findViewById(R.id.detailListView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(),
                LinearLayoutManager.VERTICAL, false));
        new PagerSnapHelper().attachToRecyclerView(recyclerView);
        adapter = new DetailResultAdapter(activityInterface.getResultViewModel(), selectedKey);
        recyclerView.setAdapter(adapter);
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof ActivityInterface) {
            activityInterface = (ActivityInterface) context;
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        Timber.d("Setting Detail Fragment to current");
        activityInterface.getResultViewModel().setExpandedView(true,
                getArguments().getInt(Constants.LAUNCH_SELECTED_KEY));
        activityInterface.getResultViewModel().setCurrentFragment(ResultsViewModel.FRAGMENT_DETAILED_RESULTS);
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
    public void selectAllEvent(SelectAllEvent event) {
        ResultsViewModel model = activityInterface.getResultViewModel();
        int key = getArguments().getInt(Constants.LAUNCH_SELECTED_KEY);
        SparseArray<List<ImageData>> searchResults = model.getSearchResults();
        for (int i = 0; i < searchResults.get(key).size(); i++) {
            model.selectItemAtPos(key, i, false);
        }
        adapter.notifyDataSetChanged();
    }

    @Subscribe
    public void updateAdapter(UpdateAdapterEvent event) {
        adapter.notifyDataSetChanged();
    }
}
