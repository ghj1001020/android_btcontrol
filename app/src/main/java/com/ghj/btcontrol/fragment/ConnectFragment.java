package com.ghj.btcontrol.fragment;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothDevice;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.Html;
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
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.ghj.btcontrol.BaseFragmentActivity;
import com.ghj.btcontrol.ConnectActivity;
import com.ghj.btcontrol.MainActivity;
import com.ghj.btcontrol.R;
import com.ghj.btcontrol.bluetooth.BluetoothService;
import com.ghj.btcontrol.util.PermissionUtil;

public class ConnectFragment extends Fragment {

    ImageButton btnSend, btnBack;
    TextView txtRName, txtRMAC, txtRType, txtMessage;
    EditText editMessage;
    ProgressDialog mProgressDialog;
    Button btnClear;
    ScrollView scrMessage;
    LinearLayout boxEdit;


    BluetoothDevice mRemoteDevice;
    String mRemoteName;


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

        //ui
        btnBack = (ImageButton)view.findViewById(R.id.btnBack);
        btnBack.setOnClickListener(mOnClickListener);
        btnClear = (Button)view.findViewById(R.id.btnClear);
        btnClear.setOnClickListener(mOnClickListener);
        txtRName = view.findViewById(R.id.txtRName);
        txtRMAC = view.findViewById(R.id.txtRMAC);
        txtRType = view.findViewById(R.id.txtRType);
        mProgressDialog = new ProgressDialog(getContext(), ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setMessage("연결종료 중입니다...");
        mProgressDialog.setCancelable(false);
        editMessage = view.findViewById(R.id.editMessage);
        btnSend = view.findViewById(R.id.btnSend);
        btnSend.setOnClickListener(mOnClickListener);
        txtMessage = view.findViewById(R.id.txtMessage);
        boxEdit = view.findViewById(R.id.boxEdit);
        scrMessage = view.findViewById(R.id.scrMessage);

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

        // 연결디바이스
        mRemoteDevice = ((MainActivity) getActivity()).getBTService().getRemoteDevice();

        initData();
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
    }


    /**
     * @desc 메시지 보내기
     */
    public void SendMessage(){
        String message = editMessage.getText().toString();
        ((MainActivity) getActivity()).getBTService().sendString(StringToByteArr(message));
    }

    /**
     * @desc string to byte[]
     */
    public byte[] StringToByteArr(String message){
        byte[] msgArr = message.getBytes();
        return msgArr;
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
        txtMessage.setText("");
    }

    /**
     * @desc 연결화면 종료
     */
    public void CloseConnectActvitiy(){
        ((MainActivity) getActivity()).getBTService().closeSocket();
        mProgressDialog.dismiss();
        Toast.makeText(getContext(), "연결이 종료되었습니다.", Toast.LENGTH_SHORT).show();
//        finish();
    }

    /**
     * @desc HEX 유효성 체크
     */
    public boolean CheckHexValidation(){
        String str = editMessage.getText().toString();
        str = str.replaceAll(" |\t", "");
        return str.matches("^([0-9a-fA-F]{2})+$");
    }

    /**
     * @desc 클릭 리스너
     */
    View.OnClickListener mOnClickListener = new View.OnClickListener() {
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
            }
        }
    };

    // 뒤로가기 연결끊기
    public void onBackPressed() {
        mProgressDialog.show();
        ((MainActivity) getActivity()).getBTService().closeSocket();
    }

    // 읽기
    public void readedMessage(byte[] msgArr) {
        String message = ByteArrToHexString(msgArr);
        if(Build.VERSION.SDK_INT >= 24){
            txtMessage.append(Html.fromHtml("<font color=#00A2D5>"+mRemoteName+" >></font><br/>", Html.FROM_HTML_MODE_COMPACT));
            txtMessage.append(message+"\n");
        }else{
            txtMessage.append(Html.fromHtml("<font color=#00A2D5>"+mRemoteName+" >></font><br/>"));
            txtMessage.append(message+"\n");
        }
    }

    // 쓰기
    public void writedMessage(byte[] msgArr) {
        String message = ByteArrToHexString(msgArr);
        editMessage.setText("");
        if(Build.VERSION.SDK_INT >= 24) {
            txtMessage.append(Html.fromHtml("<font color=#FF4848>나 >></font><br/>", Html.FROM_HTML_MODE_COMPACT));
            txtMessage.append(message+"\n");
        }else{
            txtMessage.append(Html.fromHtml("<font color=#FF4848>나 >></font><br/>"));
            txtMessage.append(message+"\n");
        }
    }
}
