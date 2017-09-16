package com.smutkiewicz.pagenotifier.utilities;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class MyConnectivityManager {
    // sprawdź, czy urządzenie posiada jakiekolwiek połączenie z internetem
    public static boolean isAnyNetworkConnectionAvailable(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

        return activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
    }
}