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
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.lnpdit.babycare.R;
import com.lnpdit.garden.GardenChuqinActivity.AsynMove;
import com.lnpdit.service.MessengerService;
import com.lnpdit.util.adapter.ChuQinAdapter;
import com.lnpdit.util.adapter.ChuQinMonthAdapter;
import com.lnpdit.util.adapter.FoodCommentAdapter;

public class GardenFoodAddCommentActivity extends Activity implements
		OnClickListener {

	Context context;
	Button cancel_bt;
	Button send_bt;
	EditText addcomment_et;

	String foodid = "";
	String phonecall = "";
	String comment_str = "";

	ArrayList<HashMap<String, Object>> remoteWindowItem = null;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		context = this;
		setContentView(R.layout.gardenaddcomment);

		WindowManager m = getWindowManager();
		Display d = m.getDefaultDisplay(); // 为获取屏幕宽、高

		LayoutParams p = getWindow().getAttributes(); // 获取对话框当前的参数值
		p.height = (int) (d.getHeight() * 0.5); // 高度设置为屏幕的1.0
		p.width = (int) (d.getWidth() * 1.0); // 宽度设置为屏幕的0.8
		p.alpha = 1.0f; // 设置本身透明度
		p.dimAmount = 0.0f; // 设置黑暗度

		getWindow().setAttributes(p); // 设置生效
		getWindow().setGravity(Gravity.RIGHT);

		Intent intent = this.getIntent();
		foodid = intent.getStringExtra("foodid");
		phonecall = intent.getStringExtra("phonecall");
		InitView();
	}

	void InitView() {
		cancel_bt = (Button) this.findViewById(R.id.cancel_bt);
		send_bt = (Button) this.findViewById(R.id.send_bt);
		send_bt.setOnClickListener(this);
		addcomment_et = (EditText) findViewById(R.id.addcomment_et);

		cancel_bt.setOnClickListener(new View.OnClickListener() {

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
		comment_str = addcomment_et.getText().toString();

		mGetTopicDataThread runnable = new mGetTopicDataThread();
		Thread thread = new Thread(runnable);
		thread.start();
	}

	private class mGetTopicDataThread implements Runnable {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			String comurl = MessengerService.URL;
			String commethodname = MessengerService.METHOD_GETFOODCOMMENTINSERT;
			String comnamespace = MessengerService.NAMESPACE;
			String comsoapaction = comnamespace + "/" + commethodname;

			SoapObject rpc = new SoapObject(comnamespace, commethodname);
			rpc.addProperty("foodId", foodid);
			rpc.addProperty("phonecall", phonecall);
			rpc.addProperty("content", comment_str);
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

				SoapObject soapfinal = (SoapObject) soapchildsss.getProperty(0);

				String returnValue = soapfinal.getProperty("returnValue")
						.toString();

				Message msg = new Message();
				msg.arg1 = 0;
				msg.obj = returnValue;
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
			String res = msg.obj.toString();
			if(res.startsWith("t")){
				Toast.makeText(context, "发布成功", Toast.LENGTH_SHORT).show();
				finish();
			}else{
				Toast.makeText(context, "发布失败", Toast.LENGTH_SHORT).show();
			}
			// if (msg.arg1 == 0) {
			// ArrayList<HashMap<String, Object>> remoteWindowItem =
			// (ArrayList<HashMap<String, Object>>) msg.obj;
			// FoodCommentAdapter topicAdapter = new FoodCommentAdapter(
			// context, remoteWindowItem,
			// R.layout.list_in_foodcomment, new String[] {
			// "foodId", "CommnetContent", "name", "ComTime",
			// "userPic" }, new int[] { R.id.textview,
			// R.id.textview, R.id.textview, R.id.textview,
			// R.id.textview }, phonecall,lv_news);
			// lv_news.setAdapter(topicAdapter);
			//
			// }
		}
	};
}
