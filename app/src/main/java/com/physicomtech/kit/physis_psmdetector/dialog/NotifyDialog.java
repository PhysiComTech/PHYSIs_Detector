package com.physicomtech.kit.physis_psmdetector.dialog;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;

/**
 * Created by Heo on 2018-02-09.
 */

public class NotifyDialog {

    public void show(Context context, String title, String message,
                     String btnText, DialogInterface.OnClickListener clickListener)
    {
        new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(btnText, clickListener)
                .setCancelable(false).create().show();
    }

    public void show(Context context, int title, int message,
                     String btnText, DialogInterface.OnClickListener clickListener)
    {
        new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(btnText, clickListener)
                .setCancelable(false).create().show();
    }

    public void show(Context context, String title, View view)
    {
        new AlertDialog.Builder(context)
                .setTitle(title)
                .setView(view)
                .setPositiveButton(android.R.string.cancel, null)
                .setCancelable(false).create().show();
    }
}
