package com.physicomtech.kit.physis_psmdetector.customize;

import android.os.SystemClock;
import android.view.View;

public abstract class OnSingleClickListener implements View.OnClickListener{

    private static final long MIN_CLICK_INTERVAL = 500;
    private long lastClickTime;

    public abstract void onSingleClick(View v);

    @Override
    public void onClick(View v) {
        long currentClickTime = SystemClock.uptimeMillis();
        long elapsedTime = currentClickTime - lastClickTime;
        lastClickTime = currentClickTime;
        if(elapsedTime <= MIN_CLICK_INTERVAL){
            return;
        }
        onSingleClick(v);
    }
}
