package com.physicomtech.kit.physis_psmdetector.wifi;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.physicomtech.kit.physis_psmdetector.R;

public class WiFiHolder extends RecyclerView.ViewHolder {

    ImageView ivSecret, ivRSSI;
    TextView tvSSID;
    RelativeLayout rlItem;

    public WiFiHolder(@NonNull View itemView) {
        super(itemView);

        ivSecret = itemView.findViewById(R.id.iv_wifi_secret);
        ivRSSI = itemView.findViewById(R.id.iv_wifi_rssi);
        tvSSID = itemView.findViewById(R.id.tv_wifi_ssid);
        rlItem = itemView.findViewById(R.id.rl_wifi_item);
    }
}
