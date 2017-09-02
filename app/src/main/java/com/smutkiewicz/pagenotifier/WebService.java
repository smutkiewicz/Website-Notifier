package com.smutkiewicz.pagenotifier;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.widget.Toast;

/**
 * Created by Admin on 2017-08-31.
 */

public class WebService extends Service {
    /*
    * Zestaw komend do serwisu
    */
    private static final int MSG_START_SERVICE = 1;
    private static final int MSG_ADD_UPDATE_TASK = 2;
    private static final int MSG_DELETE_TASK = 3;
    private static final int MSG_STOP_SERVICE = 4;

    /** For showing and hiding our notification. */
    private NotificationManager mNM;
    private int mStartMode;       // indicates how to behave if the service is killed
    private IBinder mBinder;      // interface for clients that bind
    private boolean mAllowRebind; // indicates whether onRebind should be used

    private Looper mServiceLooper;
    private ServiceHandler mServiceHandler;

    // Handler that receives messages from the thread
    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }

        /*@Override
        public void handleMessage(Message msg) {
            // Normally we would do some work here, like download a file.
            // For our sample, we just sleep for 5 seconds.
            try {
                Thread.sleep(5000);
                Context context = getApplicationContext();
                CharSequence text = "Handle message size " + tasks.size();
                int duration = Toast.LENGTH_SHORT;

                Toast toast = Toast.makeText(context, text, duration);
                toast.show();
            } catch (InterruptedException e) {
                // Restore interrupt status.
                Thread.currentThread().interrupt();
            }
            // Stop the service using the startId, so that we don't stop
            // the service in the middle of handling another job
            //stopSelf(msg.arg1);
        }*/

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_START_SERVICE:
                    //TODO START
                    break;
                case MSG_ADD_UPDATE_TASK:
                    //TODO ADD
                    addUpdateTask(msg);
                    break;
                case MSG_DELETE_TASK:
                    //TODO DELETE
                    break;
                case MSG_STOP_SERVICE:
                    stopSelf(msg.arg1);
                    break;
                default:
                    super.handleMessage(msg);
            }
        }

        private void addUpdateTask(Message msg) {
            try {
                showToast("ADD_UPDATE_TASK START " + msg.arg1);
                Thread.sleep(5000);
                showToast("ADD_UPDATE_TASK STOP " + msg.arg1);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

    }

    public class MyBinder extends Binder {
        WebService getService() {
            return WebService.this;
        }
    }

    @Override
    public void onCreate() {
        HandlerThread thread = new HandlerThread("ServiceStartArguments",
                Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();

        mServiceLooper = thread.getLooper();
        mServiceHandler = new ServiceHandler(mServiceLooper);

        mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        showNotification();
        showToast(getText(R.string.service_started));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // The service is starting, due to a call to startService()

        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // A client is binding to the service with bindService()
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        // All clients have unbound with unbindService()
        return mAllowRebind;
    }

    @Override
    public void onRebind(Intent intent) {
        // A client is binding to the service with bindService(),
        // after onUnbind() has already been called
    }

    @Override
    public void onDestroy() {
        mNM.cancel(R.string.service_started);
        showToast(getText(R.string.service_stopped));
    }

    public void addUpdateServiceTask() {
        Message msg = mServiceHandler.obtainMessage();
        msg.arg1 = MSG_ADD_UPDATE_TASK;
        mServiceHandler.sendMessage(msg);
    }

    public void stopServiceTask() {
        Message msg = mServiceHandler.obtainMessage();
        msg.arg1 = MSG_STOP_SERVICE;
        mServiceHandler.sendMessage(msg);
    }

    /**
     * Show a notification while this service is running.
     */
    private void showNotification() {
        // In this sample, we'll use the same text for the ticker and the expanded notification
        CharSequence text = getText(R.string.service_started);

        // The PendingIntent to launch our activity if the user selects this notification
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, MainActivity.class), 0);

        // Set the info for the views that show in the notification panel.
        Notification notification = new Notification.Builder(this)
                .setSmallIcon(R.drawable.ic_not_updated_black_24dp)  // the status icon
                .setTicker(text)  // the status text
                .setWhen(System.currentTimeMillis())  // the time stamp
                .setContentTitle(getText(R.string.service_label))  // the label of the entry
                .setContentText(text)  // the contents of the entry
                .setContentIntent(contentIntent)  // The intent to send when the entry is clicked
                .build();

        // Send the notification.
        // We use a string id because it is a unique number.  We use it later to cancel.
        mNM.notify(R.string.service_started, notification);
    }

    private void showToast(CharSequence text) {
        Context context = getApplicationContext();
        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
    }

}
