package com.physicomtech.kit.physis_psmdetector;

import android.animation.AnimatorSet;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.physicomtech.kit.physis_psmdetector.customize.OnSingleClickListener;
import com.physicomtech.kit.physis_psmdetector.customize.SerialNumberView;
import com.physicomtech.kit.physis_psmdetector.dialog.LoadingDialog;
import com.physicomtech.kit.physis_psmdetector.helper.PHYSIsPreferences;
import com.physicomtech.kit.physis_psmdetector.wifi.WIFIManager;
import com.physicomtech.kit.physislibrary.PHYSIsMQTTActivity;

public class MonitoringActivity extends PHYSIsMQTTActivity {

    private static final String TAG = "SwitchActivity";

    SerialNumberView snvSetup;
    Button btnConnect, btnWiFiSetup, btnMonitoring;
    ImageView ivDetectPir, ivDetectSound, ivDetectHall;

    private static final String SUB_TOPIC = "Detector";

    private PHYSIsPreferences preferences;
    private String serialNumber = null;
    private boolean isConnected = false;
    private boolean isMonitoring = false;

    private ObjectAnimator pirAnimator, soundAnimator, hallAnimator;
    private String priState, soundState = "0", hallState = "1";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monitoring);

        init();
    }

    @Override
    protected void onMQTTConnectedStatus(boolean result) {
        super.onMQTTConnectedStatus(result);
        setConnectedResult(result);
    }

    @Override
    protected void onMQTTDisconnected() {
        super.onMQTTDisconnected();
        isConnected = false;
        btnConnect.setText("Connect");
        setMonitoring(isMonitoring = false);
    }

    @Override
    protected void onSubscribeListener(String serialNum, String topic, String data) {
        super.onSubscribeListener(serialNum, topic, data);
        if(serialNumber.equals(serialNum) && topic.equals(SUB_TOPIC)){
            showDetectedData(data);
        }
    }

    /*
            Event
     */
    final SerialNumberView.OnSetSerialNumberListener onSetSerialNumberListener = new SerialNumberView.OnSetSerialNumberListener() {
        @Override
        public void onSetSerialNumber(String serialNum) {
            preferences.setPhysisSerialNumber(serialNumber = serialNum);
            Log.e(TAG, "# Set Serial Number : " + serialNumber);
        }
    };

    final OnSingleClickListener onClickListener = new OnSingleClickListener() {
        @Override
        public void onSingleClick(View v) {
            switch (v.getId()){
                case R.id.btn_wifi_setup:
                    if(serialNumber == null){
                        Toast.makeText(getApplicationContext(), "PHYSIs Kit의 시리얼 넘버를 설정하세요.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if(!WIFIManager.getLocationProviderStatus(getApplicationContext())){
                        Toast.makeText(getApplicationContext(), "위치 상태를 활성화하고 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    startActivity(new Intent(MonitoringActivity.this, SetupActivity.class)
                            .putExtra("SERIALNUMBER", serialNumber));
                    break;
                case R.id.btn_connect:
                    if(serialNumber == null){
                        Toast.makeText(getApplicationContext(), "PHYSIs Kit의 시리얼 넘버를 설정하세요.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if(isConnected){
                        disconnectMQTT();
                    }else{
                        LoadingDialog.show(MonitoringActivity.this, "Connecting..");
                        connectMQTT();
                    }
                    break;
                case R.id.btn_monitoring:
                    if(!isConnected){
                        Toast.makeText(getApplicationContext(), "MQTT가 연결되지 않았습니다.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    setMonitoring(isMonitoring = !isMonitoring);
                    break;
            }
        }
    };

    /*
            Helper Method
     */
    private void showDetectedData(String data) {
        if(data == null || data.equals(""))
            return;

        Log.e(TAG, "Sensing State : " + data);
        String priData = String.valueOf(data.charAt(0));
        String soundData = String.valueOf(data.charAt(1));
        String hallData = String.valueOf(data.charAt(2));

        if(!priData.equals(priState)){
            priState = priData;
            if(priState.equals("1")){
                ivDetectPir.setImageResource(R.drawable.ic_pir_on);
                pirAnimator.start();
            }else{
                ivDetectPir.setImageResource(R.drawable.ic_pir_off);
                pirAnimator.end();
            }
        }

        if(!soundData.equals(soundState)){
            soundState = soundData;
            if(soundState.equals("1")){
                ivDetectSound.setImageResource(R.drawable.ic_sound_on);
                soundAnimator.start();
            }else{
                ivDetectSound.setImageResource(R.drawable.ic_sound_off);
                soundAnimator.end();
            }
        }

        if(!hallData.equals(hallState)){
            hallState = hallData;
            if(hallState.equals("1")){
                ivDetectHall.setImageResource(R.drawable.ic_lock_close);
                hallAnimator.end();
            }else{
                ivDetectHall.setImageResource(R.drawable.ic_lock_open);
                hallAnimator.start();
            }
        }
    }


    private void setMonitoring(boolean enable){
        if(enable){
            startSubscribe(serialNumber, SUB_TOPIC);
            btnMonitoring.setText("Stop Monitoring");
        }else{
            stopSubscribe(serialNumber, SUB_TOPIC);
            showDetectedData("001");
            btnMonitoring.setText("Start Monitoring");
        }
    }

    @SuppressLint("SetTextI18n")
    private void setConnectedResult(boolean state){
        LoadingDialog.dismiss();

        if(isConnected = state){
            btnConnect.setText("Disconnect");
        }

        String toastMsg;
        if(isConnected) {
            toastMsg = "PHYSIs MQTT Broker 연결에 성공하였습니다.";
        } else {
            toastMsg = "PHYSIs MQTT Broker 연결에 실패하였습니다.";
        }
        Toast.makeText(getApplicationContext(), toastMsg, Toast.LENGTH_SHORT).show();
    }


    @SuppressLint("WrongConstant")
    private void init() {
        preferences = new PHYSIsPreferences(getApplicationContext());
        serialNumber = preferences.getPhysisSerialNumber();

        snvSetup = findViewById(R.id.snv_setup);
        snvSetup.setSerialNumber(serialNumber);
        snvSetup.showEditView(serialNumber == null);
        snvSetup.setOnSetSerialNumberListener(onSetSerialNumberListener);

        btnConnect = findViewById(R.id.btn_connect);
        btnConnect.setOnClickListener(onClickListener);
        btnWiFiSetup = findViewById(R.id.btn_wifi_setup);
        btnWiFiSetup.setOnClickListener(onClickListener);
        btnMonitoring = findViewById(R.id.btn_monitoring);
        btnMonitoring.setOnClickListener(onClickListener);

        ivDetectPir = findViewById(R.id.iv_detect_pir);
        ivDetectSound = findViewById(R.id.iv_detect_sound);
        ivDetectHall = findViewById(R.id.iv_detect_hall);

        pirAnimator = ObjectAnimator.ofInt(ivDetectPir, "backgroundColor", Color.WHITE, Color.RED, Color.WHITE);
        pirAnimator.setDuration(250);
        pirAnimator.setEvaluator(new ArgbEvaluator());
        pirAnimator.setRepeatCount(Animation.INFINITE);
        pirAnimator.setRepeatMode(Animation.RESTART);

        soundAnimator = ObjectAnimator.ofInt(ivDetectSound, "backgroundColor", Color.WHITE, Color.RED, Color.WHITE);
        soundAnimator.setDuration(250);
        soundAnimator.setEvaluator(new ArgbEvaluator());
        soundAnimator.setRepeatCount(Animation.INFINITE);
        soundAnimator.setRepeatMode(Animation.RESTART);

        hallAnimator = ObjectAnimator.ofInt(ivDetectHall, "backgroundColor", Color.WHITE, Color.RED, Color.WHITE);
        hallAnimator.setDuration(250);
        hallAnimator.setEvaluator(new ArgbEvaluator());
        hallAnimator.setRepeatCount(Animation.INFINITE);
        hallAnimator.setRepeatMode(Animation.RESTART);
    }
}
