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
import androidx.navigation.Navigation;

import com.ghj.btcontrol.bluetooth.BluetoothService;
import com.ghj.btcontrol.fragment.ConnectFragment;
import com.ghj.btcontrol.fragment.ScanFragment;

public class MainActivity extends BaseFragmentActivity {

    enum MODE {
        SCAN,
        CONNECT
    }

    Handler mBTHandler = new BTHandler();
    private BluetoothService mBTService;

    // 화면
    ScanFragment mScanFragment;

    // 프래그먼트 인덱스
    MODE currentIndex = MODE.SCAN;


    ActivityResultLauncher<Intent> mConnectActivityResult = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if(result.getResultCode() == Activity.RESULT_OK) {
                        mBTService = BluetoothService.getBluetoothService(mActivity, mBTHandler);
                        mBTService.runListening();
                        init();
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
    }

    @Override
    public void onCreateAfter() {
        //블루투스
        mBTService = BluetoothService.getBluetoothService(this, mBTHandler);

        //초기화
        init();
    }

    /**
     * @desc 최초 환경 세팅
     */
    public void init(){
        if(mBTService.isEnabled()){
            mBTHandler.sendEmptyMessage(BluetoothService.STATE_ON_HANDLER_CODE);
        }else{
            mBTHandler.sendEmptyMessage(BluetoothService.STATE_OFF_HANDLER_CODE);
        }
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
                mScanFragment.stateOff();
            }
            else if(msg.what == BluetoothService.DISCOVERY_START_HANDLER_CODE){
                mScanFragment.discoveryStart();
            }
            else if(msg.what == BluetoothService.DISCOVERY_FINISH_HANDLER_CODE){
                mScanFragment.discoveryFinish();
            }
            else if(msg.what == BluetoothService.DISCOVERY_FOUND_HANDLER_CODE){
                Bundle bundle = msg.getData();
                BluetoothDevice device = bundle.getParcelable("device");
                int rssi = bundle.getInt("rssi");
                mScanFragment.discoveryFound(device, rssi);
            }
            else if(msg.what == BluetoothService.BONDED_HANDLER_CODE){
                Bundle bundle = msg.getData();
                BluetoothDevice device = bundle.getParcelable("device");
                mScanFragment.bonded(device);
            }
            else if(msg.what == BluetoothService.BONDED_CANCEL_HANDLER_CODE){
                Bundle bundle = msg.getData();
                BluetoothDevice device = bundle.getParcelable("device");
                mScanFragment.bondedCancel(device);
            }
            else if(msg.what == BluetoothService.BONDED_FAIL_HANDLER_CODE){
                String name = (String)msg.obj;
                mScanFragment.bondedFail(name);
            }
            else if(msg.what == BluetoothService.CONNECT_SUCCESS_CLIENT_HANDLER_CODE){
                mScanFragment.connectSuccessAsClient();
            }
            else if(msg.what == BluetoothService.CONNECT_SUCCESS_MASTER_HANDLER_CODE){
                mScanFragment.connectSuccessAsMaster();
            }
            else if(msg.what == BluetoothService.CONNECT_SUCCESS_ACL_HANDLER_CODE){
                mScanFragment.connectSuccessACL();
            }
            else if(msg.what == BluetoothService.CONNECT_FAIL_HANDLER_CODE){
                Bundle bundle = msg.getData();
                String message = bundle.getString("message");
                mScanFragment.connectFail(message);
            }
            else if(msg.what == BluetoothService.DISCONNECTED_HANDLER_CODE){
                mScanFragment.disconnected();
            }
            else if(msg.what == BluetoothService.DISCONNECTED_ACL_HANDLER_CODE){
                mScanFragment.disconnectedACL();
            }
        }
    }

    @Override
    public void onBackPressed() {
        if(currentIndex == MODE.CONNECT) {

            return;
        }
        AppFinish();
    }
}

