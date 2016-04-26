package io.cordova.cordova;

import android.content.Context;
import android.os.Handler;

/**
 * Created by 卫彪 on 2016/4/26.
 */
public class YouyunInstance extends YouyunChatApiImpl {

    private YouyunInstance() {
        // TODO
    }

    private static YouyunInstance youyunInstance;

    public static YouyunInstance getInstance() {
        if (null == youyunInstance) {
            synchronized (YouyunInstance.class) {
                if (null == youyunInstance) {
                    youyunInstance = new YouyunInstance();
                }
            }
        }
        return youyunInstance;
    }

    /**
     * 初始化SDK
     * @param context
     * @param handler
     */
    public void init(Context context, Handler handler){
        this.context = context;
        initReceiveThread(context, handler);
    }

    private WeimiMsgHandler weimiMsgHandler;
    private void initReceiveThread(Context context, Handler handler){
        if (weimiMsgHandler == null) {
            weimiMsgHandler = new WeimiMsgHandler(context, handler);
            Thread msgHandler = new Thread(weimiMsgHandler);
            msgHandler.start();
        }
    }


}
