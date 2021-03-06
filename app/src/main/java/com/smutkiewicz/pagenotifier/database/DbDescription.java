package com.smutkiewicz.pagenotifier.database;

import android.content.ContentUris;
import android.net.Uri;

/**
 * Created by Admin on 2017-08-17.
 */

public class DbDescription {
    // nazwa obiektu ContentProvider
    public static final String AUTHORITY =
            "com.smutkiewicz.pagenotifier.database";

    // bazowy adres URI używany do nawiązywania interakcji
    // z obiektem ContentProvider
    private static final Uri BASE_CONTENT_URI =
            Uri.parse("content://" + AUTHORITY);

    //ogólne informacje o bazie danych
    public static final int DB_VERSION = 2;
    public static final String DB_NAME = "websites.db";
    public static final String DB_TABLE_NAME = "websites";

    //kolumny tabeli odpowiadające modelowi Sound
    public static final String KEY_ID = "_id";
    public static final String KEY_NAME = "name";
    public static final String KEY_URL = "url";
    public static final String KEY_ALERTS = "alertsmode";
    public static final String KEY_DELAY = "delay";
    public static final String KEY_UPDATED = "updated";
    public static final String KEY_ISENABLED = "enabled";
    public static final String KEY_SAVE_BATTERY = "savebattery";
    public static final String KEY_ONLY_WIFI = "onlywifi";

    //typy i opcje kolumn
    public static final String ID_OPTIONS = "INTEGER PRIMARY KEY AUTOINCREMENT";
    public static final String NAME_OPTIONS = "TEXT";
    public static final String URL_OPTIONS = "TEXT NOT NULL";
    public static final String ALERTS_OPTIONS = "INTEGER DEFAULT 1";
    public static final String DELAY_OPTIONS = "INTEGER NOT NULL";
    public static final String UPDATED_OPTIONS = "INTEGER DEFAULT 0";
    public static final String ISENABLED_OPTIONS = "INTEGER DEFAULT 1";
    public static final String SAVE_BATTERY_OPTIONS = "INTEGER DEFAULT 0";
    public static final String ONLY_WIFI_OPTIONS = "INTEGER DEFAULT 0";

    //tablica z nazwami kolumn do obsługi kwerend
    public static final String[] columns = {KEY_ID, KEY_NAME, KEY_URL,
            KEY_ALERTS, KEY_DELAY, KEY_UPDATED, KEY_ISENABLED, KEY_SAVE_BATTERY, KEY_ONLY_WIFI};

    // adres Uri tabeli
    public static final Uri CONTENT_URI =
            BASE_CONTENT_URI.buildUpon().appendPath(DB_TABLE_NAME).build();

    // tworzy adres Uri określonego itemu
    public static Uri buildWebsiteItemUri(long id) {
        return ContentUris.withAppendedId(CONTENT_URI, id);
    }
}
