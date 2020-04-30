package com.eukaytek.duplicatephotocleaner.ui;

public class Constants {
    public static final String LAUNCH_KEY_SCAN_TYPE = Constants.class.getName() + ".SCAN_TYPE";
    public static final String LAUNCH_SELECTED_KEY = Constants.class.getName() + ".SELECTED_KEY";
    public static final String LAUNCH_SELECTED_POS = Constants.class.getName() + ".SELECTED_POS";
    public static final int SCAN_TYPE_EXACT = 0;
    public static final int SCAN_TYPE_SIMILAR = 1;
    public static final int SCAN_TYPE_DEFAULT = SCAN_TYPE_SIMILAR;


    public static final String PREF_KEY_CURRENT_THRESHOLD = Constants.class.getName() + ".PREF_KEY_CURRENT_THRESHOLD";
    public static final int DEFAULT_THRESHOLD = 50;

    public static final String PREF_KEY_COMPARE_ROTATION = Constants.class.getName() + ".PREF_KEY_COMPARE_ROTATION";
    public static final boolean DEFAULT_COMPARE_ROTATION = false;

    public static final String PREF_KEY_COMPARE_GRAYSCALE = Constants.class.getName() + ".PREF_KEY_COMPARE_GRAYSCALE";
    public static final boolean DEFAULT_COMPARE_GRAYSCALE = true;


    public static final String SCAN_FOLDER_LIST = Constants.class.getName() + ".SCAN_FOLDER_LIST";
}
