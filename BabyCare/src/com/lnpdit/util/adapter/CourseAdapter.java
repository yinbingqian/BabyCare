package com.lnpdit.util.adapter;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.lnpdit.babycare.R;

public class CourseAdapter extends BaseAdapter {
	private class buttonViewHolder {
		TextView week_tv;
		TextView course_tv;
		TextView time_tv;
	}

	private ArrayList<HashMap<String, Object>> mAppList;
	private LayoutInflater mInflater;
	private Context mContext;
	private String[] keyString;
	private buttonViewHolder holder;

	public CourseAdapter(Context c, ArrayList<HashMap<String, Object>> appList,
			int resource, String[] from, int[] to) {
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
			convertView = mInflater.inflate(R.layout.list_in_course, null);
			holder = new buttonViewHolder();
			holder.week_tv = (TextView) convertView.findViewById(R.id.week_tv);
			holder.course_tv = (TextView) convertView.findViewById(R.id.course_tv);
			holder.time_tv = (TextView) convertView
					.findViewById(R.id.time_tv);
			convertView.setTag(holder);
		}
		HashMap<String, Object> appInfo = mAppList.get(position);
		if (appInfo != null) {
			String week_str = (String) appInfo.get(keyString[0]);
			String course_str = (String) appInfo.get(keyString[1]);
			String time_str = (String) appInfo.get(keyString[2]);
			// holder.textview.setText(year + "Äê" + month + "ÔÂ");
			try{				
				String[] course_array = course_str.split("%");
				String[] time_array = time_str.split("%");
				
				String course = "";
				String time = "";
				
				for(int i=0; i<course_array.length;i++){
					course = course + course_array[i] + "\n";
				}
				for(int i=0; i<time_array.length;i++){
					time = time + time_array[i] + "\n";
				}
				
				holder.week_tv.setText(week_str);
				holder.course_tv.setText(course);
				holder.time_tv.setText(time);
			}catch(Exception e){
				
			}
//			if (attend.equals("unat")) {
//
//				holder.unat_img.setImageResource(R.drawable.radioimg_h);
//			} else if (attend.equals("at")) {
//				holder.at_img.setImageResource(R.drawable.radioimg_h);
//			}
			// convertView.setOnClickListener(new AdapterListener(position,
			// year, month));
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

		public AdapterListener(int pos, String _year, String _month) {
			// TODO Auto-generated constructor stub
			position = pos;
			year = _year;
			month = _month;
		}

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			// Intent intent = new Intent();
			// intent.setClass(mContext, ContactActivity.class);
			// intent.putExtra("Id", Id);
			// intent.putExtra("Grade", Grade);
			// intent.putExtra("Class", Class);
			// intent.putExtra("Remark", Remark);
			// mContext.startActivity(intent);
			// Toast.makeText(mContext, year, Toast.LENGTH_SHORT).show();
		}
	}
}