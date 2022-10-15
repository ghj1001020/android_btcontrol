package com.ghj.btcontrol.adapter;

import android.bluetooth.BluetoothDevice;

public interface IDevicesListener {
    void onPairingDevice(BluetoothDevice device);
}
