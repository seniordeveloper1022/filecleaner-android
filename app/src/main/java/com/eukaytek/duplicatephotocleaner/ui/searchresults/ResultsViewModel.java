package com.eukaytek.duplicatephotocleaner.ui.searchresults;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.databinding.ObservableInt;
import android.media.MediaScannerConnection;
import android.util.SparseArray;
import android.util.SparseBooleanArray;
import android.view.View;

import com.eukaytek.duplicatephotocleaner.R;
import com.eukaytek.duplicatephotocleaner.events.DeleteFilesEvent;
import com.eukaytek.duplicatephotocleaner.events.FileDeleteCompleteEvent;
import com.eukaytek.duplicatephotocleaner.events.ItemClickEvent;
import com.eukaytek.duplicatephotocleaner.events.SearchResultClicked;
import com.eukaytek.duplicatephotocleaner.events.SelectAllEvent;
import com.eukaytek.duplicatephotocleaner.events.UpdateAdapterEvent;
import com.eukaytek.duplicatephotocleaner.service.ImageData;
import com.eukaytek.duplicatephotocleaner.service.Utils;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

public class ResultsViewModel extends AndroidViewModel {
    public static final int FRAGMENT_SEARCH_RESULTS = 1;
    public static final int FRAGMENT_DETAILED_RESULTS = 2;
    public static final int FRAGMENT_EXPANDED_RESULTS = 3;
    public ObservableInt deleteButtonVisible = new ObservableInt(View.GONE);
    private SparseArray<List<ImageData>> searchResults;
    private EventBus eventBus;
    private SparseBooleanArray mSelectedPos = new SparseBooleanArray();
    private SparseBooleanArray mSelectedKey
            = new SparseBooleanArray();

    private boolean isLongPressRunning = false;
    private int currentFragment;
    private int detailedSelectedKey;

    public ResultsViewModel(Application application) {
        super(application);
    }

    public void setEventBus(EventBus bus) {
        this.eventBus = bus;
    }

    public SparseArray<List<ImageData>> getSearchResults() {
        return searchResults;
    }

    public void setSearchResults(SparseArray<List<ImageData>> searchResults) {
        this.searchResults = searchResults;
    }

    public String getImage(int position) {
        return getImage(position, 0);
    }

    public String getImage(int key, int position) {
        return searchResults.get(key).get(position).getImagePath();
    }

    public String getImageName(int position) {
        return getImageName(position, 0);
    }

    public String getImageName(int key, int position) {
        String path = searchResults.get(key).get(position).getImagePath();
        return path.substring(path.lastIndexOf('/') + 1);
    }

    public String getImageSize(int position) {
        return getImageSize(position, 0);
    }

    public String getImageSize(int key, int position) {
        return Utils.getSize(searchResults.get(key).get(position).getSize());
    }

    public String getImageDimen(int position) {
        return getImageDimen(position, 0);
    }

    public String getImageDimen(int key, int position) {
        return searchResults.get(key).get(position).getDimensions();
    }


    public String getImagePath(int position) {
        return getImagePath(position, 0);
    }

    public String getImagePath(int key, int position) {
        String path = searchResults.get(key).get(position).getImagePath();
        return path.substring(0, path.lastIndexOf('/'));
    }

    public int getKeySetAtPosition(int pos) {
        return searchResults.keyAt(pos);
    }

    public void onClicked(int key) {
        Timber.e("Clicked");
        if (!isLongPressRunning) {
            eventBus.post(new SearchResultClicked(SearchResultClicked.TYPE_SEARCH_RESULT, key));
        } else {
            onLongClicked(key, searchResults.indexOfKey(key));
        }
    }

    public void onClicked(int key, int position) {
        Timber.e("Expand item");
        if (!isLongPressRunning) {
            eventBus.post(new SearchResultClicked(SearchResultClicked.TYPE_DETAIL_RESULT, key, position));
        } else {
            onLongClicked(key, position);
        }
    }

    public String getDuplicateCount(int key, int position) {
        return String.valueOf(searchResults.get(key).size() - 1);
    }

