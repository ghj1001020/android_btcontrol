package com.ghj.btcontrol.adapter;

import android.bluetooth.BluetoothDevice;

public interface IPairedListener {
    void onCancelDevice(BluetoothDevice device);
    void onConnectDevice(BluetoothDevice device);
}