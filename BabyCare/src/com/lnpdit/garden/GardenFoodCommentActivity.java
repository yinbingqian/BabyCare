package com.lnpdit.garden;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.xmlpull.v1.XmlPullParserException;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;

import com.lnpdit.babycare.R;
import com.lnpdit.service.MessengerService;
import com.lnpdit.util.adapter.FoodCommentAdapter;

public class GardenFoodCommentActivity extends Activity implements
		OnClickListener {

	Context context;
	Button foodcomment_bt;
	Button gardenfood_back;
	ListView lv_news;
	
	String foodid = "";
	String phonecall = "";

	ArrayList<HashMap<String, Object>> remoteWindowItem = null;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		context = this;
		setContentView(R.layout.foodcomment);
		Intent intent = this.getIntent();
		foodid = intent.getStringExtra("foodid");
		phonecall = intent.getStringExtra("phonecall");
		InitView();
		mGetTopicDataThread runnable = new mGetTopicDataThread();
		Thread thread = new Thread(runnable);
		thread.start();
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		mGetTopicDataThread runnable = new mGetTopicDataThread();
		Thread thread = new Thread(runnable);
		thread.start();
	}

	void InitView(){
		gardenfood_back = (Button) this.findViewById(R.id.gardenfood_back);
		foodcomment_bt = (Button) this.findViewById(R.id.foodcomment_bt);
		lv_news = (ListView) findViewById(R.id.foodcomment_list);

		foodcomment_bt.setOnClickListener(this);
		gardenfood_back.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				finish();
			}
		});

	}
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		Intent intent = new Intent();
		intent.putExtra("foodid", foodid);
		intent.putExtra("phonecall", phonecall);
		intent.setClass(context, GardenFoodAddCommentActivity.class);
		startActivity(intent);
	}
	private class mGetTopicDataThread implements Runnable {

		
		@Override
		public void run() {
			// TODO Auto-generated method stub
			String comurl = MessengerService.URL;
			String commethodname = MessengerService.METHOD_GETFOODCOMMENT;
			String comnamespace = MessengerService.NAMESPACE;
			String comsoapaction = comnamespace + "/" + commethodname;

			SoapObject rpc = new SoapObject(comnamespace, commethodname);
			 rpc.addProperty("foodId", foodid);
			SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
					SoapEnvelope.VER11);
			envelope.bodyOut = rpc;
			envelope.dotNet = true;
			envelope.setOutputSoapObject(rpc);
			HttpTransportSE ht = new HttpTransportSE(comurl);
			ht.debug = true;
			try {
				ht.call(comsoapaction, envelope);

				remoteWindowItem = new ArrayList<HashMap<String, Object>>();

				SoapObject journal = (SoapObject) envelope.bodyIn;
				SoapObject soapchilds = (SoapObject) journal.getProperty(0);
				SoapObject soapchildss = (SoapObject) soapchilds.getProperty(1);
				SoapObject soapchildsss = (SoapObject) soapchildss
						.getProperty(0);

				for (int i = 0; i < soapchildsss.getPropertyCount(); i++) {
					SoapObject soapfinal = (SoapObject) soapchildsss
							.getProperty(i);

					String foodId = soapfinal.getProperty("foodId")
							.toString();
					String CommnetContent = soapfinal.getProperty("CommnetContent")
							.toString();
					String name = soapfinal.getProperty("name").toString();
					String ComTime = soapfinal.getProperty("ComTime").toString();
					String userPic = soapfinal.getProperty("userPic").toString();

					HashMap<String, Object> mapdevinfo = new HashMap<String, Object>();
					mapdevinfo.put("foodId", foodId);
					mapdevinfo.put("CommnetContent", CommnetContent);
					mapdevinfo.put("name", name);
					mapdevinfo.put("ComTime", ComTime);
					mapdevinfo.put("userPic", userPic);
					 remoteWindowItem.add(mapdevinfo);
				}
				Message msg = new Message();
				msg.arg1 = 0;
				 msg.obj = remoteWindowItem;
				threadMessageHandler.sendMessage(msg);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (XmlPullParserException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Exception e) {
				// TODO: handle exception
				Log.e("TOPIC GET DATA ERROR : ", e.toString());
			}
		}
	}

	Handler threadMessageHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (msg.arg1 == 0) {
				ArrayList<HashMap<String, Object>> remoteWindowItem = (ArrayList<HashMap<String, Object>>) msg.obj;
				FoodCommentAdapter topicAdapter = new FoodCommentAdapter(
						context, remoteWindowItem,
						R.layout.list_in_foodcomment, new String[] {
								"foodId", "CommnetContent", "name", "ComTime",
								"userPic" }, new int[] { R.id.textview,
								R.id.textview, R.id.textview, R.id.textview,
								R.id.textview }, phonecall,lv_news);
				lv_news.setAdapter(topicAdapter);
				
				lv_news.setSelection(lv_news.getCount()-1);

			}
		}
	};
}
