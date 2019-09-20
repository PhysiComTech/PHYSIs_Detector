package com.physicomtech.kit.physis_psmdetector.wifi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.LocationManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;
import android.util.Log;

import java.util.List;

/**
 * Created by Heo on 2017-10-07.
 */

public class WIFIManager {

    private static final String TAG = "WIFIManager";

    public static final int WIFI_SCAN_RESULT = 11;
    public static final int WIFI_STATE_CONNECT = 12;
    public static final int WIFI_STATE_DISCONNECT = 13;
    public static final int WIFI_ENABLE = 14;
    public static final int WIFI_DISABLE = 15;
    public static final int WIFI_ERROR_AUTHENTICATING = 16;

    private Context context = null;
    private WifiManager wifiManager = null;
    private Handler handler = null;
    private boolean isRegisterReceiver = false;


    public WIFIManager(Context context, Handler handler){
        this.context = context;
        this.handler = handler;
        wifiManager = (WifiManager)context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
    }

    public static boolean getLocationProviderStatus(Context context) {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        return locationManager != null && locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }


    public boolean getState(){
        return wifiManager.isWifiEnabled();
    }


    public boolean setEnable(boolean enable){
        return wifiManager.setWifiEnabled(enable);
    }


    public void startScan(){
        wifiManager.startScan();
    }

    private IntentFilter getIntentFilter(){
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION); // 와이파이상태
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION); // AP 리스트 검색
        intentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION); //와이파이 활성화
        intentFilter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION); // AP가 검색이 되면 이벤트가 들어옮

//        intentFilter.addAction(WifiManager.NETWORK_IDS_CHANGED_ACTION);
//        intentFilter.addAction(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION);
//        intentFilter.addAction(WifiManager.EXTRA_SUPPLICANT_ERROR);
//        intentFilter.addAction(WifiManager.RSSI_CHANGED_ACTION); // 안테나 감도 변경
        return intentFilter;
    }


    public void registerReceiver(){
        if(isRegisterReceiver)
            return;
        context.registerReceiver(wifiActionReceiver, getIntentFilter());
        isRegisterReceiver = true;
    }

    public void unregisterReceiver(){
        try{
            Log.e(TAG, "@ WiFi UnregisterReceiver");
            if(!isRegisterReceiver)
                return;
            context.unregisterReceiver(wifiActionReceiver);
            isRegisterReceiver = false;
        }catch (IllegalArgumentException e){
            e.getStackTrace();
        }
    }

    private BroadcastReceiver wifiActionReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            assert action != null;
            switch (action) {
                case WifiManager.SCAN_RESULTS_AVAILABLE_ACTION:
                    final List<ScanResult> scanResults = wifiManager.getScanResults();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            handler.obtainMessage(WIFI_SCAN_RESULT, scanResults).sendToTarget();
                        }
                    }, 1000);
                    break;
                case WifiManager.NETWORK_STATE_CHANGED_ACTION:
                    NetworkInfo netInfo = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                    if (netInfo.getState() == NetworkInfo.State.CONNECTED && netInfo.isConnected()) {
                        handler.obtainMessage(WIFI_STATE_CONNECT,
                                wifiManager.getConnectionInfo().getSSID().replace("\"", "")).sendToTarget();
                    } else if (netInfo.getState() == NetworkInfo.State.DISCONNECTED) {
                        handler.obtainMessage(WIFI_STATE_DISCONNECT,
                                wifiManager.getConnectionInfo().getSSID().replace("\"", "")).sendToTarget();
                    }
                    break;
                case WifiManager.WIFI_STATE_CHANGED_ACTION:
                    int state = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, -1);
                    if (state == WifiManager.WIFI_STATE_ENABLED) {
                        handler.obtainMessage(WIFI_ENABLE).sendToTarget();
                    } else if (state == WifiManager.WIFI_STATE_DISABLED) {
                        handler.obtainMessage(WIFI_DISABLE).sendToTarget();
                    }
                    break;
                case WifiManager.SUPPLICANT_STATE_CHANGED_ACTION:
                    int error = intent.getIntExtra(WifiManager.EXTRA_SUPPLICANT_ERROR, -1);
                    if (error == WifiManager.ERROR_AUTHENTICATING) {
                        // AUTHENTICATING ERROR
                        handler.obtainMessage(WIFI_ERROR_AUTHENTICATING).sendToTarget();
                    }
