package com.ghj.btcontrol.util;


import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

public class PermissionUtil {

    public static int PERMISSION_RATIONALE = -2;
    public static int PERMISSION_DENIED = -1;
    public static int PERMISSION_GRANTED = 0;

    @RequiresApi(api = Build.VERSION_CODES.S)
    public static String[] PERMISSIONS31 = new String[]{
            Manifest.permission.BLUETOOTH_ADVERTISE,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };

    public static String[] PERMISSIONS = new String[] {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };

    // 권한 있는지 확인
    public static int checkPermissions(Activity activity, String[] permissions) {
        if( Build.VERSION.SDK_INT < Build.VERSION_CODES.M ) {
            return PERMISSION_GRANTED;
        }

        if( permissions == null || permissions.length == 0 ) {
            return PERMISSION_GRANTED;
        }

        List<String> deniedPermissions = new ArrayList<>();
        boolean isRationale = false;
        for( String perm : permissions ) {
            if(ContextCompat.checkSelfPermission(activity, perm) != PackageManager.PERMISSION_GRANTED) {
                deniedPermissions.add(perm);

                // 이전에 거부한적 있는지
                if(ActivityCompat.shouldShowRequestPermissionRationale(activity, perm)) {
                    isRationale = true;
                }
            }
        }

        if( deniedPermissions.size() > 0 && !isRationale) {
            return PERMISSION_DENIED;
        }
        else if( deniedPermissions.size() > 0 && isRationale) {
            return PERMISSION_RATIONALE;
        }
        else {
            return PERMISSION_GRANTED;
        }
    }

    // 권한 요청
    public static void requestPermissions(Activity activity, String[] permissions, int requestCode) {
        if(activity != null) {
            ActivityCompat.requestPermissions(activity, permissions, requestCode);
        }
    }
}
