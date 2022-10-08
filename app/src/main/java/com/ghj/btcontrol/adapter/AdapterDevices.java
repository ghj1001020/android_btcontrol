package com.ghj.btcontrol.adapter;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.ghj.btcontrol.MainActivity;
import com.ghj.btcontrol.R;
import com.ghj.btcontrol.data.BluetoothData;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ghj on 2017. 3. 24..
 */
public class AdapterDevices extends BaseAdapter {

    List<BluetoothData> data;
    Activity mActivity;
    LayoutInflater mInflater;


    public AdapterDevices(Activity _this){
        this.mActivity = _this;
        data = new ArrayList<>();
        mInflater = (LayoutInflater)_this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }


    @Override
    public int getCount() {
        if(data!=null){ return data.size(); }
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView==null){
            convertView = mInflater.inflate(R.layout.adapter_devices, parent, false);
        }

        TextView txtName = (TextView)convertView.findViewById(R.id.txtName);
        TextView txtMAC = (TextView)convertView.findViewById(R.id.txtMAC);
        TextView txtRSSI = (TextView)convertView.findViewById(R.id.txtRSSI);
        TextView txtType = (TextView)convertView.findViewById(R.id.txtType);

        BluetoothData element = data.get(position);
        BluetoothDevice device = element.getBluetoothDevice();
        String name = device.getName();
        if(device.getName()==null){
            name = "null";
        }
        txtName.setText(name);
        txtMAC.setText(device.getAddress());
        txtRSSI.setText(element.getRssi()==Integer.MIN_VALUE? "-" : ""+element.getRssi());
        switch(device.getType()){
            case BluetoothDevice.DEVICE_TYPE_CLASSIC:
                txtType.setText("BR/EDR");
                break;
            case BluetoothDevice.DEVICE_TYPE_DUAL:
                txtType.setText("BR/EDR/LE");
                break;
            case BluetoothDevice.DEVICE_TYPE_LE:
                txtType.setText("LE-only");
                break;
            case BluetoothDevice.DEVICE_TYPE_UNKNOWN:
                txtType.setText("Unknown");
                break;
        }


        Button paired = (Button)convertView.findViewById(R.id.btnPaired);
        paired.setTag(device);
        paired.setOnClickListener(mOnClickListener);

        return convertView;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public Object getItem(int position) {
        if(data!=null && position<data.size()){ return data.get(position); }
        return null;
    }


    public void addItem(BluetoothData element){
        data.add(element);
    }

    public void addItems(List datas){
        data.addAll(datas);
    }

    public void removeItem(BluetoothDevice device){
        String address = device.getAddress();
        for(int i=0; i<data.size(); i++){
            BluetoothData element = data.get(i);
            BluetoothDevice one = element.getBluetoothDevice();
            if(address.equals(one.getAddress())){
                data.remove(i);
                break;
            }
        }
    }

    public void removeAllItem(){
        data.clear();
    }


    /**
     * @desc 페어링 버튼 클릭
     */
    View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            BluetoothDevice device = (BluetoothDevice)v.getTag();
            ((MainActivity)mActivity).requestPairing(device);
        }
    };
}
