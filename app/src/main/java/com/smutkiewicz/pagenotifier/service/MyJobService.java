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
import static com.smutkiewicz.pagenotifier.MainActivity.MSG_START;
import static com.smutkiewicz.pagenotifier.MainActivity.MSG_STOP;
import static com.smutkiewicz.pagenotifier.MainActivity.MSG_RESTART;
import static com.smutkiewicz.pagenotifier.MainActivity.WORK_DURATION_KEY;

public class MyJobService extends JobService {

    private static final String TAG = MyJobService.class.getSimpleName();
    private static final String ERROR_IN_REQUEST = "error_in_request";
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
        long duration = params.getExtras().getLong(WORK_DURATION_KEY);
        final String name = params.getExtras().getString(JOB_NAME_KEY);
        final String url = params.getExtras().getString(JOB_URL_KEY);
        final Uri uri = Uri.parse(params.getExtras().getString(JOB_URI_KEY));
        final boolean alertsEnabled = (alerts == 1);

        Handler preJobHandler = initPreJobHandler(jobId, url);

        Handler jobHandler = new Handler();
        jobHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.d("TAG", "Started request for new website");
                startRequestForNewWebsite(jobId, url);

                if(ResponseMatcher.checkForChanges(jobId)) {
                    sendMessage(MSG_STOP, jobId);
                    setCurrentItemUpdated(uri);
                    ResponseMatcher.cleanFinishedJobData(jobId);

                    if(alertsEnabled)
                        showNotification(name, url);

                    showToast("Job finished ! ! !");
                    jobFinished(params, false);
                } else {
                    //TODO jak zrestartować zadanie
                    sendMessage(MSG_RESTART, jobId);
                    ResponseMatcher.cleanNewWebsiteData(jobId);

                    showToast("Job should be restarted ! ! !");
                    jobFinished(params, false);
                }
            }
        }, duration);

        showToast("On Start Job " + jobId);
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        sendMessage(MSG_STOP, params.getJobId());

        Log.i(TAG, "on stop job: " + params.getJobId());
        showToast("Job stopped " + params.getJobId() + "! ! !");
        return false;
    }

    private Handler initPreJobHandler(final int jobId, final String url) {
        Handler preJobHandler = new Handler();
        preJobHandler.post(new Runnable() {
            @Override
            public void run() {
                startRequestForOldWebsite(jobId, url);
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
        Log.d("TAG", "Updated in database");
    }

    private void startRequestForNewWebsite(int jobId, String url) {
        RequestQueue mRequestQueue = initRequestQueue();
        StringRequest stringRequest = createStringRequestForNewWebsite(jobId, url);
        mRequestQueue.add(stringRequest);
    }

    private void startRequestForOldWebsite(int jobId, String url) {
        RequestQueue mRequestQueue = initRequestQueue();
        StringRequest stringRequest = createStringRequestForOldWebsite(jobId, url);
        mRequestQueue.add(stringRequest);
    }

    private StringRequest createStringRequestForNewWebsite(final int jobId, String url) {
        // Formulate the request and handle the response.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Do something with the response
                        showToast("Response of new is: "+ response.substring(0,500));
                        ResponseMatcher.saveNewWebsite(jobId, response, getApplicationContext());
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Handle error
                        showToast("Error!");
                        ResponseMatcher.saveNewWebsite(jobId, ERROR_IN_REQUEST, getApplicationContext());
                    }
                });

        return stringRequest;
    }

    private StringRequest createStringRequestForOldWebsite(final int jobId, String url) {
        // Formulate the request and handle the response.
        return new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Do something with the response
                        showToast("Response of old: "+ response.substring(0,500));
                        ResponseMatcher.saveOldWebsite(
                                jobId, response, getApplicationContext());
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Handle error
                        showToast("Error!");
                        ResponseMatcher.saveOldWebsite(
                                jobId, ERROR_IN_REQUEST, getApplicationContext());
                    }
                });
    }

    private RequestQueue initRequestQueue() {
        RequestQueue mRequestQueue;

        // Instantiate the cache
        Cache cache = new DiskBasedCache(getCacheDir(), 1024 * 1024); // 1MB cap

        // Set up the network to use HttpURLConnection as the HTTP client.
        Network network = new BasicNetwork(new HurlStack());

        // Instantiate the RequestQueue with the cache and network.
        mRequestQueue = new RequestQueue(cache, network);

        // Start the queue
        mRequestQueue.start();

        return mRequestQueue;
    }
}

