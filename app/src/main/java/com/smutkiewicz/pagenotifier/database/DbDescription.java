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
    public static final int DB_VERSION = 1;
    public static final String DB_NAME = "websites.db";
    public static final String DB_TABLE_NAME = "websites";

    //kolumny tabeli odpowiadające modelowi Sound
    public static final String KEY_ID = "_id";
    public static final String KEY_NAME = "name";
    public static final String KEY_URL = "url";
    public static final String KEY_ALERTS = "alertsmode";
    public static final String KEY_DELAY = "delay";

    //typy i opcje kolumn
    public static final String ID_OPTIONS = "INTEGER PRIMARY KEY AUTOINCREMENT";
    public static final String NAME_OPTIONS = "TEXT";
    public static final String URL_OPTIONS = "TEXT NOT NULL";
    public static final String ALERTS_OPTIONS = "INTEGER DEFAULT 1";
    public static final String DELAY_OPTIONS = "INTEGER NOT NULL";

    //numery kolumn
    public static final int ID_COLUMN = 0;
    public static final int URL_COLUMN = 1;
    public static final int NAME_COLUMN = 2;
    public static final int ALERTS_COLUMN = 3;
    public static final int DELAY_COLUMN = 4;

    //tablica z nazwami kolumn do obsługi kwerend
    public static final String[] columns = {KEY_ID, KEY_NAME, KEY_URL,
            KEY_ALERTS, KEY_DELAY};

    // adres Uri tabeli
    public static final Uri CONTENT_URI =
            BASE_CONTENT_URI.buildUpon().appendPath(DB_TABLE_NAME).build();

    // tworzy adres Uri określonego itemu
    public static Uri buildWebsiteItemUri(long id) {
        return ContentUris.withAppendedId(CONTENT_URI, id);
    }
}
