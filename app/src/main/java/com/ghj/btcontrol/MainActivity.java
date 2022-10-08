package com.ghj.btcontrol;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
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

import java.util.List;

public class MainActivity extends AppCompatActivity {

    final int CONNECT_ACTIVITY_REQUEST_CODE = 100;

    Button btnScan, btnClose;
    TextView txtStatus, txtNoDevice;
    Switch swiEnable;
    ListView listPaired, listDevices;
    ProgressBar pbScan;
    LinearLayout boxPaired, boxDevices;
    ProgressDialog pdPaired, pdConnect;

    AdapterPaired mAdapterPaired;
    AdapterDevices mAdapterDevices;

    BluetoothService mBTService;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //액션바
        SetCustomActionBar();

        //UI
        txtStatus = (TextView)findViewById(R.id.txtStatus);
        swiEnable = (Switch)findViewById(R.id.swiEnable);
        swiEnable.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(swiEnable.isChecked()){
                    mBTService.enableBluetooth();
                }else{
                    mBTService.disableBluetooth();
                }
            }
        });
        listPaired = (ListView)findViewById(R.id.listPaired);
        mAdapterPaired = new AdapterPaired(this);
        listPaired.setAdapter(mAdapterPaired);
        listDevices = (ListView)findViewById(R.id.listDevices);
        mAdapterDevices = new AdapterDevices(this);
        listDevices.setAdapter(mAdapterDevices);
        txtNoDevice = (TextView)findViewById(R.id.txtNoDevice);
        pbScan = (ProgressBar)findViewById(R.id.pbScan);
        boxPaired = (LinearLayout)findViewById(R.id.boxPaired);
        boxDevices = (LinearLayout)findViewById(R.id.boxDevices);
        pdPaired = new ProgressDialog(this, ProgressDialog.STYLE_SPINNER);
        pdPaired.setMessage("등록중 입니다...");
        pdPaired.setCancelable(false);
        pdConnect = new ProgressDialog(this, ProgressDialog.STYLE_SPINNER);
        pdConnect.setMessage("연결중 입니다...");
        pdConnect.setCancelable(false);

        //블루투스
        mBTService = BluetoothService.getBluetoothService(this, mBTHandler);

        //초기화
        init();
    }


    /**
     * @desc 커스텀 액션바 만들기
     */
    public void SetCustomActionBar(){
        ActionBar actionBar = getSupportActionBar();

        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(false);
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayUseLogoEnabled(false);

        //액션바 레이아웃
        View view = LayoutInflater.from(this).inflate(R.layout.activity_appbar, null);
        actionBar.setCustomView(view, new ActionBar.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        //액션바 그림자없애기 (롤리팝부터 추가)
        actionBar.setElevation(0);
        //액션바 양쪽 공백 없애기
        Toolbar toolbar = (Toolbar)view.getParent();
        toolbar.setContentInsetsAbsolute(0,0);


        //Bluetooth 찾기
        btnScan = (Button)view.findViewById(R.id.btnScan);
        btnScan.setTag(false);
        btnScan.setOnClickListener(mOnClickListener);

        //앱종료
        btnClose = (Button)view.findViewById(R.id.btnClose);
        btnClose.setOnClickListener(mOnClickListener);
    }

    /**
     * @desc 최초 환경 세팅
     */
    public void init(){
        if(mBTService.isEnabled()){
            mBTHandler.sendEmptyMessage(BluetoothService.STATE_ON_HANDLER_CODE);
            if(!swiEnable.isChecked()){
                swiEnable.setChecked(true);
            }
        }else{
            mBTHandler.sendEmptyMessage(BluetoothService.STATE_OFF_HANDLER_CODE);
            if(swiEnable.isChecked()){
                swiEnable.setChecked(false);
            }
        }
    }


    /**
     * @desc 리스트뷰 높이 계산
     */
    public void CalculateListViewHeight(final ListView listView){
        ListAdapter adapter = listView.getAdapter();
        if(adapter==null){ return; }

        int itemCount = adapter.getCount();
        int totalHeight = 0;
        for(int i=0; i<itemCount; i++){
            View item = adapter.getView(i, null, listView);
            item.measure(0, 0);
            totalHeight += item.getMeasuredHeight();
        }

        int totalDividerHeight = listView.getDividerHeight() * (itemCount-1);

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + totalDividerHeight;
        listView.setLayoutParams(params);
        listView.requestLayout();
    }


    /**
     * @desc 버튼 클릭 리스너
     */
    View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.btnScan:
                    if(!(boolean)btnScan.getTag()){
                        if(mBTService.isEnabled()){
                            mBTService.startScanDevice();
                        }else{
                            Toast.makeText(MainActivity.this, getString(R.string.requestBTEnable), Toast.LENGTH_SHORT).show();
                        }
                    }else{
                        mBTService.cancelScanDevice();
                    }

                    break;
                case R.id.btnClose:
                    AppFinish();
                    break;
            }
        }
    };

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
     * @desc 등록해제
     */
    public void removePaired(BluetoothDevice device){
        mBTService.removeBondedDevice(device);
    }

    /**
     * @desc 연결요청
     */
    public void requestConnect(BluetoothDevice device){
        pdConnect.show();
        mBTService.requestConnect(device);
    }


    /**
     * @desc 앱종료
     */
    public void AppFinish(){
        if(mBTService!=null){
            mBTService.onDestroyBluetooth();
        }

        finish();
        System.exit(0);
        android.os.Process.killProcess(android.os.Process.myPid());
    }


    /**
     * @desc 블루투스 콜백 핸들러
     */
    Handler mBTHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if(msg.what == BluetoothService.STATE_ON_HANDLER_CODE){
                boxPaired.setVisibility(View.VISIBLE);
                boxDevices.setVisibility(View.VISIBLE);
                txtStatus.setText("사용 중");
                swiEnable.setChecked(true);
                mBTService.startScanDevice();
            }
            else if(msg.what == BluetoothService.STATE_OFF_HANDLER_CODE){
                boxPaired.setVisibility(View.GONE);
                boxDevices.setVisibility(View.GONE);
                txtStatus.setText("사용 안함");
                swiEnable.setChecked(false);
                mAdapterDevices.removeAllItem();
                mAdapterDevices.notifyDataSetChanged();
                CalculateListViewHeight(listDevices);
                mAdapterPaired.removeAllItem();
                mAdapterPaired.notifyDataSetChanged();
                CalculateListViewHeight(listPaired);
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
                Intent intent = new Intent(MainActivity.this, ConnectActivity.class);
                startActivityForResult(intent, CONNECT_ACTIVITY_REQUEST_CODE);
            }
            else if(msg.what == BluetoothService.CONNECT_SUCCESS_MASTER_HANDLER_CODE){
                pdConnect.dismiss();
            }
            else if(msg.what == BluetoothService.CONNECT_SUCCESS_ACL_HANDLER_CODE){
                pdConnect.dismiss();
                Intent intent = new Intent(MainActivity.this, ConnectActivity.class);
                startActivityForResult(intent, CONNECT_ACTIVITY_REQUEST_CODE);
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
    };


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == CONNECT_ACTIVITY_REQUEST_CODE){
            mBTService = BluetoothService.getBluetoothService(this, mBTHandler);
            mBTService.runListening();
            init();
        }
    }

    @Override
    public void onBackPressed() {
        AppFinish();
    }
}

