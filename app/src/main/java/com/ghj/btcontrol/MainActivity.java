package com.ghj.btcontrol;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;

import com.ghj.btcontrol.bluetooth.BluetoothService;
import com.ghj.btcontrol.fragment.ConnectFragment;
import com.ghj.btcontrol.fragment.ScanFragment;
import com.ghj.btcontrol.util.PermissionUtil;
import com.ghj.btcontrol.util.Util;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;

public class MainActivity extends BaseFragmentActivity {

    private final String TAG = "MainActivity";

    private final Handler mBTHandler = new BTHandler(this);
    private BluetoothService mBTService;

    // 다른기기에서 내기기 찾기 콜백
    ActivityResultLauncher<Intent> mDiscovery = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {}
    );

    // SAF에서 파일선택 후 콜백
    ActivityResultLauncher<Intent> mSAF = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            (ActivityResult result) -> {
                if(result.getResultCode() == Activity.RESULT_OK) {
                    if(result.getData() == null) return;
                    Uri uri = result.getData().getData();
                    if(uri != null) {
                        Log.d(TAG, uri.getPath() + " , " + uri.toString());

                        if(getCurrentFragment() instanceof ConnectFragment) {

                            ((ConnectFragment) getCurrentFragment()).SendFile(uri);
                        }
                    }
                }
            }
    );


    @Override
    public int getFragmentID() {
        return R.id.fragment;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //블루투스
        mBTService = BluetoothService.getBluetoothService(this, mBTHandler);
    }

    @Override
    public void onCreateAfter() {

    }

    /**
     * @desc 블루투스 서비스
     */
    public BluetoothService getBTService() {
        return mBTService;
    }

    /**
     * @desc 앱종료
     */
    @Override
    public void AppFinish(){
        if(mBTService!=null){
            mBTService.onDestroyBluetooth();
        }
        super.AppFinish();
    }

    /**
     * @desc 다른기기에서 내기기 찾기
     */
    public void discoverableDevice() {
        if (PermissionUtil.checkBluetoothPermission(mActivity)) {
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            intent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 0);
            mDiscovery.launch(intent);
        }
    }

    /**
     * @desc 블루투스 콜백 핸들러
     */
    private static class BTHandler extends Handler {

        private final WeakReference<MainActivity> mActivity;

        public BTHandler(MainActivity activity) {
            super(Looper.getMainLooper());
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            if(msg.what == BluetoothService.STATE_ON_HANDLER_CODE){
                if( mActivity.get().getCurrentFragment() instanceof ScanFragment ) {
                    ((ScanFragment) mActivity.get().getCurrentFragment()).stateOn();
                }
            }
            else if(msg.what == BluetoothService.STATE_OFF_HANDLER_CODE){
                if( mActivity.get().getCurrentFragment() instanceof ScanFragment ) {
                    ((ScanFragment) mActivity.get().getCurrentFragment()).stateOff();
                }
            }
            else if(msg.what == BluetoothService.DISCOVERY_START_HANDLER_CODE){
                if( mActivity.get().getCurrentFragment() instanceof ScanFragment ) {
                    ((ScanFragment) mActivity.get().getCurrentFragment()).discoveryStart();
                }
            }
            else if(msg.what == BluetoothService.DISCOVERY_FINISH_HANDLER_CODE){
                if( mActivity.get().getCurrentFragment() instanceof ScanFragment ) {
                    ((ScanFragment) mActivity.get().getCurrentFragment()).discoveryFinish();
                }
            }
            else if(msg.what == BluetoothService.DISCOVERY_FOUND_HANDLER_CODE){
                Bundle bundle = msg.getData();
                BluetoothDevice device = bundle.getParcelable("device");
                int rssi = bundle.getInt("rssi");
                if( mActivity.get().getCurrentFragment() instanceof ScanFragment ) {
                    ((ScanFragment) mActivity.get().getCurrentFragment()).discoveryFound(device, rssi);
                }
            }
            else if(msg.what == BluetoothService.BONDED_HANDLER_CODE){
                Bundle bundle = msg.getData();
                BluetoothDevice device = bundle.getParcelable("device");
                if( mActivity.get().getCurrentFragment() instanceof ScanFragment ) {
                    ((ScanFragment) mActivity.get().getCurrentFragment()).bonded(device);
                }
            }
            else if(msg.what == BluetoothService.BONDED_CANCEL_HANDLER_CODE){
                Bundle bundle = msg.getData();
                BluetoothDevice device = bundle.getParcelable("device");
                if( mActivity.get().getCurrentFragment() instanceof ScanFragment ) {
                    ((ScanFragment) mActivity.get().getCurrentFragment()).bondedCancel(device);
                }
            }
            else if(msg.what == BluetoothService.BONDED_FAIL_HANDLER_CODE){
                String name = (String)msg.obj;
                if( mActivity.get().getCurrentFragment() instanceof ScanFragment ) {
                    ((ScanFragment) mActivity.get().getCurrentFragment()).bondedFail(name);
                }
            }
            else if(msg.what == BluetoothService.CONNECT_SUCCESS_CLIENT_HANDLER_CODE){
                if( mActivity.get().getCurrentFragment() instanceof ScanFragment ) {
                    ((ScanFragment) mActivity.get().getCurrentFragment()).connectSuccessAsClient();
                }
            }
            else if(msg.what == BluetoothService.CONNECT_SUCCESS_MASTER_HANDLER_CODE){
                if( mActivity.get().getCurrentFragment() instanceof ScanFragment ) {
                    ((ScanFragment) mActivity.get().getCurrentFragment()).connectSuccessAsMaster();
                }
            }
            else if(msg.what == BluetoothService.CONNECT_SUCCESS_ACL_HANDLER_CODE){
                if( mActivity.get().getCurrentFragment() instanceof ScanFragment ) {
                    ((ScanFragment) mActivity.get().getCurrentFragment()).connectSuccessACL();
                }
            }
            else if(msg.what == BluetoothService.CONNECT_FAIL_HANDLER_CODE){
                Bundle bundle = msg.getData();
                String message = bundle.getString("message");
                if( mActivity.get().getCurrentFragment() instanceof ScanFragment ) {
                    ((ScanFragment) mActivity.get().getCurrentFragment()).connectFail(message);
                }
            }
            else if(msg.what == BluetoothService.DISCONNECTED_HANDLER_CODE){
                if( mActivity.get().getCurrentFragment() instanceof ScanFragment ) {
                    ((ScanFragment) mActivity.get().getCurrentFragment()).disconnected();
                }
                else if( mActivity.get().getCurrentFragment() instanceof ConnectFragment ) {
                    ((ConnectFragment) mActivity.get().getCurrentFragment()).CloseConnect();
                }
            }
            else if(msg.what == BluetoothService.DISCONNECTED_ACL_HANDLER_CODE){
                if( mActivity.get().getCurrentFragment() instanceof ScanFragment ) {
                    ((ScanFragment) mActivity.get().getCurrentFragment()).disconnectedACL();
                }
                else if( mActivity.get().getCurrentFragment() instanceof ConnectFragment ) {
                    ((ConnectFragment) mActivity.get().getCurrentFragment()).CloseConnect();
                }
            }
            else if(msg.what == BluetoothService.READ_MESSAGE_HANDLER_CODE) {
                Bundle data = msg.getData();
                byte[] msgArr = data.getByteArray("message");
                if( mActivity.get().getCurrentFragment() instanceof ConnectFragment ) {
                    ((ConnectFragment) mActivity.get().getCurrentFragment()).readedMessage(msgArr);
                }
            }
            else if(msg.what == BluetoothService.WRITE_MESSAGE_HANDLER_CODE ) {
                Bundle data = msg.getData();
                String message = data.getString("message");
                if( mActivity.get().getCurrentFragment() instanceof ConnectFragment ) {
                    ((ConnectFragment) mActivity.get().getCurrentFragment()).writedMessage(message);
                }
            }
            else if(msg.what == BluetoothService.WRITE_FILE_HANDLER_CODE) {
                Bundle data = msg.getData();
                String filename = data.getString("filename");
                int filesize = data.getInt("filesize");
                if( mActivity.get().getCurrentFragment() instanceof ConnectFragment ) {
                    ((ConnectFragment) mActivity.get().getCurrentFragment()).writedFile(filename, filesize);
                }
            }
        }
    }

    @Override
    public void onBackPressed() {
        if(getCurrentFragment() instanceof ConnectFragment) {
            ((ConnectFragment) getCurrentFragment()).onBackPressed();
            return;
        }
        super.onBackPressed();
    }

    // SAF 호출
    public void callSAF() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        mSAF.launch(intent);
    }
}

