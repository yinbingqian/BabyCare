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

public class CourseMonthAdapter extends BaseAdapter {
	private class buttonViewHolder {
		TextView textview;
	}

	private ArrayList<HashMap<String, Object>> mAppList;
	private LayoutInflater mInflater;
	private Context mContext;
	private String[] keyString;
	private buttonViewHolder holder;

	public CourseMonthAdapter(Context c,
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
			convertView = mInflater.inflate(R.layout.list_in_coursemonth, null);
			holder = new buttonViewHolder();
			holder.textview = (TextView) convertView.findViewById(R.id.txt1);
			convertView.setTag(holder);
		}
		HashMap<String, Object> appInfo = mAppList.get(position);
		if (appInfo != null) {
			String comId = (String) appInfo.get(keyString[0]);
			String className = (String) appInfo.get(keyString[1]);
			String classId = (String) appInfo.get(keyString[2]);
			holder.textview.setText(className);
			
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
		private String className;

		public AdapterListener(int pos,String _className) {
			// TODO Auto-generated constructor stub
			position = pos;
			className = _className;
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