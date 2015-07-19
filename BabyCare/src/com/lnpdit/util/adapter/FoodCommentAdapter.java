package com.lnpdit.util.adapter;

import java.util.ArrayList;
import java.util.HashMap;


import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.lnpdit.babycare.R;
import com.lnpdit.util.AsyncImageLoader;
import com.lnpdit.util.AsyncImageLoader.ImageCallback;

public class FoodCommentAdapter extends BaseAdapter {
	private class buttonViewHolder {
		TextView comment_user;
		TextView comment_time;
		TextView comment_content;
		ImageView comment_img;
	}

	private ArrayList<HashMap<String, Object>> mAppList;
	private LayoutInflater mInflater;
	private Context mContext;
	private String[] keyString;
	private buttonViewHolder holder;
	String phonecall;
	ListView listview;
	private AsyncImageLoader asyncImageLoader;

	ArrayList<HashMap<String, Object>> remoteWindowItem = null;

	public FoodCommentAdapter(Context c, ArrayList<HashMap<String, Object>> appList,
			int resource, String[] from, int[] to, String _phonecall, ListView _listview) {
		mAppList = appList;
		mContext = c;
		listview = _listview;
		asyncImageLoader = new AsyncImageLoader();
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
			convertView = mInflater.inflate(R.layout.list_in_foodcomment, null);
			holder = new buttonViewHolder();

			holder.comment_user = (TextView) convertView.findViewById(R.id.comment_tv);
			holder.comment_time = (TextView) convertView.findViewById(R.id.comment_time);
			holder.comment_content = (TextView) convertView.findViewById(R.id.comment_comtent);
			holder.comment_img = (ImageView) convertView.findViewById(R.id.comment_img);
			convertView.setTag(holder);
		}
		HashMap<String, Object> appInfo = mAppList.get(position);
		if (appInfo != null) {
			String foodId = (String) appInfo.get(keyString[0]);
			String CommnetContent = (String) appInfo.get(keyString[1]);
			String name = (String) appInfo.get(keyString[2]);
			String ComTime = (String) appInfo.get(keyString[3]);
			String userPic = (String) appInfo.get(keyString[4]);
			
			String imageUrl = "http://221.180.149.201:7799/manage/pic/" + userPic;
			try {

				holder.comment_user.setText(name);
				holder.comment_time.setText(ComTime);
				holder.comment_content.setText(CommnetContent);
				
				holder.comment_img.setTag(imageUrl);
				Drawable cachedImage = asyncImageLoader.loadDrawable(imageUrl,
						new ImageCallback() {
							public void imageLoaded(Drawable imageDrawable,
									String imageUrl) {
								ImageView imageViewByTag = (ImageView) listview
										.findViewWithTag(imageUrl);
								if (imageViewByTag != null) {
									imageViewByTag.setImageDrawable(imageDrawable);
								}
							}
						});

				if (cachedImage == null) {
					holder.comment_img.setImageResource(R.drawable.iconfood);
				} else {
					holder.comment_img.setImageDrawable(cachedImage);
				}
				
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

}