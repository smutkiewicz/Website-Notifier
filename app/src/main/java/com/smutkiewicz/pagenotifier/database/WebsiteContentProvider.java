package com.smutkiewicz.pagenotifier.database;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;

import com.smutkiewicz.pagenotifier.R;

public class WebsiteContentProvider extends ContentProvider {
    private WebsiteDatabaseHelper dbHelper;

    // UriMatcher pomaga obiektowi ContentProvider
    // w określeniu operacji, która ma zostać wykonana
    private static final UriMatcher uriMatcher =
            new UriMatcher(UriMatcher.NO_MATCH);

    private static final int ONE_ITEM = 1; // wykonaj operację dotyczącą jednego itemu
    private static final int ALL_ITEMS = 2; // wykonaj operację dotyczącą tabeli itemów

    static {
        // adres Uri itemu o określonym identyfikatorze id (#)
        uriMatcher.addURI(DbDescription.AUTHORITY,
                DbDescription.DB_TABLE_NAME + "/#", ONE_ITEM);

        // adres Uri tabeli
        uriMatcher.addURI(DbDescription.AUTHORITY,
                DbDescription.DB_TABLE_NAME, ALL_ITEMS);
    }

    @Override
    public boolean onCreate() {
        dbHelper = new WebsiteDatabaseHelper(getContext());
        return true;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    // zapytanie bazy danych
    @Override
    public Cursor query(Uri uri, String[] projection,
                        String selection, String[] selectionArgs, String sortOrder) {

        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        queryBuilder.setTables(DbDescription.DB_TABLE_NAME);

        switch (uriMatcher.match(uri)) {
            case ONE_ITEM: // wybrany zostanie item o określonym identyfikatorze id
                queryBuilder.appendWhere(
                        DbDescription.KEY_ID + "=" + uri.getLastPathSegment());
                break;
            case ALL_ITEMS: // wybrane zostaną wszystkie itemy
                break;
            default:
                throw new UnsupportedOperationException(
                        getContext().getString(R.string.content_provider_invalid_uri) + uri);
        }

        Cursor cursor = queryBuilder.query(dbHelper.getReadableDatabase(),
                projection, selection, selectionArgs, null, null, sortOrder);

        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        Uri newItemUri = null;

        switch (uriMatcher.match(uri)) {
            case ALL_ITEMS:
                long rowId = dbHelper.getWritableDatabase().insert(
                        DbDescription.DB_TABLE_NAME, null, values);
                Log.d("INSERT TAG: ", uri.toString());

                if (rowId > 0) {
                    newItemUri = DbDescription.buildWebsiteItemUri(rowId);
                    getContext().getContentResolver().notifyChange(uri, null);
                } else
                    throw new SQLException(
                            "Insert failed: " + uri);
                break;
            default:
                throw new UnsupportedOperationException(
                        "Invalid insert: " + uri);
        }
        return newItemUri;
    }

    @Override
    public int update(Uri uri, ContentValues values,
                      String selection, String[] selectionArgs) {
        // 1 jeżeli aktualizacja przebiegła pomyślnie;
        // 0 w przeciwnym wypadku
        int numberOfRowsUpdated;

        switch (uriMatcher.match(uri)) {
            case ONE_ITEM:
                String id = uri.getLastPathSegment();
                numberOfRowsUpdated = dbHelper.getWritableDatabase().update(
                        DbDescription.DB_TABLE_NAME, values, DbDescription.KEY_ID + "=" + id,
                        selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException(
                        "Invalid update: " + uri);
        }

        notifyDatabaseObservers(uri, numberOfRowsUpdated);
        return numberOfRowsUpdated;
    }

    // skasuj item zapisany wcześniej w bazie danych
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int numberOfRowsDeleted;

        switch (uriMatcher.match(uri)) {
            case ONE_ITEM:
                String id = uri.getLastPathSegment();
                numberOfRowsDeleted = dbHelper.getWritableDatabase().delete(
                        DbDescription.DB_TABLE_NAME, DbDescription.KEY_ID + "=" + id, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException(
                        "Invalid delete: " + uri);
        }

        notifyDatabaseObservers(uri, numberOfRowsDeleted);
        return numberOfRowsDeleted;
    }

    private void notifyDatabaseObservers(Uri uri, int rowsUpdated) {
        // powiadom obiekty obserwujące bazę danych o jej modyfikacji
        if (rowsUpdated != 0)
            getContext().getContentResolver().notifyChange(uri, null);
    }
}

