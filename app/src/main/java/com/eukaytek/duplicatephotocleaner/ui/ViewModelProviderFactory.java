package com.eukaytek.duplicatephotocleaner.ui;

import android.app.Application;
import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.content.Context;
import android.support.annotation.NonNull;

import com.eukaytek.duplicatephotocleaner.ui.main.MainViewModel;

import org.greenrobot.eventbus.EventBus;

public class ViewModelProviderFactory implements ViewModelProvider.Factory {

    private Context context;
    private EventBus bus;
    private Application app;

    public ViewModelProviderFactory(Application application, Context context, EventBus eventBus) {
        this.context = context;
        this.bus = eventBus;
        this.app = application;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        ViewModel viewModel = null;
        if (modelClass == MainViewModel.class) {
            viewModel = new MainViewModel(bus);
        }
        //noinspection ConstantConditions
        return modelClass.cast(viewModel);
    }

}
