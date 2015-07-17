package com.lnpdit.util.adapter;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.lnpdit.babycare.R;

public class ChuQinMonthAdapter extends BaseAdapter {
	private class buttonViewHolder {
		TextView textview;
	}

	private ArrayList<HashMap<String, Object>> mAppList;
	private LayoutInflater mInflater;
	private Context mContext;
	private String[] keyString;
	private buttonViewHolder holder;

	public ChuQinMonthAdapter(Context c,
			ArrayList<HashMap<String, Object>> appList, int resource,
			String[] from, int[] to) {
		mAppList = appList;
		mContext = c;
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
			convertView = mInflater.inflate(R.layout.list_in_chuqinmonth, null);
			holder = new buttonViewHolder();
			holder.textview = (TextView) convertView.findViewById(R.id.textview);
			convertView.setTag(holder);
		}
		HashMap<String, Object> appInfo = mAppList.get(position);
		if (appInfo != null) {
			String username = (String) appInfo.get(keyString[0]);
			String classname = (String) appInfo.get(keyString[1]);
			String year = (String) appInfo.get(keyString[2]);
			String month = (String) appInfo.get(keyString[3]);
			String exist = (String) appInfo.get(keyString[4]);
			holder.textview.setText(year + "Äê" + month + "ÔÂ");
			
//			convertView.setOnClickListener(new AdapterListener(position, year, month));
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
		private String year;
		private String month;

		public AdapterListener(int pos,String _year, String _month) {
			// TODO Auto-generated constructor stub
			position = pos;
			year = _year;
			month = _month;
		}

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
//			Intent intent = new Intent();
//			intent.setClass(mContext, ContactActivity.class);
//			intent.putExtra("Id", Id);
//			intent.putExtra("Grade", Grade);
//			intent.putExtra("Class", Class);
//			intent.putExtra("Remark", Remark);
//			mContext.startActivity(intent);
//			Toast.makeText(mContext, year, Toast.LENGTH_SHORT).show();
		}
	}
}