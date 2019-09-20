package com.physicomtech.kit.physis_psmdetector.helper;

import android.content.Context;
import android.content.SharedPreferences;

import com.physicomtech.kit.physis_psmdetector.R;

public class PHYSIsPreferences {

    private static final String PHYSIs_SERIAL_NUM = "PHYSIs_SERIAL_NUM";
    private SharedPreferences pref;

    public PHYSIsPreferences(Context context){
        pref = context.getSharedPreferences(context.getResources()
                .getString(R.string.appbar_scrolling_view_behavior), Context.MODE_PRIVATE);
    }

    public void setPhysisSerialNumber(String data){
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(PHYSIs_SERIAL_NUM, data);
        editor.apply();
    }

    public String getPhysisSerialNumber(){
        return pref.getString(PHYSIs_SERIAL_NUM, null);
    }
}
