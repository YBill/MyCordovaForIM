package io.cordova.cordova;

/**
 * 游云接口回调
 * Created by 卫彪 on 2016/4/26.
 */
public interface ChatApiCallback {

    /**
     * 成功
     * @param result
     */
    void onSuccess(String result);

    /**
     * 错误
     * @param error
     */
    void onError(String error);
}
