package com.smutkiewicz.pagenotifier.service;

import android.content.ContentValues;

import com.smutkiewicz.pagenotifier.database.DbDescription;

/**
 * Created by Admin on 2017-09-02.
 */

public class JobFactory {
    private static int testId = 0;

    public Job produceJob(ContentValues contentValues) {
        int id = contentValues.getAsInteger(DbDescription.KEY_ID);
        String name = contentValues.getAsString(DbDescription.KEY_NAME);
        String url = contentValues.getAsString(DbDescription.KEY_URL);
        int alerts = contentValues.getAsInteger(DbDescription.KEY_ALERTS);
        int delayStep = contentValues.getAsInteger(DbDescription.KEY_DELAY);
        boolean alertsEnabled = (alerts == 1) ? true : false;

        Job newJob = new Job(id, delayStep, name, url, alertsEnabled);
        return newJob;
    }

    public static ContentValues getSampleJob() {
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
