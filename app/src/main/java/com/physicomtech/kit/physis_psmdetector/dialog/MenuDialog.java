package com.physicomtech.kit.physis_psmdetector.dialog;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;

/**
 * Created by Heo on 2018-02-09.
 */

public class MenuDialog {

    private AlertDialog alertDialog = null;

    public void show(Context context, String title, View view)
    {
        dismiss();

        AlertDialog.Builder dialogBuilder =  new AlertDialog.Builder(context);
        dialogBuilder.setTitle(title).setView(view)
                .setPositiveButton(android.R.string.cancel, null)
                .setCancelable(false).create();
        alertDialog = dialogBuilder.show();
    }

    public void show(Context context, String title, View view, DialogInterface.OnClickListener onPositiveButtonClickListener)
    {
        dismiss();

        AlertDialog.Builder dialogBuilder =  new AlertDialog.Builder(context);
        dialogBuilder.setTitle(title).setView(view)
                .setPositiveButton(android.R.string.ok, onPositiveButtonClickListener)
                .setNegativeButton(android.R.string.cancel, null)
                .setCancelable(false).create();
        alertDialog = dialogBuilder.show();
    }

    public void show(Context context, int title, View view, int btnText, View.OnClickListener onClickListener)
    {
        dismiss();
        AlertDialog.Builder dialogBuilder =  new AlertDialog.Builder(context);
        dialogBuilder.setTitle(title).setView(view)
                .setNegativeButton(btnText, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .setPositiveButton(android.R.string.cancel, null)
                .setCancelable(false);
        alertDialog = dialogBuilder.create();
        alertDialog.show();
        // no dismiss
        alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(onClickListener);
    }


    public void dismiss(){
        if(alertDialog != null){
            alertDialog.dismiss();
            alertDialog = null;
        }
    }
}
