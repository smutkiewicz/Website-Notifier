package com.smutkiewicz.pagenotifier.service;

import android.app.Activity;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.PersistableBundle;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.smutkiewicz.pagenotifier.R;

import java.util.List;

import static com.smutkiewicz.pagenotifier.service.MyJobService.JOB_ALERTS_KEY;
import static com.smutkiewicz.pagenotifier.service.MyJobService.JOB_NAME_KEY;
import static com.smutkiewicz.pagenotifier.service.MyJobService.JOB_URI_KEY;
import static com.smutkiewicz.pagenotifier.service.MyJobService.JOB_URL_KEY;
import static com.smutkiewicz.pagenotifier.service.MyJobService.WORK_DURATION_KEY;
import static com.smutkiewicz.pagenotifier.service.ResponseMatcher.getOldFilePath;
import static com.smutkiewicz.pagenotifier.service.ResponseMatcher.saveFile;

public class MyJobScheduler {
    private static final String SCHEDULER_TAG = "Response";

    private Context mContext;
    private ComponentName mServiceComponent;
    private Activity mActivity;

    public MyJobScheduler(Context context, ComponentName serviceComponent, Activity activity) {
        mContext = context;
        mServiceComponent = serviceComponent;
        mActivity = activity;
    }

    public void scheduleJob(Job job) {
        Log.d(SCHEDULER_TAG, "Scheduling job");

        // sample values
        boolean requiresUnmetered = job.requiresUnmetered; // wymaga połączenia tylko przez WiFi
        boolean requiresAnyConnectivity = job.requiresAnyConnectivity; // wymaga WiFi lub czegokolwiek

        // build Job for JobService
        JobInfo.Builder builder = new JobInfo.Builder(job.id, mServiceComponent);
        builder.setRequiresDeviceIdle(job.requiresIdle);
        builder.setRequiresCharging(job.requiresCharging);
        setJobPeriodic(builder, job.delay);

        if (requiresUnmetered) {
            builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED);
        } else if (requiresAnyConnectivity) {
            builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY);
        }

        // put extras for service
        PersistableBundle extras = putExtrasToAPersistableBundle(job);
        builder.setExtras(extras);

        // schedule
        JobScheduler service =
                (JobScheduler) mContext.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        service.schedule(builder.build());

        // handle pre job tasks
        initPreJobTasks(job);

        showToast(mContext.getString(R.string.job_scheduler_scheduling_job)
                + " delay: " + job.delay);
        updatePendingJobs(mContext);
    }

    public void cancelAllJobs() {
        JobScheduler service = getSystemJobScheduler();
        service.cancelAll();
        Log.d(SCHEDULER_TAG, "Cancelling all jobs");

        showToast(R.string.job_scheduler_all_jobs_cancelled);
    }

    public void finishJob(int jobId) {
        JobScheduler jobScheduler = getSystemJobScheduler();
        List<JobInfo> allPendingJobs = jobScheduler.getAllPendingJobs();

        if (allPendingJobs.size() > 0) {
            jobScheduler.cancel(jobId);
            updatePendingJobs(mContext);
            showToastFormatted(R.string.job_scheduler_cancelled_job, jobId);
        } else {
            showToast(R.string.job_scheduler_no_jobs_to_cancel);
        }
    }

    public static void cancelFinishedPeriodicJob(Context context, int jobId) {
        JobScheduler jobScheduler =
                (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        List<JobInfo> allPendingJobs = jobScheduler.getAllPendingJobs();

        if (allPendingJobs.size() > 0) {
            jobScheduler.cancel(jobId);
            Log.d(SCHEDULER_TAG, "Forcing cancel succeded");
        } else {
            Log.d(SCHEDULER_TAG, "Forcing cancel failed");
        }
    }

    public void resetJob(Job job) {
        finishJob(job.id);
        scheduleJob(job);
    }

    public static void updatePendingJobs(Context context) {
        JobScheduler jobScheduler =
                (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        List<JobInfo> allPendingJobs = jobScheduler.getAllPendingJobs();
        String text = "";

        if (allPendingJobs.size() > 0) {
            for(JobInfo job : allPendingJobs) {
                text = text + job.getId() + " ";
            }
            Log.d(SCHEDULER_TAG, "Pending jobs info: " + text);
        } else {
            Log.d(SCHEDULER_TAG, "Pending jobs info: no pending jobs");
        }
    }

    private void setJobPeriodic(JobInfo.Builder builder, long delay) {
        // obsługa różnicy w funkcjonowaniu serwisu na API > 23
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            // TODO periodic for >= N problem
            builder.setPeriodic(delay, delay);
        } else {
            builder.setPeriodic(delay);
        }

        builder.setPersisted(true);
    }

    private PersistableBundle putExtrasToAPersistableBundle(Job job) {
        PersistableBundle extras = new PersistableBundle();
        extras.putLong(WORK_DURATION_KEY, job.workDuration);
        //do ustawienia powiadomienia
        extras.putString(JOB_NAME_KEY, job.name);
        extras.putString(JOB_URL_KEY, job.url);
        extras.putInt(JOB_ALERTS_KEY, (job.alertsEnabled) ? 1 : 0);
        extras.putString(JOB_URI_KEY, (job.uri).toString());
        return extras;
    }

    private void initPreJobTasks(final Job job) {
        Handler preJobTask = new Handler();
        preJobTask.post(new Runnable() {
            @Override
            public void run() {
                startRequestForOldWebsite(job.id, job.url);
            }
        });
    }

    private void startRequestForOldWebsite(final int jobId, String url) {
        MyStringRequest request =
                new MyStringRequest(mContext,
                        new MyStringRequest.ResponseInterface() {
                            @Override
                            public void onResponse(String response) {
                                Log.d(SCHEDULER_TAG,
                                        "Prejob task - downloading old website: success");
                                saveFile(getOldFilePath(jobId),
                                        response, mContext);
                            }

                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Log.d(SCHEDULER_TAG,
                                        "Prejob task - downloading old website: failed");
                            }
                        });

        request.startRequestForWebsite(url);
    }

    private JobScheduler getSystemJobScheduler() {
        return (JobScheduler) mContext.getSystemService(Context.JOB_SCHEDULER_SERVICE);
    }

    private void showToast(int resId) {
        Toast.makeText(
                mActivity,
                mContext.getString(resId),
                Toast.LENGTH_SHORT).show();
    }

    private void showToast(String text) {
        Toast.makeText(
                mActivity,
                text,
                Toast.LENGTH_SHORT).show();
    }

    private void showToastFormatted(int resId, int jobId) {
        Toast.makeText(
                mActivity,
                String.format(
                        mContext.getString(resId),
                        jobId),
                Toast.LENGTH_SHORT).show();
    }
}
