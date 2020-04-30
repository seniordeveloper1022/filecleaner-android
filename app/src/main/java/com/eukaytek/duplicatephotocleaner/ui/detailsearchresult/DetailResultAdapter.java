package com.eukaytek.duplicatephotocleaner.ui.detailsearchresult;

import android.databinding.DataBindingUtil;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.eukaytek.duplicatephotocleaner.R;
import com.eukaytek.duplicatephotocleaner.databinding.DetailResultItemBinding;
import com.eukaytek.duplicatephotocleaner.ui.searchresults.ResultsViewModel;

import timber.log.Timber;

public class DetailResultAdapter extends RecyclerView.Adapter<DetailResultItem> {

    private ResultsViewModel viewModel;
    private int selectionKey;

    public DetailResultAdapter(ResultsViewModel viewModel, int key) {
        this.viewModel = viewModel;
        this.selectionKey = key;
    }

    @NonNull
    @Override
    public DetailResultItem onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        DetailResultItemBinding binding = DataBindingUtil.inflate(
                LayoutInflater.from(viewGroup.getContext()),
                R.layout.detail_result_item, viewGroup, false);
        DetailResultItem item = new DetailResultItem(binding.getRoot(), viewModel, selectionKey, binding);
        binding.setSelf(viewModel);
        binding.setKey(selectionKey);
        return item;
    }

    @Override
    public void onBindViewHolder(@NonNull DetailResultItem detailResultItem, int i) {
        detailResultItem.setPosition(i);
    }

    @Override
    public int getItemCount() {
        return viewModel.getItemCount(selectionKey);
    }

    public void selectItem(int clickPos) {
        Timber.d("Adapter -> Notifying item changed at position = %d", clickPos);
        notifyItemChanged(clickPos);
    }
}
