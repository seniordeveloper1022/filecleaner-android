package com.eukaytek.duplicatephotocleaner.events;

public class SettingsChangeEvent {

    public static final int CANCEL = 1;
    public static final int APPLY = 2;

    public int eventType;

    public SettingsChangeEvent(int type) {
        this.eventType = type;
    }
}
