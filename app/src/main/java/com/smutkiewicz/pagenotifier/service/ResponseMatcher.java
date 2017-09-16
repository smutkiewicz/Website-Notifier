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
            Log.e("Response", "File write failed: " + e.toString());
        }
    }

    public static List<String> openFile(String path, Context context) {
        ArrayList<String> fileArray = new ArrayList<>();
        FileInputStream in = null;

        try {
            in = context.openFileInput(path);
        } catch (FileNotFoundException e) {
            Log.d("Response", "IOException in open file");
        }

        try {
            String line;
            InputStreamReader inputStreamReader = new InputStreamReader(in);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

            while ((line = bufferedReader.readLine()) != null)
                fileArray.add(line);

        } catch (IOException e) {
            Log.d("Response", "IOException in opening file");
        }

        return fileArray;
    }

    public static boolean checkForChanges(int jobId, Context context) {
        if(ifResponseIsValid(jobId, context)) {
            if (ifWebsitesMatch(jobId, context)) {
                Log.d("Response", "Websites match, no changes");
                cleanNotFinishedJobData(jobId, context);
                return false;
            } else {
                Log.d("Response", "Websites don't match, changes appeared");
                cleanFinishedJobData(jobId, context);
                return true;
            }
        } else {
            Log.d("Response", "Websites don't exist - error (treating this like change)");
            return true;
        }
    }

    public static void cleanNotFinishedJobData(int jobId, Context context) {
        if(cleanNewWebsiteData(jobId, context)) {
            Log.d("Response", "Clean not finished job data succeded");
        } else {
            Log.d("Response", "Clean not finished job data failed");
        }
    }

    public static void cleanFinishedJobData(int jobId, Context context) {
        if(cleanOldWebsiteData(jobId, context) && cleanNewWebsiteData(jobId, context)) {
            Log.d("Response", "Clean finished job data succeded");
        } else {
            Log.d("Response", "Clean finished job data failed");
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
                Log.d("Response", "Matcher: !ifLinesMatch()");
                return false;
            }
        } else {
            // nierówna ilość linii, zmiany na stronie
            Log.d("Response", "Matcher: oldFile.size() != newFile.size()");
            return false;
        }

        // równa ilość linii i pasują do siebie, brak zmian
        Log.d("Response", "Matcher: oldFile.size() == newFile.size() && lines matches");
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
                Log.d("Response", "Old line: " + oldLine);
                Log.d("Response", "New line: " + newLine);

                return false;
            }
        }

        return true;
    }

    private static boolean ifResponseIsValid(int jobId, Context context) {
        boolean bothFilesExist;
        String pathToNewFile = getFullPathToFile(getNewFilePath(jobId), context);
        String pathToOldFile = getFullPathToFile(getOldFilePath(jobId), context);

        File oldFile = new File(pathToOldFile);
        File newFile = new File(pathToNewFile);

        Log.d("Response", "Is response valid?");
        bothFilesExist = (oldFile.exists() && newFile.exists());

        if(bothFilesExist)
            Log.d("Response", "Both files exist");
        else
            Log.d("Response", "Some files lack");

        return bothFilesExist;
    }

    private static boolean cleanOldWebsiteData(int jobId, Context context) {
        String pathToFile = getFullPathToFile(getOldFilePath(jobId), context);
        Log.d("Response", "Path to file: " + pathToFile);
        File file = new File(pathToFile);

        return file.delete();
    }

    private static boolean cleanNewWebsiteData(int jobId, Context context) {
        String pathToFile = getFullPathToFile(getNewFilePath(jobId), context);
        Log.d("Response", "Path to file: " + pathToFile);
        File file = new File(pathToFile);

        return file.delete();
    }

    private static String getFullPathToFile(String filename, Context context) {
        String pathToData = context.getFilesDir().getPath();

        return pathToData + "/" + filename;
    }
}