//                [note] 에러 발생 원인 확인 필요
//                WifiManager.WPS_OVERLAP_ERROR
                    break;
            }
        }
    };


    public int getConnectingNetworkId(String ssid, String pwd, String capablities){
        Log.e(TAG, "# Connecting Wifi - " + ssid + " :: " + pwd + " :: " + capablities);
        WifiConfiguration wfc = new WifiConfiguration();
        wfc.SSID = "\"".concat( ssid ).concat("\"");
        wfc.status = WifiConfiguration.Status.DISABLED;
        wfc.priority = 40;

        if(capablities.contains("WEP")){
            wfc.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            wfc.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
            wfc.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
            wfc.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
            wfc.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
            wfc.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);

            wfc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
            wfc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
            wfc.wepKeys[0] = "\"".concat(pwd).concat("\"");
            wfc.wepTxKeyIndex = 0;
        }else if(capablities.contains("WPA") || capablities.contains("WPA2")) {
            //wfc.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
            wfc.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
            wfc.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
            wfc.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            wfc.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
            wfc.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
            wfc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            wfc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            wfc.preSharedKey = "\"".concat(pwd).concat("\"");
        }else {
            wfc.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            wfc.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
            wfc.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
            wfc.allowedAuthAlgorithms.clear();
            wfc.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);

            wfc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
            wfc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
            wfc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            wfc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
        }

        int nid = wifiManager.addNetwork(wfc);
        Log.e(TAG, "> Connecting Network ID : " + nid);
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            wifiManager.saveConfiguration();
        }
        return nid;
    }


    public int getNetworkId(String ssid){
        int nid = -1;
        for(WifiConfiguration wifiConfiguration : wifiManager.getConfiguredNetworks()){
//            Log.e(TAG, wifiConfiguration.toString());
            if(ssid.equals(wifiConfiguration.SSID.replace("\"",""))){
                nid = wifiConfiguration.networkId;
                break;
            }
        }
        Log.e(TAG, "# Get Network ID : " + ssid + " :: " + nid);
        return nid;
    }


    public int getConnectedNetworkID(){
        WifiInfo wifiinfo = wifiManager.getConnectionInfo();
        Log.e(TAG, "# Connected WIFI : " + wifiinfo.getSSID() + " :: " + wifiinfo.getNetworkId());
        return wifiinfo.getNetworkId();
    }

    public String getConnectedSSID(){
        WifiInfo wifiinfo = wifiManager.getConnectionInfo();
        Log.e(TAG, "# Connected WIFI : " + wifiinfo.getSSID() + " :: " + wifiinfo.getNetworkId());
        return wifiinfo.getSSID().replace("\"","");
    }


    /**
     *  마시멜로우 버전 이후부터 어플 내에서 생성(연결)한 WIFI가 아니면
     *  removeNetwork() 에러가 발생(False 리턴)
     *  [ 안드로이드 버그 ]
     */
    public boolean removeNetworkID(int networkID){
        if(networkID == -1)
            return false;

        boolean status = wifiManager.removeNetwork(networkID);

        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            wifiManager.saveConfiguration();
        }

        Log.e(TAG, "# Remove WiFi Status : " + status);
        return status;
    }


    public boolean connect(int networkID){
        if(networkID == -1)
            return false;

        boolean status = wifiManager.enableNetwork(networkID, true);
        Log.e(TAG, "# Connect Status : " + status);
        return status;
    }

}
