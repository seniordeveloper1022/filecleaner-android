package com.eukaytek.duplicatephotocleaner;

import android.databinding.BindingAdapter;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.RelativeSizeSpan;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.eukaytek.duplicatephotocleaner.ui.search.SearchState;
import com.github.lzyzsd.circleprogress.ArcProgress;

import java.util.Locale;

public class Bindings {

    @BindingAdapter("imageSource")
    public static void loadImage(ImageView imageView, Drawable drawableImage) {
        Glide.with(imageView).load(R.drawable.background_one)
                .into(imageView);
    }

    @BindingAdapter("setProgress")
    public static void setProgress(ArcProgress progressView, int progress) {
        progressView.setProgress(progress);
    }

    @BindingAdapter("bindImage")
    public static void loadImage(ImageView view, String imagePath) {
        Glide.with(view).load(imagePath).apply(new RequestOptions().circleCrop())
                .into(view);
    }

    @BindingAdapter("bindImageFullSize")
    public static void bindImageFullSize(ImageView view, String imagePath) {
        Glide.with(view).load(imagePath).into(view);
    }

    @BindingAdapter("setTextProgress")
    public static void bindProgressToView(TextView view, int progress) {
        SpannableString string = new SpannableString(view.getContext().getString(R.string.progress, progress));
        string.setSpan(new RelativeSizeSpan(1.3f), 0, string.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        SpannableString scanComplete = null;
        if (progress != 0) {
            scanComplete = new SpannableString(view.getContext().getString(
                    progress > 0 && progress < 100 ? R.string.scanning : R.string.scan_complete));
            scanComplete.setSpan(new RelativeSizeSpan(0.3f), 0, scanComplete.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        if (scanComplete != null) {
            view.setText(new SpannableStringBuilder().append(string).append(System.lineSeparator()).append(scanComplete));
        } else {
            view.setText(string);
        }
    }

    @BindingAdapter("setDuplicateText")
    public static void setDuplicateText(TextView view, int count) {
        SpannableString string = new SpannableString(view.getContext().getResources()
                .getQuantityString(R.plurals.match_count, count, count));
        string.setSpan(new RelativeSizeSpan(2.0f), 0, string.toString().indexOf(' '),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        view.setText(string);
    }

    @BindingAdapter("setSearchText")
    public static void setStartStopString(TextView view, SearchState isSearching) {
        int textToDisplay = R.string.stopScan;
        int color = R.color.white;
        switch (isSearching) {
            case SEARCH_STATE_INITIAL:
                textToDisplay = R.string.startScan;
                color = R.color.white;
                break;
            case SEARCH_STATE_SEARCHING:
                textToDisplay = R.string.stopScan;
                color = android.R.color.holo_red_light;
                break;
            case SEARCH_STATE_COMPLETED:
                textToDisplay = R.string.restart_scan;
                color = android.R.color.holo_orange_light;
                break;
        }
        view.setText(textToDisplay);
        view.setTextColor(ContextCompat.getColor(view.getContext(), color));
    }

    @BindingAdapter("selectedFolderTextvisibility")
    public static void setSelectedFolderVisibility(View view, String selectedFolder) {
        view.setVisibility(TextUtils.isEmpty(selectedFolder) ? View.GONE : View.VISIBLE);
    }


    @BindingAdapter("infoViewVisibility")
    public static void setInfoViewVisiblity(View view, int scanFileCount) {
        view.setVisibility(scanFileCount > 0 ? View.VISIBLE : View.GONE);
    }

    @BindingAdapter("scanCountText")
    public static void setFilesCount(TextView view, int scanFileCount) {
        view.setText(view.getContext().getString(R.string.scan_file_count, scanFileCount));
    }

    @BindingAdapter("elapsedTime")
    public static void setElapsedTime(TextView view, long timer) {
        view.setText(view.getContext().getString(R.string.countdown_timer_text, convertMsToHHMMSS(timer)));
    }


    @BindingAdapter("setScanElapsedTime")
    public static void setScanElapsedTime(TextView view, long timer) {
        view.setText(view.getContext().getString(R.string.scan_finished_time, convertMsToHHMMSS(timer)));
    }

    @BindingAdapter("misVisibility")
    public static void metaVisibility(View view, String text) {
        if (TextUtils.isEmpty(text)) {
            view.setVisibility(View.GONE);
        } else {
            view.setVisibility(View.VISIBLE);
        }
    }

    @BindingAdapter("setMetaData")
    public static void setMetaData(TextView view, String text) {
        if (TextUtils.isEmpty(text)) {
            view.setVisibility(View.GONE);
        } else {
            view.setText(text);
            view.setVisibility(View.VISIBLE);
        }
    }

    private static String convertMsToHHMMSS(long seconds) {
        long s = seconds % 60;
        long m = (seconds / 60) % 60;
        long h = (seconds / (60 * 60)) % 24;
        return String.format(Locale.ENGLISH, "%02d:%02d:%02d", h, m, s);
    }
}
