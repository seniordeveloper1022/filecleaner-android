package com.eukaytek.duplicatephotocleaner.ui.main;

import android.arch.lifecycle.ViewModel;

import com.eukaytek.duplicatephotocleaner.events.ScanExactEvent;
import com.eukaytek.duplicatephotocleaner.events.ScanSimilarEvent;

import org.greenrobot.eventbus.EventBus;

public class MainViewModel extends ViewModel {

    private EventBus eventBus;

    public MainViewModel(EventBus bus) {
        this.eventBus = bus;
    }


    public void scanExactPhoto() {
        eventBus.post(new ScanExactEvent());
    }

    public void scanSimilarPhoto() {
        eventBus.post(new ScanSimilarEvent());
    }

}
