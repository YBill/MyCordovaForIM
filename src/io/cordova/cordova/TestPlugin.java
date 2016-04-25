package io.cordova.cordova;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.json.JSONArray;
import org.json.JSONException;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

public class TestPlugin extends CordovaPlugin {

	private CordovaWebView webView;
	private String str = "Hello Bill";

	@Override
	public void initialize(CordovaInterface cordova, CordovaWebView webView) {
		super.initialize(cordova, webView);
		this.webView = webView;
		Log.v("Bill", "TestPlugin initialize");
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.v("Bill", "TestPlugin onDestroy");
	}

	public boolean execute(String action, JSONArray args, CallbackContext callbackContext) {
		Log.v("Bill", "TestPlugin execute");
		Context context = this.cordova.getActivity().getApplicationContext();
		
		String arg0 = "", arg1 = "";
		if(args != null){
			try {
				arg0 = args.getString(0);
				arg1 = args.getString(1);
				Log.v("Bill", "action:" + action);
				Log.v("Bill", "args1:" + arg0);
				Log.v("Bill", "args2:" + arg1);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		
		if("action1".equals(action)){
			Toast.makeText(context, arg0, Toast.LENGTH_LONG).show();

			callbackContext.success("成功回调");
			return true;
		}else if("action2".equals(action)){
			Toast.makeText(context, arg1, Toast.LENGTH_LONG).show();
			callbackContext.error("失败回调");
			return true;
		}
        
		//返回false表示invalid action
		return false;
	}
}