    public String getSimilarity(boolean expanded, int key, int position) {
        ImageData data = searchResults.get(key).get(position);
        if (data.isOriginal()) {
            return getApplication().getApplicationContext().getString(R.string.original_image);
        } else {
            return getApplication().getString(
                    expanded ?
                            R.string.similarity_text :
                            R.string.similarity_text_unexpanded
                    , (int) data.getSimilarity());
        }
    }

    public int getItemCount(int selectionKey) {
        return searchResults.get(selectionKey) == null ? 0 : searchResults.get(selectionKey).size();
    }

    public void deleteFiles() throws IllegalAccessException {
        Timber.d("Deleting files now");
        if (mSelectedPos.size() == 0) {
            throw new IllegalStateException("Can't delete without first having selections");
        }

        String files[] = null;
        if (currentFragment == FRAGMENT_EXPANDED_RESULTS || currentFragment == FRAGMENT_DETAILED_RESULTS) {
            //we are in expanded or detailed fragment
            int key = detailedSelectedKey;
            List<ImageData> images = searchResults.get(key);
            if (images.get(0).getSimilarTo().hashCode() != key) {
                throw new IllegalAccessException("The selected key doesn't matches with the image data");
            }
            files = new String[mSelectedPos.size()];
            ArrayList<ImageData> filesToRemove = new ArrayList<>(mSelectedPos.size());
            ArrayList<Integer> indexesToRemove = new ArrayList<>();
            for (int i = 0; i < mSelectedPos.size(); i++) {
                int index = mSelectedPos.keyAt(i);
                Timber.e("i = %d, key = %d", i, index);
                filesToRemove.add(images.get(index));
                indexesToRemove.add(index);
                if (index == 0) {
                    //we don't want to delete original
                    continue;
                }
                files[i] = images.get(index).getImagePath();
            }
            for (Integer index : indexesToRemove) {
                mSelectedPos.delete(index);
            }
            Timber.e("mSelectedPos size = %d", mSelectedPos.size());
            images.removeAll(filesToRemove);
            if (mSelectedPos.size() == 0) {
                mSelectedKey.clear();
            }
            if (images.size() == 1) {
                searchResults.remove(key);
            } else {
                searchResults.put(key, images);
            }
        } else {
            ArrayList<String> fileList = new ArrayList<>();
            ArrayList<Integer> indexesToRemove = new ArrayList<>();
            ArrayList<Integer> keysToRemove = new ArrayList<>();
            ArrayList<Integer> selectedIndexToRemove = new ArrayList<>();

            for (int i = 0; i < mSelectedKey.size(); i++) {
                int key = mSelectedKey.keyAt(i);
                List<ImageData> images = searchResults.get(key);
                for (int j = 1; j < images.size(); j++) {
                    fileList.add(images.get(j).getImagePath());
                }
                selectedIndexToRemove.add(mSelectedKey.keyAt(i));
                keysToRemove.add(key);
                indexesToRemove.add(i);
            }

            for (Integer index : indexesToRemove) {
                mSelectedPos.delete(index);
            }

            for (Integer index : selectedIndexToRemove) {
                mSelectedKey.delete(index);
            }

            for (Integer index : keysToRemove) {
                searchResults.remove(index);
            }

            if (fileList.size() > 0) {
                files = new String[fileList.size()];
                files = fileList.toArray(files);
            }
        }
        updateAdapter();
        if (files != null) {
            boolean hasErrors = false;
            int errorCount = 0;
            for (String path : files) {
                if (path == null)
                    continue;

                Timber.e("Create File for path : %s", path);
                File file = new File(path);
                if (file.delete()) {
                    Timber.i("File %s delete successfully", path);
                } else {
                    hasErrors = true;
                    errorCount++;
                }
            }
            MediaScannerConnection.scanFile(getApplication().getApplicationContext(), files, null, null);
            final boolean error = hasErrors;
            final int count = errorCount;
            if (!hasSelection()) {
                deleteButtonVisible.set(View.GONE);
            }
            eventBus.post(new FileDeleteCompleteEvent(error, count));
        }
        clearSelection();
    }

