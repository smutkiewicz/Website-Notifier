package com.smutkiewicz.pagenotifier.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.smutkiewicz.pagenotifier.R;

import static com.smutkiewicz.pagenotifier.MainActivity.JOB_NAME_KEY;
import static com.smutkiewicz.pagenotifier.MainActivity.JOB_URL_KEY;
import static com.smutkiewicz.pagenotifier.MainActivity.MESSENGER_INTENT_KEY;
import static com.smutkiewicz.pagenotifier.MainActivity.MSG_START;
import static com.smutkiewicz.pagenotifier.MainActivity.MSG_STOP;
import static com.smutkiewicz.pagenotifier.MainActivity.WORK_DURATION_KEY;

public class MyJobService extends JobService {

    private static final String TAG = MyJobService.class.getSimpleName();

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
        showToast("On Start Job " + params.getJobId());
        sendMessage(MSG_START, params.getJobId());

        long duration = params.getExtras().getLong(WORK_DURATION_KEY);
        final String name = params.getExtras().getString(JOB_NAME_KEY);
        final String url = params.getExtras().getString(JOB_URL_KEY);

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                sendMessage(MSG_STOP, params.getJobId());
                showToast("Job finished ! ! !");
                showNotification(name, url);
                jobFinished(params, false);
            }
        }, duration);

        Log.i(TAG, "on start job: " + params.getJobId());

        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        sendMessage(MSG_STOP, params.getJobId());
        Log.i(TAG, "on stop job: " + params.getJobId());
        showToast("Job stopped " + params.getJobId() + "! ! !");
        return false;
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
        CharSequence text = getText(R.string.service_started);

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                intent, 0);

        Notification notification = new Notification.Builder(this)
                .setSmallIcon(R.drawable.ic_not_updated_black_24dp)
                .setTicker(text)
                .setWhen(System.currentTimeMillis())
                .setContentTitle(name)
                .setContentText(url)
                .setContentIntent(contentIntent)
                .build();

        manager.notify(R.string.service_started, notification);
    }
}

