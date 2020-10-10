package com.bzc.bluetoothapp.utils;

import android.content.Context;
import android.widget.Toast;

/**
 * 管理所有Toast 防止连续点击产生的消息滞留
 * @author BZC
 *
 */
public class ToastUtils {

    private static Toast mToast;

    public static void showToast(Context context, int strResId) {
        if (mToast != null) {
            mToast.cancel();
        }
        mToast = Toast.makeText(context, strResId, Toast.LENGTH_SHORT);
        mToast.show();
    }

    public static void showToast(Context context, String str) {
        if (mToast != null) {
            mToast.cancel();
        }
        mToast = Toast.makeText(context, str, Toast.LENGTH_SHORT);
        mToast.show();
    }

    public static void cancelToast() {
        if (mToast != null) {
            mToast.cancel();
        }
    }
}