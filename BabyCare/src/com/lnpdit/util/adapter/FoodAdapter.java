package com.lnpdit.util.adapter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.lnpdit.babycare.R;
import com.lnpdit.garden.GardenFoodCommentActivity;
import com.lnpdit.service.MessengerService;

public class FoodAdapter extends BaseAdapter {
	private class buttonViewHolder {
		TextView week_tv;
		TextView food_tv;
		// TextView time_tv;
		TextView good_tv;
		TextView comment_tv;
		Button good_bt;
		Button comment_bt;
	}

	private ArrayList<HashMap<String, Object>> mAppList;
	private LayoutInflater mInflater;
	private Context mContext;
	private String[] keyString;
	private buttonViewHolder holder;
	String phonecall;

	ArrayList<HashMap<String, Object>> remoteWindowItem = null;

	public FoodAdapter(Context c, ArrayList<HashMap<String, Object>> appList,
			int resource, String[] from, int[] to, String _phonecall) {
		mAppList = appList;
		mContext = c;
		mInflater = (LayoutInflater) mContext
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		keyString = new String[from.length];
		System.arraycopy(from, 0, keyString, 0, from.length);
		phonecall = _phonecall;
	}

	@Override
	public int getCount() {
		return mAppList.size();
	}

	@Override
	public Object getItem(int position) {
		return mAppList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	public void removeItem(int positon) {
		mAppList.remove(positon);
		this.notifyDataSetChanged();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView != null) {
			holder = (buttonViewHolder) convertView.getTag();
		} else {
			convertView = mInflater.inflate(R.layout.list_in_food, null);
			holder = new buttonViewHolder();
			holder.week_tv = (TextView) convertView.findViewById(R.id.week_tv);
			holder.food_tv = (TextView) convertView.findViewById(R.id.food_tv);
			// holder.time_tv = (TextView)
			// convertView.findViewById(R.id.time_tv);
			holder.good_bt = (Button) convertView.findViewById(R.id.good_bt);
			holder.comment_bt = (Button) convertView
					.findViewById(R.id.comment_bt);
			holder.good_tv = (TextView) convertView.findViewById(R.id.good_tv);
			holder.comment_tv = (TextView) convertView
					.findViewById(R.id.comment_tv);
			convertView.setTag(holder);
		}
		HashMap<String, Object> appInfo = mAppList.get(position);
		if (appInfo != null) {
			String week_str = (String) appInfo.get(keyString[0]);
			String course_str = (String) appInfo.get(keyString[1]);
			String laud_str = (String) appInfo.get(keyString[2]);
			String countcomment_str = (String) appInfo.get(keyString[3]);
			String foodid_str = (String) appInfo.get(keyString[4]);
			try {
//				String[] food_array = course_str.split("%");

//				String food = "";

//				for (int i = 0; i < food_array.length; i++) {
//					food = food + food_array[i] + "\n";
//				}

				holder.week_tv.setText(week_str);
				holder.food_tv.setText(course_str);
				holder.good_tv.setText(laud_str);
				holder.comment_tv.setText(countcomment_str);
				holder.good_bt.setOnClickListener(new AdapterListener(position,
						foodid_str, phonecall));
				holder.comment_bt.setOnClickListener(new CommentAdapterListener(position,
						foodid_str, phonecall));
			} catch (Exception e) {

			}
		}
		return convertView;
	}

	public void addItem(ArrayList<HashMap<String, Object>> item) {
		int count = item.size();
		for (int i = 0; i < count; i++) {
			mAppList.add(item.get(i));
		}
	}
	
	class CommentAdapterListener implements OnClickListener {
		private int position;
		private String foodid;
		private String phonecall;

		public CommentAdapterListener(int pos, String _foodid, String _phonecall) {
			// TODO Auto-generated constructor stub
			position = pos;
			foodid = _foodid;
			phonecall = _phonecall;
		}

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			Intent intent = new Intent();
			intent.putExtra("foodid", foodid);
			intent.putExtra("phonecall", phonecall);
			intent.setClass(mContext, GardenFoodCommentActivity.class);
			mContext.startActivity(intent);
		}
	}

	class AdapterListener implements OnClickListener {
		private int position;
		private String foodid;
		private String phonecall;

		public AdapterListener(int pos, String _foodid, String _phonecall) {
			// TODO Auto-generated constructor stub
			position = pos;
			foodid = _foodid;
			phonecall = _phonecall;
		}

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub

			mGetTopicDataThread runnable = new mGetTopicDataThread();
			Thread thread = new Thread(runnable);
			thread.start();
		}
	}

	Handler threadMessageHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (msg.arg1 == 1) {
				String res = msg.obj.toString();
				if(res.startsWith("ok")){
					Toast.makeText(mContext, "点赞成功", Toast.LENGTH_SHORT).show();
				}else{
					Toast.makeText(mContext, "不能重复点赞", Toast.LENGTH_SHORT).show();
				}
//				ArrayList<HashMap<String, Object>> remoteWindowItem = (ArrayList<HashMap<String, Object>>) msg.obj;
//
//				FoodAdapter displayAdapter = new FoodAdapter(mContext,
//						remoteWindowItem, R.layout.list_in_food, new String[] {
//								"week_str", "food_str", "laud", "countcomment",
//								"foodid_str" }, new int[] { R.id.textview,
//								R.id.textview, R.id.textview, R.id.textview },
//						phonecall);
//				lv_news.setAdapter(displayAdapter);
			}
		}
	};

	private class mGetTopicDataThread implements Runnable {

		String fdid = "";
		String pc = "";
		String item = "";

		public void setParser(String _foodid, String _phonecall, String _item) {
			fdid = _foodid;
			pc = _phonecall;
			item = _item;
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			String comurl = MessengerService.URL;
			String commethodname = MessengerService.METHOD_LOUDINFOINSERT;
			String comnamespace = MessengerService.NAMESPACE;
			String comsoapaction = comnamespace + "/" + commethodname;

			SoapObject rpc = new SoapObject(comnamespace, commethodname);
			rpc.addProperty("phonecall", phonecall);
			rpc.addProperty("itemId", fdid);
			rpc.addProperty("item", "food");
			SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
					SoapEnvelope.VER11);
			envelope.bodyOut = rpc;
			envelope.dotNet = true;
			envelope.setOutputSoapObject(rpc);
			HttpTransportSE ht = new HttpTransportSE(comurl);
			ht.debug = true;
			try {
				ht.call(comsoapaction, envelope);

				SoapObject journal = (SoapObject) envelope.bodyIn;
				SoapObject soapchilds = (SoapObject) journal.getProperty(0);
				SoapObject soapchildss = (SoapObject) soapchilds.getProperty(1);
				SoapObject soapchildsss = (SoapObject) soapchildss
						.getProperty(0);

				SoapObject soapfinal = (SoapObject) soapchildsss.getProperty(0);

				String returnValue = soapfinal.getProperty("returnValue")
						.toString();
				Message msg = new Message();
				msg.arg1 = 1;
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
}