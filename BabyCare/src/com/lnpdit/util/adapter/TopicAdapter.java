package com.lnpdit.util.adapter;

import java.util.ArrayList;
import java.util.HashMap;

import org.ksoap2.serialization.SoapObject;

import com.lnpdit.babycare.R;
import com.lnpdit.garden.GardenTopicReplyActivity;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;


public class TopicAdapter extends BaseAdapter {
	private class buttonViewHolder {
		TextView apptitle;
		TextView appisanswer;
	}

	private ArrayList<HashMap<String, Object>> mAppList;
	private LayoutInflater mInflater;
	private Context mContext;
	private String[] keyString;
	private buttonViewHolder holder;
	private Resources resources;

	private String namespace;
	private String url;
	private String soapaction;
	private String methodname;
	private SoapObject answer;

	public TopicAdapter(Context c,
			ArrayList<HashMap<String, Object>> appList, int resource,
			String[] from, int[] to, Resources mResources) {
		mAppList = appList;
		mContext = c;
		resources = mResources;
		mInflater = (LayoutInflater) mContext
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		keyString = new String[from.length];
		System.arraycopy(from, 0, keyString, 0, from.length);
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
			convertView = mInflater.inflate(R.layout.list_in_communication_reply, null);
			holder = new buttonViewHolder();
			holder.apptitle = (TextView) convertView
					.findViewById(R.id.list_in_com_reply_title_text);
			holder.appisanswer = (TextView) convertView
					.findViewById(R.id.list_in_com_reply_num_text);
			convertView.setTag(holder);
		}
		HashMap<String, Object> appInfo = mAppList.get(position);
		if (appInfo != null) {
			String id = (String) appInfo.get(keyString[0]);
			String title = (String) appInfo.get(keyString[1]);
			String content = (String) appInfo.get(keyString[2]);
			String time = (String) appInfo.get(keyString[3]);
			String number = (String) appInfo.get(keyString[4]);

			holder.apptitle.setText(title);
			holder.appisanswer.setText(number + "»Ø¸´");

			convertView.setOnClickListener(new AdapterListener(position, id,
					title, content, time, number));

		}
		return convertView;
	}
	
	public void addItem(ArrayList<HashMap<String, Object>> item) {
		int count = item.size();
		for (int i = 0; i < count; i++) {
			mAppList.add(item.get(i));
		}
	}

	class AdapterListener implements OnClickListener {
		private int position;
		private String Webid;
		private String Title;
		private String Content;
		private String Time;
		private String Number;

		public AdapterListener(int pos, String webid, String title,
				String content, String time, String number) {
			// TODO Auto-generated constructor stub
			position = pos;
			Webid = webid;
			Title = title;
			Content = content;
			Time = time;
			Number = number;
		}

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			Intent intent = new Intent();
			intent.setClass(mContext, GardenTopicReplyActivity.class);
			intent.putExtra("Webid", Webid);
			intent.putExtra("Title", Title);
			intent.putExtra("Content", Content);
			intent.putExtra("Time", Time);
			intent.putExtra("Number", Number);
			mContext.startActivity(intent);
		}
	}
}
