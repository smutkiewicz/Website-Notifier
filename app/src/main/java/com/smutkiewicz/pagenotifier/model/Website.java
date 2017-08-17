package com.smutkiewicz.pagenotifier.model;

import android.provider.BaseColumns;

public class Website implements BaseColumns {
    private int id;
    private String name;
    private String url;
    private boolean alertMode = true;
    private int delayInMiliseconds;

    public Website(String name, String url) {
        this.id = 0;
        this.name = name;
        this.url = url;
    }

    public void setDelayInMiliseconds(int secs) {
        delayInMiliseconds = secs;
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
}
