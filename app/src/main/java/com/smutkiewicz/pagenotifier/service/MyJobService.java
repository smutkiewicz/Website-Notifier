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
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Cache;
import com.android.volley.Network;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.StringRequest;
import com.smutkiewicz.pagenotifier.R;
import com.smutkiewicz.pagenotifier.database.DbDescription;

import static com.smutkiewicz.pagenotifier.MainActivity.JOB_ALERTS_KEY;
import static com.smutkiewicz.pagenotifier.MainActivity.JOB_NAME_KEY;
import static com.smutkiewicz.pagenotifier.MainActivity.JOB_URI_KEY;
import static com.smutkiewicz.pagenotifier.MainActivity.JOB_URL_KEY;
import static com.smutkiewicz.pagenotifier.MainActivity.MESSENGER_INTENT_KEY;
import static com.smutkiewicz.pagenotifier.MainActivity.MSG_FINISHED;
import static com.smutkiewicz.pagenotifier.MainActivity.MSG_RESTART;
import static com.smutkiewicz.pagenotifier.MainActivity.MSG_START;
import static com.smutkiewicz.pagenotifier.MainActivity.MSG_STOP;
import static com.smutkiewicz.pagenotifier.service.ResponseMatcher.checkForChanges;
import static com.smutkiewicz.pagenotifier.service.ResponseMatcher.cleanFinishedJobData;
import static com.smutkiewicz.pagenotifier.service.ResponseMatcher.cleanNotFinishedJobData;
import static com.smutkiewicz.pagenotifier.service.ResponseMatcher.getNewFilePath;
import static com.smutkiewicz.pagenotifier.service.ResponseMatcher.getOldFilePath;
import static com.smutkiewicz.pagenotifier.service.ResponseMatcher.saveFile;

public class MyJobService extends JobService {
    private static final String TAG = MyJobService.class.getSimpleName();
    private static final String PAGE_NOTIFIER_CHANNEL_ID = "page_notifier_channel_id";

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
        final RequestQueue mRequestQueue = initRequestQueue();

        Handler preJobHandler = initPreJobHandler(mRequestQueue, jobId, url);

        Handler jobHandler = new Handler();
        jobHandler.post(new Runnable() {
            @Override
            public void run() {
                StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                Log.d("ResponseMatcher", "onResponse new");
                                saveFile(getNewFilePath(jobId),
                                        response, getApplicationContext());

                                if(checkForChanges(jobId, getApplicationContext())) {
                                    if(alertsEnabled) {
                                        showNotification(name, url);
                                    }

                                    handleFinishedJob(jobId, uri);
                                    jobFinished(params, false);
                                } else {
                                    handleRestartedJob(jobId, uri);
                                    jobFinished(params, true);
                                }
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Log.d("ResponseMatcher", "onErrorResponse new");
                                handleErrorJob(jobId, uri);
                                jobFinished(params, false);
                            }
                        });

                mRequestQueue.add(stringRequest);
            }
        });

        showToast("On Start Job " + jobId);
        return true;
    }

    private void handleFinishedJob(int jobId, Uri uri) {
        Log.d("ResponseMatcher", "Checked for changes: true");
        sendMessage(MSG_FINISHED, jobId);
        setCurrentItemUpdated(uri);

        cleanFinishedJobData(jobId, getApplicationContext());
        showToast("Job finished ! ! !");
    }

    private void handleRestartedJob(int jobId, Uri uri) {
        //TODO jak zrestartować zadanie
        Log.d("ResponseMatcher", "Checked for changes: false");
        sendMessage(MSG_RESTART, jobId);

        cleanNotFinishedJobData(jobId, getApplicationContext());
        showToast("Job should be restarted ! ! !");
    }

    private void handleErrorJob(int jobId, Uri uri) {
        Log.d("ResponseMatcher", "Checked for changes: error");
        sendMessage(MSG_STOP, jobId);
        setCurrentItemJobEscapedWithError(uri);

        cleanFinishedJobData(jobId, getApplicationContext());
        showToast("Job error.");
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        sendMessage(MSG_STOP, params.getJobId());

        Log.i(TAG, "on stop job: " + params.getJobId());
        showToast("Job stopped " + params.getJobId() + "! ! !");
        return false;
    }

    private Handler initPreJobHandler(final RequestQueue requestQueue,
                                      final int jobId, final String url) {
        Handler preJobHandler = new Handler();
        preJobHandler.post(new Runnable() {
            @Override
            public void run() {
                startRequestForOldWebsite(requestQueue, jobId, url);
            }
        });
        return preJobHandler;
    }

    private void sendMessage(int messageID, @Nullable Object params) {
        if (mActivityMessenger == null) {
            Log.d(TAG, "Service is bound, not started. There's no callback to send a message to.");
            return;
        }

        Message m = Message.obtain();
        m.what = messageID;
        m.obj = params;

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

    private void startRequestForOldWebsite(RequestQueue requestQueue, int jobId, String url) {
        StringRequest stringRequest = createStringRequestForOldWebsite(jobId, url);
        requestQueue.add(stringRequest);
    }

    private RequestQueue initRequestQueue() {
        RequestQueue mRequestQueue;
        Cache cache = new DiskBasedCache(getCacheDir(), 1024 * 1024);
        Network network = new BasicNetwork(new HurlStack());
        mRequestQueue = new RequestQueue(cache, network);
        mRequestQueue.start();
        return mRequestQueue;
    }

    private StringRequest createStringRequestForOldWebsite(final int jobId, String url) {
        return new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("ResponseMatcher", "onResponse old");
                        ResponseMatcher.saveFile(getOldFilePath(jobId),
                                response, getApplicationContext());
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("ResponseMatcher", "onErrorResponse old");
                        showToast("Response of old: error");
                    }
                });
    }
}