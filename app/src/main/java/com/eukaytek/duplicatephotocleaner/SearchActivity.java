package com.eukaytek.duplicatephotocleaner;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.eukaytek.duplicatephotocleaner.databinding.SearchActivityBinding;
import com.eukaytek.duplicatephotocleaner.events.DeleteFilesEvent;
import com.eukaytek.duplicatephotocleaner.events.FileDeleteCompleteEvent;
import com.eukaytek.duplicatephotocleaner.events.ScanProgressEvent;
import com.eukaytek.duplicatephotocleaner.events.ScanResultsEvent;
import com.eukaytek.duplicatephotocleaner.events.ScanningFilesCountEvent;
import com.eukaytek.duplicatephotocleaner.events.SearchResultClicked;
import com.eukaytek.duplicatephotocleaner.events.SettingsChangeEvent;
import com.eukaytek.duplicatephotocleaner.events.ShowResultsEvent;
import com.eukaytek.duplicatephotocleaner.service.ImageData;
import com.eukaytek.duplicatephotocleaner.ui.ActivityInterface;
import com.eukaytek.duplicatephotocleaner.ui.Constants;
import com.eukaytek.duplicatephotocleaner.ui.detail.DetailFragment;
import com.eukaytek.duplicatephotocleaner.ui.detailsearchresult.DetailSearchResult;
import com.eukaytek.duplicatephotocleaner.ui.help.HelpFragment;
import com.eukaytek.duplicatephotocleaner.ui.search.SearchFragment;
import com.eukaytek.duplicatephotocleaner.ui.search.SearchViewModel;
import com.eukaytek.duplicatephotocleaner.ui.searchresults.ResultsFragment;
import com.eukaytek.duplicatephotocleaner.ui.searchresults.ResultsViewModel;
import com.eukaytek.duplicatephotocleaner.ui.settings.Settings;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.reward.RewardItem;
import com.google.android.gms.ads.reward.RewardedVideoAd;
import com.google.android.gms.ads.reward.RewardedVideoAdListener;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import timber.log.Timber;

public class SearchActivity extends AppCompatActivity implements ActivityInterface {

    ResultsViewModel mModel;
    SearchViewModel mSearchModel;
    RewardedVideoAd mRewardAd;

