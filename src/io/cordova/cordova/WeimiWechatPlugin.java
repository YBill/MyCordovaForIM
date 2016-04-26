package io.cordova.cordova;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Message;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;

import matrix.sdk.message.ConvType;

/**
 * Created by 卫彪 on 2016/4/12.
 * IM插件
 */
public class WeimiWechatPlugin extends CordovaPlugin {

    private Context context;
    private Activity activity;
    private CordovaWebView webView;
    private YouyunHandler handler;

    class YouyunHandler extends Handler {

        private WeakReference<WeimiWechatPlugin> weakReference;

        public YouyunHandler(WeimiWechatPlugin plugin) {
            weakReference = new WeakReference<WeimiWechatPlugin>(plugin);
        }

        @Override
        public void handleMessage(final Message msg) {
            super.handleMessage(msg);
            if (weakReference.get() == null) {
                return;
            }
            final int what = msg.what;
            YouyunUtil.log("handler what:" + what);

            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    switch (what) {
                        case YouyunUtil.RECEIVE_TEXT:
                            receiveText(msg);
                            break;
                        case YouyunUtil.RECEIVE_PICTURE:
                            receivePicture(msg);
                            break;
                        case YouyunUtil.UPLOAD_PIC_PRO:
                            uploadPicPro(msg);
                            break;
                        case YouyunUtil.DOWNLOAD_PIC_PRO:
                            downloadPicPro(msg);
                            break;
                    }
                }
            });

        }
    }

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
        YouyunUtil.log("WeimiWechatPlugin initialize");
        this.webView = webView;
        context = this.cordova.getActivity().getApplicationContext();
        activity = this.cordova.getActivity();
        if (handler == null)
            handler = new YouyunHandler(WeimiWechatPlugin.this);
        YouyunInstance.getInstance().init(context, handler); // 初始化游云SDK
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if ("login".equals(action)) {
            login(callbackContext);
            return true;
        } else if ("logout".equals(action)) {
            logout(callbackContext);
            return true;
        } else if ("sendText".equals(action)) {
            String arg0 = args.getString(0);
            if (args != null) {
                JSONObject object = new JSONObject(arg0);
                if (object != null) {
                    String userId = object.optString("userID");
                    String content = object.optString("content");
                    sendText(userId, content, ConvType.single, callbackContext);
                }
            }

            return true;
        } else if ("sendGroupText".equals(action)) {
            String arg0 = args.getString(0);
            if (args != null) {
                JSONObject object = new JSONObject(arg0);
                if (object != null) {
                    String groupId = object.optString("groupID");
                    String content = object.optString("content");
                    sendText(groupId, content, ConvType.group, callbackContext);
                }
            }

            return true;
        } else if ("sendImage".equals(action)) {
            String arg0 = args.getString(0);
            if (args != null) {
                JSONObject object = new JSONObject(arg0);
                if (object != null) {
                    String uid = object.optString("userID");
                    String filePath = object.optString("filePath");
                    String thumbnailPath = object.optString("nailPath");
                    sendImage(uid, filePath, thumbnailPath, ConvType.single, callbackContext);
                }
            }
            return true;
        } else if ("sendGroupImage".equals(action)) {
            String arg0 = args.getString(0);
            if (args != null) {
                JSONObject object = new JSONObject(arg0);
                if (object != null) {
                    String gid = object.optString("groupID");
                    String filePath = object.optString("filePath");
                    String thumbnailPath = object.optString("nailPath");
                    sendImage(gid, filePath, thumbnailPath, ConvType.group, callbackContext);
                }
            }
            return true;
        } else if ("groupCreate".equals(action)) {
            createGroup(callbackContext);
            return true;
        } else if ("groupGetTotalUsers".equals(action)) {
            String arg0 = args.getString(0);
            if (args != null) {
                JSONObject object = new JSONObject(arg0);
                if (object != null) {
                    String groupId = object.optString("groupID");
                    getUserListByGroupId(groupId, callbackContext);
                }
            }
            return true;
        } else if ("groupGetUserGroups".equals(action)) {
            String arg0 = args.getString(0);
            if (args != null) {
                JSONObject object = new JSONObject(arg0);
                if (object != null) {
                    String userId = object.optString("userID");
                    getGroupListByUserId(userId, callbackContext);
                }
            }
            return true;
        } else if ("groupAddUser".equals(action)) {
            String arg0 = args.getString(0);
            if (args != null) {
                JSONObject object = new JSONObject(arg0);
                if (object != null) {
                    String groupId = object.optString("groupID");
                    String userIds = object.optString("userIDs");
                    groupAddUser(groupId, userIds, callbackContext);
                }
            }
            return true;
        } else if ("groupDeleteUser".equals(action)) {
            String arg0 = args.getString(0);
            if (args != null) {
                JSONObject object = new JSONObject(arg0);
                if (object != null) {
                    String groupId = object.optString("groupID");
                    String userIds = object.optString("userIDs");
                    groupDeleteUser(groupId, userIds, callbackContext);
                }
            }
            return true;
        } else if ("groupExit".equals(action)) {
            String arg0 = args.getString(0);
            if (args != null) {
                JSONObject object = new JSONObject(arg0);
                if (object != null) {
                    String groupId = object.optString("groupID");
                    exitGroup(groupId, callbackContext);
                }
            }
            return true;
        } else if ("getFile".equals(action)) {
            String arg0 = args.getString(0);
            if (args != null) {
                JSONObject object = new JSONObject(arg0);
                if (object != null) {
                    String fileId = object.optString("fileID");
                    String filePath = object.optString("filePath");
                    int fileLength = object.optInt("length");
                    int pieceSize = object.optInt("pieceSize");
                    downloadImg(fileId, filePath, fileLength, pieceSize, callbackContext);
                }
            }
            return true;
        }

        return false;
    }

    /**
     * 下载图片进度条
     *
     * @param msg
     */
    private void downloadPicPro(Message msg) {
        String result = (String) msg.obj;
        YouyunUtil.log("download:" + result);
        webView.loadUrl("javascript:receiveMessageThread('" + result + "')");
    }

    /**
     * 上传图片进度条
     *
     * @param msg
     */
    private void uploadPicPro(Message msg) {
        String result = (String) msg.obj;
        YouyunUtil.log("upload:" + result);
        webView.loadUrl("javascript:receiveMessageThread('" + result + "')");
    }

    /**
     * 接收图片
     *
     * @param msg
     */
    private void receivePicture(Message msg) {
        String result = (String) msg.obj;
        YouyunUtil.log("receivepicture：" + result);
        webView.loadUrl("javascript:receiveMessageThread('" + result + "')");
    }

    /**
     * 接收文本
     *
     * @param msg
     */
    private void receiveText(Message msg) {
        String result = (String) msg.obj;
        YouyunUtil.log("receivetext：" + result);
        webView.loadUrl("javascript:receiveMessageThread('" + result + "')");
    }

    /**
     * 退群成功
     *
     * @param groupId
     * @param callbackContext
     */
    private void exitGroup(String groupId, final CallbackContext callbackContext) {
        YouyunInstance.getInstance().exitGroup(groupId, new ChatApiCallback() {
            @Override
            public void onSuccess(String result) {
                callbackContext.success(result);
            }

            @Override
            public void onError(String error) {
                callbackContext.error(error);
            }
        });
    }

    /**
     * 删除群成员
     *
     * @param groupId
     * @param uids
     * @param callbackContext
     */
    private void groupDeleteUser(String groupId, String uids, final CallbackContext callbackContext) {
        YouyunInstance.getInstance().groupDeleteUser(groupId, uids, new ChatApiCallback() {
            @Override
            public void onSuccess(String result) {
                callbackContext.success(result);
            }

            @Override
            public void onError(String error) {
                callbackContext.error(error);
            }
        });
    }

    /**
     * 加群
     *
     * @param groupId
     * @param uids
     * @param callbackContext
     */
    private void groupAddUser(String groupId, String uids, final CallbackContext callbackContext) {
        YouyunInstance.getInstance().groupAddUser(groupId, uids, new ChatApiCallback() {
            @Override
            public void onSuccess(String result) {
                callbackContext.success(result);
            }

            @Override
            public void onError(String error) {
                callbackContext.error(error);
            }
        });
    }

    /**
     * 获取用户群列表
     *
     * @param callbackContext
     */
    private void getGroupListByUserId(String userId, final CallbackContext callbackContext) {
        YouyunInstance.getInstance().getGroupListByUserId(userId, new ChatApiCallback() {
            @Override
            public void onSuccess(String result) {
                callbackContext.success(result);
            }

            @Override
            public void onError(String error) {
                callbackContext.error(error);
            }
        });
    }

    /**
     * 获取群成员
     *
     * @param groupId
     * @param callbackContext
     */
    private void getUserListByGroupId(String groupId, final CallbackContext callbackContext) {
        YouyunInstance.getInstance().getUserListByGroupId(groupId, new ChatApiCallback() {
            @Override
            public void onSuccess(String result) {
                callbackContext.success(result);
            }

            @Override
            public void onError(String error) {
                callbackContext.error(error);
            }
        });
    }

    /**
     * 创建群
     *
     * @param callbackContext
     */
    private void createGroup(final CallbackContext callbackContext) {
        YouyunInstance.getInstance().createGroup(new ChatApiCallback() {
            @Override
            public void onSuccess(String result) {
                callbackContext.success(result);
            }

            @Override
            public void onError(String error) {
                callbackContext.error(error);
            }
        });
    }

    /**
     * 下载图片
     *
     * @param fileId
     * @param filePath
     * @param fileLength
     * @param pieceSize
     * @param callbackContext
     */
    private void downloadImg(String fileId, String filePath, int fileLength, int pieceSize, final CallbackContext callbackContext) {
        YouyunInstance.getInstance().downloadImage(fileId, filePath, fileLength, pieceSize, new ChatApiCallback() {
            @Override
            public void onSuccess(String result) {
                callbackContext.success(result);
            }

            @Override
            public void onError(String error) {
                callbackContext.error(error);
            }
        });
    }

    /**
     * 发送图片
     *
     * @param touchId
     * @param filePath        图片路径
     * @param thumbnailPath   缩略图路径
     * @param convType
     * @param callbackContext
     */
    private void sendImage(String touchId, String filePath, String thumbnailPath, ConvType convType, final CallbackContext callbackContext) {
        YouyunInstance.getInstance().sendImage(touchId, filePath, thumbnailPath, convType, new ChatApiCallback() {
            @Override
            public void onSuccess(String result) {
                callbackContext.success(result);
            }

            @Override
            public void onError(String error) {
                callbackContext.error(error);
            }
        });
    }

    /**
     * 发送文本
     *
     * @param touchId
     * @param text
     * @param callbackContext
     */
    private void sendText(String touchId, String text, ConvType convType, final CallbackContext callbackContext) {
        YouyunInstance.getInstance().sendText(touchId, text, convType, new ChatApiCallback() {
            @Override
            public void onSuccess(String result) {
                callbackContext.success(result);
            }

            @Override
            public void onError(String error) {
                callbackContext.error(error);
            }
        });

    }

    /**
     * 登出
     *
     * @param callbackContext
     */
    private void logout(final CallbackContext callbackContext) {
        YouyunInstance.getInstance().logout(new ChatApiCallback() {
            @Override
            public void onSuccess(String result) {
                callbackContext.success(result);
            }

            @Override
            public void onError(String error) {
                callbackContext.error(error);
            }
        });
    }

    /**
     * 登录
     */
    private void login(final CallbackContext callbackContext) {
        YouyunInstance.getInstance().login(new ChatApiCallback() {
            @Override
            public void onSuccess(String result) {
                callbackContext.success(result);
            }

            @Override
            public void onError(String error) {
                callbackContext.error(error);
            }
        });
    }

}

