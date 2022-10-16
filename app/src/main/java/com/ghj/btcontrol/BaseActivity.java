package com.ghj.btcontrol;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.ghj.btcontrol.util.PermissionUtil;

import cn.pedant.SweetAlert.SweetAlertDialog;

public abstract class BaseActivity extends AppCompatActivity
{
    private final int PERMISSION_REQUEST_CODE = 1;

    public Activity mActivity;
    public Context mContext;

    boolean isAppReady = false;
    boolean isPostCreate = false;
    String[] permissions = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) ? PermissionUtil.PERMISSIONS31 : PermissionUtil.PERMISSIONS;

    // GPS On 설정결과
    ActivityResultLauncher<Intent> mGPSOnResult = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    checkActivity();
                }
            }
    );


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mActivity = this;
        mContext = this;
        checkActivity();
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        isPostCreate = true;
        runActivity();
    }

    /**
     * @desc 액티비트 실행 체크
     */
    private void checkActivity() {
        boolean checked = CheckPermission();
        if(!checked)
            return;

        checked = CheckGPSOn();

        if(checked) {
            isAppReady = true;
            runActivity();
        }
    }

    /**
     * @desc 앱실행
     */
    private void runActivity() {
        // 로직 수행
        if(isAppReady && isPostCreate) {
            onCreateAfter();
        }
    }

    abstract public void onCreateAfter();

    /**
     * @desc 권한체크
     */
    private boolean CheckPermission(){
        if(PermissionUtil.checkPermissions(this, permissions) == PermissionUtil.PERMISSION_GRANTED) {
            return true;
        }
        else {
            if(this instanceof LoadingActivity) {
                PermissionUtil.requestPermissions(this, permissions, PERMISSION_REQUEST_CODE);
            }
            else {
                alert("권한이 필요합니다.", sweetAlertDialog -> {
                    sweetAlertDialog.dismiss();
                    AppFinish();
                });
            }
            return false;
        }
    }

    /**
     * @desc GPS On 체크
     */
    private boolean CheckGPSOn() {
        LocationManager lm = (LocationManager) getSystemService(LOCATION_SERVICE);
        if(lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            return true;
        }
        else {
            alert("GPS를 켜주세요.", sweetAlertDialog -> {
                sweetAlertDialog.dismiss();
                moveToGPSOn();
            });
            return false;
        }
    }

    /**
     * @desc 권한요청 후 결과
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                boolean result = true;
                for (int grant : grantResults) {
                    if (grant != PackageManager.PERMISSION_GRANTED) {
                        result = false;
                        break;
                    }
                }

                if (result) {
                    checkActivity();
                } else {
                    alert("권한이 필요합니다.", sweetAlertDialog -> {
                        sweetAlertDialog.dismiss();
                        AppFinish();
                    });
                }
                break;
        }
    }

    /**
     * @desc GPS 설정
     */
    private void moveToGPSOn() {
        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        mGPSOnResult.launch(intent);
    }

    /**
     * @desc 앱종료
     */
    public void AppFinish(){
        finish();
        System.exit(0);
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    /**
     * @desc 다이얼로그
     */
    public void alert(String msg, SweetAlertDialog.OnSweetClickListener listener) {
        new SweetAlertDialog(this)
                .setTitleText(msg)
                .setConfirmText("확인")
                .setConfirmClickListener(listener)
                .show();
    }
}
