package com.ghj.btcontrol;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothDevice;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.ghj.btcontrol.bluetooth.BluetoothService;

/**
 * Created by ghj on 2017. 3. 24..
 */
public class ConnectActivity extends AppCompatActivity {

    ImageButton btnSend, btnBack, btnSettings;
    TextView txtRName, txtRMAC, txtRType, txtRTStatus, txtMessage;
    EditText editMessage;
    ProgressDialog mProgressDialog;
    Button btnClear, btnIOMode, btnEndFlag, btnKeyboard, btnSave;
    ScrollView scrTooltip, scrMessage;
    LinearLayout boxEdit, boxKeyboard;
    Button btnKeyboardSet;
    View lineKeyboardSet;
    ImageView imgCheck;

    //다이얼로그
    View mIOModeView, mEndFlagView;
    AlertDialog mIOModeAlert, mEndFlagAlert;
    RadioGroup rgDlgIOModeIn, rgDlgIOModeOut, rgDlgEndFlag;
    String tagIOModeInput, tagIOModeOutput, tagEndFlag;


    BluetoothService mBTService;
    BluetoothDevice mRemoteDevice;
    String mRemoteName;

    InputMethodManager imm;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect);

        SetCustomActionBar();

        //ui
        txtRName = (TextView)findViewById(R.id.txtRName);
        txtRMAC = (TextView)findViewById(R.id.txtRMAC);
        txtRType = (TextView)findViewById(R.id.txtRType);
        txtRTStatus = (TextView)findViewById(R.id.txtRTStatus);
        mProgressDialog = new ProgressDialog(this, ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setMessage("연결종료 중입니다...");
        mProgressDialog.setCancelable(false);
        editMessage = (EditText)findViewById(R.id.editMessage);
        btnSend = (ImageButton)findViewById(R.id.btnSend);
        btnSend.setOnClickListener(mOnClickListener);
        txtMessage = (TextView)findViewById(R.id.txtMessage);
        scrTooltip = (ScrollView)findViewById(R.id.scrTooltip);
        btnIOMode = (Button)findViewById(R.id.btnIOMode);
        btnIOMode.setOnClickListener(mOnClickListener);
        btnEndFlag = (Button)findViewById(R.id.btnEndFlag);
        btnEndFlag.setOnClickListener(mOnClickListener);
        btnKeyboard = (Button)findViewById(R.id.btnKeyboard);
        btnKeyboard.setOnClickListener(mOnClickListener);
        btnSave = (Button)findViewById(R.id.btnSave);
        btnSave.setOnClickListener(mOnClickListener);
        boxEdit = (LinearLayout)findViewById(R.id.boxEdit);
        boxKeyboard = (LinearLayout)findViewById(R.id.boxKeyboard);
        scrMessage = (ScrollView)findViewById(R.id.scrMessage);
        btnKeyboardSet = (Button)findViewById(R.id.btnKeyboardSet);
        lineKeyboardSet = findViewById(R.id.lineKeyboardSet);
        imgCheck = (ImageView)findViewById(R.id.imgCheck);

        imm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);

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
                //유효성 체크
                if("1".equals(tagIOModeInput)){
                    if(CheckHexValidation()){
                        editMessage.setTextColor(Color.rgb(0,0,0));
                    }else{
                        editMessage.setTextColor(Color.RED);
                    }
                }
            }
        });

        mBTService = BluetoothService.getBluetoothService(this, mHandler);
        mRemoteDevice = mBTService.getRemoteDevice();

        initData();
        initDialog();

        //현재 설정상태
        if(Build.VERSION.SDK_INT >= 24) {
            txtMessage.setText(Html.fromHtml("<font color=#509500>입력 타입 : ASCII , 출력 타입 : ASCII , End Flag : (공백)</font><br/>", Html.FROM_HTML_MODE_COMPACT));
        }else{
            txtMessage.setText(Html.fromHtml("<font color=#509500>입력 타입 : ASCII , 출력 타입 : ASCII , End Flag : (공백)</font><br/>"));
        }
    }


    /**
     * @desc 커스텀 액션바 만들기
     */
    public void SetCustomActionBar(){
        ActionBar actionBar = getSupportActionBar();

        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(false);
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayUseLogoEnabled(false);

        //액션바 레이아웃
        View view = LayoutInflater.from(this).inflate(R.layout.activity_appbar_connect, null);
        actionBar.setCustomView(view, new ActionBar.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        //액션바 그림자없애기 (롤리팝부터 추가)
        actionBar.setElevation(0);
        //액션바 양쪽 공백 없애기
        Toolbar toolbar = (Toolbar)view.getParent();
        toolbar.setContentInsetsAbsolute(0,0);

        //event
        btnBack = (ImageButton)view.findViewById(R.id.btnBack);
        btnBack.setOnClickListener(mOnClickListener);
        btnClear = (Button)view.findViewById(R.id.btnClear);
        btnClear.setOnClickListener(mOnClickListener);
        btnSettings = (ImageButton)view.findViewById(R.id.btnSettings);
        btnSettings.setOnClickListener(mOnClickListener);
    }

    /**
     * @desc 데이터 설정
     */
    public void initData(){
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
     * @desc 다이얼로그 설정
     */
    public void initDialog(){
        tagIOModeInput = "0";
        tagIOModeOutput = "0";
        tagEndFlag = "0";

        LayoutInflater mInflater = getLayoutInflater();
        mIOModeView = mInflater.inflate(R.layout.dialog_iomode, null);
        mEndFlagView = mInflater.inflate(R.layout.dialog_endflag, null);
        Button btnDlgIOModeOk = (Button)mIOModeView.findViewById(R.id.btnDlgIOModeOk);
        btnDlgIOModeOk.setOnClickListener(mOnDialogClickListener);
        Button btnDlgEndFlagOk = (Button)mEndFlagView.findViewById(R.id.btnDlgEndFlagOk);
        btnDlgEndFlagOk.setOnClickListener(mOnDialogClickListener);

        AlertDialog.Builder iomodeBuilder = new AlertDialog.Builder(this);
        mIOModeAlert = iomodeBuilder.create();
        mIOModeAlert.setView(mIOModeView);
        rgDlgIOModeIn = (RadioGroup)mIOModeView.findViewById(R.id.rgDlgIOModeIn);
        rgDlgIOModeOut = (RadioGroup)mIOModeView.findViewById(R.id.rgDlgIOModeOut);
        rgDlgIOModeIn.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                RadioButton btnDlgIOModeInAscii = (RadioButton)mIOModeView.findViewById(R.id.btnDlgIOModeInAscii);
                RadioButton btnDlgIOModeInHex = (RadioButton)mIOModeView.findViewById(R.id.btnDlgIOModeInHex);

                btnDlgIOModeInAscii.setTextColor(Color.rgb(71,78,85));
                btnDlgIOModeInHex.setTextColor(Color.rgb(71,78,85));
                RadioButton rb = (RadioButton)mIOModeView.findViewById(checkedId);
                rb.setTextColor(Color.rgb(255,255,255));
            }
        });
        rgDlgIOModeOut.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                RadioButton btnDlgIOModeOutAscii = (RadioButton)mIOModeView.findViewById(R.id.btnDlgIOModeOutAscii);
                RadioButton btnDlgIOModeOutHex = (RadioButton)mIOModeView.findViewById(R.id.btnDlgIOModeOutHex);

                btnDlgIOModeOutAscii.setTextColor(Color.rgb(71,78,85));
                btnDlgIOModeOutHex.setTextColor(Color.rgb(71,78,85));
                RadioButton rb = (RadioButton)mIOModeView.findViewById(checkedId);
                rb.setTextColor(Color.rgb(255,255,255));
            }
        });

        AlertDialog.Builder endflagBuilder = new AlertDialog.Builder(this);
        mEndFlagAlert = endflagBuilder.create();
        mEndFlagAlert.setView(mEndFlagView);
        rgDlgEndFlag = (RadioGroup)mEndFlagView.findViewById(R.id.rgDlgEndFlag);
        rgDlgEndFlag.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                RadioButton btnDlgEndFlagRN = (RadioButton)mEndFlagView.findViewById(R.id.btnDlgEndFlagRN);
                RadioButton btnDlgEndFlagN = (RadioButton)mEndFlagView.findViewById(R.id.btnDlgEndFlagN);
                RadioButton btnDlgEndFlagOther = (RadioButton)mEndFlagView.findViewById(R.id.btnDlgEndFlagOther);

                btnDlgEndFlagRN.setTextColor(Color.rgb(71,78,85));
                btnDlgEndFlagN.setTextColor(Color.rgb(71,78,85));
                btnDlgEndFlagOther.setTextColor(Color.rgb(71,78,85));
                RadioButton rb = (RadioButton)mEndFlagView.findViewById(checkedId);
                rb.setTextColor(Color.rgb(255,255,255));
            }
        });
    }

    /**
     * @desc 뒤로가기 클릭
     */
    public void mOnBackClicked(){
        txtRTStatus.setText("연결종료 중");
        mProgressDialog.show();
        mBTService.closeSocket();
    }

    /**
     * @desc 메시지 보내기
     */
    public void SendMessage(){
        imm.hideSoftInputFromWindow(editMessage.getWindowToken(), 0);
        //유효성 체크
        if("1".equals(tagIOModeInput)){
            if(!CheckHexValidation()){
                Toast.makeText(this, "Invalid HEX String", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        String message = editMessage.getText().toString();
        if("1".equals(tagEndFlag)){
            if("0".equals(tagIOModeInput)){
                message += "\r\n";
            }else{
                message += "0D0A";
            }
        }else if("2".equals(tagEndFlag)){
            if("0".equals(tagIOModeInput)){
                message += "\n";
            }else{
                message += "0A";
            }
        }
        mBTService.sendString(StringToByteArr(message));
    }

    /**
     * @desc string to byte[]
     */
    public byte[] StringToByteArr(String message){


        byte[] msgArr = null;
        if("0".equals(tagIOModeInput)){
            msgArr = message.getBytes();
        }else if("1".equals(tagIOModeInput)){
            message = message.replaceAll(" |\t", "");

            msgArr = new byte[message.length() / 2];
            for(int i = 0; i < msgArr.length; i++){
                //string to 16진수 int
                msgArr[i] = (byte) Integer.parseInt(message.substring(2*i,2*i+2), 16);
            }
        }
        return msgArr;
    }

    /**
     * @desc byte[] to string
     */
    public String ByteArrToHexString(byte[] msgArr){
        if(msgArr==null || msgArr.length==0) { return ""; }

        String message = "";
        if ("0".equals(tagIOModeOutput)){
            message = new String(msgArr);
            message = message.replaceAll("\r", "\\\\r").replaceAll("\n", "\\\\n");
        }else if("1".equals(tagIOModeOutput)){
            StringBuffer strBuffer = new StringBuffer(msgArr.length * 2);
            String hexNumber;
            for(int x = 0; x < msgArr.length; x++){
                hexNumber = "0" + Integer.toHexString(0xff & msgArr[x]);
                strBuffer.append(hexNumber.substring(hexNumber.length() - 2));
                strBuffer.append(" ");
            }
            message = strBuffer.toString();
        }

        return message;
    }

    /**
     * @desc 메시지 지우기
     */
    public void ClearMessage(){
        txtMessage.setText("");
    }

    /**
     * @desc 설정화면 보이기/안보이기
     */
    public void ShowSettings(){
        if(scrTooltip.getVisibility() == View.GONE){
            scrTooltip.setVisibility(View.VISIBLE);
        }else{
            scrTooltip.setVisibility(View.GONE);
        }
    }

    /**
     * @desc 연결화면 종료
     */
    public void CloseConnectActvitiy(){
        if(mIOModeAlert.isShowing()){ mIOModeAlert.dismiss(); }
        if(mEndFlagAlert.isShowing()){ mEndFlagAlert.dismiss(); }

        mBTService.closeSocket();
        mProgressDialog.dismiss();
        Toast.makeText(ConnectActivity.this, "연결이 종료되었습니다.", Toast.LENGTH_SHORT).show();
        finish();
    }

    /**
     * @desc 설정 > IO Mode 클릭
     */
    public void OnClickIOMode(){
        if(scrTooltip.getVisibility() == View.VISIBLE){
            scrTooltip.setVisibility(View.GONE);
        }
        RadioButton rbInput = (RadioButton)rgDlgIOModeIn.findViewWithTag(tagIOModeInput);
        rbInput.setChecked(true);
        RadioButton rbOutput = (RadioButton)rgDlgIOModeOut.findViewWithTag(tagIOModeOutput);
        rbOutput.setChecked(true);
        mIOModeAlert.show();
    }

    /**
     * @desc 설정 > IO Mode 확인
     */
    public void OnClickIOModeOk(){
        RadioButton checkedIn = (RadioButton)mIOModeView.findViewById(rgDlgIOModeIn.getCheckedRadioButtonId());
        tagIOModeInput = (String)checkedIn.getTag();
        RadioButton checkedOut = (RadioButton)mIOModeView.findViewById(rgDlgIOModeOut.getCheckedRadioButtonId());
        tagIOModeOutput = (String)checkedOut.getTag();
        mIOModeAlert.dismiss();

        String input = "";
        if("0".equals(tagIOModeInput)){
            input = "ASCII";
            editMessage.setTextColor(Color.rgb(0,0,0));
        }else if("1".equals(tagIOModeInput)){
            input = "HEX";
            if(CheckHexValidation()){
                editMessage.setTextColor(Color.rgb(0,0,0));
            }else{
                editMessage.setTextColor(Color.RED);
            }
        }
        String output = "";
        if("0".equals(tagIOModeOutput)){
            output = "ASCII";
        }else if("1".equals(tagIOModeOutput)){
            output = "HEX";
        }
        if(Build.VERSION.SDK_INT >= 24) {
            txtMessage.append(Html.fromHtml("<font color=#509500>입력 타입 : "+input+" , 출력 타입 : "+output+"</font><br/>", Html.FROM_HTML_MODE_COMPACT));
        }else{
            txtMessage.append(Html.fromHtml("<font color=#509500>입력 타입 : "+input+" , 출력 타입 : "+output+"</font><br/>"));
        }
    }

    /**
     * @desc 설정 > End Flag 클릭
     */
    public void OnClickEndFlag(){
        if(scrTooltip.getVisibility() == View.VISIBLE){
            scrTooltip.setVisibility(View.GONE);
        }
        RadioButton rbEndFlag = (RadioButton)rgDlgEndFlag.findViewWithTag(tagEndFlag);
        rbEndFlag.setChecked(true);
        mEndFlagAlert.show();
    }

    /**
     * @desc 설정 > End Flag 확인
     */
    public void OnClickEndFlagOk(){
        RadioButton checkedEndFlag = (RadioButton)mEndFlagView.findViewById(rgDlgEndFlag.getCheckedRadioButtonId());
        tagEndFlag = (String)checkedEndFlag.getTag();
        mEndFlagAlert.dismiss();

        String endflag = "";
        if("0".equals(tagEndFlag)){
            endflag = "(공백)";
        }else if("1".equals(tagEndFlag)){
            endflag = "\\r\\n";
        }else if("2".equals(tagEndFlag)){
            endflag = "\\n";
        }
        if(Build.VERSION.SDK_INT >= 24) {
            txtMessage.append(Html.fromHtml("<font color=#509500>End Flag : "+endflag+"</font><br/>", Html.FROM_HTML_MODE_COMPACT));
        }else{
            txtMessage.append(Html.fromHtml("<font color=#509500>End Flag : "+endflag+"</font><br/>"));
        }
    }

    /**
     * @desc 설정 > 키보드 설정 클릭
     */
    public void OnClickKeyboard(){
        if(scrTooltip.getVisibility() == View.VISIBLE){
            scrTooltip.setVisibility(View.GONE);
        }

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        String mode = (String)btnKeyboard.getTag();
        //false -> keyboard mode 로
        if("false".equals(mode)){
            btnKeyboard.setTag("true");
            imgCheck.setVisibility(View.VISIBLE);
            btnKeyboardSet.setVisibility(View.VISIBLE);
            lineKeyboardSet.setVisibility(View.VISIBLE);

            params.addRule(RelativeLayout.ABOVE, R.id.boxKeyboard);
            params.addRule(RelativeLayout.BELOW, R.id.boxHeader);
            boxKeyboard.setVisibility(View.VISIBLE);
            boxEdit.setVisibility(View.GONE);
            //터치 리스너 설정
            for(int i=0; i<boxKeyboard.getChildCount(); i++){
                LinearLayout subKeyboard = (LinearLayout)boxKeyboard.getChildAt(i);
                for(int j=0; j<subKeyboard.getChildCount(); j++){
                    Button key = (Button)subKeyboard.getChildAt(j);
                    key.setOnTouchListener(mOnKeyboardTouchListener);
                }
            }
        }
        //keyboard mode -> false 에디트텍스트 로
        else if("true".equals(mode)){
            btnKeyboard.setTag("false");
            imgCheck.setVisibility(View.GONE);
            btnKeyboardSet.setVisibility(View.GONE);
            lineKeyboardSet.setVisibility(View.GONE);

            params.addRule(RelativeLayout.ABOVE, R.id.boxEdit);
            params.addRule(RelativeLayout.BELOW, R.id.boxHeader);
            boxEdit.setVisibility(View.VISIBLE);
            boxKeyboard.setVisibility(View.GONE);
            //터치 리스너 설정
            for(int i=0; i<boxKeyboard.getChildCount(); i++){
                LinearLayout subKeyboard = (LinearLayout)boxKeyboard.getChildAt(i);
                for(int j=0; j<subKeyboard.getChildCount(); j++){
                    Button key = (Button)subKeyboard.getChildAt(j);
                    key.setOnTouchListener(null);
                }
            }
        }
        scrMessage.setLayoutParams(params);
    }

    /**
     * @desc 키보드 모드시 키보드 터치 이벤트
     */
    View.OnTouchListener mOnKeyboardTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()){
                case MotionEvent.ACTION_DOWN:
                    Log.d("aaa", "ACTION_DOWN");
                    break;
                case MotionEvent.ACTION_UP:
                    Log.d("aaa", "ACTION_UP");
                    break;
            }
            return false;
        }
    };

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
                    mOnBackClicked();
                    break;
                case R.id.btnSend:
                    SendMessage();
                    break;
                case R.id.btnClear:
                    ClearMessage();
                    break;
                case R.id.btnSettings:
                    ShowSettings();
                    break;
                case R.id.btnIOMode:
                    OnClickIOMode();
                    break;
                case R.id.btnEndFlag:
                    OnClickEndFlag();
                    break;
                case R.id.btnKeyboard:
                    OnClickKeyboard();
                    break;
            }
        }
    };

    /**
     * @desc 다이얼로그 클릭 리스너
     */
    View.OnClickListener mOnDialogClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.btnDlgIOModeOk:
                    OnClickIOModeOk();
                    break;
                case R.id.btnDlgEndFlagOk:
                    OnClickEndFlagOk();
                    break;
            }
        }
    };

    /**
     * @desc 블루투스 콜백 핸들러
     */
    Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if(msg.what == BluetoothService.CONNECT_SUCCESS_MASTER_HANDLER_CODE){

            }
            else if(msg.what == BluetoothService.CONNECT_SUCCESS_CLIENT_HANDLER_CODE){

            }
            else if(msg.what == BluetoothService.DISCONNECTED_HANDLER_CODE){
                CloseConnectActvitiy();
            }
            else if(msg.what == BluetoothService.DISCONNECTED_ACL_HANDLER_CODE){
                CloseConnectActvitiy();
            }
            else if(msg.what == BluetoothService.READ_MESSAGE_HANDLER_CODE){
                Bundle data = msg.getData();
                byte[] msgArr = data.getByteArray("message");

                String message = ByteArrToHexString(msgArr);
                if(Build.VERSION.SDK_INT >= 24){
                    txtMessage.append(Html.fromHtml("<font color=#00A2D5>"+mRemoteName+" >></font><br/>", Html.FROM_HTML_MODE_COMPACT));
                    txtMessage.append(message+"\n");
                }else{
                    txtMessage.append(Html.fromHtml("<font color=#00A2D5>"+mRemoteName+" >></font><br/>"));
                    txtMessage.append(message+"\n");
                }
            }
            else if(msg.what == BluetoothService.WRITE_MESSAGE_HANDLER_CODE){
                Bundle data = msg.getData();
                byte[] msgArr = data.getByteArray("message");

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
    };


    @Override
    public void onBackPressed() {
        if(scrTooltip.getVisibility() == View.VISIBLE){
            scrTooltip.setVisibility(View.GONE);
        }else{
            mOnBackClicked();
        }
    }
}
