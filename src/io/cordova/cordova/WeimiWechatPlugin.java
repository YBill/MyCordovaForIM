package io.cordova.cordova;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
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
 * Created by 卫彪 on 2016/4/12.
 * IM插件
 */
public class WeimiWechatPlugin extends CordovaPlugin {

	private Context context;
	private Activity activity;
	private CordovaWebView webView;
	private static WeimiMsgHandler weimiMsgHandler;
	private static MyInnerReceiver receiver;
	class MyInnerReceiver extends BroadcastReceiver{

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals("receivetext")) {
				String fromuid = intent.getStringExtra("fromuid");
				String content = intent.getStringExtra("content");
				long time = intent.getLongExtra("time", 0);
				JSONObject object = new JSONObject();
				try {
					object.put("msgType", "receiveText");
					object.put("fromuid", fromuid);
					object.put("content", content);
					object.put("time", time);
				} catch (JSONException e) {
					e.printStackTrace();
				}
				WeimiUtil.log("receivetext：" + object.toString());
				webView.loadUrl("javascript:receiveMessageThread('"+object.toString()+"')");
			}else if(action.equals("receivepicture")){
				String fromuid = intent.getStringExtra("fromuid");
				long time = intent.getLongExtra("time", 0);
				String fileId = intent.getStringExtra("fileId");
				int fileLength = intent.getIntExtra("fileLength", 0);
				int pieceSize = intent.getIntExtra("pieceSize", 0);
				String thumbData = intent.getStringExtra("thumbData");
				JSONObject object = new JSONObject();
				try {
					object.put("msgType", "receiveImage");
					object.put("fromuid",fromuid);
					object.put("time",time);
					object.put("fileId",fileId);
					object.put("fileLength",fileLength);
					object.put("pieceSize",pieceSize);
					object.put("thumbData", thumbData);
				} catch (JSONException e) {
					e.printStackTrace();
				}
				WeimiUtil.log("receivepicture：" + object.toString());
				webView.loadUrl("javascript:receiveMessageThread('"+object.toString()+"')");
			}else if(action.equals("uploadpicpro")){
				String fileId = intent.getStringExtra("fileID");
				int progress = intent.getIntExtra("progress", 0);
				JSONObject object = new JSONObject();
				try {
					object.put("msgType", "uploadProgress");
					object.put("fileID", fileId);
					object.put("progress", progress);
				} catch (JSONException e) {
					e.printStackTrace();
				}
				WeimiUtil.log("upload:" + object.toString());
				webView.loadUrl("javascript:receiveMessageThread('" + object.toString() + "')");
			}else if(action.equals("downloadpicpro")){
				String fileId = intent.getStringExtra("fileID");
				int progress = intent.getIntExtra("progress", 0);
				JSONObject object = new JSONObject();
				try {
					object.put("msgType", "downloadProgress");
					object.put("fileID", fileId);
					object.put("progress", progress);
				} catch (JSONException e) {
					e.printStackTrace();
				}
				WeimiUtil.log("download:" + object.toString());
				webView.loadUrl("javascript:receiveMessageThread('" + object.toString() + "')");
			}

		}
	}

	@Override
	public void initialize(CordovaInterface cordova, CordovaWebView webView) {
		super.initialize(cordova, webView);
		System.out.println("initialize");
		WeimiUtil.log("WeimiWechatPlugin initialize");
		this.webView = webView;
		context = this.cordova.getActivity().getApplicationContext();
		activity = this.cordova.getActivity();
		if(weimiMsgHandler == null){
			weimiMsgHandler = new WeimiMsgHandler(context);
			Thread msgHandler = new Thread(weimiMsgHandler);
			msgHandler.start();
		}
		if(receiver == null){
			IntentFilter filter = new IntentFilter();
			filter.addAction("receivetext");
			filter.addAction("receivepicture");
			filter.addAction("uploadpicpro");
			filter.addAction("downloadpicpro");
			receiver = new MyInnerReceiver();
			context.registerReceiver(receiver, filter);
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if(receiver != null){
			context.unregisterReceiver(receiver);
		}
	}

	@Override
	public boolean execute(String action, JSONArray args,
						   CallbackContext callbackContext) throws JSONException {
		if("login".equals(action)){
			login(callbackContext);
			return true;
		}else if("logout".equals(action)){
			logout(callbackContext);
			return true;
		}else if("sendText".equals(action)){
			String arg0 = args.getString(0);
			if(args != null){
				JSONObject object = new JSONObject(arg0);
				if(object != null){
					String userId = object.optString("userID");
					String content = object.optString("content");
					sendText(userId, content, ConvType.single, callbackContext);
				}
			}

			return true;
		}else if("sendGroupText".equals(action)){
			String arg0 = args.getString(0);
			if(args != null){
				JSONObject object = new JSONObject(arg0);
				if(object != null){
					String groupId = object.optString("groupID");
					String content = object.optString("content");
					sendText(groupId, content, ConvType.group, callbackContext);
				}
			}

			return true;
		}else if("sendImage".equals(action)){
			String arg0 = args.getString(0);
			if(args != null){
				JSONObject object = new JSONObject(arg0);
				if(object != null){
					String uid = object.optString("userID");
					String filePath = object.optString("filePath");
					String thumbnailPath = object.optString("nailPath");
					sendImage(uid, filePath, thumbnailPath, ConvType.single, callbackContext);
				}
			}
			return true;
		}else if("sendGroupImage".equals(action)){
			String arg0 = args.getString(0);
			if(args != null){
				JSONObject object = new JSONObject(arg0);
				if(object != null){
					String gid = object.optString("groupID");
					String filePath = object.optString("filePath");
					String thumbnailPath = object.optString("nailPath");
					sendImage(gid, filePath, thumbnailPath, ConvType.group, callbackContext);
				}
			}
			return true;
		}else if("groupCreate".equals(action)){
			createGroup(callbackContext);
			return true;
		}else if("groupGetTotalUsers".equals(action)){
			String arg0 = args.getString(0);
			if(args != null){
				JSONObject object = new JSONObject(arg0);
				if(object != null){
					String groupId = object.optString("groupID");
					getUserListByGroupId(groupId, callbackContext);
				}
			}
			return true;
		}else if("groupGetUserGroups".equals(action)){
			String arg0 = args.getString(0);
			if(args != null){
				JSONObject object = new JSONObject(arg0);
				if(object != null){
					String userId = object.optString("userID");
					getGroupListByUserId(userId, callbackContext);
				}
			}
			return true;
		}else if("groupAddUser".equals(action)){
			String arg0 = args.getString(0);
			if(args != null){
				JSONObject object = new JSONObject(arg0);
				if(object != null){
					String groupId = object.optString("groupID");
					String userIds = object.optString("userIDs");
					groupAddUser(groupId, userIds, callbackContext);
				}
			}
			return true;
		}else if("groupDeleteUser".equals(action)){
			String arg0 = args.getString(0);
			if(args != null){
				JSONObject object = new JSONObject(arg0);
				if(object != null){
					String groupId = object.optString("groupID");
					String userIds = object.optString("userIDs");
					groupDeleteUser(groupId, userIds, callbackContext);
				}
			}
			return true;
		}else if("groupExit".equals(action)) {
			String arg0 = args.getString(0);
			if (args != null) {
				JSONObject object = new JSONObject(arg0);
				if (object != null) {
					String groupId = object.optString("groupID");
					exitGroup(groupId, callbackContext);
				}
			}
			return true;
		}else if("getFile".equals(action)){
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
	 * 退群成功
	 * @param groupId
	 * @param callbackContext
	 */
	private void exitGroup(String groupId, final CallbackContext callbackContext){
		if(null == groupId || "".equals(groupId))
			return;
		WeimiInstance.getInstance().shortExitGroup(Long.parseLong(groupId), new HttpCallback() {
			@Override
			public void onResponse(String s) {
				WeimiUtil.log("退群成功回调：" + s);
				try {
					JSONObject obj = new JSONObject(s);
					JSONObject object = new JSONObject();
					if (obj != null) {
						int status = obj.optInt("apistatus");
						boolean result = obj.optBoolean("result");
						if (status == 1 && result) {
							object.put("status", 1);
							WeimiUtil.log(object.toString());
							callbackContext.success(object.toString());
							return;
						}
					}
					object.put("status", 0);
					object.put("msg", "");
					WeimiUtil.log(object.toString());
					callbackContext.success(object.toString());
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
				WeimiUtil.log("退群失败:" + e.getMessage());
				callbackContext.error(e.getMessage());
			}
		}, 120);
	}

	/**
	 * 删除群成员
	 * @param groupId
	 * @param uids
	 * @param callbackContext
	 */
	private  void groupDeleteUser(String groupId, String uids, final CallbackContext callbackContext){
		if(null == groupId || "".equals(groupId) || null == uids || "".equals(uids))
			return;
		WeimiUtil.log("uids:" + uids);
		StringBuffer buffer = new StringBuffer();
		JSONArray jsonArray = null;
		try {
			jsonArray = new JSONArray(uids);
			for (int i = 0; i < jsonArray.length(); i++){
				if(i < jsonArray.length() - 1)
					buffer.append(",");
				buffer.append(jsonArray.getString(0));
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		WeimiInstance.getInstance().shortGroupuserDel(Long.parseLong(groupId), buffer.toString(), new HttpCallback() {
			@Override
			public void onResponse(String s) {
				WeimiUtil.log("删除群成员回调成功:" + s);
				try {
					JSONObject obj = new JSONObject(s);
					JSONObject object = new JSONObject();
					if (obj != null) {
						int status = obj.optInt("apistatus");
						int result = obj.optInt("result");
						if (status == 1) {
							if (result > 0) {
								object.put("status", 1);
								WeimiUtil.log(object.toString());
							} else {
								object.put("status", 0);
								object.put("msg", "No member of this group");
							}
							callbackContext.success(object.toString());
							return;
						}
					}
					object.put("status", 0);
					object.put("msg", "");
					WeimiUtil.log(object.toString());
					callbackContext.success(object.toString());

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
				WeimiUtil.log("删除群成员失败:" + e.getMessage());
				callbackContext.error(e.getMessage());
			}
		}, 120);

	}

	/**
	 * 加群
	 * @param groupId
	 * @param uids
	 * @param callbackContext
	 */
	private void groupAddUser(String groupId, String uids, final CallbackContext callbackContext){
		if(null == groupId || "".equals(groupId) || null == uids || "".equals(uids))
			return;
		StringBuffer buffer = new StringBuffer();
		JSONArray jsonArray = null;
		try {
			jsonArray = new JSONArray(uids);
			for (int i = 0; i < jsonArray.length(); i++){
				buffer.append(jsonArray.getString(i));
				if(i < jsonArray.length() - 1)
					buffer.append(",");
			}
			WeimiUtil.log("uids:" + buffer.toString());
		} catch (JSONException e) {
			e.printStackTrace();
		}

		WeimiInstance.getInstance().shortGroupuserAdd(Long.parseLong(groupId), buffer.toString(), new HttpCallback() {
			@Override
			public void onResponse(String s) {
				WeimiUtil.log("加群回调成功：" + s);
				try {
					JSONObject obj = new JSONObject(s);
					JSONObject object = new JSONObject();
					if (obj != null) {
						int status = obj.optInt("apistatus");
						int result = obj.optInt("result");
						if (status == 1 && result > 0) {
							if (result > 0) {
								object.put("status", 1);
								WeimiUtil.log(object.toString());
							} else {
								object.put("status", 0);
								object.put("msg", "It is already a member of this group");
							}
							callbackContext.success(object.toString());
							return;
						}
					}
					object.put("status", 0);
					object.put("msg", "");
					WeimiUtil.log(object.toString());
					callbackContext.success(object.toString());

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
				WeimiUtil.log("加群失败:" + e.getMessage());
				callbackContext.error(e.getMessage());
			}
		}, 120);

	}

	/**
	 * 获取用户群列表
	 * @param callbackContext
	 */
	private void getGroupListByUserId(String userId, final CallbackContext callbackContext){
		if(null == userId || "".equals(userId))
			return;
		WeimiInstance.getInstance().shortGroupList(userId, new HttpCallback() {
			@Override
			public void onResponse(String s) {
				WeimiUtil.log("群列表：" + s);
				try {
					JSONArray array = new JSONArray(s);
					JSONObject object = new JSONObject();
					if (array != null) {
						object.put("status", 1);
						JSONObject obj = new JSONObject();
						obj.put("groups", array);
						object.put("result", obj);
						WeimiUtil.log(object.toString());
						callbackContext.success(object.toString());
						return;
					}
					object.put("status", 0);
					object.put("msg", "");
					WeimiUtil.log(object.toString());
					callbackContext.success(object.toString());
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
				WeimiUtil.log("获取用户群列表失败:" + e.getMessage());
				callbackContext.error(e.getMessage());
			}
		}, 120);
	}

	/**
	 * 获取群成员
	 * @param groupId
	 * @param callbackContext
	 */
	private void getUserListByGroupId(String groupId, final CallbackContext callbackContext){
		if(null == groupId || "".equals(groupId))
			return;
		WeimiInstance.getInstance().shortGroupuserList(Long.parseLong(groupId), new HttpCallback() {
			@Override
			public void onResponse(String s) {
				WeimiUtil.log("群成员：" + s);
				try {
					JSONArray array = new JSONArray(s);
					JSONObject object = new JSONObject();
					if (array != null) {
						object.put("status", 1);
						JSONObject obj = new JSONObject();
						obj.put("users", array);
						object.put("result", obj);
						WeimiUtil.log(object.toString());
						callbackContext.success(object.toString());
						return;
					}
					object.put("status", 0);
					object.put("msg", "");
					WeimiUtil.log(object.toString());
					callbackContext.success(object.toString());
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
				WeimiUtil.log("获取群成员失败:" + e.getMessage());
				callbackContext.error(e.getMessage());
			}
		}, 120);
	}

	/**
	 * 创建群
	 * @param callbackContext
	 */
	private void createGroup(final CallbackContext callbackContext){
		WeimiInstance.getInstance().shortGroupCreate(new HttpCallback() {
			@Override
			public void onResponse(String s) {
				WeimiUtil.log("creategroup groupId:" + s);
				JSONObject object = new JSONObject();
				try {
					if (null != s && !"".equals(s)) {
						object.put("status", 1);
						JSONObject obj = new JSONObject();
						obj.put("groupID", s);
						object.put("result", obj);
						WeimiUtil.log(object.toString());
						callbackContext.success(object.toString());
						return;
					}
					object.put("status", 0);
					object.put("msg", "No group ID");
					WeimiUtil.log(object.toString());
					callbackContext.success(object.toString());
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
				WeimiUtil.log("创建群失败:" + e.getMessage());
				callbackContext.error(e.getMessage());
			}
		}, 120);
	}

	/**
	 * 下载图片
	 * @param fileId
	 * @param filePath
	 * @param fileLength
	 * @param pieceSize
	 * @param callbackContext
	 */
	private void downloadImg(String fileId, String filePath, int fileLength, int pieceSize, final CallbackContext callbackContext){
		if(null == fileId || "".equals(fileId) || null == filePath || "".equals(filePath) || fileLength <= 0 || pieceSize <= 0)
			return;
		try {
			boolean result = WeimiInstance.getInstance().downloadFile(fileId, filePath, fileLength, null, pieceSize, 120);
			JSONObject object = new JSONObject();
			if(result){
				object.put("status", 1);
				object.put("msg", "Call Interface success");
			}else{
				object.put("status", 0);
				object.put("msg", "Call Interface Failure");
			}
			WeimiUtil.log(object.toString());
			callbackContext.success(object.toString());
		} catch (WChatException e) {
			callbackContext.error(e.getMessage());
			e.printStackTrace();
		} catch (JSONException e) {
			callbackContext.error(e.getMessage());
			e.printStackTrace();
		}
	}

	/**
	 * 发送图片
	 * @param touchId
	 * @param filePath 图片路径
	 * @param thumbnailPath 缩略图路径
	 * @param convType
	 * @param callbackContext
	 */
	private void sendImage(String touchId, String filePath, String thumbnailPath, ConvType convType, final CallbackContext callbackContext){
		if(null == touchId || "".equals(touchId) || null == filePath || "".equals(filePath) || null == thumbnailPath || "".equals(thumbnailPath))
			return;

		byte[] thumbnail = WeimiUtil.getByteByPath(thumbnailPath);
		if(thumbnail == null)
			return;

		String msgId = WeimiUtil.genLocalMsgId();
		int sliceCount = 0;
		try {
			sliceCount = WeimiInstance.getInstance().sendFile(msgId, touchId, filePath, filePath.substring(filePath.lastIndexOf("/") + 1),
					MetaMessageType.image, null, convType, null, thumbnail, 600);
			JSONObject object = new JSONObject();
			if(sliceCount == 0){
				object.put("status", 1);
				object.put("msg", "Call Interface success");
			}else{
				object.put("status", 0);
				object.put("msg", "Call Interface Failure");
			}
			WeimiUtil.log(object.toString());
			callbackContext.success(object.toString());
		} catch (WChatException e) {
			callbackContext.error(e.getMessage());
			e.printStackTrace();
		} catch (JSONException e) {
			callbackContext.error(e.getMessage());
			e.printStackTrace();
		}

		List<Integer> list = new LinkedList<Integer>();
		for (int i = 1; i <= sliceCount; i++) {
			list.add(i);
		}
		WeimiMsgHandler.fileSend.put(msgId, list);
		WeimiMsgHandler.fileSendCount.put(msgId, sliceCount);
	}

	/**
	 * 发送文本
	 * @param touchId
	 * @param text
	 * @param callbackContext
	 */
	private void sendText(String touchId, String text, ConvType convType, CallbackContext callbackContext){
		if(touchId == null || "".equals(touchId) || text == null || "".equals(text)){
			return;
		}

		String msgId = WeimiUtil.genLocalMsgId();

		try {
			boolean result = WeimiInstance.getInstance().sendText(msgId, touchId, text, convType, null, 120);
			JSONObject object = new JSONObject();
			if(result){
				object.put("status", 1);
				object.put("msg", "Call Interface success");
			}else{
				object.put("status", 0);
				object.put("msg", "Call Interface Failure");
			}
			WeimiUtil.log(object.toString());
			callbackContext.success(object.toString());
		} catch (WChatException e) {
			callbackContext.error(e.getMessage());
			e.printStackTrace();
		} catch (JSONException e) {
			callbackContext.error(e.getMessage());
			e.printStackTrace();
		}

	}

	/**
	 * 登出
	 * @param callbackContext
	 */
	private void logout(final CallbackContext callbackContext){
		boolean result = WeimiInstance.getInstance().logout();
		try {
			JSONObject object = new JSONObject();
			if(result){
				object.put("status", 1);
			}else{
				object.put("status", 0);
				object.put("msg", "");
			}
			WeimiUtil.log(object.toString());
			callbackContext.success(object.toString());
		} catch (JSONException e) {
			callbackContext.error(e.getMessage());
			e.printStackTrace();
		}
	}

	/**
	 * 登录
	 */
	private void login(final CallbackContext callbackContext) {
		new Thread(new Runnable() {
			boolean loginResult = false;

			@Override
			public void run() {
				try {
					String clientIdDefault;
					String clientSecretDefault;
					AuthResultData authResultData = null;
					String udid = WeimiUtil.generateOpenUDID(context);

					ApplicationInfo activityInfo = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
					if(activityInfo == null)
						return;
					Bundle bundle = activityInfo.metaData;
					if(bundle == null)
						return;
					String clientId = bundle.getString("CLIENT_ID");
					String secret = bundle.getString("SECRET");
					WeimiUtil.log("clientId:" + clientId);
					WeimiUtil.log("secret:" + secret);
					if(null == clientId || "".equals(clientId) || null == secret || "".equals(secret)){
						return;
					}
//					clientIdDefault = "1-10001-e879c692ba5e9bca45a1fe864e946134-android";
//					clientSecretDefault = "28192ae641a58d1ede7624eea565f497";
					authResultData = WeimiInstance.getInstance().testRegisterApp(
							context, udid, clientId, secret, 30);

					if (authResultData.success) {
						loginResult = true;
					}

				} catch (WChatException e) {
					e.printStackTrace();
				} catch (PackageManager.NameNotFoundException e) {
					e.printStackTrace();
				}

				WeimiUtil.uid = WeimiInstance.getInstance().getUID();
				JSONObject object = new JSONObject();
				try {
					if (loginResult) {
						if(null != WeimiUtil.uid && !"".equals(WeimiUtil.uid)){
							object.put("status", 1);
							JSONObject obj = new JSONObject();
							obj.put("id", WeimiUtil.uid);
							object.put("result", obj);
//							        webView.loadUrl("file:///android_asset/www/chat.html");
						}else{
							object.put("status", 0);
							object.put("msg", "No userID");
						}
					} else {
						object.put("status", 0);
						object.put("msg", "");
					}
					WeimiUtil.log(object.toString());
					callbackContext.success(object.toString());
				} catch (JSONException e) {
					callbackContext.error(e.getMessage());
					e.printStackTrace();
				}

			}

		}).start();

	}

}
