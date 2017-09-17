package com.smutkiewicz.pagenotifier.service;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.smutkiewicz.pagenotifier.BuildConfig;
import com.smutkiewicz.pagenotifier.R;
import com.smutkiewicz.pagenotifier.database.DbDescription;

import static com.smutkiewicz.pagenotifier.MainActivity.MSG_FINISHED;
import static com.smutkiewicz.pagenotifier.MainActivity.MSG_FINISHED_WITH_ERROR;
import static com.smutkiewicz.pagenotifier.MainActivity.MSG_RESTART;
import static com.smutkiewicz.pagenotifier.MainActivity.MSG_START;
import static com.smutkiewicz.pagenotifier.MainActivity.MSG_STOP;
import static com.smutkiewicz.pagenotifier.service.ResponseMatcher.checkForChanges;
import static com.smutkiewicz.pagenotifier.service.ResponseMatcher.getNewFilePath;
import static com.smutkiewicz.pagenotifier.service.ResponseMatcher.saveFile;
import static com.smutkiewicz.pagenotifier.utilities.MyConnectivityManager.isAnyNetworkConnectionAvailable;

public class MyJobService extends JobService {
    private static final String TAG = MyJobService.class.getSimpleName();
    private static final String SERVICE_TAG = "Response";

    // kanał powiadomień dla Android O
    private static final String PAGE_NOTIFIER_CHANNEL_ID = "page_notifier_channel_id";

    // zestaw stałych do obsługi dołączania Extras dla serwisu
    public static final String MESSENGER_INTENT_KEY
            = BuildConfig.APPLICATION_ID + ".MESSENGER_INTENT_KEY";
    public static final String WORK_DURATION_KEY =
            BuildConfig.APPLICATION_ID + ".WORK_DURATION_KEY";
    public static final String JOB_NAME_KEY =
            BuildConfig.APPLICATION_ID + ".JOB_NAME_KEY";
    public static final String JOB_URL_KEY =
            BuildConfig.APPLICATION_ID + ".JOB_URL_KEY";
    public static final String JOB_ALERTS_KEY =
            BuildConfig.APPLICATION_ID + ".JOB_ALERTS_KEY";
    public static final String JOB_URI_KEY =
            BuildConfig.APPLICATION_ID + ".JOB_URI_KEY";
    public static final String JOB_ID_KEY =
            BuildConfig.APPLICATION_ID + ".JOB_ID_KEY";

