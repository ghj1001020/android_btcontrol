package com.ghj.btcontrol.data;

/**
 * Created by ghj on 2017. 4. 5..
 */
public class BTCConstants {
    public static final String SERIAL_PORT_SERVICE_UUID = "00001101-0000-1000-8000-00805F9B34FB";

    public static final String APPNAME = "블루투스컨트롤";
    public static final String BLUETOOTH_CONNECT_BROADCAST_ACTION = "com.ghj.btcontrol.BTConnected";

    // 데이터 통신타입
    public static final int MY_TEXT = 0;
    public static final int MY_FILE = 1;
    public static final int YOUR_TEXT = 2;
    public static final int YOUR_FILE = 3;

    // 데이터 통신 Sequence
    public static int DATA_SEQ = 0;
}
