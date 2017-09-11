package com.smutkiewicz.pagenotifier.service;

public class ResponseMatcher {
    public static void saveOldWebsite(int jobId, String response) {
        //TODO save website to file "job_old" + jobId + ".txt"
    }

    public static void saveNewWebsite(int jobId, String response) {
        //TODO save website to file "job_new" + jobId + ".txt"
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
        cleanOldWebsiteData(jobId);
        cleanNewWebsiteData(jobId);
    }

    public static void cleanNewWebsiteData(int jobId) {
        // TODO remove new data
    }

    private static boolean ifResponseIsValid(int jobId) {
        //TODO check validity
        return true;
    }

    private static boolean ifWebsitesMatch(int jobId) {
        //TODO if matches
        return true;
    }

    private static void cleanOldWebsiteData(int jobId) {
        // TODO remove old data
    }
}
