package com.tianxiaolei.testlibrary;

import android.content.Context;
import android.widget.Toast;

/**
 * Created by tianxiaolei on 2017/6/26.
 */

public class ToastUtils {
    public static void Toast(Context context,String string){
        Toast.makeText(context, string, Toast.LENGTH_LONG).show();
    }
}
