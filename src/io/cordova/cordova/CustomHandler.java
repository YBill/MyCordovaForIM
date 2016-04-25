package io.cordova.cordova;

import android.app.Activity;
import android.os.Handler;
import android.os.Message;

import java.lang.ref.WeakReference;

/**
 * 这里面处理Activity于handler线程不同步而引起的内存泄露
 * Created by 卫彪 on 2016/4/25.
 */
public abstract class CustomHandler extends Handler{

    private WeakReference<Activity> weak;

    public CustomHandler(Activity activity){
        weak = new WeakReference<Activity>(activity);
    }

    @Override
    public void handleMessage(Message msg) {
        super.handleMessage(msg);
        if(weak != null && weak.get() != null){
            astHandleMassage(msg);
        }
    }

    public abstract void astHandleMassage(Message msg);

}