    private Messenger mActivityMessenger;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "Service created");
        showToast("Service created");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "Service destroyed");
        showToast("Service destroyed");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mActivityMessenger = intent.getParcelableExtra(MESSENGER_INTENT_KEY);
        showToast("OnStartCommand");
        return START_NOT_STICKY;
    }

    @Override
    public boolean onStartJob(final JobParameters params) {
        final int jobId = params.getJobId();
        sendMessage(MSG_START, jobId);

        final int alerts = params.getExtras().getInt(JOB_ALERTS_KEY);
        final String name = params.getExtras().getString(JOB_NAME_KEY);
        final String url = params.getExtras().getString(JOB_URL_KEY);
        final Uri uri = Uri.parse(params.getExtras().getString(JOB_URI_KEY));
        final boolean alertsEnabled = (alerts == 1);

        if(!isAnyNetworkConnectionAvailable(getApplicationContext())) {
            Log.d(SERVICE_TAG,
                    "New task - downloading new website: no connectivity available");
            handleNoConnectivityJob(jobId);
            jobFinished(params, false);
            return true;
        }

        Handler jobHandler = new Handler();
        jobHandler.post(new Runnable() {
            @Override
            public void run() {
                MyStringRequest requestForNewWebsite =
                        new MyStringRequest(getApplicationContext(),
                                new MyStringRequest.ResponseInterface() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(SERVICE_TAG,
                                "New task - downloading new website: success");
                        saveFile(getNewFilePath(jobId),
                                response, getApplicationContext());

                        if(checkForChanges(jobId, getApplicationContext())) {
                            if(alertsEnabled)
                                showNotification(name, url);

                            handleFinishedJob(jobId, uri);
                            jobFinished(params, true);
                        } else {
                            handleRestartedJob(jobId);
                            jobFinished(params, false);
                        }
                    }

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d(SERVICE_TAG,
                                "New task - downloading new website: error");
                        handleErrorJob(jobId, uri);
                        jobFinished(params, true);
                    }
                });

                requestForNewWebsite.startRequestForWebsite(url);
            }
        });

        showToast("On Start Job " + jobId);
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        sendMessage(MSG_STOP, params.getJobId());
        showToast("Job stopped " + params.getJobId() + " ! ! !");

        return false;
    }

    private void handleNoConnectivityJob(int jobId) {
        Log.d(SERVICE_TAG, "handle no connectivity job");
        sendMessage(MSG_RESTART, jobId);
    }

    private void handleFinishedJob(int jobId, Uri uri) {
        Log.d(SERVICE_TAG, "handle finished job");
        sendMessage(MSG_FINISHED, jobId);
        setCurrentItemUpdated(uri);

        showToast("Job finished ! ! !");
    }

    private void handleRestartedJob(int jobId) {
        Log.d(SERVICE_TAG, " handle restarted job");
        sendMessage(MSG_RESTART, jobId);

        showToast("Job should be restarted ! ! !");
    }

    private void handleErrorJob(int jobId, Uri uri) {
        Log.d(SERVICE_TAG, "handle error job");
        sendMessage(MSG_FINISHED_WITH_ERROR, jobId);
        setCurrentItemJobEscapedWithError(uri);

        showToast("Job error.");
    }

    private void sendMessage(int messageID, @Nullable Object params) {
        if (mActivityMessenger == null) {
            Log.d(TAG, "Service is bound, not started. There's no callback to send a message to.");
            return;
        }

        // włóż klucz zadania do wiadomości
        int jobId = (int) params;
        Bundle bundle = new Bundle();
        bundle.putInt(JOB_ID_KEY, jobId);

        Message m = Message.obtain();
        m.what = messageID;
        m.obj = params;
        m.setData(bundle);

        try {
            mActivityMessenger.send(m);
        } catch (RemoteException e) {
            Log.e(TAG, "Error passing service object back to activity.");
        }
    }

    private void showToast(CharSequence text) {
        Context context = getApplicationContext();
        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
    }

    private void showNotification(String name, String url) {
        NotificationManager manager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        CharSequence text = getText(R.string.service_website_updated);

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                intent, 0);

        Notification.Builder builder = new Notification.Builder(this)
                .setSmallIcon(R.drawable.ic_updated_white_24dp)
                .setTicker(text)
                .setWhen(System.currentTimeMillis())
                .setContentTitle(name)
                .setContentText(url)
                .setContentIntent(contentIntent);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            setNotificationChannel();
            builder.setChannelId(PAGE_NOTIFIER_CHANNEL_ID);
        }

        Notification notification = builder.build();
        manager.notify(R.string.service_started, notification);
    }

    @TargetApi(26)
    private void setNotificationChannel() {
        NotificationChannel notificationChannel =
                new NotificationChannel(PAGE_NOTIFIER_CHANNEL_ID,
                        getString(R.string.service_notification_channel_name),
                        NotificationManager.IMPORTANCE_DEFAULT);
        notificationChannel.setLightColor(Color.RED);

        NotificationManager nm =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        nm.createNotificationChannel(notificationChannel);
    }

    private void setCurrentItemUpdated(Uri jobUri) {
        ContentValues values = new ContentValues();
        values.put(DbDescription.KEY_UPDATED, 1);
        values.put(DbDescription.KEY_ISENABLED, 0);
        getContentResolver().update(jobUri, values, null, null);
    }

    private void setCurrentItemJobEscapedWithError(Uri jobUri) {
        ContentValues values = new ContentValues();
        values.put(DbDescription.KEY_UPDATED, 0);
        values.put(DbDescription.KEY_ISENABLED, 0);
        getContentResolver().update(jobUri, values, null, null);
    }
}