package com.lnpdit.service;

import java.io.IOException;


import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.xmlpull.v1.XmlPullParserException;

import com.lnpdit.babycare.R;
import com.lnpdit.garden.NewPushMsgActivity;
import com.lnpdit.sqllite.BBGJDB;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.IBinder;
import android.util.Log;

public class NewsPushService extends Service {
	private Resources resources;
	public static String pushversion;
	private SoapObject versionlist;
	private Context context;
	private BBGJDB tdd;
	private String mVersion;

	public Thread thread;
	int rate = 0;

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		resources = this.getResources();
		context = this;
		thread = new Thread(new GetPushData());
		thread.start();

	}

	@Override
	public void onStart(Intent intent, int startId) {
		// TODO Auto-generated method stub
		super.onStart(intent, startId);
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}

	public class GetPushData implements Runnable {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			while (true) {
				try {
					int rate_now = 0;
					Thread.sleep(15000);
					BBGJDB tdd = new BBGJDB(context);
					Cursor cursor = tdd.selectuser();
					cursor.moveToFirst();
					if (cursor.getCount() != 0) {
						rate_now = Integer.valueOf(cursor.getString(6)
								.toString());
					}
					if (rate >= rate_now) {
						checkUpdate();
						rate = 0;
						Log.e("PUSH", "PUSH IS RUNNING, USERID = "
								+ cursor.getString(1).toString()
								+ "|REFRESH_RATE = " + String.valueOf(rate));
					}
					rate = rate + 10000;
				} catch (Exception e) {
					// TODO: handle exception
				}
			}
		}
	}

	public void checkUpdate() {
		BBGJDB tdd = new BBGJDB(context);
		Cursor cursor = tdd.selectuser();
		if (cursor.getCount() == 0) {
			return;
		} else {
			cursor.moveToFirst();
			String versionurl = MessengerService.URL;
			String versionmethodname = MessengerService.METHOD_GETINTERACTIONMESSAGE;
			String versionnamespace = MessengerService.NAMESPACE;
			String versionsoapaction = versionnamespace + "/"
					+ versionmethodname;

			SoapObject rpc = new SoapObject(versionnamespace, versionmethodname);
			rpc.addProperty("toUser", cursor.getString(1).toString());
			SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
					SoapEnvelope.VER11);
			envelope.bodyOut = rpc;
			envelope.dotNet = true;
			envelope.setOutputSoapObject(rpc);
			HttpTransportSE ht = new HttpTransportSE(versionurl);
			ht.debug = true;
			try {
				ht.call(versionsoapaction, envelope);
				versionlist = (SoapObject) envelope.bodyIn;
				SoapObject tempso = (SoapObject) versionlist.getProperty(0);
				SoapObject tempsoson = (SoapObject) tempso.getProperty(1);
				SoapObject tempsosonson = (SoapObject) tempsoson.getProperty(0);
				SoapObject finalsoap = (SoapObject) tempsosonson.getProperty(0);
				String pushid = finalsoap.getProperty("Id").toString();
				if (!pushid.equals("0")) {
					String pushremark = finalsoap.getProperty("Content")
							.toString();
					String pushpic = finalsoap.getProperty("Photo").toString();
					String pushtime = finalsoap.getProperty("Crtime")
							.toString();
					String pushsendername = finalsoap.getProperty(
							"FromUserName").toString();
					String pushsendertel = finalsoap.getProperty("FromUserSim")
							.toString();
					String pushsendertype = finalsoap.getProperty(
							"FromUserType").toString();
					String pushaudio = finalsoap.getProperty("Audio")
							.toString();
					String senderid = finalsoap.getProperty("FromUserID")
							.toString();
					String audiolength = finalsoap.getProperty("AudioLength")
							.toString();
					String devtype = finalsoap.getProperty("DevType")
							.toString();
					String namelist = finalsoap.getProperty("NameList")
							.toString();

					tdd = new BBGJDB(context);
					ContentValues cv = new ContentValues();
					cv.put(tdd.PUSH_WEBID, pushid);
					cv.put(tdd.PUSH_REMARK, pushremark);
					cv.put(tdd.PUSH_PIC, pushpic);
					cv.put(tdd.PUSH_TIME, pushtime);
					cv.put(tdd.PUSH_SENDER_NAME, pushsendername);
					cv.put(tdd.PUSH_SENDER_TEL, pushsendertel);
					cv.put(tdd.PUSH_SENDER_TYPE, pushsendertype);
					cv.put(tdd.PUSH_AUDIO, pushaudio);
					cv.put(tdd.PUSH_SENDER_ID, senderid);
					cv.put(tdd.PUSH_AUDIO_LENGTH, audiolength);
					cv.put(tdd.PUSH_DEV_TYPE, devtype);
					cv.put(tdd.PUSH_RCVER_NAME, namelist);
					tdd.insertpush(cv);

					showNotification(pushremark, pushid, pushremark, pushpic,
							pushtime, pushsendername, pushsendertel,
							pushsendertype, pushaudio);
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (XmlPullParserException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public void showNotification(String msgtitle, String _id, String _remark,
			String _pic, String _time, String _name, String _tel, String _type,
			String _audio) {
		String title = msgtitle;
		String ns = Context.NOTIFICATION_SERVICE;
		NotificationManager mNotificationManager = (NotificationManager) getSystemService(ns);
		CharSequence text = title;
		long when = System.currentTimeMillis();
		Notification notification = new Notification(R.drawable.pushlogo, text,
				when);

		notification.defaults = Notification.DEFAULT_SOUND;
		notification.flags = Notification.FLAG_AUTO_CANCEL;

		CharSequence contentTitle = "您有新的消息";
		CharSequence contentText = text;
		Intent notificationIntent = new Intent(this, NewPushMsgActivity.class);
		notificationIntent.putExtra("_id", _id);
		notificationIntent.putExtra("_remark", _remark);
		notificationIntent.putExtra("_pic", MessengerService.PIC_PUSH + _pic);
		notificationIntent.putExtra("_time", _time);
		notificationIntent.putExtra("_name", _name);
		notificationIntent.putExtra("_tel", _tel);
		notificationIntent.putExtra("_type", _type);
		notificationIntent.putExtra("_audio", _audio);

		// Intent notificationIntent = new Intent(this, TabComActivity.class);

		PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
				notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);
		notification.setLatestEventInfo(NewsPushService.this, contentTitle,
				contentText, contentIntent);
		System.out.println("#########################");
		mNotificationManager.notify(1, notification);
		System.out.println("#########################");
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

}
