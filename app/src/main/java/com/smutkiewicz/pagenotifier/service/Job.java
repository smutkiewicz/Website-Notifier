package com.smutkiewicz.pagenotifier.service;

import android.net.Uri;

import com.smutkiewicz.pagenotifier.MainActivity;

public class Job {
    // default values
    public boolean requiresWifi = false; // WiFi Connectivity
    public boolean alertsEnabled = true;
    public boolean saveBatteryOptions = false;
    public boolean requiresIdle = false;
    public boolean requiresCharging = false;

    public final long delay;
    public final long deadline;

    public final String name;
    public final String url;
    public final Uri uri;
    public final int id;

    public Job(int id, int delayStep, String name, String url, boolean alertsEnabled) {
        this.id = id;
        this.uri = Uri.EMPTY;
        this.name = name;
        this.url = url;
        this.delay =
                MainActivity.scanDelayTranslator
                        .putStepAndReturnItsValueInMilliseconds(delayStep);
        this.deadline = this.delay;
        this.alertsEnabled = alertsEnabled;
    }

    public Job(int id, Uri uri, int delayStep, String name, String url, boolean alertsEnabled) {
        this.id = id;
        this.uri = uri;
        this.name = name;
        this.url = url;
        this.delay =
                MainActivity.scanDelayTranslator
                        .putStepAndReturnItsValueInMilliseconds(delayStep);
        this.deadline = this.delay;
        this.alertsEnabled = alertsEnabled;
    }

    public Job(int id, Uri uri, int delayStep, String name, String url, boolean alertsEnabled,
               boolean saveBattery, boolean onlyWifi) {
        this.id = id;
        this.uri = uri;
        this.name = name;
        this.url = url;
        this.delay =
                MainActivity.scanDelayTranslator
                        .putStepAndReturnItsValueInMilliseconds(delayStep);
        this.deadline = this.delay;
        this.alertsEnabled = alertsEnabled;
        this.requiresWifi = onlyWifi;
        this.saveBatteryOptions = saveBattery;
    }
}
