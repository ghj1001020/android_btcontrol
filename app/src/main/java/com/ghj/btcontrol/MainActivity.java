package com.ghj.btcontrol;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;

import com.ghj.btcontrol.bluetooth.BluetoothService;
import com.ghj.btcontrol.fragment.ConnectFragment;
import com.ghj.btcontrol.fragment.ScanFragment;

public class MainActivity extends BaseFragmentActivity {

    Handler mBTHandler = new BTHandler();
    private BluetoothService mBTService;


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
    public void AppFinish(){
        if(mBTService!=null){
            mBTService.onDestroyBluetooth();
        }
        super.AppFinish();
    }

    /**
     * @desc 블루투스 콜백 핸들러
     */
    class BTHandler extends Handler {
        @Override
        public void handleMessage(@NonNull Message msg) {
            if(msg.what == BluetoothService.STATE_ON_HANDLER_CODE){
                if( getCurrentFragment() instanceof ScanFragment ) {
                    ((ScanFragment) getCurrentFragment()).stateOn();
                }
            }
            else if(msg.what == BluetoothService.STATE_OFF_HANDLER_CODE){
                if( getCurrentFragment() instanceof ScanFragment ) {
                    ((ScanFragment) getCurrentFragment()).stateOff();
                }
            }
            else if(msg.what == BluetoothService.DISCOVERY_START_HANDLER_CODE){
                if( getCurrentFragment() instanceof ScanFragment ) {
                    ((ScanFragment) getCurrentFragment()).discoveryStart();
                }
            }
            else if(msg.what == BluetoothService.DISCOVERY_FINISH_HANDLER_CODE){
                if( getCurrentFragment() instanceof ScanFragment ) {
                    ((ScanFragment) getCurrentFragment()).discoveryFinish();
                }
            }
            else if(msg.what == BluetoothService.DISCOVERY_FOUND_HANDLER_CODE){
                Bundle bundle = msg.getData();
                BluetoothDevice device = bundle.getParcelable("device");
                int rssi = bundle.getInt("rssi");
                if( getCurrentFragment() instanceof ScanFragment ) {
                    ((ScanFragment) getCurrentFragment()).discoveryFound(device, rssi);
                }
            }
            else if(msg.what == BluetoothService.BONDED_HANDLER_CODE){
                Bundle bundle = msg.getData();
                BluetoothDevice device = bundle.getParcelable("device");
                if( getCurrentFragment() instanceof ScanFragment ) {
                    ((ScanFragment) getCurrentFragment()).bonded(device);
                }
            }
            else if(msg.what == BluetoothService.BONDED_CANCEL_HANDLER_CODE){
                Bundle bundle = msg.getData();
                BluetoothDevice device = bundle.getParcelable("device");
                if( getCurrentFragment() instanceof ScanFragment ) {
                    ((ScanFragment) getCurrentFragment()).bondedCancel(device);
                }
            }
            else if(msg.what == BluetoothService.BONDED_FAIL_HANDLER_CODE){
                String name = (String)msg.obj;
                if( getCurrentFragment() instanceof ScanFragment ) {
                    ((ScanFragment) getCurrentFragment()).bondedFail(name);
                }
            }
            else if(msg.what == BluetoothService.CONNECT_SUCCESS_CLIENT_HANDLER_CODE){
                if( getCurrentFragment() instanceof ScanFragment ) {
                    ((ScanFragment) getCurrentFragment()).connectSuccessAsClient();
                }
            }
            else if(msg.what == BluetoothService.CONNECT_SUCCESS_MASTER_HANDLER_CODE){
                if( getCurrentFragment() instanceof ScanFragment ) {
                    ((ScanFragment) getCurrentFragment()).connectSuccessAsMaster();
                }
            }
            else if(msg.what == BluetoothService.CONNECT_SUCCESS_ACL_HANDLER_CODE){
                if( getCurrentFragment() instanceof ScanFragment ) {
                    ((ScanFragment) getCurrentFragment()).connectSuccessACL();
                }
            }
            else if(msg.what == BluetoothService.CONNECT_FAIL_HANDLER_CODE){
                Bundle bundle = msg.getData();
                String message = bundle.getString("message");
                if( getCurrentFragment() instanceof ScanFragment ) {
                    ((ScanFragment) getCurrentFragment()).connectFail(message);
                }
            }
            else if(msg.what == BluetoothService.DISCONNECTED_HANDLER_CODE){
                if( getCurrentFragment() instanceof ScanFragment ) {
                    ((ScanFragment) getCurrentFragment()).disconnected();
                }
            }
            else if(msg.what == BluetoothService.DISCONNECTED_ACL_HANDLER_CODE){
                if( getCurrentFragment() instanceof ScanFragment ) {
                    ((ScanFragment) getCurrentFragment()).disconnectedACL();
                }
            }
        }
    }
}

