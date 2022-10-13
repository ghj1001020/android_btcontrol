package com.ghj.btcontrol;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.ghj.btcontrol.adapter.AdapterDevices;
import com.ghj.btcontrol.adapter.AdapterPaired;
import com.ghj.btcontrol.bluetooth.BluetoothService;
import com.ghj.btcontrol.data.BluetoothData;
import com.ghj.btcontrol.fragment.ConnectFragment;
import com.ghj.btcontrol.fragment.ScanFragment;

import java.util.List;

public class MainActivity extends BaseFragmentActivity {

    final int CONNECT_ACTIVITY_REQUEST_CODE = 100;

    Handler mBTHandler = new BTHandler();
    BluetoothService mBTService;

    // 화면
    ScanFragment mScanFragment;
    ConnectFragment mConnectFragment;


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
        changeFragment(0);

        if(mBTService.isEnabled()){
            mBTHandler.sendEmptyMessage(BluetoothService.STATE_ON_HANDLER_CODE);
        }else{
            mBTHandler.sendEmptyMessage(BluetoothService.STATE_OFF_HANDLER_CODE);
        }
    }

    /**
     * @desc 프래그먼트 선택
     */
    public void changeFragment(int index) {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fm.beginTransaction();

        switch(index) {
            case 0:
                if(mConnectFragment != null) {
                    fragmentTransaction.remove(mConnectFragment);
                }
                mScanFragment = new ScanFragment(mBTService);
                fragmentTransaction.add(R.id.fragment, mScanFragment);
                break;
            case 1:
                mConnectFragment = new ConnectFragment();
                fragmentTransaction.add(R.id.fragment, mConnectFragment);
                break;
        }
        fragmentTransaction.commit();
    }

    /**
     * @desc 어답터 버튼 클릭 리스너
     */
    public void mOnPairedClickListener(int btnId, BluetoothDevice device){
        switch (btnId){
            case R.id.btnConnect:
                requestConnect(device);
                break;
            case R.id.btnDelete:
                removePaired(device);
                break;
        }
    }

    /**
     * @desc 페어링 요청
     */
    public void requestPairing(BluetoothDevice device){
        pdPaired.show();
        mBTService.requestBond(device);
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
                mScanFragment.stateOn();
            }
            else if(msg.what == BluetoothService.STATE_OFF_HANDLER_CODE){
                mScanFragment.stateOff();
            }
            else if(msg.what == BluetoothService.DISCOVERY_START_HANDLER_CODE){
                btnScan.setTag(true);
                btnScan.setText("STOP");
                pbScan.setVisibility(View.VISIBLE);
                txtNoDevice.setVisibility(View.GONE);
                listDevices.setVisibility(View.VISIBLE);
                mAdapterDevices.removeAllItem();
                mAdapterDevices.notifyDataSetChanged();
                CalculateListViewHeight(listDevices);
                List<BluetoothDevice> paired = mBTService.getBondedDevice();
                mAdapterPaired.removeAllItem();
                if(paired.size()>0){
                    boxPaired.setVisibility(View.VISIBLE);
                    mAdapterPaired.addItems(paired);
                }else{
                    boxPaired.setVisibility(View.GONE);
                }
                mAdapterPaired.notifyDataSetChanged();
                CalculateListViewHeight(listPaired);
            }
            else if(msg.what == BluetoothService.DISCOVERY_FINISH_HANDLER_CODE){
                btnScan.setTag(false);
                btnScan.setText("SCAN");
                pbScan.setVisibility(View.GONE);
                if(mAdapterDevices.getCount()>0){
                    txtNoDevice.setVisibility(View.GONE);
                    listDevices.setVisibility(View.VISIBLE);
                }else{
                    listDevices.setVisibility(View.GONE);
                    txtNoDevice.setVisibility(View.VISIBLE);
                }
            }
            else if(msg.what == BluetoothService.DISCOVERY_FOUND_HANDLER_CODE){
                Bundle bundle = msg.getData();
                BluetoothDevice device = bundle.getParcelable("device");
                int rssi = bundle.getInt("rssi");
                BluetoothData btData = new BluetoothData(device, rssi);
                mAdapterDevices.addItem(btData);
                mAdapterDevices.notifyDataSetChanged();
                CalculateListViewHeight(listDevices);
            }
            else if(msg.what == BluetoothService.BONDED_HANDLER_CODE){
                pdPaired.dismiss();
                pdConnect.dismiss();
                Bundle bundle = msg.getData();
                BluetoothDevice device = bundle.getParcelable("device");
                mAdapterPaired.addItem(device);
                mAdapterPaired.notifyDataSetChanged();
                CalculateListViewHeight(listPaired);
                mAdapterDevices.removeItem(device);
                mAdapterDevices.notifyDataSetChanged();
                CalculateListViewHeight(listDevices);
                if(boxPaired.getVisibility()==View.GONE){
                    boxPaired.setVisibility(View.VISIBLE);
                }
                if(mAdapterDevices.getCount()==0){
                    listDevices.setVisibility(View.GONE);
                    txtNoDevice.setVisibility(View.VISIBLE);
                }
            }
            else if(msg.what == BluetoothService.BONDED_CANCEL_HANDLER_CODE){
                pdPaired.dismiss();
                pdConnect.dismiss();
                Bundle bundle = msg.getData();
                BluetoothDevice device = bundle.getParcelable("device");
                BluetoothData data = new BluetoothData(device, Integer.MIN_VALUE);
                mAdapterDevices.addItem(data);
                mAdapterDevices.notifyDataSetChanged();
                CalculateListViewHeight(listDevices);
                mAdapterPaired.removeItem(device);
                mAdapterPaired.notifyDataSetChanged();
                CalculateListViewHeight(listPaired);
                if(listDevices.getVisibility()==View.GONE){
                    txtNoDevice.setVisibility(View.GONE);
                    listDevices.setVisibility(View.VISIBLE);
                }
                if(mAdapterPaired.getCount()==0){
                    boxPaired.setVisibility(View.GONE);
                }
            }
            else if(msg.what == BluetoothService.BONDED_FAIL_HANDLER_CODE){
                pdPaired.dismiss();
                pdConnect.dismiss();
                String name = (String)msg.obj;
                Toast.makeText(MainActivity.this, name+"에 연결할 수 없습니다.", Toast.LENGTH_SHORT).show();
            }
            else if(msg.what == BluetoothService.CONNECT_SUCCESS_CLIENT_HANDLER_CODE){
                pdConnect.dismiss();
//                Intent intent = new Intent(MainActivity.this, ConnectActivity.class);
//                startActivityForResult(intent, CONNECT_ACTIVITY_REQUEST_CODE);
                Intent intent = new Intent(mActivity, ConnectActivity.class);
                mConnectActivityResult.launch(intent);
            }
            else if(msg.what == BluetoothService.CONNECT_SUCCESS_MASTER_HANDLER_CODE){
                pdConnect.dismiss();
            }
            else if(msg.what == BluetoothService.CONNECT_SUCCESS_ACL_HANDLER_CODE){
                pdConnect.dismiss();
//                Intent intent = new Intent(MainActivity.this, ConnectActivity.class);
//                startActivityForResult(intent, CONNECT_ACTIVITY_REQUEST_CODE);
                Intent intent = new Intent(mActivity, ConnectActivity.class);
                mConnectActivityResult.launch(intent);
            }
            else if(msg.what == BluetoothService.CONNECT_FAIL_HANDLER_CODE){
                pdConnect.dismiss();
                Bundle bundle = msg.getData();
                String message = bundle.getString("message");
                Toast.makeText(MainActivity.this, "블루투스 연결에 실패하였습니다.\n["+message+"]", Toast.LENGTH_SHORT).show();
            }
            else if(msg.what == BluetoothService.DISCONNECTED_HANDLER_CODE){
                pdConnect.dismiss();
            }
            else if(msg.what == BluetoothService.DISCONNECTED_ACL_HANDLER_CODE){
                pdConnect.dismiss();
            }
        }
    }

    @Override
    public void onBackPressed() {
        AppFinish();
    }
}

