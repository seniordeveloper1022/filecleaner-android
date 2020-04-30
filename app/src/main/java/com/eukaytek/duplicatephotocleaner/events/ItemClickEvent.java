package com.eukaytek.duplicatephotocleaner.events;

public class ItemClickEvent {
    public static final int LONG_CLICK = 1;
    public static final int CLICK = 2;

    public int clickType;
    public int clickPos;
    public int clickKey;

    public ItemClickEvent(int type, int key, int pos) {
        this.clickKey = key;
        this.clickPos = pos;
        this.clickType = type;
    }
}
