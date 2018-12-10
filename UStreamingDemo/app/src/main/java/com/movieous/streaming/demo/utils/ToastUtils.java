package com.movieous.streaming.demo.utils;

import android.content.Context;
import android.widget.Toast;

public class ToastUtils {
    public static Toast mToast;

    public synchronized static void s(Context context, String msg) {
        initToast(context.getApplicationContext());
        mToast.cancel();
        mToast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }

    public synchronized static void l(Context context, String msg) {
        initToast(context.getApplicationContext());
        mToast.cancel();
        mToast.makeText(context, msg, Toast.LENGTH_LONG).show();
    }

    private synchronized static void initToast(Context context) {
        if (mToast == null) {
            mToast = new Toast(context);
        }
    }
}
