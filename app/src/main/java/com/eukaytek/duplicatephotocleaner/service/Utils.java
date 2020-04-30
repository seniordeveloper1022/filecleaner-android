package com.eukaytek.duplicatephotocleaner.service;

import android.graphics.BitmapFactory;

import java.text.DecimalFormat;

public class Utils {

    public static BitmapFactory.Options getBitmap(String imagePath) {
        BitmapFactory.Options localOptions = new BitmapFactory.Options();
        localOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imagePath, localOptions);
        return localOptions;
    }

    public static String getSize(long fileSize) {
        double m = fileSize / 1024.0;
        double g = fileSize / 1048576.0;
        double t = fileSize / 1073741824.0;
        String hrSize;
        DecimalFormat dec = new DecimalFormat("0");
        if (t > 1) {
            hrSize = dec.format(t).concat("TB");
        } else if (g > 1) {
            hrSize = dec.format(g).concat("GB");
        } else if (m > 1) {
            hrSize = dec.format(m).concat("MB");
        } else {
            hrSize = dec.format(fileSize).concat("KB");
        }
        return hrSize;
    }
}
