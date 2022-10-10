package com.ghj.btcontrol;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

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


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d("aaaaa", "1111!");
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
        Log.d("aaaaa", "22222!");
        boolean isPermission = CheckPermission();

        if(isPermission) {
            isAppReady = true;
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
                alert("권한이 필요합니다.", sweetAlertDialog -> AppFinish());
            }
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
                    Log.d("aaaaa", "33333!");
                    checkActivity();
                    runActivity();
                } else {
                    alert("권한이 필요합니다.", sweetAlertDialog -> AppFinish());
                }
                break;
        }
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
