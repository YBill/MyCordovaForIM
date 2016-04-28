package io.cordova.cordova;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;

import com.weimi.push.WeimiPush;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedList;
import java.util.List;

import matrix.sdk.WeimiInstance;
import matrix.sdk.data.AuthResultData;
import matrix.sdk.message.ConvType;
import matrix.sdk.message.HistoryMessage;
import matrix.sdk.message.WChatException;
import matrix.sdk.protocol.MetaMessageType;
import matrix.sdk.util.HttpCallback;

/**
 * 游云接口实现类
 * Created by 卫彪 on 2016/4/26.
 */
public class YouyunChatApiImpl implements YouyunChatApi {

    protected Context context;

    @Override
    public void login(final ChatApiCallback callback) {
        if (null == callback)
            return;
        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    String clientId = "";
                    String secret = "";
                    String udid = YouyunUtil.generateOpenUDID(context);

                    JSONObject object = new JSONObject();

                    boolean isHaveMetaData = false;
                    ApplicationInfo activityInfo = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
                    if (activityInfo != null) {
                        Bundle bundle = activityInfo.metaData;
                        if (bundle != null) {
                            clientId = bundle.getString("CLIENT_ID");
                            secret = bundle.getString("SECRET");
                            YouyunUtil.log("clientId:" + clientId + "|secret:" + secret);
                            if (null != clientId && !"".equals(clientId) && null != secret && !"".equals(secret)) {
                                isHaveMetaData = true;
                            }
                        }
                    }
                    if (!isHaveMetaData) {
                        object.put("status", 0);
                        object.put("msg", "No clientId or secret in plugin.xml");
                        YouyunUtil.log(object.toString());
                        callback.onSuccess(object.toString());
                        return;
                    }
                    AuthResultData authResultData = WeimiInstance.getInstance().registerApp(
                            context, udid, clientId, secret, 120);
                    if (authResultData.success) {
                        YouyunUtil.uid = WeimiInstance.getInstance().getUID();
                        if (null != YouyunUtil.uid && !"".equals(YouyunUtil.uid)) {
                            object.put("status", 1);
                            JSONObject obj = new JSONObject();
                            obj.put("id", YouyunUtil.uid);
                            object.put("result", obj);
//							        webView.loadUrl("file:///android_asset/www/chat.html");
                        } else {
                            object.put("status", 0);
                            object.put("msg", "No userID");
                        }
                    } else {
                        object.put("status", 0);
                        object.put("msg", "");
                    }
                    YouyunUtil.log(object.toString());
                    callback.onSuccess(object.toString());
                } catch (WChatException e) {
                    callback.onError(e.getMessage());
                    e.printStackTrace();
                } catch (PackageManager.NameNotFoundException e) {
                    callback.onError(e.getMessage());
                    e.printStackTrace();
                } catch (JSONException e) {
                    callback.onError(e.getMessage());
                    e.printStackTrace();
                }

            }

        }).start();
    }

    @Override
    public void logout(ChatApiCallback callback) {
        if (null == callback)
            return;
        boolean result = WeimiInstance.getInstance().logout();
        try {
            JSONObject object = new JSONObject();
            if (result) {
                object.put("status", 1);
            } else {
                object.put("status", 0);
                object.put("msg", "");
            }
            YouyunUtil.log(object.toString());
            callback.onSuccess(object.toString());
        } catch (JSONException e) {
            callback.onError(e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void sendText(String touchId, String text, ConvType convType, ChatApiCallback callback) {
        YouyunUtil.log("touchId:" + touchId + "|text:" + text + "convType:" + convType);
        if (null == callback || touchId == null || "".equals(touchId) || text == null || "".equals(text)) {
            return;
        }
        String msgId = YouyunUtil.genLocalMsgId();
        try {
            boolean result = WeimiInstance.getInstance().sendText(msgId, touchId, text, convType, null, 120);
            JSONObject object = new JSONObject();
            if (result) {
                object.put("status", 1);
                object.put("msg", "Call Interface success");
            } else {
                object.put("status", 0);
                object.put("msg", "Call Interface Failure");
            }
            YouyunUtil.log(object.toString());
            callback.onSuccess(object.toString());
        } catch (WChatException e) {
            callback.onError(e.getMessage());
            e.printStackTrace();
        } catch (JSONException e) {
            callback.onError(e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void sendImage(String touchId, String filePath, String thumbnailPath, ConvType convType, ChatApiCallback callback) {
        YouyunUtil.log("touchId:" + touchId + "|filePath:" + filePath + "|thumbnailPath:" + thumbnailPath);
        if (null == callback || null == touchId || "".equals(touchId) || null == filePath || "".equals(filePath) || null == thumbnailPath || "".equals(thumbnailPath))
            return;

        byte[] thumbnail;
        if ("Youyun_SendImg_Test".equals(thumbnailPath)) {
            // 测试发图，暂用
            thumbnail = YouyunUtil.getByteByBitmap(YouyunUtil.getSmallBitmap(filePath));
        } else {
            thumbnail = YouyunUtil.getByteByPath(thumbnailPath);
        }

        if (thumbnail == null)
            return;

        String msgId = YouyunUtil.genLocalMsgId();
        int sliceCount = 0;
        try {
            sliceCount = WeimiInstance.getInstance().sendFile(msgId, touchId, filePath, filePath.substring(filePath.lastIndexOf("/") + 1),
                    MetaMessageType.image, null, convType, null, thumbnail, 600);
            JSONObject object = new JSONObject();
            if (sliceCount == 0) {
                object.put("status", 0);
                object.put("msg", "Call Interface Failure");
            } else {
                object.put("status", 1);
                object.put("msg", "Call Interface success");
            }
            YouyunUtil.log(object.toString());
            callback.onSuccess(object.toString());
        } catch (WChatException e) {
            callback.onError(e.getMessage());
            e.printStackTrace();
        } catch (JSONException e) {
            callback.onError(e.getMessage());
            e.printStackTrace();
        }

        List<Integer> list = new LinkedList<Integer>();
        for (int i = 1; i <= sliceCount; i++) {
            list.add(i);
        }
        YouyunReceiveMsgThread.fileSend.put(msgId, list);
        YouyunReceiveMsgThread.fileSendCount.put(msgId, sliceCount);
    }

    @Override
    public void downloadImage(String fileId, String filePath, int fileLength, int pieceSize, ChatApiCallback callback) {
        YouyunUtil.log("fileId:" + fileId + "|filePath:" + filePath + "|fileLength:" + fileLength + "|pieceSize:" + pieceSize);
        if (null == callback || null == fileId || "".equals(fileId) || null == filePath || "".equals(filePath) || fileLength <= 0 || pieceSize <= 0)
            return;
        try {
            boolean result = WeimiInstance.getInstance().downloadFile(fileId, filePath, fileLength, null, pieceSize, 120);
            JSONObject object = new JSONObject();
            if (result) {
                object.put("status", 1);
                object.put("msg", "Call Interface success");
            } else {
                object.put("status", 0);
                object.put("msg", "Call Interface Failure");
            }
            YouyunUtil.log(object.toString());
            callback.onSuccess(object.toString());
        } catch (WChatException e) {
            callback.onError(e.getMessage());
            e.printStackTrace();
        } catch (JSONException e) {
            callback.onError(e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void createGroup(final ChatApiCallback callback) {
        if (null == callback)
            return;
        WeimiInstance.getInstance().shortGroupCreate(new HttpCallback() {
            @Override
            public void onResponse(String s) {
                YouyunUtil.log("creategroup groupId:" + s);
                JSONObject object = new JSONObject();
                try {
                    if (null != s && !"".equals(s)) {
                        object.put("status", 1);
                        JSONObject obj = new JSONObject();
                        obj.put("groupID", s);
                        object.put("result", obj);
                        YouyunUtil.log(object.toString());
                        callback.onSuccess(object.toString());
                        return;
                    }
                    object.put("status", 0);
                    object.put("msg", "No group ID");
                    YouyunUtil.log(object.toString());
                    callback.onSuccess(object.toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onResponse(byte[] bytes) {
            }

            @Override
            public void onResponseHistory(List<HistoryMessage> list) {
            }

            @Override
            public void onError(Exception e) {
                YouyunUtil.log("创建群失败:" + e.getMessage());
                callback.onError(e.getMessage());
            }
        }, 120);
    }

    @Override
    public void exitGroup(String groupId, final ChatApiCallback callback) {
        YouyunUtil.log("groupId:" + groupId);
        if (null == callback || null == groupId || "".equals(groupId))
            return;
        WeimiInstance.getInstance().shortExitGroup(Long.parseLong(groupId), new HttpCallback() {
            @Override
            public void onResponse(String s) {
                YouyunUtil.log("退群成功回调：" + s);
                try {
                    JSONObject obj = new JSONObject(s);
                    JSONObject object = new JSONObject();
                    if (obj != null) {
                        int status = obj.optInt("apistatus");
                        boolean result = obj.optBoolean("result");
                        if (status == 1 && result) {
                            object.put("status", 1);
                            YouyunUtil.log(object.toString());
                            callback.onSuccess(object.toString());
                            return;
                        }
                    }
                    object.put("status", 0);
                    object.put("msg", "");
                    YouyunUtil.log(object.toString());
                    callback.onSuccess(object.toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onResponse(byte[] bytes) {
            }

            @Override
            public void onResponseHistory(List<HistoryMessage> list) {
            }

            @Override
            public void onError(Exception e) {
                YouyunUtil.log("退群失败:" + e.getMessage());
                callback.onError(e.getMessage());
            }
        }, 120);
    }

    @Override
    public void groupAddUser(String groupId, String uids, final ChatApiCallback callback) {
        YouyunUtil.log("groupId:" + groupId + "uids:" + uids);
        if (null == callback || null == groupId || "".equals(groupId) || null == uids || "".equals(uids))
            return;
        StringBuffer buffer = new StringBuffer();
        try {
            JSONArray jsonArray = new JSONArray(uids);
            if (jsonArray == null)
                return;
            for (int i = 0; i < jsonArray.length(); i++) {
                buffer.append(jsonArray.getString(i));
                if (i < jsonArray.length() - 1)
                    buffer.append(",");
            }
            YouyunUtil.log("uids:" + buffer.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        WeimiInstance.getInstance().shortGroupuserAdd(Long.parseLong(groupId), buffer.toString(), new HttpCallback() {
            @Override
            public void onResponse(String s) {
                YouyunUtil.log("加群回调成功：" + s);
                try {
                    JSONObject obj = new JSONObject(s);
                    JSONObject object = new JSONObject();
                    if (obj != null) {
                        int status = obj.optInt("apistatus");
                        int result = obj.optInt("result");
                        if (status == 1 && result > 0) {
                            if (result > 0) {
                                object.put("status", 1);
                                YouyunUtil.log(object.toString());
                            } else {
                                object.put("status", 0);
                                object.put("msg", "It is already a member of this group");
                            }
                            callback.onSuccess(object.toString());
                            return;
                        }
                    }
                    object.put("status", 0);
                    object.put("msg", "");
                    YouyunUtil.log(object.toString());
                    callback.onSuccess(object.toString());

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onResponse(byte[] bytes) {
            }

            @Override
            public void onResponseHistory(List<HistoryMessage> list) {
            }

            @Override
            public void onError(Exception e) {
                YouyunUtil.log("加群失败:" + e.getMessage());
                callback.onError(e.getMessage());
            }
        }, 120);

    }

    @Override
    public void groupDeleteUser(String groupId, String uids, final ChatApiCallback callback) {
        YouyunUtil.log("groupId:" + groupId + "uids:" + uids);
        if (null == callback || null == groupId || "".equals(groupId) || null == uids || "".equals(uids))
            return;
        YouyunUtil.log("uids:" + uids);
        StringBuffer buffer = new StringBuffer();
        try {
            JSONArray jsonArray = new JSONArray(uids);
            if (jsonArray == null)
                return;
            for (int i = 0; i < jsonArray.length(); i++) {
                if (i < jsonArray.length() - 1)
                    buffer.append(",");
                buffer.append(jsonArray.getString(0));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        WeimiInstance.getInstance().shortGroupuserDel(Long.parseLong(groupId), buffer.toString(), new HttpCallback() {
            @Override
            public void onResponse(String s) {
                YouyunUtil.log("删除群成员回调成功:" + s);
                try {
                    JSONObject obj = new JSONObject(s);
                    JSONObject object = new JSONObject();
                    if (obj != null) {
                        int status = obj.optInt("apistatus");
                        int result = obj.optInt("result");
                        if (status == 1) {
                            if (result > 0) {
                                object.put("status", 1);
                                YouyunUtil.log(object.toString());
                            } else {
                                object.put("status", 0);
                                object.put("msg", "No member of this group");
                            }
                            callback.onSuccess(object.toString());
                            return;
                        }
                    }
                    object.put("status", 0);
                    object.put("msg", "");
                    YouyunUtil.log(object.toString());
                    callback.onSuccess(object.toString());

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onResponse(byte[] bytes) {
            }

            @Override
            public void onResponseHistory(List<HistoryMessage> list) {
            }

            @Override
            public void onError(Exception e) {
                YouyunUtil.log("删除群成员失败:" + e.getMessage());
                callback.onError(e.getMessage());
            }
        }, 120);
    }

    @Override
    public void getUserListByGroupId(String groupId, final ChatApiCallback callback) {
        YouyunUtil.log("groupId:" + groupId);
        if (null == callback || null == groupId || "".equals(groupId))
            return;
        WeimiInstance.getInstance().shortGroupuserList(Long.parseLong(groupId), new HttpCallback() {
            @Override
            public void onResponse(String s) {
                YouyunUtil.log("群成员：" + s);
                try {
                    JSONArray array = new JSONArray(s);
                    JSONObject object = new JSONObject();
                    if (array != null) {
                        object.put("status", 1);
                        JSONObject obj = new JSONObject();
                        obj.put("users", array);
                        object.put("result", obj);
                        YouyunUtil.log(object.toString());
                        callback.onSuccess(object.toString());
                        return;
                    }
                    object.put("status", 0);
                    object.put("msg", "");
                    YouyunUtil.log(object.toString());
                    callback.onSuccess(object.toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onResponse(byte[] bytes) {
            }

            @Override
            public void onResponseHistory(List<HistoryMessage> list) {
            }

            @Override
            public void onError(Exception e) {
                YouyunUtil.log("获取群成员失败:" + e.getMessage());
                callback.onError(e.getMessage());
            }
        }, 120);
    }

    @Override
    public void getGroupListByUserId(String userId, final ChatApiCallback callback) {
        YouyunUtil.log("userId：" + userId);
        if (null == callback || null == userId || "".equals(userId))
            return;
        WeimiInstance.getInstance().shortGroupList(userId, new HttpCallback() {
            @Override
            public void onResponse(String s) {
                YouyunUtil.log("群列表：" + s);
                try {
                    JSONArray array = new JSONArray(s);
                    JSONObject object = new JSONObject();
                    if (array != null) {
                        object.put("status", 1);
                        JSONObject obj = new JSONObject();
                        obj.put("groups", array);
                        object.put("result", obj);
                        YouyunUtil.log(object.toString());
                        callback.onSuccess(object.toString());
                        return;
                    }
                    object.put("status", 0);
                    object.put("msg", "");
                    YouyunUtil.log(object.toString());
                    callback.onSuccess(object.toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onResponse(byte[] bytes) {
            }

            @Override
            public void onResponseHistory(List<HistoryMessage> list) {
            }

            @Override
            public void onError(Exception e) {
                YouyunUtil.log("获取用户群列表失败:" + e.getMessage());
                callback.onError(e.getMessage());
            }
        }, 120);
    }

    @Override
    public void buildPushConnect(boolean isLogEnable, ChatApiCallback callback) {
        if (null == callback)
            return;
        boolean result = WeimiPush.connect(context, WeimiPush.pushServerIp, isLogEnable);
        YouyunUtil.log("push connect：" + result);
        JSONObject object = new JSONObject();
        try {
            if (result) {
                object.put("status", 1);
            } else {
                object.put("status", 0);
                object.put("msg", "");
            }
            YouyunUtil.log(object.toString());
            callback.onSuccess("");
        } catch (JSONException e) {
            callback.onError(e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void registerPushInfo(String startTime, String endTime, final ChatApiCallback callback) {
        YouyunUtil.log("startTime：" + startTime + "|endTime:" + endTime);
        if (null == callback || null == startTime || "".equals(startTime) || null == endTime || "".equals(endTime))
            return;
        WeimiInstance.getInstance().shortPushCreate(startTime, endTime, new HttpCallback() {
            @Override
            public void onResponse(String s) {
                YouyunUtil.log("registerPushInfo:" + s);
                JSONObject object = new JSONObject();
                try {
                    if (s != null && !s.equals("")) {
                        JSONObject obj = new JSONObject(s);
                        String code = obj.optString("code");
                        if (code != null && code.equals("200")) {
                            object.put("status", 1);
                            object.put("msg", "Set success");
                            callback.onSuccess(object.toString());
                            return;
                        }
                    }
                    object.put("status", 0);
                    object.put("msg", "Set faild");
                    callback.onSuccess(object.toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onResponse(byte[] bytes) {

            }

            @Override
            public void onResponseHistory(List<HistoryMessage> list) {

            }

            @Override
            public void onError(Exception e) {
                callback.onError(e.getMessage());
            }
        }, 120);
    }

    @Override
    public void logoutPush(final ChatApiCallback callback) {
        if (null == callback)
            return;
        WeimiInstance.getInstance().shortPushCancel(new HttpCallback() {
            @Override
            public void onResponse(String s) {
                YouyunUtil.log("logoutPush:" + s);
                JSONObject object = new JSONObject();
                try {
                    if (s != null && !s.equals("")) {
                        JSONObject obj = new JSONObject(s);
                        String code = obj.optString("code");
                        if (code != null && code.equals("200")) {
                            object.put("status", 1);
                            object.put("msg", "Set success");
                            callback.onSuccess(object.toString());
                            return;
                        }
                    }
                    object.put("status", 0);
                    object.put("msg", "Set faild");
                    callback.onSuccess(object.toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onResponse(byte[] bytes) {

            }

            @Override
            public void onResponseHistory(List<HistoryMessage> list) {

            }

            @Override
            public void onError(Exception e) {
                callback.onError(e.getMessage());
            }
        }, 120);
    }

    @Override
    public void getPushInfo(final ChatApiCallback callback) {
        if (null == callback)
            return;
        WeimiInstance.getInstance().shortPushShowUser(new HttpCallback() {
            @Override
            public void onResponse(String s) {
                YouyunUtil.log("getPushInfo:" + s);
                JSONObject object = new JSONObject();
                try {
                    if (s != null && !s.equals("")) {
                        JSONObject obj = new JSONObject(s);
                        String code = obj.optString("code");
                        if (code != null && code.equals("200")) {
                            JSONObject msgObj = obj.optJSONObject("msg");
                            String userId = msgObj.optString("user_id");
                            String statrTime = msgObj.optString("start_time");
                            String endTime = msgObj.optString("end_time");
                            object.put("status", 1);
                            object.put("userId", userId);
                            object.put("statrTime", statrTime);
                            object.put(endTime, endTime);
                            callback.onSuccess(object.toString());
                            return;
                        }
                    }
                    object.put("status", 0);
                    object.put("msg", "Get User Info Faild");
                    callback.onSuccess(object.toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onResponse(byte[] bytes) {

            }

            @Override
            public void onResponseHistory(List<HistoryMessage> list) {

            }

            @Override
            public void onError(Exception e) {
                callback.onError(e.getMessage());
            }
        }, 120);
    }

}
