package io.cordova.cordova;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.RemoteViews;

import com.weimi.push.WeimiPushReceiver;
import com.weimi.push.data.PayLoadMessage;

public class PushMsgReceiver extends WeimiPushReceiver {
	private int rq = 0;
	private Notification notification;

	@SuppressWarnings("deprecation")
	@Override
	public void onMessage(Context context, PayLoadMessage payLoadMessage) {
		String alert = payLoadMessage.alert;
		if (alert == null || alert.equals(""))
			return;
		Intent intent1 = new Intent();
		intent1.setClass(context, MainActivity.class);
		PendingIntent pendingIntent = PendingIntent.getActivity(context, rq++,
				intent1, PendingIntent.FLAG_UPDATE_CURRENT);

		NotificationManager nManager = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);

		if (notification == null) {
			notification = new Notification(R.drawable.icon, context
					.getResources().getString(R.string.app_name),
					System.currentTimeMillis());
			notification.contentView = new RemoteViews(
					context.getPackageName(), R.layout.notifycation_layout);
		}
		notification.contentView.setTextViewText(R.id.tv1, alert);
		notification.contentView.setViewVisibility(R.id.tv1, View.VISIBLE);
		notification.contentView.setViewVisibility(R.id.tv2, View.GONE);

		notification.icon = R.drawable.icon;
		notification.tickerText = alert;
		notification.flags = Notification.FLAG_AUTO_CANCEL
				| Notification.FLAG_SHOW_LIGHTS;
		notification.contentIntent = pendingIntent;
		notification.ledARGB = 0xff00ff00;
		notification.ledOnMS = 500;
		notification.ledOffMS = 2000;
		nManager.cancel(300);
		nManager.notify(300, notification);
		// TODO Auto-generated method stub
	}
}
