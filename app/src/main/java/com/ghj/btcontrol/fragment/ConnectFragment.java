package com.ghj.btcontrol.fragment;

import static com.ghj.btcontrol.data.BTCConstants.DATA_SEQ;
import static com.ghj.btcontrol.data.BTCConstants.MY_FILE;
import static com.ghj.btcontrol.data.BTCConstants.MY_TEXT;
import static com.ghj.btcontrol.data.BTCConstants.YOUR_FILE;
import static com.ghj.btcontrol.data.BTCConstants.YOUR_TEXT;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothDevice;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.ghj.btcontrol.BaseFragmentActivity;
import com.ghj.btcontrol.MainActivity;
import com.ghj.btcontrol.R;
import com.ghj.btcontrol.adapter.AdapterConnect;
import com.ghj.btcontrol.data.BTCConstants;
import com.ghj.btcontrol.data.ConnectData;
import com.ghj.btcontrol.util.PermissionUtil;
import com.ghj.btcontrol.util.Util;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ConnectFragment extends Fragment implements View.OnClickListener {

    // 보내는사람여부
    private boolean mSender = false;

    ImageButton btnSend, btnBack, btnAttach;
    TextView txtRName, txtRMAC, txtRType;
    EditText editMessage;
    ProgressDialog mProgressDialog;
    Button btnClear;
    LinearLayout boxEdit;


    BluetoothDevice mRemoteDevice;
    String mRemoteName;

    // 메시지 목록
    RecyclerView rvMessage;
    AdapterConnect mAdapterConnect;
    List<ConnectData> mConnectDatas = new ArrayList<>();


    public ConnectFragment() {}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if( getActivity() instanceof BaseFragmentActivity) {
            ((BaseFragmentActivity) getActivity()).addToFragmentStack(this);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_connect, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if(getArguments() != null){
            mSender = getArguments().getBoolean("sender", false);
        }

        BTCConstants.DATA_SEQ = 0;

        //ui
        btnBack = (ImageButton)view.findViewById(R.id.btnBack);
        btnClear = (Button)view.findViewById(R.id.btnClear);
        txtRName = view.findViewById(R.id.txtRName);
        txtRMAC = view.findViewById(R.id.txtRMAC);
        txtRType = view.findViewById(R.id.txtRType);
        mProgressDialog = new ProgressDialog(getContext(), ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setMessage("연결종료 중입니다...");
        mProgressDialog.setCancelable(false);
        editMessage = view.findViewById(R.id.editMessage);
        btnSend = view.findViewById(R.id.btnSend);
        btnAttach = view.findViewById(R.id.btnAttach);
        boxEdit = view.findViewById(R.id.boxEdit);
        rvMessage = view.findViewById(R.id.rvMessage);

        btnBack.setOnClickListener(this);
        btnClear.setOnClickListener(this);

        if(mSender) {
            btnSend.setOnClickListener(this);
            btnAttach.setOnClickListener(this);
            editMessage.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    final boolean isEnterEvent = event != null && event.getKeyCode()==KeyEvent.KEYCODE_ENTER;

                    //엔터 치면 보내기
                    if(actionId == EditorInfo.IME_ACTION_DONE || isEnterEvent){
                        SendMessage();
                        return true;
                    }
                    return false;
                }
            });
            editMessage.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) { }
                @Override
                public void afterTextChanged(Editable s) {
                    editMessage.setTextColor(Color.RED);
                }
            });
        }
        else {
            btnSend.setEnabled(false);
            btnAttach.setEnabled(false);
            editMessage.setEnabled(false);
        }

        // 연결디바이스
        mRemoteDevice = ((MainActivity) getActivity()).getBTService().getRemoteDevice();

        initData();
    }

    @Override
    public void onResume() {
        super.onResume();
        if( ((MainActivity) getActivity()).getBTService().isConnected() ) {
            Toast.makeText(getContext(), "Connected", Toast.LENGTH_SHORT).show();
        }
        else {
            Toast.makeText(getContext(), "Disconnected", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * @desc 데이터 설정
     */
    public void initData(){
        if(!PermissionUtil.checkBluetoothPermission(getActivity())) {
            return;
        }

        mRemoteName = mRemoteDevice.getName();
        String address = mRemoteDevice.getAddress();
        txtRName.setText(mRemoteName);
        txtRMAC.setText(address);
        switch(mRemoteDevice.getType()){
            case BluetoothDevice.DEVICE_TYPE_CLASSIC:
                txtRType.setText("BR/EDR");
                break;
            case BluetoothDevice.DEVICE_TYPE_DUAL:
                txtRType.setText("BR/EDR/LE");
                break;
            case BluetoothDevice.DEVICE_TYPE_LE:
                txtRType.setText("LE-only");
                break;
            case BluetoothDevice.DEVICE_TYPE_UNKNOWN:
                txtRType.setText("Unknown");
                break;
        }

        mAdapterConnect = new AdapterConnect(getContext(), mConnectDatas);
        rvMessage.setAdapter(mAdapterConnect);
    }

    /**
     * @desc byte[] to string
     */
    public String ByteArrToHexString(byte[] msgArr){
        if(msgArr==null || msgArr.length==0) { return ""; }

        String message = "";
        message = new String(msgArr);
        message = message.replaceAll("\r", "\\\\r").replaceAll("\n", "\\\\n");
        return message;
    }

    /**
     * @desc 메시지 지우기
     */
    public void ClearMessage(){
        mConnectDatas.clear();
        mAdapterConnect.notifyDataSetChanged();
    }

    /**
     * @desc HEX 유효성 체크
     */
    public boolean CheckHexValidation(){
        String str = editMessage.getText().toString();
        str = str.replaceAll(" |\t", "");
        return str.matches("^([0-9a-fA-F]{2})+$");
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btnBack:
                onBackPressed();
                break;
            case R.id.btnSend:
                SendMessage();
                break;
            case R.id.btnClear:
                ClearMessage();
                break;
            case R.id.btnAttach:
                ((MainActivity) getActivity()).callSAF();
                break;
        }
    }

    /**
     * @desc 연결화면 종료
     */
    public void CloseConnect(){
        mProgressDialog.dismiss();
        ((MainActivity) getActivity()).getBTService().disconnectSocket();
        Toast.makeText(getContext(), "연결이 종료되었습니다.", Toast.LENGTH_SHORT).show();

        Bundle bundle = new Bundle();
        bundle.putString("CLOSE", "Y");
        getParentFragmentManager().setFragmentResult("CONNECT", bundle);
        ((MainActivity) getActivity()).popStack();
    }

    // 뒤로가기
    public void onBackPressed() {
        mProgressDialog.show();
        ((MainActivity) getActivity()).getBTService().closeSocket();
    }


    // 읽기
    public void readedMessage(String message) {
        ConnectData data = new ConnectData(YOUR_TEXT, DATA_SEQ, message);
        DATA_SEQ++;
        mAdapterConnect.addItem(data);
        mAdapterConnect.notifyDataSetChanged();
    }

    // 파일전송 읽기
    public void readedFile(String filename, long filesize) {
        ConnectData data = new ConnectData(YOUR_FILE, DATA_SEQ, filename, filesize);
        DATA_SEQ++;
        mAdapterConnect.addItem(data);
        mAdapterConnect.notifyDataSetChanged();
    }

    /**
     * 보낸 메시지
     */
    public void writedMessage(int seq, String message) {
        editMessage.setText("");
        ConnectData data = new ConnectData(MY_TEXT, seq, message);
        mAdapterConnect.addItem(data);
        mAdapterConnect.notifyDataSetChanged();
    }

    /**
     * 보낸 파일
     */
    public void writedFile(int seq, String filename, long filesize) {
        editMessage.setText("");
        ConnectData data = new ConnectData(MY_FILE, seq, filename, filesize);
        mAdapterConnect.addItem(data);
        mAdapterConnect.notifyDataSetChanged();
    }

    /**
     * 메시지 보내기
     */
    public void SendMessage(){
        String message = editMessage.getText().toString();
        ((MainActivity) getActivity()).getBTService().send(BTCConstants.DATA_SEQ, message);
        DATA_SEQ++;
    }

    /**
     * 파일 보내기
     */
    public void SendFile(List<Uri> uris) {
        for(Uri uri : uris) {
            String filename = Util.getFilenameFromUri(getContext(), uri);
            long filesize = Util.getFilesizeFromUri(getContext(), uri);
            ((MainActivity) getActivity()).getBTService().send(BTCConstants.DATA_SEQ, uri, filename, filesize);
            DATA_SEQ++;
        }
    }

    /**
     * 데이터 진행시작
     */
    public void dataStart(int seq) {
        for(ConnectData item : mConnectDatas) {
            if(item.getSeq() == seq) {
                item.setState("시작");
                break;
            }
        }
        mAdapterConnect.notifyDataSetChanged();
    }

    /**
     * 데이터 진행
     */
    public void dataProgress(int seq, long progress) {
        for(ConnectData item : mConnectDatas) {
            if(item.getSeq() == seq) {
                item.setProgress(progress);
                long p = item.getProgress();
                long t = item.getFilesize();

                String mode = (item.getDataType() == MY_TEXT || item.getDataType() == MY_FILE) ? "전송" : "수신";
                String strP = NumberFormat.getInstance(Locale.KOREA).format(p);
                String strT = NumberFormat.getInstance(Locale.KOREA).format(t);
                double percent = Math.round((double) p / t * 100) / 100.0;
                item.setState(mode + " " + strP + " / " + strT + " (" + percent + "%)");
                break;
            }
        }
        mAdapterConnect.notifyDataSetChanged();
    }

    /**
     * 데이터 진행종료
     */
    public void dataEnd(int seq) {
        for(ConnectData item : mConnectDatas) {
            if(item.getSeq() == seq) {
                String state = (item.getDataType() == MY_TEXT || item.getDataType() == MY_FILE) ? "전송 완료" : "수신 완료";
                item.setState(state);
                break;
            }
        }
        mAdapterConnect.notifyDataSetChanged();
    }
}
