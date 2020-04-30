package com.eukaytek.duplicatephotocleaner.ui.detail;

import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.eukaytek.duplicatephotocleaner.BR;
import com.eukaytek.duplicatephotocleaner.R;
import com.eukaytek.duplicatephotocleaner.ui.searchresults.ResultsViewModel;

import timber.log.Timber;

public class DetailAdapter extends RecyclerView.Adapter<DetailViewHolder> {

    private ResultsViewModel viewModel;
    private int selectedKey;

    DetailAdapter(int selectedKey, ResultsViewModel model) {
        this.viewModel = model;
        this.selectedKey = selectedKey;
    }

    @NonNull
    @Override
    public DetailViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        ViewDataBinding binding = DataBindingUtil.inflate(LayoutInflater.from(viewGroup.getContext()),
                R.layout.expanded_list_item, viewGroup, false);
        binding.setVariable(BR.self, viewModel);
        return new DetailViewHolder(selectedKey, binding, viewModel);
    }

    @Override
    public void onBindViewHolder(@NonNull DetailViewHolder resultListItem, int i) {
        resultListItem.setPosition(i);
    }

    @Override
    public int getItemCount() {
        return viewModel.getItemCount(selectedKey);
    }

    public void selectItem(int clickPos) {
        Timber.d("Adapter -> Notifying item changed at position = %d", clickPos);
        notifyItemChanged(clickPos);
    }
}
