package com.physicomtech.kit.physis_psmdetector;

import android.net.wifi.ScanResult;
import android.os.CountDownTimer;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.physicomtech.kit.physis_psmdetector.customize.OnSingleClickListener;
import com.physicomtech.kit.physis_psmdetector.dialog.LoadingDialog;
import com.physicomtech.kit.physis_psmdetector.wifi.WIFIManager;
import com.physicomtech.kit.physis_psmdetector.wifi.WiFiAdapter;
import com.physicomtech.kit.physislibrary.PHYSIsBLEActivity;

import java.util.List;

public class SetupActivity extends PHYSIsBLEActivity {

    private static final String TAG = "SetupActivity";

    Button btnScan, btnSetup;
    EditText etWiFiSSID, etWiFiPwd;
    SwipeRefreshLayout srlWiFiList;
    RecyclerView rcWiFiList;

    private WIFIManager wifiManager;
    private WiFiAdapter wifiAdapter;
    private ScanResult selectedScanResult;
    private CountDownTimer countDownTimer;

    private static final long SETUP_TIMEOUT = 15000;
    private static final long SETUP_TIMEOUT_TICK = 1000;
    private static final String SET_MSG_STX = "$";
    private static final String SET_WIFI_SSID = "WI";
    private static final String SET_WIFI_PWD = "WP";
    private static final String SET_MSG_ETX = "#";

    private String serialNumber;
    private boolean isSetupConnected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        setFinishOnTouchOutside(false);
        init();

