package com.eukaytek.duplicatephotocleaner.events;

public class FileDeleteCompleteEvent {
    public int errorCount;
    public boolean hasError;

    public FileDeleteCompleteEvent(boolean error, int errorCount) {
        this.errorCount = errorCount;
        this.hasError = error;
    }
}
