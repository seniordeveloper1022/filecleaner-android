package com.eukaytek.duplicatephotocleaner.ui.search;

import android.arch.lifecycle.ViewModel;
import android.content.Context;
import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;
import android.databinding.ObservableInt;
import android.databinding.ObservableLong;
import android.os.Handler;
import android.util.SparseArray;
import android.view.View;

import com.eukaytek.duplicatephotocleaner.events.ShowResultsEvent;
import com.eukaytek.duplicatephotocleaner.events.StartFolderSelectionEvent;
import com.eukaytek.duplicatephotocleaner.events.StartSearchEvent;
import com.eukaytek.duplicatephotocleaner.service.ImageData;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import timber.log.Timber;

public class SearchViewModel extends ViewModel {

    private int scanType;
    private Runnable timerRunnable;
    private ObservableInt currentProgress;
    private ObservableInt mGetFixButtonVisibility;
    private Handler handler;
    private ObservableBoolean searchInProgress;
    private ObservableField<String> mCurrentSelectedFolder;
    private ObservableInt mDuplicateFilesCount;
    private ObservableInt selectFolderVisibility;
    private ObservableLong mElapsedTimer;

    private EventBus eventBus;
    private SparseArray<List<ImageData>> resultSet;
    private String lastSelectedFolder = null;
    private ObservableInt mScanFilesCount;
    private ObservableField<SearchState> mCurrentState;

    public SearchViewModel() {
        currentProgress = new ObservableInt(0);
        mGetFixButtonVisibility = new ObservableInt(View.GONE);
        searchInProgress = new ObservableBoolean(Boolean.FALSE);
        mCurrentSelectedFolder = new ObservableField<>("");
        mDuplicateFilesCount = new ObservableInt(0);
        selectFolderVisibility = new ObservableInt(View.VISIBLE);
        mScanFilesCount = new ObservableInt(0);
        mElapsedTimer = new ObservableLong(0);
        handler = new Handler();
        mCurrentState = new ObservableField<>(SearchState.SEARCH_STATE_INITIAL);
    }

    public void init(EventBus bus, Context context) {
        this.eventBus = bus;
    }

    public int getScanType() {
        return scanType;
    }

    public void setScanType(int scanType) {
        this.scanType = scanType;
    }

    public int getProgressViewVisibility() {
        return View.VISIBLE; //searchInProgress ? View.VISIBLE : View.INVISIBLE;
    }

    public void setSearchInProgress(boolean status) {
        this.searchInProgress.set(status);
        if (status) {
            lastSelectedFolder = mCurrentSelectedFolder.get();
            selectFolderVisibility.set(View.GONE);
            mCurrentSelectedFolder.set(null);
            mCurrentState.set(SearchState.SEARCH_STATE_SEARCHING);
        } else {
            mScanFilesCount.set(0);
            handler.removeCallbacks(timerRunnable);
            selectFolderVisibility.set(resultSet == null || resultSet.size() == 0 ? View.VISIBLE
                    : View.GONE);
            mCurrentState.set(resultSet == null || resultSet.size() == 0 ?
                    SearchState.SEARCH_STATE_INITIAL : SearchState.SEARCH_STATE_COMPLETED);
            setCurrentProgress(resultSet == null || resultSet.size() == 0 ? 0 : 100);

        }
    }

    public ObservableInt currentSearchProgress() {
        return currentProgress;
    }

    public ObservableBoolean isSearchInProgress() {
        return searchInProgress;
    }

    public void setCurrentProgress(int progress) {
        this.currentProgress.set(progress);
    }

    public void onStartStopClicked() {
        StartSearchEvent event = new StartSearchEvent();
        event.start = !this.searchInProgress.get();
        if (mCurrentState.get() == SearchState.SEARCH_STATE_COMPLETED) {
            mCurrentState.set(SearchState.SEARCH_STATE_INITIAL);
            setScanResult(null);
            currentProgress.set(0);
            setSearchInProgress(false);
        } else {
            if (event.start) {
                currentProgress.set(1);
                mElapsedTimer.set(0);
                setScanResult(null);
                setSearchInProgress(true);
            } else {
                //currentProgress.set(100);
                //setSearchInProgress(false);
            }
            eventBus.post(event);
        }
    }

    public void setScanResult(SparseArray<List<ImageData>> resultSet) {
        this.resultSet = resultSet;
        int count = 0;
        if (resultSet != null && resultSet.size() > 0) {
            for (int i = 0; i < resultSet.size(); i++) {
                int key = resultSet.keyAt(i);
                count += resultSet.get(key).size() - 1;
            }
            mCurrentState.set(SearchState.SEARCH_STATE_COMPLETED);
        }
        mGetFixButtonVisibility.set(count > 0 ? View.VISIBLE : View.GONE);
        mDuplicateFilesCount.set(count);
    }

    public void onSelectFolderClicked() {
        eventBus.post(new StartFolderSelectionEvent());
    }

    public void setSelectedFolder(String folder) {
        mCurrentSelectedFolder.set(folder);
    }

    public ObservableInt getFixButtonVisibility() {
        return mGetFixButtonVisibility;
    }

    public ObservableField<String> getSelectedFolderText() {
        return mCurrentSelectedFolder;
    }

    public void showResultView() {
        ShowResultsEvent event = new ShowResultsEvent();
        event.resultSet = resultSet;
        eventBus.post(event);
    }

    public ObservableInt getDuplicateCount() {
        return mDuplicateFilesCount;
    }

    public ObservableInt getSelectFolderVisibility() {
        return selectFolderVisibility;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        Timber.e("OnCleared called ->>> unexpected");
    }

    public SparseArray<List<ImageData>> getResultSet() {
        return resultSet;
    }

    public ObservableInt getScanFilesCount() {
        return mScanFilesCount;
    }

    public void setScanFilesCount(int filesCount) {
        mScanFilesCount.set(filesCount);
        if (timerRunnable == null) {
            timerRunnable = new Runnable() {
                @Override
                public void run() {
                    mElapsedTimer.set(mElapsedTimer.get() + 1);
                    handler.postDelayed(timerRunnable, 1000);
                }
            };
        }
        handler.postDelayed(timerRunnable, 1000);

    }

    public ObservableLong getElapsedTime() {
        return mElapsedTimer;
    }

    public ObservableField<SearchState> getCurrentSearchState() {
        return mCurrentState;
    }
}
