package com.lnpdit.service;

import java.io.IOException;
import java.util.ArrayList;


import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.xmlpull.v1.XmlPullParserException;

import com.lnpdit.sqllite.BBGJDB;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.IBinder;

public class SyncService extends Service {

	private Context context;
	private Resources resources;

	private String namespace;
	private String url;
	private String soapaction;
	private String methodname;
	private SoapObject newslist;
	private String newsid;
	private String newstitle;
	private String newscontent;
	private String newstime;
	private String newssource;
	private String newspicture;
	private String newstype;
	private String newsauthor;
	private String newsthumbnail;

	private SoapObject questionslist;
	private String webid = "";
	private String type = "";
	private String title = "";
	private String userid = "";
	private String time = "";
	private String isanswer = "";
	// private ProgressDialog dialog;

	private SoapObject comlist;
	private String comid;
	private String comtitle;
	private String comcontent;
	private String comnumber;
	private String comtime;

	private SoapObject versionlist;

	private BBGJDB tdd;
	public static final String SERVICEUI_REMOVEDIALOG = "com.syml.mobilenewspaper.removecatalogdialog.hnyx";

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}

	@Override
	public void onStart(Intent intent, int startId) {
		// TODO Auto-generated method stub
		super.onStart(intent, startId);
		context = this.getApplicationContext();
		resources = this.getResources();
		if (intent != null) {
			tdd = new BBGJDB(context);
			tdd.cleardb();
			Thread getnews_thread = new Thread(new mGetNewsData());
			getnews_thread.start();
		}

	}

	public class mGetNewsData implements Runnable {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			url = MessengerService.URL;
			methodname = MessengerService.METHOD_GETNEWSLIST;
			namespace = MessengerService.NAMESPACE;
			soapaction = namespace + "/" + methodname;
			SoapObject rpc = new SoapObject(namespace, methodname);
			rpc.addProperty("pagesize", "100");
			SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
					SoapEnvelope.VER11);
			envelope.bodyOut = rpc;
			envelope.dotNet = true;
			envelope.setOutputSoapObject(rpc);
			HttpTransportSE ht = new HttpTransportSE(url);
			ht.debug = true;
			try {
				ht.call(soapaction, envelope);
				newslist = (SoapObject) envelope.bodyIn;
				for (int i = 0; i < newslist.getPropertyCount(); i++) {
					SoapObject soapchilds = (SoapObject) newslist
							.getProperty(i);
					for (int j = 0; j < soapchilds.getPropertyCount(); j++) {
						SoapObject soapchildsson = (SoapObject) soapchilds
								.getProperty(j);

						newsid = soapchildsson.getProperty("Id").toString();
						newstitle = soapchildsson.getProperty("Title")
								.toString();
						newscontent = soapchildsson.getProperty("Content")
								.toString();
						newstime = soapchildsson.getProperty("Crtime")
								.toString();
						newssource = soapchildsson.getProperty("Source")
								.toString();
						newsauthor = soapchildsson.getProperty("Author")
								.toString();
						newsthumbnail = soapchildsson.getProperty("Thumbnail")
								.toString();
						try {
							newspicture = soapchildsson.getProperty("Picture")
									.toString();
						} catch (Exception e) {
							// TODO: handle exception
							newspicture = "";
						}
						newstype = soapchildsson.getProperty("Value")
								.toString();

						ContentValues values = new ContentValues();
						values.put(tdd.NEWS_WEBID, newsid);
						values.put(tdd.NEWS_TITLE, newstitle);
						values.put(tdd.NEWS_CONTENT, newscontent);
						values.put(tdd.NEWS_CREATETIME, newstime);
						values.put(tdd.NEWS_SOURCE, newssource);
						values.put(tdd.NEWS_PICTURE, newspicture);
						values.put(tdd.NEWS_TYPE, newstype);
						values.put(tdd.NEWS_AUTHOR, newsauthor);
						values.put(tdd.NEWS_THUMBNAIL, newsthumbnail);
						tdd.insertnews(values);
					}
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (XmlPullParserException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			removeProcessdialog();
			boolean pushservicestate = isPushServiceWork();
			getUrlData();
			if (pushservicestate == false) {
				Intent i = new Intent(context, NewsPushService.class);
				context.startService(i);
			}
		}

	}

	public void getUrlData() {
		String qurl = MessengerService.URL;
		String qmethodname = MessengerService.METHOD_GETWEBURL;
		String qnamespace = MessengerService.NAMESPACE;
		String qsoapaction = qnamespace + "/" + qmethodname;

		SoapObject rpc = new SoapObject(qnamespace, qmethodname);
		rpc.addProperty("pagesize", 1000);
		rpc.addProperty("pageindex", 1);
		SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
				SoapEnvelope.VER11);
		envelope.bodyOut = rpc;
		envelope.dotNet = true;
		envelope.setOutputSoapObject(rpc);
		HttpTransportSE ht = new HttpTransportSE(qurl);
		ht.debug = true;
		try {
			ht.call(qsoapaction, envelope);
			questionslist = (SoapObject) envelope.bodyIn;
			for (int i = 0; i < questionslist.getPropertyCount(); i++) {
				SoapObject soapchilds = (SoapObject) questionslist
						.getProperty(i);
				for (int j = 0; j < soapchilds.getPropertyCount(); j++) {
					SoapObject soapchildsson = (SoapObject) soapchilds
							.getProperty(j);

					webid = soapchildsson.getProperty("Id").toString();
					String title = soapchildsson.getProperty("Title")
							.toString();
					String pic = soapchildsson.getProperty("Pic").toString();
					String url = soapchildsson.getProperty("Url").toString();
					String detail = soapchildsson.getProperty("Detail")
							.toString();
					String time = soapchildsson.getProperty("Crtime")
							.toString();

					ContentValues values = new ContentValues();
					values.put(tdd.URL_WEBID, webid);
					values.put(tdd.URL_TITLE, title);
					values.put(tdd.URL_PIC, pic);
					values.put(tdd.URL_URL, url);
					values.put(tdd.URL_DETAIL, detail);
					values.put(tdd.URL_CRTIME, time);
					tdd.inserturl(values);
				}
			}
			// submitLogin();
			boolean pushservicestate = isPushServiceWork();
			if (pushservicestate == false) {
				Intent i = new Intent(context, NewsPushService.class);
				context.startService(i);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (XmlPullParserException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public boolean isPushServiceWork() {
		ActivityManager myManager = (ActivityManager) context
				.getSystemService(Context.ACTIVITY_SERVICE);
		ArrayList<RunningServiceInfo> runningService = (ArrayList<RunningServiceInfo>) myManager
				.getRunningServices(30);
		for (int i = 0; i < runningService.size(); i++) {
			if (runningService.get(i).service.getClassName().toString()
					.equals("lnpdit.babycare.pushservice")) {
				return true;
			}
		}
		return false;
	}

	private void removeProcessdialog() {
		Intent intent = new Intent(SERVICEUI_REMOVEDIALOG);
		sendBroadcast(intent);
	}

}
