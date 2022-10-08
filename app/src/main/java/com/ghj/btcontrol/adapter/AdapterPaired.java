package com.ghj.btcontrol.adapter;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.ghj.btcontrol.MainActivity;
import com.ghj.btcontrol.R;

import java.util.ArrayList;
import java.util.List;

import at.markushi.ui.CircleButton;

/**
 * Created by ghj on 2017. 3. 24..
 */
public class AdapterPaired extends BaseAdapter {

    List<BluetoothDevice> data;
    Activity mActivity;
    LayoutInflater mInflater;


    public AdapterPaired(Activity _this){
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
            convertView = mInflater.inflate(R.layout.adapter_paired, parent, false);
        }

        TextView txtPName = (TextView)convertView.findViewById(R.id.txtPName);
        TextView txtPMac = (TextView)convertView.findViewById(R.id.txtPMAC);
        TextView txtPType = (TextView)convertView.findViewById(R.id.txtPType);

        BluetoothDevice element = data.get(position);
        String name = element.getName();
        if(name==null){
            name = "null";
        }
        txtPName.setText(name);
        txtPMac.setText(element.getAddress());
        switch(element.getType()){
            case BluetoothDevice.DEVICE_TYPE_CLASSIC:
                txtPType.setText("BR/EDR");
                break;
            case BluetoothDevice.DEVICE_TYPE_DUAL:
                txtPType.setText("BR/EDR/LE");
                break;
            case BluetoothDevice.DEVICE_TYPE_LE:
                txtPType.setText("LE-only");
                break;
            case BluetoothDevice.DEVICE_TYPE_UNKNOWN:
                txtPType.setText("Unknown");
                break;
        }

        convertView.setTag(element);


        CircleButton delete = (CircleButton)convertView.findViewById(R.id.btnDelete);
        delete.setTag(element);
        delete.setOnClickListener(mOnClickListener);

        Button connect = (Button)convertView.findViewById(R.id.btnConnect);
        connect.setOnClickListener(mOnClickListener);

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


    public void addItem(BluetoothDevice element){
        data.add(element);
    }

    public void addItems(List datas){
        data.addAll(datas);
    }

    public void removeItem(BluetoothDevice device){
        String address = device.getAddress();
        for(int i=0; i<data.size(); i++){
            BluetoothDevice element = data.get(i);
            if(address.equals(element.getAddress())){
                data.remove(i);
                break;
            }
        }
    }

    public void removeAllItem(){
        data.clear();
    }


    /**
     * @desc 연결 버튼 클릭
     */
    View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            ViewParent parent = (v.getParent()).getParent();
            BluetoothDevice device = (BluetoothDevice)((View)parent).getTag();
            ((MainActivity)mActivity).mOnPairedClickListener(v.getId(), device);
        }
    };
}
