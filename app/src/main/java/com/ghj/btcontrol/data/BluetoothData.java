package com.ghj.btcontrol.data;

import android.bluetooth.BluetoothDevice;

/**
 * Created by ghj on 2017. 3. 27..
 */
public class BluetoothData {

    BluetoothDevice device;
    int rssi;


    public BluetoothData(BluetoothDevice device, int rssi){
        this.device = device;
        this.rssi = rssi;
    }

    public BluetoothDevice getBluetoothDevice(){
        return device;
    }

    public int getRssi(){
        return rssi;
    }
}
