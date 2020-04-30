package com.eukaytek.duplicatephotocleaner.events;

public class ScanningFilesCountEvent {
    public int filesCount;

    public ScanningFilesCountEvent(int count) {
        this.filesCount = count;
    }
}
