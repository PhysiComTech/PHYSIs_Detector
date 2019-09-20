package com.physicomtech.kit.physis_psmdetector.dialog;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.physicomtech.kit.physis_psmdetector.R;

public class LoadingDialog {
    private static Dialog dialog = null;

    public static void show(Context context, String msg) {
        if(dialog == null) {
            try {
                LayoutInflater inflator = (LayoutInflater) context
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                assert inflator != null;
                @SuppressLint("InflateParams")
                final View view = inflator.inflate(R.layout.dialog_loading, null);
                dialog = new Dialog(context, R.style.LoadingTheme);
                if(msg != null) {
                    final TextView tvMsg = view.findViewById(R.id.tv_load_msg);
                    tvMsg.setText(msg);
                }
                dialog.setContentView(view);
                dialog.setCancelable(false);
                dialog.show();

            }catch(Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void dismiss() {
        if(dialog != null) {
            try {
                dialog.dismiss();
            }catch(Exception e) {
                e.printStackTrace();
            }finally{
                dialog = null;
            }
        }
    }
}
