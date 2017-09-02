package com.smutkiewicz.pagenotifier;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class WebValuesService extends Service {
    private final IBinder mBinder = new MyBinder();
    private List<String> resultList = new ArrayList<String>();
    private int counter = 1;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        addResultValues();
        return Service.START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        addResultValues();
        return mBinder;
    }

    public class MyBinder extends Binder {
        WebValuesService getService() {
            return WebValuesService.this;
        }
    }

    public List<String> getWordList() {
        return resultList;
    }

    private void addResultValues() {
        Log.d("TAG", "AddResultValues");
        Random random = new Random();
        List<String> input = Arrays.asList("Linux", "Android","iPhone","Windows7");
        resultList.add(input.get(random.nextInt(3)) + " " + counter++);
        if (counter == Integer.MAX_VALUE) {
            counter = 0;
        }
    }

    private void pushNotification() {
        int notificationId = 1;
        String url = "https://github.com/aron-bordin/Android-Tutorials/blob/master";
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));

        PendingIntent pendingIntent =
                PendingIntent.getActivity(WebValuesService.this, notificationId, intent,
                        PendingIntent.FLAG_UPDATE_CURRENT);

        Notification.Builder builder = new Notification.Builder(this)
                .setSmallIcon(R.drawable.ic_not_updated_black_24dp)
                .setContentTitle("Zmiany na stronie")
                .setContentText("Przejdź do adresu URL")
                .setContentIntent(pendingIntent);

        NotificationManager manager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(notificationId, builder.build());
    }
}

