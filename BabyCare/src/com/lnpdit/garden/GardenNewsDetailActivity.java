package com.lnpdit.garden;

import java.io.IOException;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.xmlpull.v1.XmlPullParserException;

import com.lnpdit.babycare.R;
import com.lnpdit.service.MessengerService;
import com.lnpdit.sqllite.BBGJDB;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebSettings.LayoutAlgorithm;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class GardenNewsDetailActivity extends Activity implements
		OnClickListener {
	private Context context;
	private Resources resources;
	public static GardenNewsDetailActivity newsInstance = null;

	private ListView listview;
	private ImageView imageview;

	private String title = "";
	private String time = "";
	private String source = "";
	private String author = "";
	private String pic = "";
	private String content = "";
	private String id = "";
	private String type = "";

	private String namespace;
	private String url;
	private String soapaction;
	private String methodname;
	private SoapObject news;

	WebView content_webview;
	private TextView gardennewsdetail_back;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.list_in_newsdetail);
		context = this;
		newsInstance = this;
		resources = this.getResources();
		gardennewsdetail_back = (TextView) findViewById(R.id.gardennewsdetail_back);
		gardennewsdetail_back.setOnClickListener(this);

		Intent intent = getIntent();
		title = intent.getStringExtra("Title");
		time = intent.getStringExtra("Time");
		source = intent.getStringExtra("Source");
		author = intent.getStringExtra("Author");
		pic = intent.getStringExtra("Pic");
		content = intent.getStringExtra("Content");
		type = intent.getStringExtra("Type");
		id = intent.getStringExtra("Webid");

		TextView title_text = (TextView) this
				.findViewById(R.id.newsdetail_title);
		TextView time_text = (TextView) this.findViewById(R.id.newsdetail_time);
		content_webview = (WebView) this.findViewById(R.id.newsdetail_content);

		title_text.setText(title);
		time_text.setText(time);

		try {
			// if (content.trim().equals("anyType{}")) {
			mGetNewsData getContentRunnable = new mGetNewsData();
			Thread thread = new Thread(getContentRunnable);
			thread.start();
			// } else {
			// content_webview.getSettings().setSupportZoom(true);
			// content_webview.clearCache(true);
			// content_webview.getSettings().setDefaultTextEncodingName(
			// "utf-8");
			// content_webview.getSettings().setLayoutAlgorithm(
			// LayoutAlgorithm.SINGLE_COLUMN);
			// content_webview.loadDataWithBaseURL(null, content, "text/html",
			// "utf-8", null);
			// Toast.makeText(context, "old_news", Toast.LENGTH_SHORT).show();
			// }
		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	Handler threadMessageHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.arg1) {
			case 1:
				// getData();
				// dialog.dismiss();
				try {
					content_webview.getSettings().setSupportZoom(true);
					content_webview.clearCache(true);
					content_webview.getSettings().setDefaultTextEncodingName(
							"utf-8");
					content_webview.getSettings().setLayoutAlgorithm(
							LayoutAlgorithm.SINGLE_COLUMN);
					content_webview.loadDataWithBaseURL(null,
							msg.obj.toString(), "text/html", "utf-8", null);
				} catch (Exception e) {
					// TODO: handle exception
				}
				break;
			default:
				break;
			}
		}
	};

	public class mGetNewsData implements Runnable {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			String url = MessengerService.URL;
			String methodname = MessengerService.METHOD_GETNEWSCONTENT;
			String namespace = MessengerService.NAMESPACE;
			String soapaction = namespace + "/" + methodname;
			SoapObject rpc = new SoapObject(namespace, methodname);
			rpc.addProperty("id", id);
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
				SoapObject newslist = (SoapObject) envelope.bodyIn;
				SoapObject soapchilds = (SoapObject) newslist.getProperty(0);
				String newscontent = soapchilds.getProperty("Content")
						.toString();
				ContentValues values = new ContentValues();
				values.put(tdd.NEWS_CONTENT, newscontent);
				tdd.updatenews(id, values);
				tdd.close();
				Message msg = new Message();
				msg.arg1 = 1;
				msg.obj = newscontent;
				threadMessageHandler.sendMessage(msg);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (XmlPullParserException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
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
		case R.id.gardennewsdetail_back:

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
