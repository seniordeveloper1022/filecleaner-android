package com.eukaytek.duplicatephotocleaner.ui.search;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BaseTransientBottomBar;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.eukaytek.duplicatephotocleaner.R;
import com.eukaytek.duplicatephotocleaner.databinding.SearchFragmentBinding;
import com.eukaytek.duplicatephotocleaner.events.ScanFileEvent;
import com.eukaytek.duplicatephotocleaner.events.StartFolderSelectionEvent;
import com.eukaytek.duplicatephotocleaner.events.StartSearchEvent;
import com.eukaytek.duplicatephotocleaner.service.DuplicateFinderService;
import com.eukaytek.duplicatephotocleaner.ui.ActivityInterface;
import com.eukaytek.duplicatephotocleaner.ui.Constants;
import com.obsez.android.lib.filechooser.ChooserDialog;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.File;
import java.util.List;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;
import timber.log.Timber;

public class SearchFragment extends Fragment implements EasyPermissions.PermissionCallbacks {

    private static final String requiredPermissions[] = new String[]
            {Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private static final int RC_READ_WRITE_EXTERNAL_STORAGE = 0x22;
    private static final int RC_SELECT_READ_WRITE_EXTERNAL_STORAGE = 0x23;
    private SearchViewModel mViewModel;
    private String folderToScan[] = null;
    private int requestedScanType = -1;
    private ActivityInterface activityInterface;

    public static SearchFragment newInstance(int scanType) {
        Bundle bundle = new Bundle();
        bundle.putInt(Constants.LAUNCH_KEY_SCAN_TYPE, scanType);
        SearchFragment fragment = new SearchFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        SearchFragmentBinding binding = DataBindingUtil.inflate(inflater, R.layout.search_fragment, container, false);
        requestedScanType = getArguments().getInt(Constants.LAUNCH_KEY_SCAN_TYPE);
        mViewModel = activityInterface.getSearchViewModel();
        assert getArguments() != null;
        mViewModel.setScanType(getArguments().getInt(Constants.LAUNCH_KEY_SCAN_TYPE));
        binding.setViewModel(mViewModel);
        return binding.getRoot();
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof ActivityInterface) {
            activityInterface = (ActivityInterface) context;
        } else {
            throw new IllegalArgumentException("Hosting activity must implement ActivityInterface");
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe
    public void startStopScan(StartSearchEvent event) {
        if (event.start) {
            if (EasyPermissions.hasPermissions(getContext(), requiredPermissions)) {
                startScanning();
            } else {
                requestPermissionAndStartScan(true);
            }

        } else {
            DuplicateFinderService.shouldContinue = false;
            //mViewModel.setSearchInProgress(false);
            //mViewModel.setCurrentProgress(0);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    private void requestPermissionAndStartScan(boolean onStart) {
        EasyPermissions.requestPermissions(this, getString(R.string.storage_permission),
                onStart ? RC_READ_WRITE_EXTERNAL_STORAGE : RC_SELECT_READ_WRITE_EXTERNAL_STORAGE
                , requiredPermissions);
    }

    @AfterPermissionGranted(RC_READ_WRITE_EXTERNAL_STORAGE)
    private void startScanning() {
        Intent intent = new Intent(getContext(), DuplicateFinderService.class);
        intent.setAction(folderToScan == null ? DuplicateFinderService.ACTION_FIND_DUPLICATE :
                DuplicateFinderService.ACTION_FIND_DUPLICATE_IN_FOLDERS);
        intent.putExtra(Constants.LAUNCH_KEY_SCAN_TYPE, requestedScanType);
        if (folderToScan != null) {
            intent.putExtra(Constants.SCAN_FOLDER_LIST, folderToScan);
            folderToScan = null;
        }
        ContextCompat.startForegroundService(getContext(), intent);
        mViewModel.setSearchInProgress(true);
    }

    @AfterPermissionGranted(RC_SELECT_READ_WRITE_EXTERNAL_STORAGE)
    private void selectFolderForScanning() {
        ChooserDialog chooser = new ChooserDialog(getActivity())
                .withFilter(true, false)
                .withStartFile(Environment.getExternalStorageDirectory().getPath())
                .enableMultiple(false)
                .build();
        chooser.withChosenListener(new ChooserDialog.Result() {
            @Override
            public void onChoosePath(String s, File file) {
                Timber.d("Selected directory = %s", s);
                mViewModel.setSelectedFolder(s);
                folderToScan = new String[]{s};
            }
        });
        chooser.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Subscribe
    public void onFileNameEvent(ScanFileEvent event) {
        Timber.d("Scanning file : %s", event.currentFile);
    }

    @Subscribe
    public void onFolderSelectionEvent(StartFolderSelectionEvent event) {
        requestPermissionAndStartScan(false);
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {

    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        mViewModel.setSearchInProgress(false);
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            new AppSettingsDialog.Builder(this)
                    .setRequestCode(requestCode)
                    .build().show();
        } else {
            Snackbar.make(getActivity().findViewById(R.id.root), R.string.storage_permission,
                    BaseTransientBottomBar.LENGTH_LONG).show();
        }
    }
}
