package com.eukaytek.duplicatephotocleaner.ui.searchresults;

import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.eukaytek.duplicatephotocleaner.BR;
import com.eukaytek.duplicatephotocleaner.R;

import timber.log.Timber;

public class ResultsAdapter extends RecyclerView.Adapter<ResultListItem> {

    private ResultsViewModel viewModel;

    ResultsAdapter(ResultsViewModel model) {
        super();
        this.viewModel = model;
    }

    @NonNull
    @Override
    public ResultListItem onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        ViewDataBinding binding = DataBindingUtil.inflate(LayoutInflater.from(viewGroup.getContext()),
                R.layout.result_list_item, viewGroup, false);
        binding.setVariable(BR.viewModel, viewModel);
        return new ResultListItem(binding, viewModel);
    }

    @Override
    public void onBindViewHolder(@NonNull ResultListItem resultListItem, int i) {
        resultListItem.setPosition(i);
    }

    @Override
    public int getItemCount() {
        int count = viewModel.getSearchResults() == null ? 0 : viewModel.getSearchResults().size();
        Timber.d("ItemCount = %d", count);
        return count;
    }

    public void selectItem(int clickPos) {
        Timber.d("Adapter -> Notifying item changed at position = %d", clickPos);
        notifyItemChanged(clickPos);
    }
}
