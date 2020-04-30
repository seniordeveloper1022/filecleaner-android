package com.eukaytek.duplicatephotocleaner;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.eukaytek.duplicatephotocleaner.events.ScanExactEvent;
import com.eukaytek.duplicatephotocleaner.events.ScanSimilarEvent;
import com.eukaytek.duplicatephotocleaner.ui.Constants;
import com.eukaytek.duplicatephotocleaner.ui.main.MainFragment;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, MainFragment.newInstance())
                    .commitNow();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);

    }

    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onScanExact(ScanExactEvent event) {
        launchScanActivity(Constants.SCAN_TYPE_EXACT);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onScanSimilar(ScanSimilarEvent event) {
        launchScanActivity(Constants.SCAN_TYPE_SIMILAR);
    }

    private void launchScanActivity(int scanType) {
        Intent scanActivityIntent = new Intent(this, SearchActivity.class);
        scanActivityIntent.putExtra(Constants.LAUNCH_KEY_SCAN_TYPE, scanType);
        finish();
        startActivity(scanActivityIntent);
    }
}
