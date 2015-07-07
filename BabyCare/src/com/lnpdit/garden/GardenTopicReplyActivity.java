package com.lnpdit.garden;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.xmlpull.v1.XmlPullParserException;

import com.lnpdit.babycare.LoginActivity;
import com.lnpdit.babycare.R;
import com.lnpdit.service.MessengerService;
import com.lnpdit.sqllite.BBGJDB;
import com.lnpdit.util.ShowDialog;
import com.lnpdit.util.adapter.ReplyAdapter;
import com.lnpdit.util.adapter.TopicAdapter;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebSettings.LayoutAlgorithm;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class GardenTopicReplyActivity extends Activity {

	Context context;
	Resources resources;
	String webid;
	String title;
	String content;
	String time;
	String number;

	RelativeLayout titleLayout;
	TextView titleTextView;
	TextView timeTextView;
	Button commitButton;
	ListView replyListView;

	private TextView gardentopicreply_back;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.gardentopic_reply);
		context = this;
		resources = this.getResources();
		try {
			Intent intent = this.getIntent();
			webid = intent.getStringExtra("Webid");
			title = intent.getStringExtra("Title");
			content = intent.getStringExtra("Content");
			time = intent.getStringExtra("Time");
			number = intent.getStringExtra("Number");
			viewInit();

			mGetReplyDataThread gdt = new mGetReplyDataThread();
			gdt.getSyncState(context);
			Thread thread = new Thread(gdt);
			thread.start();
		} catch (Exception e) {
			// TODO: handle exception
			Log.e("Topic Reply Error : ", e.toString());
		}
	}

	private void viewInit() {
		titleLayout = (RelativeLayout) this
				.findViewById(R.id.topic_title_content_head_layout);
		titleTextView = (TextView) this
				.findViewById(R.id.policy_content_head_title_text);
		timeTextView = (TextView) this.findViewById(R.id.topic_reply_time_text);
		commitButton = (Button) this.findViewById(R.id.topic_reply_commit_bt);
		replyListView = (ListView) this.findViewById(R.id.topic_reply_listview);
		gardentopicreply_back = (TextView) findViewById(R.id.gardentopicreply_back);

		titleLayout.setClickable(true);
		titleLayout.setOnClickListener(btListener);
		commitButton.setOnClickListener(btListener);
		gardentopicreply_back.setOnClickListener(btListener);
		titleTextView.setText(title);
		timeTextView.setText("发表时间：" + time);
	}

	Handler threadMessageHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			View contentView = LayoutInflater.from(context).inflate(
					R.layout.viewlayout_topic_reply_content, null);
			WebView contentWebView = (WebView) contentView
					.findViewById(R.id.topic_reply_content_webview);
			contentWebView.getSettings().setSupportZoom(true);
			contentWebView.clearCache(true);
			contentWebView.getSettings().setDefaultTextEncodingName("utf-8");
			contentWebView.getSettings().setLayoutAlgorithm(
					LayoutAlgorithm.SINGLE_COLUMN);
			contentWebView.loadDataWithBaseURL(null, content, "text/html",
					"utf-8", null);
			// contentWebView.getSettings().setBuiltInZoomControls(true); //
			// 显示放大缩小
			// // controler
			// contentWebView.getSettings().setSupportZoom(true); // 可以缩放
			// contentWebView.getSettings().setDefaultZoom(ZoomDensity.CLOSE);//
			// 默认缩放模式
			replyListView.addHeaderView(contentView);
			contentWebView.setBackgroundColor(0);
			GetData();
		}
	};

	private void commitReply() {
		try {
			BBGJDB tdd = new BBGJDB(context);
			Cursor cursor = tdd.selectuser();
			if (cursor.getCount() == 0) {
				Toast.makeText(context, "请先进行登录", Toast.LENGTH_SHORT).show();
				Intent intent = new Intent();
				intent.setClass(GardenTopicReplyActivity.this,
						LoginActivity.class);
				startActivity(intent);
			} else {
				cursor.moveToFirst();
				EditText reply_edit = (EditText) this
						.findViewById(R.id.topic_reply_edit);
				final String reply_str = reply_edit.getText().toString();
				final String usrID = cursor.getString(1).toString();
				if (reply_str.trim().equals("")) {
					Toast.makeText(context, "请填写回复内容!", Toast.LENGTH_SHORT)
							.show();
				} else {
					new Thread() {
						public void run() {
							SubmitReply(reply_str, usrID);
						}
					}.start();
					new GardenTopicActivity().isAlive = false;
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}

	private void SubmitReply(String reply_content, String com_id) {
		String content = reply_content;
		String comid = com_id;

		String rp_namespace;
		String rp_url;
		String rp_soapaction;
		String rp_methodname;
		SoapObject replylist;
		rp_url = MessengerService.URL_WITHOUT_WSDL;
		rp_methodname = MessengerService.METHOD_COMMUNREPLYADD;
		rp_namespace = MessengerService.NAMESPACE;
		rp_soapaction = rp_namespace + "/" + rp_methodname;
		SoapObject rpc = new SoapObject(rp_namespace, rp_methodname);

		SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
				SoapEnvelope.VER11);
		rpc.addProperty("replyid", webid);
		rpc.addProperty("content", content);
		rpc.addProperty("userId", com_id);
		envelope.bodyOut = rpc;
		envelope.dotNet = true;
		envelope.setOutputSoapObject(rpc);
		HttpTransportSE ht = new HttpTransportSE(rp_url);
		ht.debug = true;
		try {
			ht.call(rp_soapaction, envelope);
			replylist = (SoapObject) envelope.bodyIn;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (XmlPullParserException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			ShowDialog sd_ep = new ShowDialog();
			sd_ep.mShowDialog(context, "系统提示",
					resources.getString(R.string.replay_failed));
		}
		// mGetReplyDataThread gdt = new mGetReplyDataThread();
		// gdt.getSyncState(context);
		// Thread thread = new Thread(gdt);
		// thread.start();
		// ArrayList<HashMap<String, Object>> remoteWindowItem = new
		// ArrayList<HashMap<String, Object>>();
		// HashMap<String, Object> mapdevinfo = new HashMap<String, Object>();
		// mapdevinfo.put("Title", cursor.getString(4));
		// mapdevinfo.put("Time", cursor.getString(3));
		// mapdevinfo.put("User", cursor.getString(5));
		// mapdevinfo.put("RealName", cursor.getString(2));
		// remoteWindowItem.add(mapdevinfo);
		// ReplyAdapter lvbt = new ReplyAdapter(context, remoteWindowItem,
		// R.layout.list_in_question, new String[] { "Title", "Time",
		// "User", "RealName" }, new int[] {
		// R.id.list_question_title, R.id.list_question_title,
		// R.id.list_question_title, R.id.list_question_title },
		// resources);
		// lvbt.addItem(remoteWindowItem);
		GardenTopicReplyActivity.this.finish();
	}

	private android.view.View.OnClickListener btListener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			switch (v.getId()) {
			case R.id.topic_title_content_head_layout:
				finish();
				break;
			case R.id.topic_reply_commit_bt:
				commitReply();
				break;
			case R.id.gardentopicreply_back:

				finish();

				break;
			default:
				break;
			}
		}
	};

	private void GetData() {
		ArrayList<HashMap<String, Object>> remoteWindowItem = new ArrayList<HashMap<String, Object>>();
		BBGJDB tdd = new BBGJDB(context);
		Cursor cursor = tdd.selectreply();
		cursor.moveToFirst();
		for (int i = 0; i < cursor.getCount(); i++) {
			HashMap<String, Object> mapdevinfo = new HashMap<String, Object>();
			mapdevinfo.put("Title", cursor.getString(4));
			mapdevinfo.put("Time", cursor.getString(3));
			mapdevinfo.put("User", cursor.getString(5));
			mapdevinfo.put("RealName", cursor.getString(2));
			remoteWindowItem.add(mapdevinfo);
			cursor.moveToNext();
		}
		ReplyAdapter lvbt = new ReplyAdapter(context, remoteWindowItem,
				R.layout.list_in_question, new String[] { "Title", "Time",
						"User", "RealName" }, new int[] {
						R.id.list_question_title, R.id.list_question_title,
						R.id.list_question_title, R.id.list_question_title },
				resources);
		replyListView.setAdapter(lvbt);
	}

	private class mGetReplyDataThread implements Runnable {

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
			BBGJDB tdd = new BBGJDB(_context);
			tdd.clearreply();
			String qurl = MessengerService.URL;
			String qmethodname = MessengerService.METHOD_GETCOMUNREPLY;
			String qnamespace = MessengerService.NAMESPACE;
			String qsoapaction = qnamespace + "/" + qmethodname;

			SoapObject rpc = new SoapObject(qnamespace, qmethodname);
			rpc.addProperty("id", webid);
			rpc.addProperty("pagesize", 30);
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
				SoapObject replylist = (SoapObject) envelope.bodyIn;
				for (int i = 0; i < replylist.getPropertyCount(); i++) {
					SoapObject soapchilds = (SoapObject) replylist
							.getProperty(0);
					for (int j = 0; j < soapchilds.getPropertyCount(); j++) {
						SoapObject soapchildsson = (SoapObject) soapchilds
								.getProperty(j);

						String rtitle = soapchildsson.getProperty("Content")
								.toString();
						String rtime = soapchildsson.getProperty("Crtime")
								.toString();
						String ruser = soapchildsson.getProperty("Userid")
								.toString();
						String rusername = soapchildsson
								.getProperty("RealName").toString();

						ContentValues values = new ContentValues();
						values.put(tdd.RPL_CONTENT, rtitle);
						values.put(tdd.RPL_TIME, rtime);
						values.put(tdd.RPL_WEBID, ruser);
						values.put(tdd.RPL_NAME, rusername);
						tdd.insertreply(values);
					}
				}
			} catch (Exception e) {
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