        wifiManager.registerReceiver();
        scanWiFi();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try{
            wifiManager.unregisterReceiver();
            countDownTimer.cancel();
            countDownTimer = null;
        } catch (Exception ep) {
            ep.getStackTrace();
        }
    }


    @Override
    protected void onBLEConnectedStatus(int result) {
        super.onBLEConnectedStatus(result);
        if(result == CONNECTED){
            String wifiSSID = SET_MSG_STX + SET_WIFI_SSID + etWiFiSSID.getText() + SET_MSG_ETX;
            sendMessage(wifiSSID);

            countDownTimer.start();
            isSetupConnected = true;
        }else{
            countDownTimer.cancel();
            LoadingDialog.dismiss();
            if(!isSetupConnected){
                Toast.makeText(getApplicationContext(), getConnectErrorMsg(result), Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onBLEReceiveMsg(String msg) {
        super.onBLEReceiveMsg(msg);
        countDownTimer.cancel();
        if(!msg.startsWith(SET_MSG_STX) || !msg.endsWith(SET_MSG_ETX)){
            Toast.makeText(getApplicationContext(), "메시지 포맷 오류가 발생하였습니다. 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
            LoadingDialog.dismiss();
            disconnectDevice();
            return;
        }

        if(msg.startsWith(SET_MSG_STX + SET_WIFI_PWD)){
            String wifiPwd = SET_MSG_STX + SET_WIFI_PWD + etWiFiPwd.getText() + SET_MSG_ETX;
            sendMessage(wifiPwd);
            countDownTimer.start();
        }else{
            LoadingDialog.dismiss();
            disconnectDevice();
            boolean setupResult = msg.equals(SET_MSG_STX + 1 + SET_MSG_ETX);
            Toast.makeText(getApplicationContext(),getSetupResultMsg(setupResult), Toast.LENGTH_SHORT).show();
            if(setupResult)
                finish();
        }
    }


    /*
            Handler
     */
    @Override
    protected void handleMessage(Message msg) {
        super.handleMessage(msg);
        switch (msg.what) {
            case WIFIManager.WIFI_SCAN_RESULT:
                wifiAdapter.setItems((List<ScanResult>) msg.obj);
                srlWiFiList.setRefreshing(false);
                break;
        }
    }

    /*
            Event Listener
     */
    final OnSingleClickListener onSingleClickListener = new OnSingleClickListener() {
        @Override
        public void onSingleClick(View v) {
            if(v.getId() == R.id.btn_wifi_scan){
                scanWiFi();
            }else{
                if(etWiFiSSID.getText().length() == 0){
                    Toast.makeText(getApplicationContext(), "PHYSIs Kit와 연결할 WiFi를 선택하세요.", Toast.LENGTH_SHORT).show();
                    return;
                }

                if(etWiFiSSID.getText().length() > 16){
                    Toast.makeText(getApplicationContext(), "WiFi 명칭이 너무 깁니다. (최대 길이 : 16).", Toast.LENGTH_SHORT).show();
                    return;
                }

                isSetupConnected = false;
                connectDevice(serialNumber);
                LoadingDialog.show(SetupActivity.this, "PHYSIs WiFi Setup..");
            }
        }
    };

    /*
            Helper Methods
     */
    private String getSetupResultMsg(boolean state){
        if(state){
            return "PHYSIs Kit의 WiFi 설정이 완료되었습니다.";
        }else{
            return "PHYSIs Kit의 WiFi 설정에 실패하였습니다.\n입력 정보를 확인하고 다시 시도해주세요.";
        }
    }

    private String getConnectErrorMsg(int state){
        if(state == DISCONNECTED){
            return "Physi Kit 연결이 실패/종료되었습니다.";
        }else{
            return "연결할 Physi Kit가 존재하지 않습니다.";
        }
    }

    private void scanWiFi(){
        wifiManager.startScan();
        srlWiFiList.setRefreshing(true);
        wifiAdapter.initItems();
    }

    private void init() {
        wifiManager = new WIFIManager(getApplicationContext(), physisHandle);
        countDownTimer = new CountDownTimer(SETUP_TIMEOUT, SETUP_TIMEOUT_TICK) {
            @Override
            public void onTick(long millisUntilFinished) {
                Log.e(TAG, "@ Setup Timeout Tick..");
            }

            @Override
            public void onFinish() {
                Log.e(TAG, "@ CountDown Finished : Setup Timeout..");
                LoadingDialog.dismiss();
                disconnectDevice();
                Toast.makeText(getApplicationContext(), "PHYSIs Kit 전원 상태 및 WiFi 비밀번호를 확인하시고 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
            }
        };

        etWiFiSSID = findViewById(R.id.et_wifi_ssid);
        etWiFiPwd = findViewById(R.id.et_wifi_pwd);

        btnScan = findViewById(R.id.btn_wifi_scan);
        btnSetup = findViewById(R.id.btn_wifi_setup);
        btnScan.setOnClickListener(onSingleClickListener);
        btnSetup.setOnClickListener(onSingleClickListener);

        rcWiFiList = findViewById(R.id.rc_wifi_list);
        // Set Recycler Division Line
        DividerItemDecoration decoration
                = new DividerItemDecoration(getApplicationContext(), LinearLayoutManager.VERTICAL);
        decoration.setDrawable(getApplicationContext().getResources().getDrawable(R.drawable.rc_item_division_line));
        rcWiFiList.addItemDecoration(decoration);
        // Set Layout Manager
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(SetupActivity.this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        linearLayoutManager.setItemPrefetchEnabled(true);
        rcWiFiList.setLayoutManager(linearLayoutManager);
        // Set Adapter
        rcWiFiList.setAdapter(wifiAdapter = new WiFiAdapter());
        wifiAdapter.setOnSelectedWiFiListener(new WiFiAdapter.OnSelectedWiFiListener() {
            @Override
            public void onSelectedWiFi(ScanResult scanResult) {
                selectedScanResult = scanResult;
                etWiFiSSID.setText(selectedScanResult.SSID);
            }
        });

        srlWiFiList = findViewById(R.id.srl_wifi_list);
        srlWiFiList.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                scanWiFi();
            }
        });

        serialNumber = getIntent().getStringExtra("SERIALNUMBER");
    }

}
