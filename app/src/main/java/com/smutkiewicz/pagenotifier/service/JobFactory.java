package com.smutkiewicz.pagenotifier.service;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

import com.smutkiewicz.pagenotifier.database.DbDescription;

import static com.smutkiewicz.pagenotifier.service.MyJobService.JOB_URI_KEY;

public class JobFactory {
    public Job produceJob(ContentValues contentValues) {
        Uri uri = Uri.parse(contentValues.getAsString(JOB_URI_KEY));
        int id = contentValues.getAsInteger(DbDescription.KEY_ID);
        String name = contentValues.getAsString(DbDescription.KEY_NAME);
        String url = contentValues.getAsString(DbDescription.KEY_URL);
        int delayStep = contentValues.getAsInteger(DbDescription.KEY_DELAY);

        return new Job(id, uri, delayStep, name, url,
                getBooleanValueFromContentValue(contentValues, DbDescription.KEY_ALERTS),
                getBooleanValueFromContentValue(contentValues, DbDescription.KEY_SAVE_BATTERY),
                getBooleanValueFromContentValue(contentValues, DbDescription.KEY_ONLY_WIFI));
    }

    public Job produceJobFromACursor(Cursor cursor, Uri uri) {
        int id = cursor.getInt(cursor.getColumnIndex(DbDescription.KEY_ID));
        String name = cursor.getString(cursor.getColumnIndex(DbDescription.KEY_NAME));
        String url = cursor.getString(cursor.getColumnIndex(DbDescription.KEY_URL));
        int delayStep = cursor.getInt(cursor.getColumnIndex(DbDescription.KEY_DELAY));

        return new Job(id, uri, delayStep, name, url,
                getBooleanValueFromACursor(cursor, DbDescription.KEY_ALERTS),
                getBooleanValueFromACursor(cursor, DbDescription.KEY_SAVE_BATTERY),
                getBooleanValueFromACursor(cursor, DbDescription.KEY_ONLY_WIFI));
    }

    private boolean getBooleanValueFromACursor(Cursor cursor, String key) {
        int value = cursor.getInt(cursor.getColumnIndex(key));
        return (value == 1);
    }

    private boolean getBooleanValueFromContentValue(ContentValues values, String key) {
        int value = values.getAsInteger(key);
        return (value == 1);
    }
}