package com.lnpdit.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BootReceiver extends BroadcastReceiver {

	String actionStr = "android.intent.action.news.BOOT_COMPLETED";

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		Log.d("ARD", "BootReceiver onReceive");
		if (intent.getAction().equals(actionStr)) {
			Intent i = new Intent(context, NewsPushService.class);
			context.startService(i);
			Log.d("ARD", "BootReceiver startService");
		} 

	}

}
