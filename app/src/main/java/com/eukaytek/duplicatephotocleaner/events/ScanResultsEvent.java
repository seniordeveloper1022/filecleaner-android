package com.eukaytek.duplicatephotocleaner.events;

import android.util.SparseArray;

import com.eukaytek.duplicatephotocleaner.service.ImageData;

import java.util.List;

public class ScanResultsEvent {
    public SparseArray<List<ImageData>> resultSet;
}
