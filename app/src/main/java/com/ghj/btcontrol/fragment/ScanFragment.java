package com.ghj.btcontrol.fragment;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.ghj.btcontrol.BaseActivity;
import com.ghj.btcontrol.MainActivity;
import com.ghj.btcontrol.R;
import com.ghj.btcontrol.adapter.AdapterDevices;
import com.ghj.btcontrol.adapter.AdapterPaired;
import com.ghj.btcontrol.adapter.IPairedListener;
import com.ghj.btcontrol.bluetooth.BluetoothService;

public class ScanFragment extends Fragment implements View.OnClickListener {

    BluetoothService mBTService;

    Button btnScan, btnClose;
    TextView txtStatus, txtNoDevice;
    Switch swiEnable;
    ListView listPaired, listDevices;
    ProgressBar pbScan;
    LinearLayout boxPaired, boxDevices;
    ProgressDialog pdPaired, pdConnect;

    AdapterPaired mAdapterPaired;
    AdapterDevices mAdapterDevices;


    public ScanFragment(BluetoothService service) {
        this.mBTService = service;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_scan, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //Bluetooth 찾기
        btnScan = (Button)view.findViewById(R.id.btnScan);
        btnScan.setTag(false);
        btnScan.setOnClickListener(this);

        //앱종료
        btnClose = (Button)view.findViewById(R.id.btnClose);
        btnClose.setOnClickListener(this);

        txtStatus = (TextView)view.findViewById(R.id.txtStatus);
        swiEnable = (Switch)view.findViewById(R.id.swiEnable);
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
        listPaired = (ListView)view.findViewById(R.id.listPaired);
        mAdapterPaired = new AdapterPaired(getActivity(), new IPairedListener() {
            @Override
            public void onCancelDevice(BluetoothDevice device) {
                mBTService.removeBondedDevice(device);
            }
            @Override
            public void onConnectDevice(BluetoothDevice device) {
                pdConnect.show();
                mBTService.requestConnect(device);
            }
        });
        listPaired.setAdapter(mAdapterPaired);
        listDevices = (ListView)view.findViewById(R.id.listDevices);
        mAdapterDevices = new AdapterDevices(getActivity());
        listDevices.setAdapter(mAdapterDevices);
        txtNoDevice = (TextView)view.findViewById(R.id.txtNoDevice);
        pbScan = (ProgressBar)view.findViewById(R.id.pbScan);
        boxPaired = (LinearLayout)view.findViewById(R.id.boxPaired);
        boxDevices = (LinearLayout)view.findViewById(R.id.boxDevices);
        pdPaired = new ProgressDialog(getContext(), ProgressDialog.STYLE_SPINNER);
        pdPaired.setMessage("등록중 입니다...");
        pdPaired.setCancelable(false);
        pdConnect = new ProgressDialog(getContext(), ProgressDialog.STYLE_SPINNER);
        pdConnect.setMessage("연결중 입니다...");
        pdConnect.setCancelable(false);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            // 스캔
            case R.id.btnScan:
                if(!(boolean)btnScan.getTag()){
                    if(mBTService.isEnabled()){
                        mBTService.startScanDevice();
                    }else{
                        Toast.makeText(getContext(), getString(R.string.requestBTEnable), Toast.LENGTH_SHORT).show();
                    }
                }else{
                    mBTService.cancelScanDevice();
                }
                break;

            // 닫기
            case R.id.btnClose:
                ((MainActivity) getActivity()).AppFinish();
                break;
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
     * @desc enable on
     */
    public void stateOn() {
        boxPaired.setVisibility(View.VISIBLE);
        boxDevices.setVisibility(View.VISIBLE);
        txtStatus.setText("사용 중");
        swiEnable.setChecked(true);
        mBTService.startScanDevice();
    }

    /**
     * @desc enable off
     */
    public void stateOff() {
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
}
