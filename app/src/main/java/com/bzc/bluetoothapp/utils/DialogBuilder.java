package com.bzc.bluetoothapp.utils;

import android.app.ProgressDialog;
import android.content.Context;

public class DialogBuilder {
    public static ProgressDialog buildProgressDialog(Context context, String title,
                                                     String msg) {
        ProgressDialog result = new ProgressDialog(context);

        result.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        result.setTitle(title);
        result.setMessage(msg);
        result.setIndeterminate(false);
        result.setCanceledOnTouchOutside(false);
        result.setCancelable(true);

        return result;
    }
}