    private void loadAd() {
        if (!BuildConfig.DEBUG) {
            mRewardAd.loadAd("ca-app-pub-7297936339438250/5629104547", new AdRequest.Builder()
                    .build());
        } else {
            mRewardAd.loadAd("ca-app-pub-3940256099942544/5224354917", new AdRequest.Builder()
                    .build());
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SearchActivityBinding binding = DataBindingUtil.setContentView(this, R.layout.search_activity);
        if (savedInstanceState == null) {
            int requestedScanType = getIntent().getIntExtra(Constants.LAUNCH_KEY_SCAN_TYPE,
                    Constants.SCAN_TYPE_DEFAULT);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, SearchFragment
                            .newInstance(requestedScanType))
                    .commitNow();

        }
        mSearchModel = ViewModelProviders.of(this).get(SearchViewModel.class);
        mRewardAd = MobileAds.getRewardedVideoAdInstance(this);
        mRewardAd.setRewardedVideoAdListener(new RewardedVideoAdListener() {
            @Override
            public void onRewardedVideoAdLoaded() {
                Timber.i("Reward ad loaded successfully");
            }

            @Override
            public void onRewardedVideoAdOpened() {

            }

            @Override
            public void onRewardedVideoStarted() {

            }

            @Override
            public void onRewardedVideoAdClosed() {
                loadAd();
            }

            @Override
            public void onRewarded(RewardItem rewardItem) {

            }

            @Override
            public void onRewardedVideoAdLeftApplication() {

            }

            @Override
            public void onRewardedVideoAdFailedToLoad(int i) {
                Timber.e("Ad failed to load for reason : %d", i);
            }

            @Override
            public void onRewardedVideoCompleted() {

            }
        });
        loadAd();
        mModel = ViewModelProviders.of(this).get(ResultsViewModel.class);
        mModel.setEventBus(EventBus.getDefault());
        mSearchModel.init(EventBus.getDefault(), this);
        binding.setViewModel(mModel);
        binding.setLifecycleOwner(this);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        // getSupportActionBar().setLogo(R.drawable.toollogo);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.defaultmenu, menu);

        MenuItem item = menu.findItem(R.id.selectAll);
        SpannableString spanString = new SpannableString(item.getTitle().toString());
        spanString.setSpan(new ForegroundColorSpan(Color.WHITE), 0, spanString.length(), 0); //fix the color to white
        item.setTitle(spanString);

        item = menu.findItem(R.id.clear);
        spanString = new SpannableString(item.getTitle().toString());
        spanString.setSpan(new ForegroundColorSpan(Color.WHITE), 0, spanString.length(), 0); //fix the color to white
        item.setTitle(spanString);

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.selectAll).setVisible(mModel.hasSelection() && !mModel.isAllSelected());
        menu.findItem(R.id.clear).setVisible(mModel.hasSelection());
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.settings:
                Timber.d("Show settings Fragment");
                showSettingsFragment();
                return true;
            case R.id.help:
                Timber.d("Show help fragment");
                showHelpFragment();
                return true;
            case R.id.selectAll:
                mModel.selectAllItems();
                return true;
            case R.id.clear:
                mModel.clearSelection();
                return true;
            case R.id.shareap:
                shareApp();
                return true;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void shareApp() {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT,
                getString(R.string.share_app_text));
        sendIntent.setType("text/plain");
        startActivity(sendIntent);
    }

    private void showSettingsFragment() {
        if (mSearchModel.isSearchInProgress().get()) {
            showWaitSnackbar();
        } else {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, Settings.newInstance())
                    .addToBackStack(null)
                    .commit();
        }
    }

    private void showWaitSnackbar() {
        Snackbar.make(findViewById(R.id.root), R.string.wait_while_scan_in_progress,
                Snackbar.LENGTH_LONG).show();
    }

    private void showHelpFragment() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, HelpFragment.newInstance())
                .addToBackStack(null)
                .commit();
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

    @Subscribe
    public void showResultsView(ShowResultsEvent event) {
        mModel.setSearchResults(event.resultSet);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, ResultsFragment.newInstance())
                .addToBackStack(null)
                .commit();
    }

    @Subscribe
    public void onSearchResultClicked(SearchResultClicked event) {
        switch (event.type) {
            case SearchResultClicked.TYPE_SEARCH_RESULT:
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container, DetailSearchResult.newInstance(event.key))
                        .addToBackStack(null)
                        .commit();
                break;
            case SearchResultClicked.TYPE_DETAIL_RESULT:
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container, DetailFragment.newInstance(event.key, event.position))
                        .addToBackStack(null)
                        .commit();
                break;
            default:
                break;
        }
    }

    @Subscribe
    public void onSettingsChanged(SettingsChangeEvent event) {
        onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


    @Override
    public ResultsViewModel getResultViewModel() {
        return mModel;
    }

    @Subscribe
    public void deleteSelectedFiles(DeleteFilesEvent event) {
        try {
            mModel.deleteFiles();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        if (mModel.hasSelection()) {
            mModel.clearSelection();
        } else {
            super.onBackPressed();
        }
    }

    @Subscribe
    public void onFileDeletedEvent(FileDeleteCompleteEvent event) {
        int currentFragment = mModel.getCurrentFragment();
        if (currentFragment == ResultsViewModel.FRAGMENT_SEARCH_RESULTS) {
            if (mModel.getSearchResults().size() == 0) {
                onBackPressed();
            }
        } else {
            int key = mModel.getDetailedSelectedKey();
            if (key == -1) {
                throw new IllegalStateException("Key can't be -1 when in detail or expanded fragment");
            }
            List<ImageData> images = mModel.getSearchResults().get(key);
            if (images == null || images.size() <= 1) {
                onBackPressed();
            }
        }
    }

    @Override
    public SearchViewModel getSearchViewModel() {
        return mSearchModel;
    }


    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onProgressUpdate(ScanProgressEvent event) {
        EventBus.getDefault().removeStickyEvent(event);
        mSearchModel.setCurrentProgress(event.currentProgress);
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onScanFinished(ScanResultsEvent event) {
        if (mRewardAd.isLoaded()) {
            mRewardAd.show();
            Timber.i("Ad loaded -> showing ad");
        } else {
            Timber.e("Can't show ad: Ad not loaded");
        }
        EventBus.getDefault().removeStickyEvent(event);
        mSearchModel.setScanResult(event.resultSet);
        mSearchModel.setSearchInProgress(false);
        if (event.resultSet == null || event.resultSet.size() == 0) {
            showZeroResultSnackBar();
        }

    }

    private void showZeroResultSnackBar() {
        Snackbar.make(findViewById(R.id.root), R.string.no_duplicates_found, Snackbar.LENGTH_LONG)
                .setActionTextColor(Color.WHITE)
                .setAction(R.string.new_folder_ok, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                })
                .show();
    }


    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onScanFilesCountEvent(ScanningFilesCountEvent event) {
        EventBus.getDefault().removeStickyEvent(event);
        mSearchModel.setScanFilesCount(event.filesCount);
    }
}
