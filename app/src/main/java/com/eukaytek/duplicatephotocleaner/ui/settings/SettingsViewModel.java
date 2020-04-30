package com.eukaytek.duplicatephotocleaner.ui.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.databinding.BaseObservable;
import android.view.View;
import android.widget.SeekBar;

import com.eukaytek.duplicatephotocleaner.R;
import com.eukaytek.duplicatephotocleaner.events.SettingsChangeEvent;
import com.eukaytek.duplicatephotocleaner.ui.Constants;

import org.greenrobot.eventbus.EventBus;

public class SettingsViewModel extends BaseObservable {

    private int currentProgress;
    private boolean compareRotation;
    private boolean compareGrayScale;

    private Context context;
    private SharedPreferences preferences;
    private EventBus eventBus;


    public SettingsViewModel(Context context, EventBus bus) {
        this.context = context;
        preferences = context.getSharedPreferences("default", Context.MODE_PRIVATE);
        compareRotation = preferences.getBoolean(Constants.PREF_KEY_COMPARE_ROTATION, Constants.DEFAULT_COMPARE_ROTATION);
        compareGrayScale = preferences.getBoolean(Constants.PREF_KEY_COMPARE_GRAYSCALE, Constants.DEFAULT_COMPARE_GRAYSCALE);
        currentProgress = preferences.getInt(Constants.PREF_KEY_CURRENT_THRESHOLD, Constants.DEFAULT_THRESHOLD);
        this.eventBus = bus;
    }

    public String getCurrentThreshold() {
        return context.getString(R.string.similarityThreshold, currentProgress);
    }

    public void onProgressChanged(SeekBar seekBar, int progresValue, boolean fromUser) {
        currentProgress = progresValue;
        if (progresValue < 40) {
            seekBar.setProgress(40);
        }
        notifyChange();
    }

    public int getCurrentProgress() {
        return currentProgress;
    }

    public boolean getOptionRotation() {
        return compareRotation;
    }

    public boolean getOptionGrayScale() {
        return compareGrayScale;
    }

    public void onOptionGrayScaleChanged(View view, boolean checked) {
        compareGrayScale = checked;
    }

    public void onOptionRotationChanged(View view, boolean checked) {
        compareRotation = checked;
    }

    public void onCancelSettings() {
        eventBus.post(new SettingsChangeEvent(SettingsChangeEvent.CANCEL));
    }

    public void onApplySettings() {
        preferences.edit().putBoolean(Constants.PREF_KEY_COMPARE_GRAYSCALE, compareGrayScale)
                .putBoolean(Constants.PREF_KEY_COMPARE_ROTATION, compareRotation)
                .putInt(Constants.PREF_KEY_CURRENT_THRESHOLD, currentProgress)
                .apply();
        eventBus.post(new SettingsChangeEvent(SettingsChangeEvent.APPLY));
    }

    public void onRestoreSettings() {
        currentProgress = Constants.DEFAULT_THRESHOLD;
        compareRotation = Constants.DEFAULT_COMPARE_ROTATION;
        compareGrayScale = Constants.DEFAULT_COMPARE_GRAYSCALE;
        notifyChange();
    }
}
