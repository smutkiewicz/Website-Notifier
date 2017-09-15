package com.smutkiewicz.pagenotifier.service;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

import com.smutkiewicz.pagenotifier.database.DbDescription;

import static com.smutkiewicz.pagenotifier.service.MyJobService.JOB_URI_KEY;

/**
 * Created by Admin on 2017-09-02.
 */

public class JobFactory {
    public Job produceJob(ContentValues contentValues) {
        Uri uri = Uri.parse(contentValues.getAsString(JOB_URI_KEY));
        int id = contentValues.getAsInteger(DbDescription.KEY_ID);
        String name = contentValues.getAsString(DbDescription.KEY_NAME);
        String url = contentValues.getAsString(DbDescription.KEY_URL);
        int delayStep = contentValues.getAsInteger(DbDescription.KEY_DELAY);

        int alerts = contentValues.getAsInteger(DbDescription.KEY_ALERTS);
        boolean alertsEnabled = (alerts == 1) ? true : false;

        return new Job(id, uri, delayStep, name, url, alertsEnabled);
    }

    public Job produceJobFromACursor(Cursor cursor, Uri uri) {
        int id = cursor.getInt(cursor.getColumnIndex(DbDescription.KEY_ID));
        String name = cursor.getString(cursor.getColumnIndex(DbDescription.KEY_NAME));
        String url = cursor.getString(cursor.getColumnIndex(DbDescription.KEY_URL));
        int delayStep = cursor.getInt(cursor.getColumnIndex(DbDescription.KEY_DELAY));

        int alerts = cursor.getInt(cursor.getColumnIndex(DbDescription.KEY_ALERTS));
        boolean alertsEnabled = (alerts == 1) ? true : false;

        return new Job(id, uri, delayStep, name, url, alertsEnabled);
    }

    public static ContentValues getSampleJob() {
        int testId = 0;
        ContentValues contentValues = new ContentValues();
        contentValues.put(DbDescription.KEY_ID, testId++);
        contentValues.put(DbDescription.KEY_NAME, "nazwa");
        contentValues.put(DbDescription.KEY_URL,
                "https://catinean.com/2014/10/19/smart-background-tasks-with-jobscheduler/");
        contentValues.put(DbDescription.KEY_ALERTS, 1);
        contentValues.put(DbDescription.KEY_DELAY, 1);

        return contentValues;
    }
}
