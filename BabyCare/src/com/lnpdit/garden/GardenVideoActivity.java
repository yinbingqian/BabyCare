package com.lnpdit.garden;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.xmlpull.v1.XmlPullParserException;

import com.lnpdit.babycare.R;
import com.lnpdit.service.MessengerService;
import com.lnpdit.sqllite.BBGJDB;
import com.lnpdit.util.ImageAndTextVideo;
import com.lnpdit.util.adapter.ImageAndTextListVideolAdapter;
import com.lnpdit.util.adapter.TopicAdapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ListView;
import android.widget.TextView;

public class GardenVideoActivity extends Activity implements OnClickListener {

	private Context context;
	private Resources resources;

	ListView listview;
	private ProgressDialog dialog;
	public boolean sync_state;
	int CLEAR_INFO = 2;
	int REFRESH_RATE = 1;
	private TextView gardenvideo_back;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.gardenvideo);
		context = this;
		resources = this.getResources();
		viewInit();
		try {
			BBGJDB tdd = new BBGJDB(context);
			tdd.clearjournallistinfo();
			tdd.close();
		} catch (Exception e) {
			// TODO: handle exception
		}
		if (listview.getCount() == 0) {
			sync_state = false;
			dialog = new ProgressDialog(this);
			dialog.setMessage("正在加载视频列表,请稍等.");
			dialog.setIndeterminate(true);
			dialog.setCancelable(true);
			dialog.show();
		}
		Thread thread = new Thread(new mGetJournalDataThread());
		thread.start();
	}

	private void viewInit() {
		listview = (ListView) findViewById(R.id.garden_video);
		gardenvideo_back = (TextView) findViewById(R.id.gardenvideo_back);
		gardenvideo_back.setOnClickListener(this);
	}

	Handler threadMessageHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			GetData();
			dialog.dismiss();
		}
	};

	private void GetData() {

		List<ImageAndTextVideo> imageAndTexts = new ArrayList<ImageAndTextVideo>();
		BBGJDB tdd = new BBGJDB(context);
		Cursor cursor = tdd.selectvideo();
		int a = cursor.getCount();
		cursor.moveToFirst();
		if (cursor.moveToFirst()) {
			for (int i = 0; i < cursor.getCount(); i++) {
				String webid1 = cursor.getString(1);
				String title1 = cursor.getString(2);
				String pic1 = cursor.getString(3);
				String video1 = cursor.getString(4);
				String extension1 = cursor.getString(5);
				String size1 = cursor.getString(6);
				String content1 = cursor.getString(7);
				String crtime1 = cursor.getString(8);
				cursor.moveToNext();
				ImageAndTextVideo itv = new ImageAndTextVideo(
						MessengerService.VIDEO_PATH + pic1, webid1, title1,
						pic1, MessengerService.VIDEO_PATH + video1, extension1,
						size1, content1, crtime1);
				imageAndTexts.add(itv);
			}
			ImageAndTextListVideolAdapter itj_adapter = new ImageAndTextListVideolAdapter(
					GardenVideoActivity.this, imageAndTexts, listview, context);
			listview.setAdapter(itj_adapter);
		}
	}

	@SuppressLint("HandlerLeak")
	private class mGetJournalDataThread implements Runnable {

		Context _context;
		TopicAdapter lvbt;
		ListView _listview;

		public void getSyncState(Context c) {
			this._context = c;
		}

		public void setListAdapter(ListView l) {
			this._listview = l;
			_listview.setAdapter(lvbt);
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			SharedPreferences share = getSharedPreferences("BBGJ_UserInfo",
					Activity.MODE_WORLD_READABLE);
			int comId = share.getInt("comId", 0);
			BBGJDB tdd = new BBGJDB(context);
			String qurl = MessengerService.URL;
			String qmethodname = MessengerService.METHOD_GETVIDEOINFO;
			String qnamespace = MessengerService.NAMESPACE;
			String qsoapaction = qnamespace + "/" + qmethodname;

			SoapObject rpc = new SoapObject(qnamespace, qmethodname);
			rpc.addProperty("pagesize", 1000);
			rpc.addProperty("pageindex", 1);
			rpc.addProperty("comId", comId);
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
					for (int j = 0; j < soapchilds.getPropertyCount(); j++) {
						SoapObject soapchildsson = (SoapObject) soapchilds
								.getProperty(j);

						String webid = soapchildsson.getProperty("Id")
								.toString();
						String title = soapchildsson.getProperty("Title")
								.toString();
						String pic = soapchildsson.getProperty("Pic")
								.toString();
						String video = soapchildsson.getProperty("Video")
								.toString();
						String extension = soapchildsson.getProperty(
								"Extension").toString();
						String size = soapchildsson.getProperty("Size")
								.toString();
						String content = soapchildsson.getProperty("Content")
								.toString();
						String crtime = soapchildsson.getProperty("Crtime")
								.toString();

						ContentValues values = new ContentValues();
						values.put(tdd.VIDEO_WEBID, webid);
						values.put(tdd.VIDEO_TITLE, title);
						values.put(tdd.VIDEO_PIC, pic);
						values.put(tdd.VIDEO_VIDEO, video);
						values.put(tdd.VIDEO_EXTENSION, extension);
						values.put(tdd.VIDEO_SIZE, size);
						values.put(tdd.VIDEO_CONTENT, content);
						values.put(tdd.VIDEO_CRTIME, crtime);
						long aaa = tdd.insertvideo(values);
					}
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (XmlPullParserException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Message msg = new Message();
			msg.obj = lvbt;
			threadMessageHandler.sendMessage(msg);
		}
	}

	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.gardenvideo_back:

			this.finish();

			break;

		default:
			break;
		}
	}

	/** 重定义返回键事件 **/
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// 拦截back按键
		if (keyCode == KeyEvent.KEYCODE_BACK
				&& event.getAction() == KeyEvent.ACTION_DOWN) {
			finish();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

}
