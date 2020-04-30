package com.eukaytek.duplicatephotocleaner.ui.settings;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.eukaytek.duplicatephotocleaner.R;
import com.eukaytek.duplicatephotocleaner.databinding.SettingsFragmentBinding;

import org.greenrobot.eventbus.EventBus;

public class Settings extends Fragment {

    SettingsFragmentBinding binding;
    private SettingsViewModel mViewModel;

    public static Settings newInstance() {
        return new Settings();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.settings_fragment, container, false);
        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new SettingsViewModel(getContext(), EventBus.getDefault());
        binding.setViewModel(mViewModel);
    }
}
