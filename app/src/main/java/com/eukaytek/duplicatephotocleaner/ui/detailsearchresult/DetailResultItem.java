package com.eukaytek.duplicatephotocleaner.ui.detailsearchresult;

import android.databinding.ViewDataBinding;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.eukaytek.duplicatephotocleaner.BR;
import com.eukaytek.duplicatephotocleaner.databinding.DetailResultItemBinding;
import com.eukaytek.duplicatephotocleaner.ui.searchresults.ResultsViewModel;

public class DetailResultItem extends RecyclerView.ViewHolder {

    ViewDataBinding binding;
    private ResultsViewModel viewModel;
    private int key;

    public DetailResultItem(@NonNull View itemView, ResultsViewModel viewModel, int key, DetailResultItemBinding binding) {
        super(itemView);
        this.key = key;
        this.viewModel = viewModel;
        this.binding = binding;
    }

    public void setPosition(int adapterPosition) {
        binding.setVariable(BR.key, key);
        binding.setVariable(BR.position, adapterPosition);
        binding.notifyChange();
    }

}
