package com.smutkiewicz.pagenotifier;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Admin on 2017-08-31.
 */

public class WebService extends Service {
    private final IBinder mBinder = new MyBinder();
    private Looper mServiceLooper;
    private ServiceHandler mServiceHandler;

    private List<MyThread> tasks = new ArrayList<>();

    // Handler that receives messages from the thread
    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            // Normally we would do some work here, like download a file.
            // For our sample, we just sleep for 5 seconds.
            try {
                for(MyThread t : tasks) {
                    t.start();
                }

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
        }
    }

    public class MyBinder extends Binder {
        WebService getService() {
            return WebService.this;
        }

        void updateServiceTask(int id, int delayStep, String name, String url) {
            if(doTaskAlreadyExist(id)) {
                //TODO jeśli task już istnieje, to usuń go i wywołaj nowy
                try {
                    deleteServiceTask(getIndexById(id));
                    MyThread newServiceTask = new MyThread(id, delayStep, name, url);
                    tasks.add(newServiceTask);
                    newServiceTask.start();

                } catch (InvaildIndexByIdException e) {
                    Log.d("TAG", e.getMessage());
                }

            } else {
                MyThread newServiceTask = new MyThread(id, delayStep, name, url);
                tasks.add(newServiceTask);
                newServiceTask.start();
            }
        }

        void deleteServiceTask(int id) {
            try {
                tasks.remove(getIndexById(id));
            } catch (InvaildIndexByIdException e) {
                Log.d("TAG", e.getMessage());
            }
        }

        void stopTask(int id) {
            try {
                tasks.get(getIndexById(id)).stop();
            } catch (InvaildIndexByIdException e) {
                Log.d("TAG", e.getMessage());
            }
        }

        private boolean doTaskAlreadyExist(int newId) {
            for(MyThread t : tasks) {
                if(t.getId() == newId)
                    return true;
            }
            return false;
        }

        int getIndexById(int id) throws InvaildIndexByIdException {
            int arrayIndex = 0;
            for(MyThread t : tasks) {
                if(t.getId() == id)
                    return arrayIndex;
                else
                    arrayIndex++;
            }

            throw new InvaildIndexByIdException("Invalid index");
        }
    }

    private class MyThread {
        private int id;
        private int delayStep;
        private String name;
        private String url;

        public MyThread(int id, int delayStep, String name, String url) {
            this.id = id;
            this.delayStep = delayStep;
            this.name = name;
            this.url = url;
        }

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getUrl() {
            return url;
        }

        public void start() {
            //TODO start()
            Toast.makeText(getApplicationContext(), "thread starting", Toast.LENGTH_SHORT).show();
            Message msg = mServiceHandler.obtainMessage();
            msg.arg1 = 1;
            mServiceHandler.sendMessage(msg);
            pushNotification(this);
            /*final Handler handler = new Handler();
            final Runnable r = new Runnable() {
                public void run() {
                    handler.postDelayed(this, 1000);
                    pushNotification(MyThread.this);
                }
            };
            handler.postDelayed(r, 1000);*/
        }

        public void stop() {
            //TODO stop()
        }
    }

    @Override
    public void onCreate() {
        // Start up the thread running the service.  Note that we create a
        // separate thread because the service normally runs in the process's
        // main thread, which we don't want to block.  We also make it
        // background priority so CPU-intensive work will not disrupt our UI.
        HandlerThread thread = new HandlerThread("ServiceStartArguments",
                Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();

        // Get the HandlerThread's Looper and use it for our Handler
        mServiceLooper = thread.getLooper();
        mServiceHandler = new ServiceHandler(mServiceLooper);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(this, "service starting", Toast.LENGTH_SHORT).show();

        // For each start request, send a message to start a job and deliver the
        // start ID so we know which request we're stopping when we finish the job
        Message msg = mServiceHandler.obtainMessage();
        msg.arg1 = startId;
        mServiceHandler.sendMessage(msg);

        // If we get killed, after returning from here, restart
        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onDestroy() {
        Toast.makeText(this, "service done", Toast.LENGTH_SHORT).show();
    }

    private void pushNotification(MyThread t) {
        int notificationId = t.getId();
        String url = t.getUrl();
        String name = t.getName();

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));

        PendingIntent pendingIntent =
                PendingIntent.getActivity(WebService.this, notificationId, intent,
                        PendingIntent.FLAG_UPDATE_CURRENT);

        Notification.Builder builder = new Notification.Builder(this)
                .setSmallIcon(R.drawable.ic_not_updated_black_24dp)
                .setContentTitle("Zmiany na stronie w zadaniu: " + name)
                .setContentText("Przejdź do adresu URL")
                .setContentIntent(pendingIntent);

        NotificationManager manager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(notificationId, builder.build());
    }

    private class InvaildIndexByIdException extends Exception {

        public InvaildIndexByIdException(String message) {
            super(message);
        }

        public InvaildIndexByIdException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
