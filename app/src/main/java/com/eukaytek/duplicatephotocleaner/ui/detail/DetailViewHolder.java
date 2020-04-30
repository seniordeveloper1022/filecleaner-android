package com.eukaytek.duplicatephotocleaner.ui.detail;

import android.databinding.ViewDataBinding;
import android.support.v7.widget.RecyclerView;

import com.eukaytek.duplicatephotocleaner.BR;
import com.eukaytek.duplicatephotocleaner.ui.searchresults.ResultsViewModel;

public class DetailViewHolder extends RecyclerView.ViewHolder {

    private ViewDataBinding binding;
    private ResultsViewModel viewModel;
    private int selectedKey;


    public DetailViewHolder(int key, ViewDataBinding binding, ResultsViewModel viewModel) {
        super(binding.getRoot());
        this.binding = binding;
        this.viewModel = viewModel;
        this.selectedKey = key;
    }

    public void setPosition(int position) {
        binding.setVariable(BR.key, selectedKey);
        binding.setVariable(BR.position, position);
        binding.setVariable(BR.viewModel, viewModel);
        binding.notifyChange();
    }
}
