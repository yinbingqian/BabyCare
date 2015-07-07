package com.lnpdit.garden;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.xmlpull.v1.XmlPullParserException;

import com.lnpdit.babycare.R;
import com.lnpdit.service.MessengerService;
import com.lnpdit.sqllite.BBGJDB;
import com.lnpdit.util.PhotoZoomListener;
import com.lnpdit.util.PhotoZoomState;
import com.lnpdit.util.PhotoZoomView;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ZoomControls;


public class GardenPhotoInfoActivity extends Activity {

	private PhotoZoomView mZoomView;
	private PhotoZoomState mZoomState;
	private PhotoZoomListener mZoomListener;

	Context context;
	Resources resources;
	PhotoZoomView imageview;
	Bundle bundle;
	String pic_url;
	String webid;
	Bitmap bitmap = null;
	private ProgressDialog dialog;

	Button bt_next_page;
	Button bt_previous_page;
	TextView tv_page_chip;
	int page = 1;
	int allpage = 1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.gardenphotoinfo);
		context = this;
		resources = this.getResources();
		mZoomView = (PhotoZoomView) findViewById(R.id.journalinfo_imageview);
		bt_next_page = (Button) findViewById(R.id.journal_info_nextpage);
		bt_previous_page = (Button) findViewById(R.id.journal_info_previouspage);
		tv_page_chip = (TextView) findViewById(R.id.journalinfo_pagechip_textview);
		page = 1;
		bt_next_page.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				getNextpage();
			}
		});
		bt_previous_page.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				getPreviouspage();
			}
		});
		Intent intent = getIntent();
		pic_url = intent.getStringExtra("PIC");
		webid = intent.getStringExtra("WEBID");

		dialog = new ProgressDialog(context);
		dialog.setMessage("正在加载图片,请稍等.");
		dialog.setIndeterminate(true);
		dialog.setCancelable(true);
		dialog.show();
		UpdatePicture up = new UpdatePicture();
		up.setURL(pic_url);
		Thread thread = new Thread(up);
		thread.start();
		Thread thread_picinfo = new Thread(new getJournalInfoThread());
		thread_picinfo.start();

		ZoomControls zoomCtrl = (ZoomControls) findViewById(R.id.journalinfo_zoomCtrl);
		zoomCtrl.setOnZoomInClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				float z = mZoomState.getZoom() + 0.25f;
				float up_state = mZoomState.getZoom();
				if (up_state < 4.1) {
					mZoomState.setZoom(z);
					mZoomState.notifyObservers();
				}
			}
		});
		zoomCtrl.setOnZoomOutClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				float z = mZoomState.getZoom() - 0.25f;
				float down_state = mZoomState.getZoom();
				if (down_state > 0.9) {
					mZoomState.setZoom(z);
					mZoomState.notifyObservers();
				}
			}
		});
	}

	private void getNextpage() {
		if (page == allpage) {
			Toast.makeText(context, "当前已经是最后一页", Toast.LENGTH_SHORT).show();
			return;
		}
		page = page + 1;
		Message msg = new Message();
		msg.arg1 = page;
		journalinfothreadMessageHandler.sendMessage(msg);
		getPageData(page);
	}

	private void getPreviouspage() {
		if (page == 1) {
			Toast.makeText(context, "当前已经是第一页", Toast.LENGTH_SHORT).show();
			return;
		}
		page = page - 1;
		Message msg = new Message();
		msg.arg1 = page;
		journalinfothreadMessageHandler.sendMessage(msg);
		getPageData(page);
	}

	private void getPageData(int _page) {
		BBGJDB tdd = new BBGJDB(context);
		Cursor cursor = tdd.selectjournalinfo();
		UpdatePicture up = new UpdatePicture();
		bitmap = null;
		if (_page > 1) {
			cursor.moveToPosition(page - 2);
			// String a = resources.getString(R.string.pic_journal);
			// String b = cursor.getString(3).toString();
			up.setURL(MessengerService.PIC_JOURNAL + cursor.getString(3));
			// up.setURL(a + b);
		} else {
			up.setURL(pic_url);
		}
		Thread thread = new Thread(up);
		thread.start();
		dialog.show();
	}

	Handler threadMessageHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			bitmap = (Bitmap) msg.obj;
			mZoomView.setImage(bitmap);
			mZoomState = new PhotoZoomState();
			mZoomView.setZoomState(mZoomState);
			mZoomListener = new PhotoZoomListener();
			mZoomListener.setZoomState(mZoomState);
			mZoomView.setOnTouchListener(mZoomListener);
			resetZoomState();
			dialog.dismiss();
		}
	};

	Handler journalinfothreadMessageHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			tv_page_chip.setText(String.valueOf(msg.arg1) + " / "
					+ String.valueOf(allpage));
		}
	};

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (bitmap != null)
			bitmap.recycle();
		// mZoomView.setOnTouchListener(null);
		// mZoomState.deleteObservers();
	}

	private void resetZoomState() {
		mZoomState.setPanX(0.5f);
		mZoomState.setPanY(0.5f);
		mZoomState.setZoom(1f);
		mZoomState.notifyObservers();
	}

	public class getJournalInfoThread implements Runnable {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			BBGJDB tdd = new BBGJDB(context);
			String qurl = MessengerService.URL;
			String qmethodname = MessengerService.METHOD_GETMAGAZINEPICINFO;
			String qnamespace = MessengerService.NAMESPACE;
			String qsoapaction = qnamespace + "/" + qmethodname;

			SoapObject rpc = new SoapObject(qnamespace, qmethodname);
			rpc.addProperty("magid", webid);
			SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
					SoapEnvelope.VER11);
			envelope.bodyOut = rpc;
			envelope.dotNet = true;
			envelope.setOutputSoapObject(rpc);
			HttpTransportSE ht = new HttpTransportSE(qurl);
			ht.debug = true;
			try {
				ht.call(qsoapaction, envelope);
				SoapObject journal = (SoapObject) envelope.bodyIn;
				for (int i = 0; i < journal.getPropertyCount(); i++) {
					SoapObject soapchilds = (SoapObject) journal.getProperty(i);
					allpage = soapchilds.getPropertyCount() + 1;
					for (int j = 0; j < soapchilds.getPropertyCount(); j++) {
						SoapObject soapchildsson = (SoapObject) soapchilds
								.getProperty(j);

						String webid = soapchildsson.getProperty("Id")
								.toString();
						String journalid = soapchildsson.getProperty("MagID")
								.toString();
						String pic = soapchildsson.getProperty("Pic")
								.toString();
						String orders = soapchildsson.getProperty("Orders")
								.toString();

						ContentValues values = new ContentValues();
						values.put(tdd.JOURNAL_INFO_WEBID, webid);
						values.put(tdd.JOURNAL_INFO_JID, journalid);
						values.put(tdd.JOURNAL_INFO_PIC, pic);
						values.put(tdd.JOURNAL_INFO_ORDER, orders);
						tdd.insertjournalinfo(values);
					}
				}
				Message msg = new Message();
				msg.arg1 = 1;
				journalinfothreadMessageHandler.sendMessage(msg);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (XmlPullParserException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	public class UpdatePicture implements Runnable {

		String url = "";

		public void setURL(String _url) {
			this.url = _url;
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			String url_str = url;
			try {
				URL url = new URL(url_str);
				HttpURLConnection conn = (HttpURLConnection) url
						.openConnection();
				conn.setDoInput(true);
				conn.connect();
				InputStream is = conn.getInputStream();
				Bitmap bmImg = BitmapFactory.decodeStream(is);
				Message msg = new Message();
				msg.obj = bmImg;
				threadMessageHandler.sendMessage(msg);
				Thread.sleep(300);
				is.close();

			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
