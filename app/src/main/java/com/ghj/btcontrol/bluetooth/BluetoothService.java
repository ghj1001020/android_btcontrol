package com.ghj.btcontrol.bluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.ghj.btcontrol.data.BTCConstants;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
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


    BluetoothAdapter mBTAdapter;
    Activity mActivity;
    Handler mHandler;

    List<BluetoothDevice> mDevices;
    List<BluetoothDevice> mBonded;
    BluetoothSocket mSocket;
    BluetoothServerSocket mServerSocket;

    BluetoothDevice mRemoteDevice;

    int mPrevState;
    boolean isClose;
    boolean isFailed;
    boolean isBondRequest;

    BluetoothDataThread mBluetoothDataThread;


    public static BluetoothService getBluetoothService(Activity activity, Handler handler){
        if(instance==null){
            synchronized (BluetoothService.class){
                instance = new BluetoothService(activity, handler);
            }
        }else{
            instance.setActivity(activity);
            instance.setHandler(handler);
        }

        return instance;
    }

    private void setActivity(Activity activity){
        this.mActivity = activity;
    }

    private void setHandler(Handler handler){
        this.mHandler = handler;
    }


    private BluetoothService(Activity activity, Handler handler){
        this.mActivity = activity;
        this.mHandler = handler;
        mBTAdapter = BluetoothAdapter.getDefaultAdapter();
        mDevices = new ArrayList<>();
        mBonded = new ArrayList<>();
        mPrevState = -1;

        onRegisterBluetooth();
        runListening();
    }


    //블루투스 리시버 등록
    public boolean onRegisterBluetooth(){
        if(getDeviceState()) {
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
    public boolean getDeviceState(){
        if(mBTAdapter == null){
            return false;
        }else{
            return true;
        }
    }


    //블루투스 활성화
    public void enableBluetooth(){
        if(!mBTAdapter.isEnabled()){
            mBTAdapter.enable();
        }
    }

    //블루투스 비활성화
    public void disableBluetooth(){
        if(mBTAdapter.isEnabled()){
            cancelScanDevice();
            closeSocket();
            mBTAdapter.disable();
        }
    }

    //블루투스 활성화 여부
    public boolean isEnabled(){
        return mBTAdapter.isEnabled();
    }


    //기기 검색하기
    public void startScanDevice(){
        if(mBTAdapter.isEnabled() && !mBTAdapter.isDiscovering()){
            mBTAdapter.startDiscovery();
        }
    }

    //기기 검색하기 중지
    public void cancelScanDevice(){
        if(mBTAdapter.isDiscovering()){
            mBTAdapter.cancelDiscovery();
        }
    }

    //등록된 디바이스 검색
    public List<BluetoothDevice> getBondedDevice(){
        mBonded.clear();
        Set<BluetoothDevice> devices = mBTAdapter.getBondedDevices();
        for(BluetoothDevice device : devices){
            mBonded.add(device);
        }
        return mBonded;
    }

    //페어링 요청
    public boolean requestBond(BluetoothDevice device){
        if(device.getBondState() == BluetoothDevice.BOND_NONE){
            //API 19이상
            return device.createBond();
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

    //Remote Device 반환
    public BluetoothDevice getRemoteDevice(){
        return mRemoteDevice;
    }

    //서버소켓 닫기
    public void closeServerSocket(){
        if(mServerSocket != null){
            try{
                isClose = true;
                mServerSocket.close();
                mServerSocket = null;
            }catch (IOException e){
                e.printStackTrace();
            }
        }
    }

    //소켓닫기
    public void closeSocket(){
        try{
            if(mSocket!=null) {
                mSocket.close();
                mSocket = null;
            }
            closeServerSocket();
        }catch (IOException e){
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
                try{
                    isFailed = false;
                    if(!mSocket.isConnected()){
                        mSocket.connect();
                        mBluetoothDataThread = new BluetoothDataThread(mSocket);
                        mBluetoothDataThread.start();
                        BluetoothDevice device = mSocket.getRemoteDevice();
                        Intent intent = new Intent(BTCConstants.BLUETOOTH_CONNECT_BROADCAST_ACTION);
                        Bundle bundle = new Bundle();
                        bundle.putParcelable("device", device);
                        intent.putExtras(bundle);
                        mActivity.sendBroadcast(intent);
                    }
                }catch (IOException e){
                    e.printStackTrace();
                    connectFailed(e.getMessage());
                }
            }
        }.start();
    }

    //서버 모드로 리스닝
    public void runListening(){
        if(!isEnabled()){
            return;
        }

        new Thread(){
            @Override
            public void run() {
                isClose = false;
                isFailed = false;
                UUID uuid = UUID.fromString(BTCConstants.SERIAL_PORT_SERVICE_UUID);
                try{
                    mServerSocket = mBTAdapter.listenUsingRfcommWithServiceRecord("BLUETOOTH", uuid);
                    mSocket = mServerSocket.accept();
                    mBluetoothDataThread = new BluetoothDataThread(mSocket);
                    mBluetoothDataThread.start();
                    BluetoothDevice device = mSocket.getRemoteDevice();
                    Intent intent = new Intent(BTCConstants.BLUETOOTH_CONNECT_BROADCAST_ACTION);
                    Bundle bundle = new Bundle();
                    bundle.putParcelable("device", device);
                    intent.putExtras(bundle);
                    mActivity.sendBroadcast(intent);
                }catch (IOException e){
                    if(!isClose){
                        e.printStackTrace();
                        connectFailed(e.getMessage());
                    }
                }finally {
                    if(mServerSocket!=null){
                        try{ mServerSocket.close(); mServerSocket = null; }catch (IOException e){}
                    }
                }
            }
        }.start();
    }

    //문자보내기
    public void sendString(byte[] message){
        if(message==null || message.length==0){
            return;
        }

        if(mBluetoothDataThread!=null){
            mBluetoothDataThread.write(message);
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
            else if(BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)){
                isBondRequest = true;
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                int state = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.BOND_NONE);
                int prevState = intent.getIntExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, BluetoothDevice.BOND_NONE);

                Message msg = new Message();
                if(state == BluetoothDevice.BOND_BONDED){
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
                    mPrevState = prevState;
                }
                else if(state == BluetoothDevice.BOND_NONE){
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
                Log.d("aaa", "ACTION_CONNECTION_STATE_CHANGED");
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
                Log.d("aaa", "BluetoothDevice.ACTION_ACL_CONNECTED");
//                if(isBondRequest || isFailed){
//                    return;
//                }
//                mRemoteDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
//                if(mRemoteDevice.getBondState() == BluetoothDevice.BOND_BONDED){
//                    mHandler.sendEmptyMessage(CONNECT_SUCCESS_ACL_HANDLER_CODE);
//                }
            }
            else if(BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)){
                Log.d("aaa", "BluetoothDevice.ACTION_ACL_DISCONNECTED");
                if(isBondRequest){
                    isBondRequest = false;
                    return;
                }
                mHandler.sendEmptyMessage(DISCONNECTED_ACL_HANDLER_CODE);
            }
            else if(BTCConstants.BLUETOOTH_CONNECT_BROADCAST_ACTION.equals(action)){
                Log.d("aaa", "BLUETOOTH_CONNECT_BROADCAST_ACTION");
                if(isBondRequest || isFailed){
                    return;
                }
                mRemoteDevice = intent.getParcelableExtra("device");
                if(mRemoteDevice.getBondState() == BluetoothDevice.BOND_BONDED){
                    mHandler.sendEmptyMessage(CONNECT_SUCCESS_ACL_HANDLER_CODE);
                }
            }
        }
    };


    //데이터 통신 클랙스
    class BluetoothDataThread extends Thread {

        BluetoothSocket mBluetoothSocket;
        InputStream mInputStream;
        OutputStream mOutputStream;


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
                    int len = 0;
                    while( (len = mInputStream.read(buffer)) != -1 ){
                        String str = new String(buffer, 0, len);
                        Log.d("aaa", str);

                        byte[] msgArr = new byte[len];
                        System.arraycopy(buffer, 0, msgArr, 0, len);
                        Message msg = mHandler.obtainMessage();
                        msg.what = READ_MESSAGE_HANDLER_CODE;
                        Bundle bundle = msg.getData();
                        bundle.putByteArray("message", msgArr);
                        msg.setData(bundle);
                        mHandler.sendMessage(msg);
                    }
                }catch (IOException e){
                    if(mSocket==null || mSocket.isConnected()){
                        break;
                    }
                    e.printStackTrace();
                }
            }
        }

        //쓰기
        public void write(byte[] message){
            if(message.length == 0){
                return;
            }

            try{
                mOutputStream.write(message);
                mOutputStream.flush();

                Message msg = mHandler.obtainMessage();
                msg.what = BluetoothService.WRITE_MESSAGE_HANDLER_CODE;
                Bundle bundle = msg.getData();
                bundle.putByteArray("message", message);
                msg.setData(bundle);
                mHandler.sendMessage(msg);
            }catch (IOException e){
                e.printStackTrace();
            }
        }
    }
}
