package com.google.zxing.lite.view;

import android.os.Bundle;
import android.os.Handler;
import com.google.zxing.Result;

/**
 * Created by lsl on 2016/9/21.
 */

public interface IViewfinder {

    /**
     * 处理decode
     */
    void handleDecode(Result result, Bundle bundle);

    //public ViewfinderView getViewfinderView();

    Handler getHandler();
}
