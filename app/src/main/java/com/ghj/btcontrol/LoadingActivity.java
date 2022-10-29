package com.ghj.btcontrol;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;

import cn.pedant.SweetAlert.SweetAlertDialog;

/**
 * Created by ghj on 2017. 3. 22..
 */
public class LoadingActivity extends BaseActivity {

    ImageView imgIntro;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);

        imgIntro = findViewById(R.id.imgIntro);
        Animation scaleAnim = AnimationUtils.loadAnimation(this, R.anim.anim_scale);
        imgIntro.startAnimation(scaleAnim);
    }

    @Override
    public void onCreateAfter() {
        GoToMain();
    }

    /**
     * @desc 메인으로 이동
     */
    public void GoToMain(){
        new Handler(getMainLooper()){
            @Override
            public void handleMessage(Message msg) {
                Intent intent = new Intent(LoadingActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        }.sendEmptyMessageDelayed(0, 1500);
    }


    /**
     * @desc 뒤로가기시 앱 종료
     */
    @Override
    public void onBackPressed() {
        AppFinish();
    }


}
