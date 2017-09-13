package com.smutkiewicz.pagenotifier.service;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ResponseMatcher {
    private static final String jobOld = "job_old_";
    private static final String jobNew = "job_new_";
    private static final String format = ".txt";

    public static void saveFile(String path, String response, Context context) {
        try {
            FileOutputStream fos =
                    context.openFileOutput(path, Context.MODE_PRIVATE);
            fos.write(response.getBytes());
            fos.close();
        } catch (IOException e) {
            Log.e("ResponseMatcher", "File write failed: " + e.toString());
        }
    }

    public static List<String> openFile(String path, Context context) {
        ArrayList<String> fileArray = new ArrayList<>();
        FileInputStream in = null;

        try {
            in = context.openFileInput(path);
        } catch (FileNotFoundException e) {
            Log.d("ResponseMatcher", "IOException in open file");
        }

        try {
            String line;
            InputStreamReader inputStreamReader = new InputStreamReader(in);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

            while ((line = bufferedReader.readLine()) != null)
                fileArray.add(line);

        } catch (IOException e) {
            Log.d("ResponseMatcher", "IOException in opening file");
        }

        return fileArray;
    }

    public static boolean checkForChanges(int jobId, Context context) {
        if(ifWebsitesMatch(jobId, context)) {
            Log.d("ResponseMatcher", "Websites match, no changes");
            cleanNotFinishedJobData(jobId, context);
            return false;
        } else {
            Log.d("ResponseMatcher", "Websites don't match, changes appeared");
            cleanFinishedJobData(jobId, context);
            return true;
        }
    }

    public static void cleanNotFinishedJobData(int jobId, Context context) {
        if(cleanNewWebsiteData(jobId, context)) {
            Log.d("ResponseMatcher", "Clean not finished job data succeded");
        } else {
            Log.d("ResponseMatcher", "Clean not finished job data failed");
        }
    }

    public static void cleanFinishedJobData(int jobId, Context context) {
        if(cleanOldWebsiteData(jobId, context) && cleanNewWebsiteData(jobId, context)) {
            Log.d("ResponseMatcher", "Clean finished job data succeded");
        } else {
            Log.d("ResponseMatcher", "Clean finished job data failed");
        }
    }

    // "job_old" + jobId + ".txt"
    public static String getOldFilePath(int jobId) {
        return jobOld + jobId + format;
    }

    // "job_new" + jobId + ".txt"
    public static String getNewFilePath(int jobId) {
        return jobNew + jobId + format;
    }

    private static boolean ifWebsitesMatch(int jobId, Context context) {
        List<String> oldFile = openFile(getOldFilePath(jobId), context);
        List<String> newFile = openFile(getNewFilePath(jobId), context);

        if(oldFile.size() == newFile.size()) {
            if(!ifLinesMatch(oldFile, newFile)) {
                // różnice w liniach, zmiany na stronie
                Log.d("ResponseMatcher", "!ifLinesMatch()");
                return false;
            }
        } else {
            // nierówna ilość linii, zmiany na stronie
            Log.d("ResponseMatcher", "oldFile.size() != newFile.size()");
            return false;
        }

        // równa ilość linii i pasują do siebie, brak zmian
        Log.d("ResponseMatcher", "oldFile.size() == newFile.size() && lines matches");
        return true;
    }

    private static boolean ifLinesMatch(List<String> oldFile, List<String> newFile) {
        Iterator<String> oldFileIterator = oldFile.iterator();
        Iterator<String> newFileIterator = newFile.iterator();

        while (oldFileIterator.hasNext() && newFileIterator.hasNext()) {
            String oldLine = oldFileIterator.next();
            String newLine = newFileIterator.next();
            if (!oldLine.equals(newLine)) {
                // różnice w linach, zmiany na stronie
                return false;
            }
        }

        return true;
    }

    private static boolean ifResponseIsValid(int jobId, Context context) {
        String pathOld =
                context.getApplicationInfo().dataDir + "/" + getOldFilePath(jobId);
        String pathNew =
                context.getApplicationInfo().dataDir + "/" + getNewFilePath(jobId);
        Log.d("ResponseMatcher", pathOld);
        Log.d("ResponseMatcher", pathNew);

        File oldWebsite = new File(pathOld);
        File newWebsite = new File(pathNew);
        return (oldWebsite.exists() && newWebsite.exists());
    }

    private static boolean cleanOldWebsiteData(int jobId, Context context) {
        // TODO remove old data
        String pathToData = context.getFilesDir().getPath();
        String pathToFile = pathToData + "/" + getOldFilePath(jobId);
        Log.d("ResponseMatcher", "Path to file: " + pathToFile);

        File file = new File(pathToFile);

        if(file.exists())
            Log.d("ResponseMatcher", "Old file exists");

        return file.delete();
    }

    private static boolean cleanNewWebsiteData(int jobId, Context context) {
        // TODO remove new data
        String pathToData = context.getFilesDir().getPath();
        String pathToFile = pathToData + "/" + getNewFilePath(jobId);
        Log.d("ResponseMatcher", "Path to file: " + pathToFile);
        File file = new File(pathToFile);

        if(file.exists())
            Log.d("ResponseMatcher", "New file exists");

        return file.delete();
    }
}
