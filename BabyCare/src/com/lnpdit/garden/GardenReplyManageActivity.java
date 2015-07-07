package com.lnpdit.garden;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.xmlpull.v1.XmlPullParserException;

import com.lnpdit.babycare.R;
import com.lnpdit.service.MessengerService;
import com.lnpdit.util.adapter.ReplyManageAdapter;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

public class GardenReplyManageActivity extends Activity implements
		OnClickListener {

	Context context;
	Resources resources;

	String userid = "";
	ListView listview;

	private ProgressDialog dialog;
	private SoapObject reply_soap;
	private TextView gardenreply_back;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.gardentopic_replymanage);
		context = this;
		resources = this.getResources();
		viewInit();
		Intent intent = this.getIntent();
		userid = intent.getStringExtra("ID");
		Thread thread = new Thread(new getDataThread());
		thread.start();
		dialog = new ProgressDialog(context);
		dialog.setMessage("正在同步您的回复信息,请稍等.");
		dialog.setIndeterminate(true);
		dialog.setCancelable(true);
		dialog.show();
	}

	private void viewInit() {
		gardenreply_back = (TextView) findViewById(R.id.gardenreply_back);
		gardenreply_back.setOnClickListener(this);
		listview = (ListView) this.findViewById(R.id.reply_listview);
	}

	Handler threadMessageHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			ReplyManageAdapter ta = (ReplyManageAdapter) msg.obj;
			listview.setAdapter(ta);
			dialog.dismiss();
		}
	};

	private class getDataThread implements Runnable {
		ReplyManageAdapter lvbt;

		@Override
		public void run() {
			// TODO Auto-generated method stub
			String comurl = MessengerService.URL;
			String commethodname = MessengerService.METHOD_GETCOMMUNREPLYBYUSER;
			String comnamespace = MessengerService.NAMESPACE;
			String comsoapaction = comnamespace + "/" + commethodname;

			SoapObject rpc = new SoapObject(comnamespace, commethodname);
			rpc.addProperty("userid", userid);
			rpc.addProperty("pagesize", 1000);
			rpc.addProperty("pageindex", 1);
			SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
					SoapEnvelope.VER11);
			envelope.bodyOut = rpc;
			envelope.dotNet = true;
			envelope.setOutputSoapObject(rpc);
			HttpTransportSE ht = new HttpTransportSE(comurl);
			ht.debug = true;
			try {
				ht.call(comsoapaction, envelope);
				reply_soap = (SoapObject) envelope.bodyIn;
				ArrayList<HashMap<String, Object>> remoteWindowItem = new ArrayList<HashMap<String, Object>>();
				SoapObject soapchilds = (SoapObject) reply_soap.getProperty(0);
				for (int i = 0; i < soapchilds.getPropertyCount(); i++) {
					SoapObject soapchildsson = (SoapObject) soapchilds
							.getProperty(i);
					String id = soapchildsson.getProperty("Id").toString();
					String topicid = soapchildsson.getProperty("Reply")
							.toString();
					String title = soapchildsson.getProperty("Title")
							.toString();
					String userid = soapchildsson.getProperty("Userid")
							.toString();
					String type = soapchildsson.getProperty("Type").toString();
					String crtime = soapchildsson.getProperty("Crtime")
							.toString();
					String content = soapchildsson.getProperty("Content")
							.toString();
					String realname = soapchildsson.getProperty("RealName")
							.toString();
					HashMap<String, Object> mapdevinfo = new HashMap<String, Object>();
					mapdevinfo.put("ID", id);
					mapdevinfo.put("TOPICID", topicid);
					mapdevinfo.put("TITLE", title);
					mapdevinfo.put("USERID", userid);
					mapdevinfo.put("TYPE", type);
					mapdevinfo.put("CRTIME", crtime);
					mapdevinfo.put("CONTENT", content);
					mapdevinfo.put("REALNAME", realname);
					remoteWindowItem.add(mapdevinfo);
				}
				lvbt = new ReplyManageAdapter(context, remoteWindowItem,
						R.layout.list_in_reply_manage, new String[] { "ID",
								"TOPICID", "TITLE", "USERID", "TYPE", "CRTIME",
								"CONTENT", "REALNAME" }, new int[] {
								R.id.list_question_title,
								R.id.list_question_title,
								R.id.list_question_title,
								R.id.list_question_title,
								R.id.list_question_title,
								R.id.list_question_title,
								R.id.list_question_title,
								R.id.list_question_title }, resources);
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
		case R.id.gardenreply_back:

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
