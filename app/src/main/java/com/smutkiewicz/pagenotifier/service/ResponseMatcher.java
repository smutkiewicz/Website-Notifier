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

    public static void saveOldWebsite(int jobId, String response, Context context) {
        //TODO save website to file "job_old" + jobId + ".txt"
        try {
            OutputStreamWriter outputStreamWriter =
                    new OutputStreamWriter(
                            context.openFileOutput(getOldFilePath(jobId),
                                    Context.MODE_PRIVATE));
            outputStreamWriter.write(response);
            outputStreamWriter.close();
            Log.d("ResponseMatcher", "File old write succeded");
        } catch (IOException e) {
            Log.e("ResponseMatcher", "File write failed: " + e.toString());
        }
    }

    public static void saveNewWebsite(int jobId, String response, Context context) {
        //TODO save website to file "job_new" + jobId + ".txt"
        try {
            OutputStreamWriter outputStreamWriter =
                    new OutputStreamWriter(
                            context.openFileOutput(getNewFilePath(jobId),
                                    Context.MODE_PRIVATE));
            outputStreamWriter.write(response);
            outputStreamWriter.close();
            Log.d("ResponseMatcher", "File new write succeded");
        } catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }

    public static boolean checkForChanges(int jobId) {
        if(ifResponseIsValid(jobId) && ifWebsitesMatch(jobId)) {
            cleanFinishedJobData(jobId);
            return true;
        } else {
            cleanNewWebsiteData(jobId);
            return false;
        }
    }

    public static void cleanFinishedJobData(int jobId) {
        if(cleanOldWebsiteData(jobId) && cleanNewWebsiteData(jobId)) {
            Log.d("ResponseMatcher", "Clean finished job data succeded");
        } else {
            Log.d("ResponseMatcher", "Clean finished job data failed");
        }
    }

    public static boolean cleanNewWebsiteData(int jobId) {
        // TODO remove new data
        File file = new File(getNewFilePath(jobId));
        return file.delete();
    }

    private static boolean ifResponseIsValid(int jobId) {
        //TODO check validity
        return true;
    }

    private static boolean ifWebsitesMatch(int jobId) {
        //TODO if matches
        return true;
    }

    private static boolean cleanOldWebsiteData(int jobId) {
        // TODO remove old data
        File file = new File(getOldFilePath(jobId));
        return file.delete();
    }

    private static String getOldFilePath(int jobId) {
        return jobOld + jobId + format;
    }

    private static String getNewFilePath(int jobId) {
        return jobNew + jobId + format;
    }
}
