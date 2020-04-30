package com.eukaytek.duplicatephotocleaner.events;

public class SearchResultClicked {
    public static final int TYPE_SEARCH_RESULT = 1;
    public static final int TYPE_DETAIL_RESULT = 2;

    public int key;
    public int type;
    public int position;

    public SearchResultClicked(int type, int key) {
        this(type, key, -1);
    }

    public SearchResultClicked(int type, int key, int pos) {
        this.type = type;
        this.key = key;
        this.position = pos;
    }
}
