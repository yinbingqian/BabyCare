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
import com.lnpdit.service.SyncService;
import com.lnpdit.sqllite.BBGJDB;
import com.lnpdit.util.adapter.ImageAndChuQinListAdapter;
import com.lnpdit.util.adapter.ImageAndText;
import com.lnpdit.util.adapter.ImageAndTextListAdapter;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.WebSettings.LayoutAlgorithm;
import android.webkit.WebSettings.PluginState;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

public class GardenChuqinActivity extends Activity implements OnClickListener {
	private Resources resources;
	private Context context;

	RelativeLayout layout;

	private mServiceRemoveProcessDialog mReceiver = null;
	private ProgressDialog dialog;
	int chuqin_number = 0;

	private int USER_NOTMATCH = 1;
	private int USER_ISLOCK = 2;

	/** Handler What加载数据完毕 **/
	private static final int WHAT_DID_LOAD_DATA = 0;
	/** Handler What更新数据完毕 **/
	private static final int WHAT_DID_REFRESH = 1;
	/** Handler What更多数据完毕 **/
	private static final int WHAT_DID_MORE = 2;

	ListView chuqin_list;
	private TextView gardenchuqin_back;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.gardenchuqin);

		resources = this.getResources();
		context = this;

		try {
			initViews();
			// getData();
		} catch (Exception e) {
			// TODO: handle exception
		}
		mReceiver = new mServiceRemoveProcessDialog();
		IntentFilter mFilter = new IntentFilter(
				SyncService.SERVICEUI_REMOVEDIALOG);
		registerReceiver(mReceiver, mFilter);
		// if (news_number == 0) {
		BBGJDB tdd = new BBGJDB(context);
		tdd.clearnews();
		dialog = new ProgressDialog(context);
		dialog.setMessage("正在加载,请稍等.");
		dialog.setIndeterminate(true);
		dialog.setCancelable(true);
		dialog.show();
		mGetNewsData getNewsRunnable = new mGetNewsData();
		Thread thread = new Thread(getNewsRunnable);
		thread.start();
		// }

	}

	Handler threadMessageHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.arg1) {
			case 1:
				getData();
				dialog.dismiss();
				break;
			default:
				break;
			}
		}
	};

	private void initViews() {
		gardenchuqin_back = (TextView) findViewById(R.id.gardenchuqin_back);
		gardenchuqin_back.setOnClickListener(this);

	}

	public void getData() {
		chuqin_list = (ListView) findViewById(R.id.chuqin_list);

		try {
			BBGJDB tdd = new BBGJDB(context);
			Cursor cursor = tdd.selectnews();
			chuqin_number = cursor.getCount();

			List<ImageAndText> imageAndTexts = new ArrayList<ImageAndText>();
			if (cursor.moveToFirst()) {
				for (int i = 0; i < chuqin_number; i++) {
					if (cursor.getString(7).equals("F")) {
						String title = cursor.getString(1);
						String time = cursor.getString(6);
						String source = cursor.getString(2);
						String author = cursor.getString(3);
						String pic = cursor.getString(4);
						String content = cursor.getString(5);
						String type = cursor.getString(7);
						String id = cursor.getString(8);
						String thumbnail = cursor.getString(9);
						ImageAndText it = new ImageAndText(
								MessengerService.PIC_FILE + thumbnail, title,
								time, content, source, author, pic, type, id);
						imageAndTexts.add(it);
					}
					cursor.moveToNext();
				}
			}

			cursor.close();

			ImageAndChuQinListAdapter ia = new ImageAndChuQinListAdapter(
					GardenChuqinActivity.this, imageAndTexts, chuqin_list,
					context);
			chuqin_list.setAdapter(ia);
			tdd.close();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}

	public class mGetNewsData implements Runnable {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			SharedPreferences share = getSharedPreferences("BBGJ_UserInfo",
					Activity.MODE_WORLD_READABLE);
			int comId = share.getInt("comId", 0);

			String url = MessengerService.URL;
			String methodname = MessengerService.METHOD_GETLATESTNEWS;
			String namespace = MessengerService.NAMESPACE;
			String soapaction = namespace + "/" + methodname;
			SoapObject rpc = new SoapObject(namespace, methodname);
			rpc.addProperty("comId", comId);
			SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
					SoapEnvelope.VER11);
			envelope.bodyOut = rpc;
			envelope.dotNet = true;
			envelope.setOutputSoapObject(rpc);
			HttpTransportSE ht = new HttpTransportSE(url);
			ht.debug = true;
			try {
				BBGJDB tdd = new BBGJDB(context);
				ht.call(soapaction, envelope);
				tdd.clearnews();
				SoapObject newslist = (SoapObject) envelope.bodyIn;
				for (int i = 0; i < newslist.getPropertyCount(); i++) {
					SoapObject soapchilds = (SoapObject) newslist
							.getProperty(i);
					for (int j = 0; j < soapchilds.getPropertyCount(); j++) {
						SoapObject soapchildsson = (SoapObject) soapchilds
								.getProperty(j);

						String newsid = soapchildsson.getProperty("Id")
								.toString();
						String newstitle = soapchildsson.getProperty("Title")
								.toString();
						String newscontent = soapchildsson.getProperty(
								"Content").toString();
						String newstime = soapchildsson.getProperty("Crtime")
								.toString();
						String newssource = soapchildsson.getProperty("Source")
								.toString();
						String newsauthor = soapchildsson.getProperty("Author")
								.toString();
						String newsthumbnail = soapchildsson.getProperty(
								"Thumbnail").toString();
						String newspicture = soapchildsson.getProperty(
								"Picture").toString();
						String newstype = soapchildsson.getProperty("Value")
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
					tdd.close();
					Message msg = new Message();
					msg.arg1 = 1;
					threadMessageHandler.sendMessage(msg);
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (XmlPullParserException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Exception e) {
				// TODO: handle exception
				Log.e("Get news error!!! ", e.toString());
			}
			// removeProcessdialog();
		}

	}

	private class mServiceRemoveProcessDialog extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			try {
				if (intent.getAction().equals(
						"com.syml.mobilenewspaper.removecatalogdialog.hnyx")) {
					dialog.dismiss();
					getData();
				}
			} catch (Exception e) {
				// TODO: handle exception
			}
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
		case R.id.gardenchuqin_back:

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
