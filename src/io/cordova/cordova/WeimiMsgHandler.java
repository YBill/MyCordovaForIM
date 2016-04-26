package io.cordova.cordova;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import matrix.sdk.GroupIdConv;
import matrix.sdk.message.ConvType;
import matrix.sdk.message.FileMessage;
import matrix.sdk.message.NoticeType;
import matrix.sdk.message.NotifyCenter;
import matrix.sdk.message.SendBackMessage;
import matrix.sdk.message.TextMessage;
import matrix.sdk.message.WChatException;
import matrix.sdk.message.WeimiNotice;

/**
 * 接收消息
 * Created by 卫彪 on 2016/4/12.
 */
public class WeimiMsgHandler implements Runnable {

    private Context context;
    private Handler handler;
    public static Map<String, List<Integer>> fileSend = new ConcurrentHashMap<String, List<Integer>>();
    public static Map<String, Integer> fileSendCount = new ConcurrentHashMap<String, Integer>();

    public WeimiMsgHandler(Context context, Handler handler){
        this.context = context;
        this.handler = handler;
    }

    @Override
    public void run() {
        WeimiUtil.log("Message Handler start");
        WeimiNotice weimiNotice;
        while (true){
            try {
                weimiNotice = NotifyCenter.clientNotifyChannel.take();
            } catch (InterruptedException e) {
                WeimiUtil.log("阻塞接受出现异常");
                break;
            }
            NoticeType type = weimiNotice.getNoticeType();
            WeimiUtil.log("noticeType" + type);
            switch (type){
                case exception:
                    exceptionMessageMethod(weimiNotice);
                    break;
                case textmessage:
                    textMessageMethod(weimiNotice);
                    break;
                case sendfile:
                    sendfileMethod(weimiNotice);
                    break;
                case sendback:
                    sendbackMethod(weimiNotice);
                    break;
                case filemessage:
                    fileMessageMethod(weimiNotice);
                    break;
                case downloadfile:
                    downloadMethod(weimiNotice);
                    break;
            }

            WeimiUtil.log("Message Handler stop");

        }

    }

    /**
     *对于下载文件的请求
     * @param weimiNotice
     */
    private void downloadMethod(WeimiNotice weimiNotice){
        WeimiUtil.log("********** 接收到文件下载的结果 **********");
        FileMessage fileMessage = (FileMessage) weimiNotice.getObject();
        String fileId = weimiNotice.getWithtag();
        List<Integer> list = fileMessage.hasReveive;
        for (int sliceMissId : list) {
            WeimiUtil.log("实际已经下载的分片号：" + sliceMissId);
        }
        double completed = (fileMessage.hasReveive.size() / (double) fileMessage.limit);
        WeimiUtil.log("文件ID：" + fileId + "--下载进度为：" + completed * 100 + "%");
        if ((int) completed == 1) {
            WeimiUtil.log("文件ID：" + fileId + "--下载完成");
            notifyHandler(WeimiUtil.DOWNLOAD_PIC_PRO, fileId, 1);
            return;
        }
        notifyHandler(WeimiUtil.DOWNLOAD_PIC_PRO, fileId, completed);
    }

