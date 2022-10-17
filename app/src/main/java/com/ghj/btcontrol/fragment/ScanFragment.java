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
import androidx.navigation.fragment.NavHostFragment;

import com.ghj.btcontrol.BaseFragmentActivity;
import com.ghj.btcontrol.MainActivity;
import com.ghj.btcontrol.R;
import com.ghj.btcontrol.adapter.AdapterDevices;
import com.ghj.btcontrol.adapter.AdapterPaired;
import com.ghj.btcontrol.adapter.IPairedListener;
import com.ghj.btcontrol.data.BluetoothData;

import java.util.List;

public class ScanFragment extends Fragment implements View.OnClickListener {

    Button btnScan, btnClose;
    TextView txtStatus, txtNoDevice;
    Switch swiEnable;
    ListView listPaired, listDevices;
    ProgressBar pbScan;
    LinearLayout boxPaired, boxDevices;
    ProgressDialog pdPaired, pdConnect;

    AdapterPaired mAdapterPaired;
    AdapterDevices mAdapterDevices;

    public ScanFragment() {}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if( getActivity() instanceof BaseFragmentActivity ) {
            ((BaseFragmentActivity) getActivity()).addToFragmentStack(this);
        }
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
                    ((MainActivity) getActivity()).getBTService().enableBluetooth();
                }else{
                    ((MainActivity) getActivity()).getBTService().disableBluetooth();
                }
            }
        });
        listPaired = (ListView)view.findViewById(R.id.listPaired);
        mAdapterPaired = new AdapterPaired(getActivity(), new IPairedListener() {
            @Override
            public void onCancelDevice(BluetoothDevice device) {
                ((MainActivity) getActivity()).getBTService().removeBondedDevice(device);
            }
            @Override
            public void onConnectDevice(BluetoothDevice device) {
                pdConnect.show();
                ((MainActivity) getActivity()).getBTService().requestConnect(device);
            }
        });
        listPaired.setAdapter(mAdapterPaired);
        listDevices = (ListView)view.findViewById(R.id.listDevices);
        mAdapterDevices = new AdapterDevices(getActivity(), device -> {
            pdPaired.show();
            ((MainActivity) getActivity()).getBTService().requestBond(device);
        });
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
        init();
    }

    /**
     * @desc 최초 환경 세팅
     */
    public void init(){
        if(((MainActivity) getActivity()).getBTService().isEnabled()){
            stateOn();
            // 서버리스닝 시작
            ((MainActivity) getActivity()).getBTService().runListening();
        }else{
            stateOff();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            // 스캔
            case R.id.btnScan:
                if(!(boolean)btnScan.getTag()){
                    if(((MainActivity) getActivity()).getBTService().isEnabled()){
                        ((MainActivity) getActivity()).discoverableDevice();
                        ((MainActivity) getActivity()).getBTService().startScanDevice();
                    }else{
                        Toast.makeText(getContext(), getString(R.string.requestBTEnable), Toast.LENGTH_SHORT).show();
                    }
                }else{
                    ((MainActivity) getActivity()).getBTService().cancelScanDevice();
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
     * @desc 연결화면으로 이동
     */
    private void moveToConnect() {
        ((MainActivity) getActivity()).getBTService().cancelScanDevice();
        NavHostFragment.findNavController(this).navigate(R.id.action_scanFragment_to_connectFragment);
    }

    /**
     * @desc enable on
     */
    public void stateOn() {
        boxPaired.setVisibility(View.VISIBLE);
        boxDevices.setVisibility(View.VISIBLE);
        txtStatus.setText("사용 중");
        swiEnable.setChecked(true);
        ((MainActivity) getActivity()).discoverableDevice();
        ((MainActivity) getActivity()).getBTService().startScanDevice();
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

    /**
     * @desc scan start
     */
    public void discoveryStart() {
        btnScan.setTag(true);
        btnScan.setText("STOP");
        pbScan.setVisibility(View.VISIBLE);
        txtNoDevice.setVisibility(View.GONE);
        listDevices.setVisibility(View.VISIBLE);
        mAdapterDevices.removeAllItem();
        mAdapterDevices.notifyDataSetChanged();
        CalculateListViewHeight(listDevices);
        List<BluetoothDevice> paired = ((MainActivity) getActivity()).getBTService().getBondedDevice();
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

    /**
     * @desc scan finish
     */
    public void discoveryFinish() {
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

    /**
     * @desc scan device found
     */
    public void discoveryFound(BluetoothDevice device, int rssi) {
        BluetoothData btData = new BluetoothData(device, rssi);
        mAdapterDevices.addItem(btData);
        mAdapterDevices.notifyDataSetChanged();
        CalculateListViewHeight(listDevices);
    }

    /**
     * @desc bonded device
     */
    public void bonded(BluetoothDevice device) {
        pdPaired.dismiss();
        pdConnect.dismiss();
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

    /**
     * @desc bonded cancel
     */
    public void bondedCancel(BluetoothDevice device) {
        pdPaired.dismiss();
        pdConnect.dismiss();

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

    /**
     * @desc bonded fail
     */
    public void bondedFail(String name) {
        pdPaired.dismiss();
        pdConnect.dismiss();
        Toast.makeText(getContext(), name+"에 연결할 수 없습니다.", Toast.LENGTH_SHORT).show();
    }

    /**
     * @desc connect success by client
     */
    public void connectSuccessAsClient() {
        pdConnect.dismiss();
//        Intent intent = new Intent(mActivity, ConnectActivity.class);
//        mConnectActivityResult.launch(intent);
    }

    /**
     * @desc connect success by master
     */
    public void connectSuccessAsMaster() {
        pdConnect.dismiss();
    }

    /**
     * @desc connect success ACL
     */
    public void connectSuccessACL() {
        pdConnect.dismiss();
        moveToConnect();
    }

    /**
     * @desc connect fail
     */
    public void connectFail(String message) {
        pdConnect.dismiss();
        Toast.makeText(getContext(), "블루투스 연결에 실패하였습니다.\n["+message+"]", Toast.LENGTH_SHORT).show();
    }

    /**
     * @desc disconnected
     */
    public void disconnected() {
        pdConnect.dismiss();
    }

    /**
     * @desc disconnected ACL
     */
    public void disconnectedACL() {
        pdConnect.dismiss();
    }
}
