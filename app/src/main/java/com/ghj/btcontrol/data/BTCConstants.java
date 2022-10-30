package com.ghj.btcontrol.data;

import android.net.Uri;
import android.os.Environment;

import java.io.File;

/**
 * Created by ghj on 2017. 4. 5..
 */
public class BTCConstants {
    public static final String SERIAL_PORT_SERVICE_UUID = "00001101-0000-1000-8000-00805F9B34FB";

    public static final String APPNAME = "블루투스컨트롤";
    public static final String BLUETOOTH_CONNECT_BROADCAST_ACTION = "com.ghj.btcontrol.BTConnected";

    // 다운로드 폴더
    public static File getDownloadDir() {
        return new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + File.separator + BTCConstants.APPNAME);
    }

    // 다운로드 폴더명
    public static final String DOWNLOAD_DIR_NAME = "내부저장소 > " + Environment.DIRECTORY_DOWNLOADS + " > " + BTCConstants.APPNAME + " 폴더에 저장 되었습니다.";

    // 데이터 통신타입
    public static final int MY_TEXT = 0;
    public static final int MY_FILE = 1;
    public static final int YOUR_TEXT = 2;
    public static final int YOUR_FILE = 3;

    // 데이터 통신 Sequence
    public static int DATA_SEQ = -1;
}
