package com.smutkiewicz.pagenotifier.service;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;

public class ResponseMatcher {
    private static final String jobOld = "job_old_";
    private static final String jobNew = "job_new_";
    private static final String format = ".txt";

    public static void saveWebsite(String path, String response, Context context) {
        //TODO save website to file "job_old" + jobId + ".txt"
        try {
            OutputStreamWriter outputStreamWriter =
                    new OutputStreamWriter(
                            context.openFileOutput(path,
                                    Context.MODE_PRIVATE));
            outputStreamWriter.write(response);
            outputStreamWriter.close();
            Log.d("ResponseMatcher", "File " + path + " write succeded");
        } catch (IOException e) {
            Log.e("ResponseMatcher", "File write failed: " + e.toString());
        }
    }

    public static boolean checkForChanges(int jobId) {
        if(ifWebsitesMatch(jobId)) {
            Log.d("ResponseMatcher", "Websites match, no changes");
            cleanFinishedJobData(jobId);
            return false;
        } else {
            Log.d("ResponseMatcher", "Websites don't match, changes appeared");
            cleanNewWebsiteData(jobId);
            return true;
        }
    }

    public static void cleanFinishedJobData(int jobId) {
        if(cleanOldWebsiteData(jobId) && cleanNewWebsiteData(jobId)) {
            Log.d("ResponseMatcher", "Clean finished job data succeded");
        } else {
            Log.d("ResponseMatcher", "Clean finished job data failed");
        }
    }

    public static void cleanNotFinishedWebsiteData(int jobId) {
        if(cleanNewWebsiteData(jobId)) {
            Log.d("ResponseMatcher", "Clean not finished job data succeded");
        } else {
            Log.d("ResponseMatcher", "Clean not finished job data failed");
        }
    }

    private static boolean ifResponseIsValid(int jobId) {
        File oldWebsite = new File(getOldFilePath(jobId));
        File newWebsite = new File(getNewFilePath(jobId));
        return (oldWebsite.exists() && newWebsite.exists());
    }

    private static boolean ifWebsitesMatch(int jobId) {
        //TODO if matches
        return false;
    }

    private static boolean cleanOldWebsiteData(int jobId) {
        // TODO remove old data
        File file = new File(getOldFilePath(jobId));
        if(file.exists()) Log.d("ResponseMatcher", "Old file exists");
        return file.delete();
    }

    private static boolean cleanNewWebsiteData(int jobId) {
        // TODO remove new data
        File file = new File(getNewFilePath(jobId));
        if(file.exists()) Log.d("ResponseMatcher", "New file exists");
        return file.delete();
    }

    public static String getOldFilePath(int jobId) {
        return jobOld + jobId + format;
    }

    public static String getNewFilePath(int jobId) {
        return jobNew + jobId + format;
    }
}
