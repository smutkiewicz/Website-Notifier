package com.smutkiewicz.pagenotifier.database;

import android.annotation.SuppressLint;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;

/**
 * Created by Admin on 2017-09-05.
 */

@SuppressLint("NewApi")
public class MyContentObserver extends ContentObserver {
    public MyContentObserver(Handler handler) {
        super(handler);
    }

    @Override
    public void onChange(boolean selfChange) {
        this.onChange(selfChange, null);
    }

    @Override
    public void onChange(boolean selfChange, Uri uri) {
        // do s.th.
        // depending on the handler you might be on the UI
        // thread, so be cautious!
    }
}
