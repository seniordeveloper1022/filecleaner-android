package com.eukaytek.duplicatephotocleaner.ui.searchresults;

import android.databinding.ViewDataBinding;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;

import com.eukaytek.duplicatephotocleaner.BR;

public class ResultListItem extends RecyclerView.ViewHolder {

    private ResultsViewModel viewModel;
    private ViewDataBinding binding;

    public ResultListItem(@NonNull ViewDataBinding binding, ResultsViewModel model) {
        super(binding.getRoot());
        this.binding = binding;
        this.viewModel = model;
    }

    public void setPosition(int adapterPosition) {
        binding.setVariable(BR.key, viewModel.getKeySetAtPosition(adapterPosition));
        binding.setVariable(BR.position, adapterPosition);
        binding.notifyChange();
    }
}
