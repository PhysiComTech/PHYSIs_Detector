package com.physicomtech.kit.physis_psmdetector.wifi;

import android.net.wifi.ScanResult;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.physicomtech.kit.physis_psmdetector.R;
import com.physicomtech.kit.physis_psmdetector.customize.OnSingleClickListener;

import java.util.ArrayList;
import java.util.List;

public class WiFiAdapter extends RecyclerView.Adapter<WiFiHolder> {

    private List<ScanResult> scanResults = new ArrayList<>();

    public interface OnSelectedWiFiListener {
        void onSelectedWiFi(ScanResult scanResult);
    }

    private OnSelectedWiFiListener onSelectedWiFiListener;

    public void setOnSelectedWiFiListener(OnSelectedWiFiListener listener){
        onSelectedWiFiListener = listener;
    }

    @NonNull
    @Override
    public WiFiHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        View view = inflater.inflate(R.layout.rc_item_wifi, viewGroup, false);
        return new WiFiHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WiFiHolder wiFiHolder, int i) {
        final ScanResult scanResult = scanResults.get(i);

        wiFiHolder.tvSSID.setText(scanResult.SSID);

        if(checkCapabilities(scanResult.capabilities))
            wiFiHolder.ivSecret.setVisibility(View.VISIBLE);
        else
            wiFiHolder.ivSecret.setVisibility(View.INVISIBLE);

        int rssi = scanResult.level;
        if(rssi > -50){
            wiFiHolder.ivRSSI.setImageResource(R.drawable.ic_wifi_4);
        }else if(rssi > -60){
            wiFiHolder.ivRSSI.setImageResource(R.drawable.ic_wifi_3);
        }else if(rssi > -70){
            wiFiHolder.ivRSSI.setImageResource(R.drawable.ic_wifi_2);
        }else {
            wiFiHolder.ivRSSI.setImageResource(R.drawable.ic_wifi_1);
        }

        wiFiHolder.rlItem.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                if(onSelectedWiFiListener != null)
                    onSelectedWiFiListener.onSelectedWiFi(scanResult);
            }
        });
    }

    @Override
    public int getItemCount() {
        return scanResults.size();
    }

    public void initItems(){
        scanResults.clear();
        notifyDataSetChanged();
    }

    public void setItems(List<ScanResult> items){
        try{
            this.scanResults.clear();
            for(ScanResult result : items){
                if(result.SSID != null && result.SSID.length() != 0 && result.level >= - 80){
                    this.scanResults.add(result);
                }
            }
        }catch (Exception e){
            e.getStackTrace();
        }
        notifyDataSetChanged();
    }

    private boolean checkCapabilities(String capabilities){
        return capabilities.contains("WEP") || capabilities.contains("WPA");
    }
}
