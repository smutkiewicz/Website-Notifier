package com.smutkiewicz.pagenotifier.model;

import android.content.ContentValues;
import android.provider.BaseColumns;

import com.smutkiewicz.pagenotifier.database.DbDescription;

public class Website implements BaseColumns {
    private int id;
    private String name;
    private String url;
    private boolean alertMode = true;
    private boolean updated = false;
    private boolean isEnabled = true;
    private int delayStep;

    public Website(String name, String url) {
        this.id = 0;
        this.name = name;
        this.url = url;
        this.delayStep = 1;
    }

    public void setDelayStep(int secs) {
        delayStep = secs;
    }

    public void changeAlertMode(boolean mode) {
        alertMode = mode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public void setEnabled(boolean enabled) {
        this.isEnabled = enabled;
    }

    public ContentValues getContentValues() {
        ContentValues contentValues = new ContentValues();
        contentValues.put(DbDescription.KEY_NAME, name);
        contentValues.put(DbDescription.KEY_URL, url);
        contentValues.put(DbDescription.KEY_ALERTS, (alertMode) ? 1 : 0);
        contentValues.put(DbDescription.KEY_UPDATED, (updated) ? 1 : 0);
        contentValues.put(DbDescription.KEY_DELAY, delayStep);
        contentValues.put(DbDescription.KEY_ISENABLED, (isEnabled) ? 1 : 0);

        return contentValues;
    }
}