    /**
     * 文件类型的meta消息进行处理
     * @param weimiNotice
     */
    private void fileMessageMethod(WeimiNotice weimiNotice){
        FileMessage fileMessage = (FileMessage) weimiNotice.getObject();
        String convId = weimiNotice.getWithtag();
        if (null != convId && convId.startsWith("G")) {
            WeimiUtil.log("********** 接收到一条群文件消息 **********");
            convId = GroupIdConv.gidTouid(convId);
        } else {
            WeimiUtil.log("********** 接收到一条单聊文件消息 **********");
        }
        WeimiUtil.log("会话ID：" + convId);
        WeimiUtil.log("消息ID：" + fileMessage.msgId);
        WeimiUtil.log("发送者：" + fileMessage.fromuid);
        WeimiUtil.log("文件类型：" + fileMessage.type);
        WeimiUtil.log("发送时间：" + fileMessage.time);
        WeimiUtil.log("文件的ID：" + fileMessage.fileId);
        WeimiUtil.log("文件的大小：" + fileMessage.fileLength);
        WeimiUtil.log("分片大小：" + fileMessage.pieceSize);
        WeimiUtil.log("文件的名称：" + fileMessage.filename);
        WeimiUtil.log("附加信息：" + fileMessage.padding.toString());
        if (null != fileMessage.thumbData) {
            WeimiUtil.log("缩略图存在：" + Arrays.toString(fileMessage.thumbData));
            String thumbData = WeimiUtil.bitmapToBase64(fileMessage.thumbData);

            JSONObject object = new JSONObject();
            try {
                object.put("msgType", "receiveImage");
                object.put("fromuid", fileMessage.fromuid);
                object.put("time", fileMessage.time);
                object.put("fileId", fileMessage.fileId);
                object.put("fileLength", fileMessage.fileLength);
                object.put("pieceSize", fileMessage.pieceSize);
                object.put("thumbData", thumbData);
                if(fileMessage.convType == ConvType.group){
                    object.put("convType", 2);
                }else{
                    object.put("convType", 1);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            Message message = handler.obtainMessage();
            message.what = WeimiUtil.RECEIVE_PICTURE;
            message.obj = object.toString();
            handler.handleMessage(message);
        }

    }

    /**
     * 发送文件的回执处理
     * @param weimiNotice
     */
    private void sendfileMethod(WeimiNotice weimiNotice){
        WeimiUtil.log("********** 接收到一条发送文件的返回通知 **********");
        List<Integer> unUploadSliceList = (List<Integer>) weimiNotice.getObject();
        String msgId = weimiNotice.getWithtag();
        for (Integer i : unUploadSliceList) {
            WeimiUtil.log("可能缺少的分片号：" + i);
        }
        if (unUploadSliceList.isEmpty()) {
            fileSend.remove(msgId);
            fileSendCount.remove(msgId);
            WeimiUtil.log(msgId + "==文件完成度：100%，发送成功！");
            notifyHandler(WeimiUtil.UPLOAD_PIC_PRO, msgId, 1);
            return;
        }

        // 如果收到缺少分片的回执，但只本地已经清理掉fileSendCount的缓存，可认定是很旧的回执，可以丢掉
        if (!fileSendCount.containsKey(msgId)) {
            WeimiUtil.log("收到旧的文件上传回执");
            return;
        }
        List<Integer> list = fileSend.get(msgId);
        List<Integer> newList = new LinkedList<Integer>();
        for (int i : unUploadSliceList) { // 排重
            // 如果包含在旧的list中，说明之前就是还未到达的分片
            // 如果不包含在旧的list中，说明之前已经确认到达，但是这个包来得迟了，所以应该去掉重复的包，忽略即可
            if (list.contains(Integer.valueOf(i)) && Integer.valueOf(i) <= Integer.valueOf(fileSendCount.get(msgId))) {
                newList.add(Integer.valueOf(i));
            }
        }
        fileSend.put(msgId, newList);
        double sliceCount = (double) fileSendCount.get(msgId);
        int listSize = newList.size();
        if (0 == listSize) {
            fileSend.remove(msgId);
            fileSendCount.remove(msgId);
            WeimiUtil.log(msgId + "--文件完成度：100%，发送成功！");
            notifyHandler(WeimiUtil.UPLOAD_PIC_PRO, msgId, 1);
            return;
        } else {
            WeimiUtil.log("还有" + listSize + "片没有收到");
            double completed = (sliceCount - listSize) / sliceCount;
            WeimiUtil.log("完成度：" + completed * 100 + "%");
            notifyHandler(WeimiUtil.UPLOAD_PIC_PRO, msgId, completed);
            for (int sliceMissId : newList) {
                WeimiUtil.log("实际缺少的分片号：" + sliceMissId);
            }
        }

    }

    private void notifyHandler(int action, String fileId, double completed){
        JSONObject object = new JSONObject();
        try {
            if(action == WeimiUtil.UPLOAD_PIC_PRO){
                object.put("msgType", "uploadProgress");
            }else if(action == WeimiUtil.DOWNLOAD_PIC_PRO){
                object.put("msgType", "downloadProgress");
            }
            object.put("fileID", fileId);
            object.put("progress", (int) (completed * 100));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Message message = handler.obtainMessage();
        message.what = action;
        message.obj = object.toString();
        handler.handleMessage(message);
    }

    /**
     * 发送文本/图片/文件/语音消息成功回执
     * @param weimiNotice
     */
    private void sendbackMethod(WeimiNotice weimiNotice){
        WeimiUtil.log("********** 接收到一条请求的回执 **********");
        SendBackMessage sendBackMessage = (SendBackMessage) weimiNotice.getObject();
        System.out.println(sendBackMessage);
    }

    /**
     * 对于文本类型的消息进行处理
     * @param weimiNotice
     */
    private void textMessageMethod(WeimiNotice weimiNotice){
        TextMessage textMessage = (TextMessage) weimiNotice.getObject();
        String msgId = textMessage.msgId;
        WeimiUtil.log("msgId:" + msgId);
        WeimiUtil.log("convType:" + textMessage.convType);
        WeimiUtil.log("receive text:" + textMessage.text);

        JSONObject object = new JSONObject();
        try {
            object.put("msgType", "receiveText");
            object.put("fromuid", textMessage.fromuid);
            object.put("content", textMessage.text);
            object.put("time", textMessage.time);
            if(textMessage.convType == ConvType.group){
                object.put("convType", 2);
            }else{
                object.put("convType", 1);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Message message = handler.obtainMessage();
        message.what = WeimiUtil.RECEIVE_TEXT;
        message.obj = object.toString();
        handler.handleMessage(message);
    }

    /**
     * 异常处理
     * @param weimiNotice
     */
    private void exceptionMessageMethod(WeimiNotice weimiNotice){
        WChatException wChatException = (WChatException) weimiNotice.getObject();
        WeimiUtil.log("新异常:" + wChatException);
        WeimiUtil.log("cause:" + wChatException.getCause());
    }

}
