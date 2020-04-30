package com.eukaytek.duplicatephotocleaner.ui.detail;

import android.content.Context;
import android.databinding.DataBindingUtil;
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
import com.eukaytek.duplicatephotocleaner.databinding.DetailFragmentBinding;
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

public class DetailFragment extends Fragment {

    DetailAdapter adapter = null;
    private ActivityInterface activityInterface;

    public static DetailFragment newInstance(int key, int position) {
        Bundle args = new Bundle();
        args.putInt(Constants.LAUNCH_SELECTED_KEY, key);
        args.putInt(Constants.LAUNCH_SELECTED_POS, position);
        DetailFragment f = new DetailFragment();
        f.setArguments(args);
        return f;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        DetailFragmentBinding binding = DataBindingUtil.inflate(inflater, R.layout.detail_fragment, container, false);
        RecyclerView recyclerView = binding.detailListView;
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        int key = getArguments().getInt(Constants.LAUNCH_SELECTED_KEY);
        adapter = new DetailAdapter(key, activityInterface.getResultViewModel());
        recyclerView.setAdapter(adapter);
        recyclerView.scrollToPosition(getArguments().getInt(Constants.LAUNCH_SELECTED_POS));
        return binding.getRoot();
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
        Timber.d("Setting Expanded Fragment to current");
        activityInterface.getResultViewModel().setExpandedView(true,
                getArguments().getInt(Constants.LAUNCH_SELECTED_KEY));
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
