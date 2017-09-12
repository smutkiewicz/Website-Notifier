package com.smutkiewicz.pagenotifier.utilities;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

/*
* Dla urządzeń powyżej API 23 (M)
*/
public class PermissionGranter {
    public static final int WRITE_READ_PERMISSIONS_FOR_ADD = 1;
    public static final int WRITE_READ_PERMISSIONS_FOR_EDIT = 2;

    // pozwolenia z manifestu
    public static String[] permissions = new String[]{
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE};

    public static boolean permissionsGranted(Activity myActivity, int code) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            List<String> listPermissionsNeeded = new ArrayList<>();
            int result;

            for (String p : permissions) {
                result = ContextCompat.checkSelfPermission(myActivity, p);
                if (result != PackageManager.PERMISSION_GRANTED) {
                    listPermissionsNeeded.add(p);
                }
            }

            if (!listPermissionsNeeded.isEmpty()) {
                ActivityCompat.requestPermissions(myActivity,
                        listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]),
                        code);
                return false;
            }

            return true;
        } else {
            return true;
        }
    }
}
