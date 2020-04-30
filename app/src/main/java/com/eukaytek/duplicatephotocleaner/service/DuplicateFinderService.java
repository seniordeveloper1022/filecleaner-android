package com.eukaytek.duplicatephotocleaner.service;

import android.annotation.TargetApi;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.SparseArray;

import com.eukaytek.duplicatephotocleaner.R;
import com.eukaytek.duplicatephotocleaner.events.ScanProgressEvent;
import com.eukaytek.duplicatephotocleaner.events.ScanResultsEvent;
import com.eukaytek.duplicatephotocleaner.events.ScanningFilesCountEvent;
import com.eukaytek.duplicatephotocleaner.ui.Constants;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import timber.log.Timber;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class DuplicateFinderService extends IntentService {

    public static final String ACTION_FIND_DUPLICATE = DuplicateFinderService.class.getName();
    public static final String ACTION_FIND_DUPLICATE_IN_FOLDERS =
            DuplicateFinderService.class.getName() + ".ACTION_SEARCH_IN_FOLDERS";
    private static final int FOREGROUND_ID = 0x1123;
    private static final int NOTIFICATION_ID = 0x1124;
    public static volatile boolean shouldContinue = true;
    private int currentThreshold;
    private SparseArray<List<ImageData>> searchResult = new SparseArray<>();

    public DuplicateFinderService() {
        super("DuplicateFinderService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            shouldContinue = true;
            currentThreshold = getSharedPreferences("default", Context.MODE_PRIVATE)
                    .getInt(Constants.PREF_KEY_CURRENT_THRESHOLD, Constants.DEFAULT_THRESHOLD);
            startForeground(FOREGROUND_ID, buildNotificationForScanning());
            if (ACTION_FIND_DUPLICATE.equals(action)) {
                int scanType = intent.getIntExtra(Constants.LAUNCH_KEY_SCAN_TYPE,
                        Constants.SCAN_TYPE_DEFAULT);
                scanAllFolders(scanType);
            } else if (ACTION_FIND_DUPLICATE_IN_FOLDERS.equals(action)) {

                int scanType = intent.getIntExtra(Constants.LAUNCH_KEY_SCAN_TYPE,
                        Constants.SCAN_TYPE_DEFAULT);

                String[] folderList = intent.getStringArrayExtra(Constants.SCAN_FOLDER_LIST);
                scanForDuplicatesInFoldes(scanType, folderList);
            }
        }
    }

    private void scanForDuplicatesInFoldes(int scanType, String[] folderList) {
        ArrayList<ImageData> listOfImages = null;
        for (final String path : folderList) {
            File directory = new File(path);
            File[] files = directory.listFiles(new FileFilter() {
                @Override
                public boolean accept(File pathname) {
                    String fullPath = pathname.getPath();
                    return fullPath.endsWith(".jpeg") ||
                            fullPath.endsWith(".jpg") ||
                            fullPath.endsWith(".png") ||
                            fullPath.endsWith(".tiff");
                }
            });
            if (files != null) {
                if (listOfImages == null) {
                    listOfImages = new ArrayList<>();
                }
                for (File file : files) {
                    Timber.d("File = %s", file.getPath());
                    ImageData data = new ImageData(file.getPath(), file.length());
                    listOfImages.add(data);
                }
            }
        }

        removeSmallPics(listOfImages);
        scanForDuplicates(scanType, listOfImages);

    }

    private Notification buildNotificationForScanning() {
        String channel = "";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            channel = createChannel();
        }
        NotificationCompat.Builder b = new NotificationCompat.Builder(this,
                channel);
        b.setOngoing(true)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(getString(R.string.scanningInProgress))
                .setTicker(getString(R.string.scanningFindingDuplicate))
                .setSmallIcon(R.drawable.ic_dup_notif)
                .setPriority(NotificationCompat.PRIORITY_HIGH).setAutoCancel(false);
        Timber.d("Creating notification");
        return (b.build());
    }

    private void scanAllFolders(int scanType) {
        Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        String[] projection = {MediaStore.MediaColumns.DATA, MediaStore.MediaColumns.SIZE,
                MediaStore.MediaColumns.MIME_TYPE};

        String[] whereArgs = {"image/jpeg", "image/png", "image/jpg", "image/tiff"};

        String where = MediaStore.Images.Media.MIME_TYPE + "=? or "
                + MediaStore.Images.Media.MIME_TYPE + "=? or "
                + MediaStore.Images.Media.MIME_TYPE + "=? or "
                + MediaStore.Images.Media.MIME_TYPE + "=? ";

        Cursor cursor = getContentResolver()
                .query(uri, projection, where, whereArgs, null);

        ArrayList<ImageData> listImages = null;
        if (cursor != null) {
            listImages = new ArrayList<>(cursor.getCount());
            cursor.moveToFirst();
            while (cursor.moveToNext()) {
                ImageData data = new ImageData(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA)),
                        cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.SIZE)));
                listImages.add(data);
            }
            cursor.close();
            removeSmallPics(listImages);
            scanForDuplicates(scanType, listImages);
        }
    }

    private void scanForDuplicates(int scanType, ArrayList<ImageData> listImages) {
        searchResult.clear();
        if (listImages != null && listImages.size() > 1) {
            EventBus.getDefault().postSticky(new ScanningFilesCountEvent(listImages.size()));
            double waveletMatix[][] = new double[128][128];
            ListIterator<ImageData> dataListIterator = listImages.listIterator();
            while (dataListIterator.hasNext() && shouldContinue) {
                int i = dataListIterator.nextIndex();
                ImageData image = dataListIterator.next();
                ImageProcessing.getMatricesForBitmap(image.getImagePath(), waveletMatix,
                        image.positiveCoeff, image.negativeCoeff);
                boolean fountMatch = false;
                for (int j = i - 1; j > 0; j--) {
                    ImageData prevImage = listImages.get(j);
                    double similarity = ImageProcessing.getSimilarity(waveletMatix,
                            prevImage.positiveCoeff, prevImage.negativeCoeff);
                    if (similarity >= currentThreshold) {
                        fountMatch = true;
                        if ((scanType == Constants.SCAN_TYPE_EXACT) && (similarity >= 100)) {
                            image.setSimilarity(similarity);
                            image.setSimilarTo(prevImage.getSimilarTo() == null ?
                                    prevImage.getImagePath() : prevImage.getSimilarTo());
                            addToResults(image, prevImage);
                        } else if (scanType == Constants.SCAN_TYPE_SIMILAR) {
                            image.setSimilarity(similarity);
                            image.setSimilarTo(prevImage.getSimilarTo() == null ?
                                    prevImage.getImagePath() : prevImage.getSimilarTo());
                            addToResults(image, prevImage);
                        }
                        break;
                    }
                }
                if (fountMatch) {
                    //free off the data
                    image.negativeCoeff = null;
                    image.positiveCoeff = null;
                    dataListIterator.remove();
                }
                if (shouldContinue) {
                    ScanProgressEvent event = new ScanProgressEvent();
                    event.currentProgress = Math.max(1, Math.min((int) (((float) i / (float) listImages.size()) * 100), 100));
                    EventBus.getDefault().postSticky(event);
                }
            }
        }
        ScanResultsEvent event = new ScanResultsEvent();
        if (listImages != null)
                listImages.clear();
        event.resultSet = searchResult;
        EventBus.getDefault().postSticky(event);
    }

    private void addToResults(ImageData image, ImageData scannedWith) {
        List<ImageData> results;
        int key = image.getSimilarTo().hashCode();
        results = searchResult.get(key);
        if (results == null) {
            results = new ArrayList<>();
            scannedWith.setSimilarTo(scannedWith.getImagePath());
            results.add(scannedWith);
        }
        results.add(image);
        searchResult.put(key, results);
    }

    private void removeSmallPics(@Nullable List<ImageData> list) {

        if (list == null)
            return;

        ListIterator<ImageData> iterator = list.listIterator();
        int i = 0;
        while (iterator.hasNext()) {
            ImageData data = iterator.next();
            if (data.getWidth() < 128 || data.getHeight() < 128) {
                iterator.remove();
                i++;
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.O)
    @NonNull
    private synchronized String createChannel() {
        //Timber.d("Creating notification channel");
        String NOTIFICATION_CHANNEL_ID = getPackageName() + ".channel";
        String channelName = "My Background Service";
        NotificationChannel chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName,
                NotificationManager.IMPORTANCE_DEFAULT);
        chan.setLightColor(Color.BLUE);
        chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        assert manager != null;
        manager.createNotificationChannel(chan);
        return NOTIFICATION_CHANNEL_ID;
    }
}
