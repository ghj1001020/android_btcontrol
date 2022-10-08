package com.ghj.btcontrol;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;

import cn.pedant.SweetAlert.SweetAlertDialog;

/**
 * Created by ghj on 2017. 3. 22..
 */
public class LoadingActivity extends AppCompatActivity {

    final int PERMISSION_REQUEST_CODE = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);

        CheckPermission();
    }


    /**
     * @desc 권한체크
     */
    public void CheckPermission(){
        if(Build.VERSION.SDK_INT>=23){
            int permission1 = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
            if(permission1 == PackageManager.PERMISSION_DENIED){
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_CODE);
            }else{
                GoToMain();
            }
        }else{
            GoToMain();
        }
    }

    /**
     * @desc 권한요청 후 결과
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case PERMISSION_REQUEST_CODE:
                boolean result = true;
                for(int grant : grantResults){
                    if(grant!=PackageManager.PERMISSION_GRANTED){
                        result = false;
                        break;
                    }
                }
                if(result){
                    GoToMain();
                }else{
                    new SweetAlertDialog(this)
                            .setTitleText("권한이 필요합니다.")
                            .setConfirmText("확인")
                            .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                @Override
                                public void onClick(SweetAlertDialog sweetAlertDialog) {
                                    AppFinish();
                                }
                            })
                            .show();
                }
                break;
        }
    }

    /**
     * @desc 메인으로 이동
     */
    public void GoToMain(){
        new Handler(){
            @Override
            public void handleMessage(Message msg) {
                Intent intent = new Intent(LoadingActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        }.sendEmptyMessageDelayed(0, 1000);
    }


    /**
     * @desc 뒤로가기시 앱 종료
     */
    @Override
    public void onBackPressed() {
        AppFinish();
    }

    /**
     * @desc 앱종료
     */
    public void AppFinish(){
        finish();
        System.exit(0);
        android.os.Process.killProcess(android.os.Process.myPid());
    }
}
