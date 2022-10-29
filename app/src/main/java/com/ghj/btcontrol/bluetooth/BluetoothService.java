package com.ghj.btcontrol.bluetooth;

import static com.ghj.btcontrol.data.BTCConstants.MY_FILE;
import static com.ghj.btcontrol.data.BTCConstants.MY_TEXT;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;

import com.ghj.btcontrol.data.BTCConstants;
import com.ghj.btcontrol.data.SendData;
import com.ghj.btcontrol.util.PermissionUtil;
import com.ghj.btcontrol.util.Util;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Created by ghj on 2017. 3. 27..
 */
public class BluetoothService {

    //메인 메모리에 BluetoothService 인스턴스 생성
    private static volatile BluetoothService instance;


    private static final String TAG = "BluetoothService";

    public final static int STATE_ON_HANDLER_CODE = 1000;
    public final static int STATE_OFF_HANDLER_CODE = 1001;
    public final static int DISCOVERY_START_HANDLER_CODE = 2000;
    public final static int DISCOVERY_FINISH_HANDLER_CODE = 2001;
    public final static int DISCOVERY_FOUND_HANDLER_CODE = 2002;
    public final static int BONDED_HANDLER_CODE = 2003;
    public final static int BONDED_CANCEL_HANDLER_CODE = 2004;
    public final static int BONDED_FAIL_HANDLER_CODE = 2005;
    public final static int CONNECT_SUCCESS_CLIENT_HANDLER_CODE = 3000;
    public final static int CONNECT_SUCCESS_MASTER_HANDLER_CODE = 3001;
    public final static int CONNECT_SUCCESS_ACL_HANDLER_CODE = 3002;
    public final static int CONNECT_FAIL_HANDLER_CODE = 3003;
    public final static int DISCONNECTED_HANDLER_CODE = 3004;
    public final static int DISCONNECTED_ACL_HANDLER_CODE = 3005;
    public final static int READ_MESSAGE_HANDLER_CODE = 4000;
    public final static int WRITE_MESSAGE_HANDLER_CODE = 4001;
    public final static int READ_FILE_HANDLER_CODE = 4002;
    public final static int WRITE_FILE_HANDLER_CODE = 4003;
    public final static int DATA_WRITE_START = 4004;
    public final static int DATA_WRITE_PROGRESS = 4005;
    public final static int DATA_WRITE_END = 4006;
    public final static int DATA_READ_PROGRESS = 4007;
    public final static int DATA_READ_END = 4008;


    BluetoothAdapter mBTAdapter;
    Activity mActivity;
    Handler mHandler;

    List<BluetoothDevice> mDevices;
    List<BluetoothDevice> mBonded;
    BluetoothSocket mSocket;
    BluetoothServerSocket mServerSocket;

    BluetoothDevice mRemoteDevice;

    int mPrevState;
    boolean isClose = false;
    boolean isFailed = false;
    boolean isBondRequest = false;

    BluetoothServerThread mBluetoothServerThread;
    BluetoothDataThread mBluetoothDataThread;


    public static BluetoothService getBluetoothService(Activity activity, Handler handler) {
        if (instance == null) {
            synchronized (BluetoothService.class) {
                instance = new BluetoothService(activity, handler);
            }
        } else {
            instance.setActivity(activity);
            instance.setHandler(handler);
        }

        return instance;
    }

    private void setActivity(Activity activity) {
        this.mActivity = activity;
    }

    private void setHandler(Handler handler) {
        this.mHandler = handler;
    }


    private BluetoothService(Activity activity, Handler handler) {
        this.mActivity = activity;
        this.mHandler = handler;
        mBTAdapter = BluetoothAdapter.getDefaultAdapter();
        mDevices = new ArrayList<>();
        mBonded = new ArrayList<>();
        mPrevState = -1;

        onRegisterBluetooth();
    }