    private void updateAdapter() {
        eventBus.post(new UpdateAdapterEvent());
    }

    public void deleteSelectedFiles() {
        eventBus.post(new DeleteFilesEvent());
    }

    public boolean onLongClicked(int key, int position) {
        Timber.d("View Long Clicked at position %d", position);
        ItemClickEvent event = new ItemClickEvent(ItemClickEvent.LONG_CLICK, key, position);
        eventBus.post(event);
        return true;
    }

    public boolean selectItemAtPos(int clickKey, int clickPos) {
        return selectItemAtPos(clickKey, clickPos, true);
    }

    public boolean selectItemAtPos(int clickKey, int clickPos, boolean delselect) {
        Timber.d("Selected item at position = %d", clickPos);
        if (mSelectedPos.get(clickPos) && delselect) {
            mSelectedPos.delete(clickPos);
            if (mSelectedPos.size() == 0) {
                mSelectedKey.delete(clickKey);
            }
        } else {
            mSelectedPos.put(clickPos, Boolean.TRUE);
            mSelectedKey.put(clickKey, Boolean.TRUE);
        }
        isLongPressRunning = mSelectedPos.size() > 0;
        deleteButtonVisible.set(mSelectedPos.size() > 0 ? View.VISIBLE : View.GONE);
        return mSelectedPos.size() > 0;
    }

    public int isSelected(int key, int position) {
        Timber.d("Returning visibility for item at position = %d, selected = %b", position, mSelectedPos.get(position));
        return mSelectedPos.get(position) ? View.VISIBLE : View.GONE;
    }

    public void clearSelection() {
        mSelectedKey.clear();
        mSelectedPos.clear();
        isLongPressRunning = false;
        //detailedSelectedKey = -1;
        deleteButtonVisible.set(View.GONE);
        //clear all selections
        ItemClickEvent event = new ItemClickEvent(ItemClickEvent.LONG_CLICK, -1, -1);
        eventBus.post(event);
    }

    public boolean hasSelection() {
        return mSelectedKey.size() != 0 && mSelectedPos.size() != 0;
    }

    public void selectAllItems() {
        eventBus.post(new SelectAllEvent());
    }

    public void setExpandedView(boolean isDetailedView, int detailedSelectedKey) {
        //this.isDetailedView = isDetailedView;
        this.detailedSelectedKey = detailedSelectedKey;
    }

    public boolean isAllSelected() {
//       if(!isDetailedView && (mSelectedPos.size() == mSelectedKey.size())) {
//           return true;
//       }else {
//           if(detailedSelectedKey != -1 && searchResults.get(detailedSelectedKey).size() == mSelectedPos.size()) {
//               return true;
//           }
//       }
        return false;
    }

    public int getDetailedSelectedKey() {
        return detailedSelectedKey;
    }

    public int getCurrentFragment() {
        return currentFragment;
    }

    public void setCurrentFragment(int fragment) {
        this.currentFragment = fragment;
        if (currentFragment == FRAGMENT_SEARCH_RESULTS) {
            detailedSelectedKey = -1;
        }
    }

    public String getImageLoc(int key, int position) {
        return searchResults.get(key).get(position).getLatLng();
    }

    public String getImageDate(int key, int position) {
        return searchResults.get(key).get(position).getPicTime();
    }

    public String getCameraName(int key, int pos) {
        return searchResults.get(key).get(pos).getCameraName();
    }

    public String getCameraISO(int key, int pos) {
        return searchResults.get(key).get(pos).getCameraISO();
    }

    public String getFlash(int key, int pos) {
        return searchResults.get(key).get(pos).getFlash();
    }

    public String getFocalLength(int key, int pos) {
        return searchResults.get(key).get(pos).getCameraFocalLength();
    }

    public String getCameraAperture(int key, int pos) {
        return searchResults.get(key).get(pos).getCameraAperture();
    }
}
