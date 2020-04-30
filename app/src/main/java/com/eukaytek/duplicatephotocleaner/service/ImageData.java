package com.eukaytek.duplicatephotocleaner.service;

import android.graphics.BitmapFactory;
import android.support.annotation.Nullable;
import android.support.media.ExifInterface;

import java.io.IOException;
import java.util.Locale;

public class ImageData {

    public ImageProcessing.TWaveletPoint positiveCoeff[];
    public ImageProcessing.TWaveletPoint negativeCoeff[];
    private String imagePath;
    private int width;
    private int height;
    private String mime;
    private long size;
    private String similarTo;
    private double similarity;
    private boolean isOriginal;
    private String picTime;
    private String latLng;
    private String cameraName;
    private String cameraAperture;
    private String flash;
    private String cameraISO;
    private String cameraFocalLength;
    private boolean hasExifInfo = false;

    public ImageData(String imagePath, long size) {
        assert imagePath != null;
        try {
            ExifInterface exifInterface = new ExifInterface(imagePath);
            picTime = exifInterface.getAttribute(ExifInterface.TAG_DATETIME);
            latLng = exifInterface.getAttribute(ExifInterface.TAG_GPS_AREA_INFORMATION);
            cameraName = exifInterface.getAttribute(ExifInterface.TAG_MODEL);
            cameraAperture = exifInterface.getAttribute(ExifInterface.TAG_APERTURE_VALUE);
            flash = exifInterface.getAttribute(ExifInterface.TAG_FLASH);
            cameraISO = exifInterface.getAttribute(ExifInterface.TAG_ISO_SPEED);
            cameraFocalLength = exifInterface.getAttribute(ExifInterface.TAG_FOCAL_LENGTH);
            hasExifInfo = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        BitmapFactory.Options options = Utils.getBitmap(imagePath);
        this.imagePath = imagePath;
        this.size = size / 1024;  //in KBs: incoming file size is in bytes
        width = options.outWidth;
        height = options.outHeight;
        mime = options.outMimeType;
        positiveCoeff = new ImageProcessing.TWaveletPoint[ImageProcessing.WaveletCoefsNumber];
        negativeCoeff = new ImageProcessing.TWaveletPoint[ImageProcessing.WaveletCoefsNumber];
    }

    public String getImagePath() {
        return imagePath;
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    public String getMime() {
        return mime;
    }

    public long getSize() {
        return size;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (!(obj instanceof ImageData)) {
            return false;
        }
        ImageData data = (ImageData) obj;
        return imagePath.equals(data.imagePath);
    }

    public String getSimilarTo() {
        return similarTo;
    }

    public void setSimilarTo(String similarTo) {
        this.similarTo = similarTo;
        isOriginal = this.imagePath.equals(similarTo);
    }

    public double getSimilarity() {
        return similarity;
    }

    public void setSimilarity(double similarity) {
        this.similarity = similarity;
    }

    public String getDimensions() {
        return String.format(Locale.ENGLISH, "%s x %s", getWidth(), getHeight());
    }

    public boolean isOriginal() {
        return isOriginal;
    }

    public boolean isHasExifInfo() {
        return hasExifInfo;
    }

    public String getPicTime() {
        return picTime;
    }

    public String getLatLng() {
        return latLng;
    }

    public String getCameraAperture() {
        return cameraAperture;
    }

    public String getCameraFocalLength() {
        return cameraFocalLength;
    }

    public String getCameraISO() {
        return cameraISO;
    }

    public String getCameraName() {
        return cameraName;
    }

    public String getFlash() {
        return flash;
    }
}