    //블루투스 리시버 등록
    public boolean onRegisterBluetooth() {
        if (getDeviceState()) {
            IntentFilter filter = new IntentFilter();
            filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
            filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
            filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
            filter.addAction(BluetoothDevice.ACTION_FOUND);
            filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
            filter.addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED);
            filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
            filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
            filter.addAction(BTCConstants.BLUETOOTH_CONNECT_BROADCAST_ACTION);
            mActivity.registerReceiver(mBroadcastReceiver, filter);
            return true;
        }
        return false;
    }


    //블루투스 지원 유무 확인
    public boolean getDeviceState() {
        if (mBTAdapter == null) {
            return false;
        } else {
            return true;
        }
    }


    //블루투스 활성화
    public void enableBluetooth() {
        if(PermissionUtil.checkBluetoothPermission(mActivity)) {
            if (!mBTAdapter.isEnabled()) {
                mBTAdapter.enable();
            }
        }
    }

    //블루투스 비활성화
    public void disableBluetooth(){
        if(PermissionUtil.checkBluetoothPermission(mActivity)) {
            if(mBTAdapter.isEnabled()){
                cancelScanDevice();
                closeSocket();
                mBTAdapter.disable();
            }
        }
    }

    //블루투스 활성화 여부
    public boolean isEnabled(){
        return mBTAdapter.isEnabled();
    }

    //블루투스 연결
    public boolean isConnected() {
        if(mBTAdapter == null) return false;
        if(isEnabled()) return false;
        if(mSocket == null) return false;

        return mSocket.isConnected();
    }

    //기기 검색하기
    public void startScanDevice(){
        if(PermissionUtil.checkBluetoothPermission(mActivity)) {
            // scan device
            if(mBTAdapter.isEnabled() && !mBTAdapter.isDiscovering()){
                boolean scan = mBTAdapter.startDiscovery();
                Log.d(TAG, "scan = " + scan);
            }
        }
    }

    //기기 검색하기 중지
    public void cancelScanDevice(){
        if(PermissionUtil.checkBluetoothPermission(mActivity)) {
            if(mBTAdapter.isDiscovering()){
                mBTAdapter.cancelDiscovery();
            }
        }
    }

    //등록된 디바이스 검색
    public List<BluetoothDevice> getBondedDevice(){
        mBonded.clear();
        if(PermissionUtil.checkBluetoothPermission(mActivity)) {
            Set<BluetoothDevice> devices = mBTAdapter.getBondedDevices();
            for(BluetoothDevice device : devices){
                mBonded.add(device);
            }
        }
        return mBonded;
    }

    //페어링 요청
    public boolean requestBond(BluetoothDevice device){
        if(PermissionUtil.checkBluetoothPermission(mActivity)) {
            if(device.getBondState() == BluetoothDevice.BOND_NONE){
                //API 19이상
                return device.createBond();
            }
        }
        return false;
    }

    //페어링 해제요청
    public void removeBondedDevice(BluetoothDevice device){
        try {
            Class<?> btDeviceInstance =  Class.forName(BluetoothDevice.class.getCanonicalName());
            Method removeBondMethod = btDeviceInstance.getMethod("removeBond");
            removeBondMethod.invoke(device);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //블루투스 연결
    public void requestConnect(BluetoothDevice device){
        if(PermissionUtil.checkBluetoothPermission(mActivity)) {
            cancelScanDevice();
            closeSocket();

            UUID uuid = UUID.fromString( BTCConstants.SERIAL_PORT_SERVICE_UUID );
            try{
                mSocket = device.createRfcommSocketToServiceRecord(uuid);
                runConnect();
            }catch (IOException e){
                e.printStackTrace();
                connectFailed(e.getMessage());
            }
        }
    }

    //Remote Device 반환
    public BluetoothDevice getRemoteDevice(){
        return mRemoteDevice;
    }

    //서버소켓 닫기
    public void closeServerSocket(){
        isClose = true;
        if(mServerSocket != null){
            try{
                mServerSocket.close();
                mServerSocket = null;
            }catch (IOException e){
                e.printStackTrace();
            }
        }
    }

    //소켓닫기
    public void closeSocket(){
        disconnectSocket();
        closeServerSocket();
    }

    // 연결끊기
    public void disconnectSocket() {
        try {
            if(mSocket!=null) {
                mSocket.close();
                mSocket = null;
            }
            if(mBluetoothDataThread !=null) {
                mBluetoothDataThread.interrupt();
                mBluetoothDataThread.closeStream();
                mBluetoothDataThread = null;
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    //클라이언트 모드로 연결
    public void runConnect(){
        if(mSocket==null){
            connectFailed("socket is null");
            return;
        }

        new Thread(){
            @Override
            public void run() {
                isFailed = false;
                if(PermissionUtil.checkBluetoothPermission(mActivity)) {
                    try{
                        if(!mSocket.isConnected()){
                            mSocket.connect();
                            mBluetoothDataThread = new BluetoothDataThread(mSocket);
                            mBluetoothDataThread.start();

                            BluetoothDevice device = mSocket.getRemoteDevice();
                            Intent intent = new Intent(BTCConstants.BLUETOOTH_CONNECT_BROADCAST_ACTION);
                            Bundle bundle = new Bundle();
                            bundle.putBoolean("sender", true);
                            bundle.putParcelable("device", device);
                            intent.putExtras(bundle);
                            mActivity.sendBroadcast(intent);

                            Log.d(TAG, "CLIENT Socket");
                        }
                    }catch (IOException e){
                        e.printStackTrace();
                        connectFailed(e.getMessage());
                    }
                }
            }
        }.start();
    }

    //서버 모드로 리스닝
    public void runListening(){
        if(!isEnabled()){
            return;
        }

        mBluetoothServerThread = new BluetoothServerThread();
        mBluetoothServerThread.start();
    }

    //문자보내기
    public void send(int seq, String message){
        if(mBluetoothDataThread!=null){
            mBluetoothDataThread.writeText(seq, message);
        }
    }

    //파일보내기
    public void send(int seq, Uri uri, String filename, long filesize) {
        if(mBluetoothDataThread!=null){
            mBluetoothDataThread.writeFile(seq, uri, filename, filesize);
        }
    }

    //연결 실패
    public void connectFailed(String message){
        isFailed = true;
        Message msg = mHandler.obtainMessage();
        msg.what = CONNECT_FAIL_HANDLER_CODE;
        Bundle bundle = msg.getData();
        bundle.putString("message", message);
        msg.setData(bundle);
        mHandler.sendMessage(msg);
    }

    //블루투스 종료
    public void onDestroyBluetooth(){
        cancelScanDevice();
        closeSocket();
        mActivity.unregisterReceiver(mBroadcastReceiver);
    }


    //리시버
    BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d(TAG, "action : " + action);

            if(BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)){
                int extra = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1);
                if(BluetoothAdapter.STATE_ON == extra){
                    runListening();
                    mHandler.sendEmptyMessage(STATE_ON_HANDLER_CODE);
                }
                else if(BluetoothAdapter.STATE_OFF == extra){
                    mHandler.sendEmptyMessage(STATE_OFF_HANDLER_CODE);
                }
            }
            else if(BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)){
                mDevices.clear();
                mHandler.sendEmptyMessage(DISCOVERY_START_HANDLER_CODE);
            }
            else if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)){
                mHandler.sendEmptyMessage(DISCOVERY_FINISH_HANDLER_CODE);
            }
            else if(BluetoothDevice.ACTION_FOUND.equals(action)){
                if(PermissionUtil.checkBluetoothPermission(mActivity)) {
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    int rssi = (int)intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, (short)-1);
                    if(device.getBondState()==BluetoothDevice.BOND_BONDED){
                        return;
                    }

                    String address = device.getAddress();
                    boolean result = false;
                    for(BluetoothDevice dev : mDevices){
                        if(address.equals(dev.getAddress())){
                            result = true;
                            break;
                        }
                    }

                    if(result){
                        return;
                    }

                    mDevices.add(device);
                    Message msg = new Message();
                    msg.what = DISCOVERY_FOUND_HANDLER_CODE;
                    Bundle bundle = new Bundle();
                    bundle.putParcelable("device", device);
                    bundle.putInt("rssi", rssi);
                    msg.setData(bundle);
                    mHandler.sendMessage(msg);
                }
            }
            else if(BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)){

                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                int state = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.BOND_NONE);
                int prevState = intent.getIntExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, BluetoothDevice.BOND_NONE);

                Message msg = new Message();
                Log.d(TAG, "bond state : " +prevState + " -> " + state);
                if(state == BluetoothDevice.BOND_BONDED){
                    isBondRequest = false;
                    if(state!=mPrevState){
                        mBonded.add(device);
                        msg.what = BONDED_HANDLER_CODE;
                        Bundle bundle = new Bundle();
                        bundle.putParcelable("device", device);
                        msg.setData(bundle);
                        mHandler.sendMessage(msg);
                    }
                    mPrevState = -1;
                }
                else if(state == BluetoothDevice.BOND_BONDING){
                    isBondRequest = true;
                    mPrevState = prevState;
                }
                else if(state == BluetoothDevice.BOND_NONE){
                    isBondRequest = false;
                    if(state!=mPrevState){
                        mBonded.remove(device);
                        mDevices.add(device);
                        msg.what = BONDED_CANCEL_HANDLER_CODE;
                        Bundle bundle = new Bundle();
                        bundle.putParcelable("device", device);
                        msg.setData(bundle);
                        mHandler.sendMessage(msg);
                    }else{
                        mPrevState = -1;
                        String name = device.getName();
                        msg.what = BONDED_FAIL_HANDLER_CODE;
                        msg.obj = name;
                        mHandler.sendMessage(msg);
                    }
                }
            }
            else if(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED.equals(action)){
                int state = intent.getIntExtra(BluetoothAdapter.EXTRA_CONNECTION_STATE, BluetoothAdapter.STATE_DISCONNECTED);

                if(state == BluetoothAdapter.STATE_CONNECTED){
                    mRemoteDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    mHandler.sendEmptyMessage(CONNECT_SUCCESS_CLIENT_HANDLER_CODE);
                }
                else if(state == BluetoothAdapter.STATE_DISCONNECTED){
                    mHandler.sendEmptyMessage(DISCONNECTED_HANDLER_CODE);
                }
            }
            else if(BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)){
//                if(isBondRequest || isFailed){
//                    return;
//                }
//                mRemoteDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
//                if(mRemoteDevice.getBondState() == BluetoothDevice.BOND_BONDED){
//                    mHandler.sendEmptyMessage(CONNECT_SUCCESS_ACL_HANDLER_CODE);
//                }
            }
            else if(BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)){
                if(isBondRequest){
                    isBondRequest = false;
                    return;
                }
                mHandler.sendEmptyMessage(DISCONNECTED_ACL_HANDLER_CODE);
            }
            else if(BTCConstants.BLUETOOTH_CONNECT_BROADCAST_ACTION.equals(action)){
                if(isBondRequest || isFailed){
                    return;
                }
                mRemoteDevice = intent.getParcelableExtra("device");
                if(mRemoteDevice.getBondState() == BluetoothDevice.BOND_BONDED){
                    boolean sender = intent.getBooleanExtra("sender", false);

                    Message msg = new Message();
                    msg.what = CONNECT_SUCCESS_ACL_HANDLER_CODE;
                    Bundle bundle = new Bundle();
                    bundle.putBoolean("sender", sender);
                    msg.setData(bundle);
                    mHandler.sendMessage(msg);
                }
            }
        }
    };

    // 서버 클래스
    class BluetoothServerThread extends Thread {
        @Override
        public void run() {
            isClose = false;
            isFailed = false;
            if(PermissionUtil.checkBluetoothPermission(mActivity)) {
                UUID uuid = UUID.fromString(BTCConstants.SERIAL_PORT_SERVICE_UUID);
                try{
                    mServerSocket = mBTAdapter.listenUsingRfcommWithServiceRecord("BLUETOOTH", uuid);
                    mSocket = mServerSocket.accept();
                    mBluetoothDataThread = new BluetoothDataThread(mSocket);
                    mBluetoothDataThread.start();

                    BluetoothDevice device = mSocket.getRemoteDevice();
                    Intent intent = new Intent(BTCConstants.BLUETOOTH_CONNECT_BROADCAST_ACTION);
                    Bundle bundle = new Bundle();
                    bundle.putBoolean("sender", false);
                    bundle.putParcelable("device", device);
                    intent.putExtras(bundle);
                    mActivity.sendBroadcast(intent);

                    Log.d(TAG, "SERVER Socket");
                }catch (IOException e){
                    e.printStackTrace();
                    if(!isClose){
                        connectFailed(e.getMessage());
                    }
                }finally {
                    closeServerSocket();
                }
            }
        }
    }

    //데이터 통신 클랙스
    class BluetoothDataThread extends Thread {

        BluetoothSocket mBluetoothSocket;
        InputStream mInputStream;
        OutputStream mOutputStream;

        private static final int MSG_WRITE_START = 100;
        private static final int MSG_WRITE_PROGRESS = 101;
        private static final int MSG_WRITE_END = 102;

        // send 쓰기 데이터목록
        List<SendData> mSendData = new ArrayList<>();
        WriteThread mWriteThread = null;

        // write read 핸들러
        DataHandler mDataHandler = new DataHandler();


        public BluetoothDataThread(BluetoothSocket socket){
            this.mBluetoothSocket = socket;
            try{
            mInputStream = mBluetoothSocket.getInputStream();
                mOutputStream = mBluetoothSocket.getOutputStream();
            }catch (IOException e){
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            read();
        }

        //읽기
        public void read(){
            byte[] buffer = new byte[2048];
            while(mSocket!=null && mSocket.isConnected()){
                try{
                    Arrays.fill(buffer, (byte)0x00);
                    mInputStream.read(buffer, 0, 1);

                    // 텍스트
                    if(buffer[0] == 0x00) {
                        Arrays.fill(buffer, (byte)0x00);
                        mInputStream.read(buffer, 0, 4);
                        int toReadLen = Util.ByteArrayToInt(buffer);

                        byte[] data = new byte[1024*1024];
                        mInputStream.read(data);
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        baos.write(data, 0, toReadLen);

                        String message = baos.toString("UTF-8");
                        Bundle bundle = new Bundle();
                        bundle.putString("message", message);
                        sendHandlerMessage(READ_MESSAGE_HANDLER_CODE, mHandler, bundle);
                    }
                    // 파일
                    else if(buffer[0] == 0x01) {
                        // 파일명
                        Arrays.fill(buffer, (byte)0x00);
                        mInputStream.read(buffer, 0, 4);
                        int filenameLen = Util.ByteArrayToInt(buffer);
                        Arrays.fill(buffer, (byte)0x00);
                        mInputStream.read(buffer, 0, filenameLen);
                        String filename = Util.ByteArrayToString(buffer);
                        // 파일사이즈
                        Arrays.fill(buffer, (byte)0x00);
                        mInputStream.read(buffer, 0, 8);
                        long filesize = Util.ByteArrayToLong(buffer);
                        // 파일Uri
                        Uri fileUri = getFileUri(filename);

                        Bundle sBundle = new Bundle();
                        sBundle.putString("filename", filename);
                        sBundle.putLong("filesize", filesize);
                        sBundle.putParcelable("fileuri", fileUri);
                        sendHandlerMessage(READ_FILE_HANDLER_CODE, mHandler, sBundle);

                        // 파일 다운로드
                        OutputStream os = getOutputStream(fileUri);
                        int len = 0;
                        long sum = 0;
                        byte[] data = new byte[1024*1024];
                        while( (len = mInputStream.read(data)) != -1 ) {
                            os.write(data, 0, len);
                            os.flush();

                            Bundle pBundle = new Bundle();
                            pBundle.putLong("progress", len);
                            sendHandlerMessage(DATA_READ_PROGRESS, mHandler, pBundle);

                            sum += len;
                            if(filesize <= sum) {
                                break;
                            }
                        }
                        os.close();

                        sendHandlerMessage(DATA_READ_END, mHandler);
                    }
                }catch (IOException e){
                    e.printStackTrace();
                    if(mSocket==null || mSocket.isConnected()){
                        break;
                    }
                }
            }
        }

        //쓰기
        public void writeText(int seq, String text) {
            if(TextUtils.isEmpty(text)) {
                return;
            }
            mSendData.add(new SendData(MY_TEXT, seq, text));

            Message msg = mHandler.obtainMessage();
            msg.what = BluetoothService.WRITE_MESSAGE_HANDLER_CODE;
            Bundle bundle = msg.getData();
            bundle.putInt("seq", seq);
            bundle.putString("message", text);
            msg.setData(bundle);
            mHandler.sendMessage(msg);

            write();
        }

        public void writeFile(int seq, Uri uri, String filename, long filesize) {
            if(uri == null) {
                return;
            }
            mSendData.add(new SendData(MY_FILE, seq, uri, filename, filesize));

            Message msg = mHandler.obtainMessage();
            msg.what = BluetoothService.WRITE_FILE_HANDLER_CODE;
            Bundle bundle = msg.getData();
            bundle.putInt("seq", seq);
            bundle.putString("filename", filename);
            bundle.putLong("filesize", filesize);
            bundle.putParcelable("fileuri", uri);
            msg.setData(bundle);
            mHandler.sendMessage(msg);

            write();
        }

        private void write() {
            synchronized ("WRITE") {
                if(!mSocket.isConnected()) {
                    Log.d(TAG, "WriteThread Socket not disconnected");
                    return;
                }
                if(mSendData.isEmpty())
                    return;

                try {
                    if (mWriteThread == null) {
                        Log.d(TAG, "WriteThread START");
                        mWriteThread = new WriteThread();
                        mWriteThread.start();
                    }
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        // 스트림닫기
        public void closeStream() {
            if(mInputStream != null) {
                try {
                    mInputStream.close();
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if(mOutputStream != null) {
                try {
                    mOutputStream.close();
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        // 쓰기 쓰레드
        private class WriteThread extends Thread {
            public WriteThread() { }

            @Override
            public void run() {
                try {
                    SendData sendData = mSendData.get(0);
                    int seq = sendData.getSeq();

                    Bundle sBundle = new Bundle();
                    sBundle.putInt("seq", seq);
                    sendHandlerMessage(MSG_WRITE_START, mDataHandler, sBundle);

                    if(sendData.getDataType() == 0) {
                        String message = sendData.getText();

                        // 0x00 텍스트 0x01 파일
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        byte[] type = new byte[]{0x00};
                        baos.write(type);
                        // 텍스트
                        byte[] textBytes = Util.StringToByteArray(message);
                        // 텍스트 크기
                        byte[] baTextLen = Util.IntToByteArray(textBytes.length);
                        baos.write(baTextLen);
                        baos.write(textBytes);

                        mOutputStream.write(baos.toByteArray());
                        mOutputStream.flush();
                    }
                    else if(sendData.getDataType() == 1) {
                        Uri uri = sendData.getUri();
                        String filename = sendData.getFilename();
                        long filesize = sendData.getFilesize();

                        // 0x00 텍스트 0x01 파일
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        byte[] type = new byte[]{0x01};
                        baos.write(type);
                        // 파일명
                        byte[] baFilename = filename.getBytes(StandardCharsets.UTF_8);
                        byte[] baFilenameLen = Util.IntToByteArray(baFilename.length);
                        baos.write(baFilenameLen);
                        baos.write(baFilename);
                        // 파일사이즈
                        byte[] baFileSize = Util.LongToByteArray(filesize);
                        baos.write(baFileSize);

                        mOutputStream.write(baos.toByteArray());
                        mOutputStream.flush();

                        // 파일데이터
                        try {
                            InputStream is = mActivity.getContentResolver().openInputStream(uri);
                            BufferedInputStream bis = new BufferedInputStream(is);
                            byte[] buffer = new byte[1024*1024];
                            int len = 0;
                            while( (len = bis.read(buffer)) > 0) {
                                Bundle pBundle = new Bundle();
                                pBundle.putInt("seq", seq);
                                pBundle.putLong("progress", len);
                                sendHandlerMessage(MSG_WRITE_PROGRESS, mDataHandler, pBundle);

                                mOutputStream.write(buffer, 0, len);
                                mOutputStream.flush();
                            }
                        }
                        catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    Bundle eBundle = new Bundle();
                    eBundle.putInt("seq", seq);
                    sendHandlerMessage(MSG_WRITE_END, mDataHandler, eBundle);
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        // 쓰기 읽기 핸들러
        private class DataHandler extends Handler {
            public DataHandler() {
                super(Looper.getMainLooper());
            }

            @Override
            public void handleMessage(@NonNull Message msg) {
                // Send 시작
                if(msg.what == MSG_WRITE_START) {
                    Bundle bundle = msg.getData();
                    sendHandlerMessage(DATA_WRITE_START, mHandler, bundle);
                }
                // Send 전송중
                else if(msg.what == MSG_WRITE_PROGRESS) {
                    Bundle bundle = msg.getData();
                    sendHandlerMessage(DATA_WRITE_PROGRESS, mHandler, bundle);
                }
                // Send 완료
                else if(msg.what == MSG_WRITE_END) {
                    Bundle bundle = msg.getData();
                    sendHandlerMessage(DATA_WRITE_END, mHandler, bundle);

                    mWriteThread.interrupt();
                    mWriteThread = null;
                    if(mSendData.size() > 0 ) {
                        mSendData.remove(0);
                    }
                    Log.d(TAG, "DataHandler MSG_WRITE_END");
                    write();
                }
            }
        }
    }

    // 파일다운로드 Uri
    private Uri getFileUri(String filename) {
        Uri fileUri = null;
        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {

                ContentValues cv = new ContentValues();
                cv.put(MediaStore.Files.FileColumns.DISPLAY_NAME, filename);
                cv.put(MediaStore.Files.FileColumns.MIME_TYPE, Util.getMimeTypeFromFilename(filename));
                cv.put(MediaStore.Files.FileColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + File.separator + BTCConstants.APPNAME);
                fileUri = mActivity.getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, cv);
            }
            else {
                File dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + File.separator + BTCConstants.APPNAME);
                if(!dir.exists()) {
                    dir.mkdir();
                }
                File file = new File(dir, filename);
//            fos = new FileOutputStream(file);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return fileUri;
    }

    // 파일다운로드 Output 스트림
    private OutputStream getOutputStream(Uri uri) {
        OutputStream fos = null;
        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                fos = mActivity.getContentResolver().openOutputStream(uri);
            }
            else {

            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return fos;
    }

    // 핸들러메시지
    public void sendHandlerMessage(int code, Handler handler, Bundle bundle) {
        Message msg = handler.obtainMessage();
        msg.what = code;
        msg.setData(bundle);
        handler.sendMessage(msg);
    }

    public void sendHandlerMessage(int code, Handler handler) {
        handler.sendEmptyMessage(code);
    }
}
