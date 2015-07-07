package com.lnpdit.util.adapter;

import java.util.ArrayList;
import java.util.HashMap;

import org.ksoap2.serialization.SoapObject;

import com.lnpdit.babycare.R;
import com.lnpdit.garden.GardenNewsDetailActivity;
import com.lnpdit.sqllite.BBGJDB;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;


public class NewsAdapter extends BaseAdapter {
	private class buttonViewHolder {
		ImageView appicon;
		TextView apptitle;
		TextView apptime;
	}

	private ArrayList<HashMap<String, Object>> mAppList;
	private LayoutInflater mInflater;
	private Context mContext;
	private Resources resources;
	private String[] keyString;
	private buttonViewHolder holder;

	private String Type;

	private String namespace;
	private String url;
	private String soapaction;
	private String methodname;
	private SoapObject newslist;
	private String newsid;
	private String newstitle;
	private String newscontent;
	private String newstime;
	private String newssource;
	private String newspicture;
	private String newstype;
	private String newsauthor;
	private BBGJDB tdd;
	private ContentValues values;
	private ProgressDialog dialog;

	private String title;
	private String time;
	private String source;
	private String author;
	private String pic;
	private String content;
	private String type;
	private String webid;
	private TextView text_title;

	// private Map<Integer, Boolean> checkState = new HashMap<Integer,
	// Boolean>();

	public NewsAdapter(Context c, ArrayList<HashMap<String, Object>> appList,
			int resource, String[] from, int[] to, Resources r) {
		mAppList = appList;
		mContext = c;
		resources = r;
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
			// resetViewHolder(holder);
		} else {
			convertView = mInflater.inflate(R.layout.list_in_news, null);
			holder = new buttonViewHolder();
			holder.appicon = (ImageView) convertView
					.findViewById(R.id.list_in_news_icon);
			holder.apptitle = (TextView) convertView
					.findViewById(R.id.list_in_news_content);
			holder.apptime = (TextView) convertView
					.findViewById(R.id.list_in_news_time);
			convertView.setTag(holder);
		}
		HashMap<String, Object> appInfo = mAppList.get(position);
		if (appInfo != null) {
			title = (String) appInfo.get(keyString[0]);
			time = (String) appInfo.get(keyString[1]);
			source = (String) appInfo.get(keyString[2]);
			author = (String) appInfo.get(keyString[3]);
			pic = (String) appInfo.get(keyString[4]);
			content = (String) appInfo.get(keyString[5]);
			type = (String) appInfo.get(keyString[6]);

			holder.apptitle.setText(title);
			holder.apptime.setText(time);
			convertView.setOnClickListener(new AdapterListener(position, title,
					time, source, author, pic, content, webid, type,
					holder.apptitle));
		}
		return convertView;
	}

	class AdapterListener implements OnClickListener {
		private int position;
		private String Title;
		private String Time;
		private String Source;
		private String Author;
		private String Pic;
		private String Content;
		private String Webid;

		public AdapterListener(int pos, String title, String time,
				String source, String author, String pic, String content,
				String webid, String type, TextView texttitle) {
			// TODO Auto-generated constructor stub
			position = pos;
			Title = title;
			Time = time;
			Source = source;
			Author = author;
			Pic = pic;
			Content = content;
			Webid = webid;
			Type = type;
			text_title = texttitle;
		}

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			Intent intent = new Intent();
			intent.setClass(mContext, GardenNewsDetailActivity.class);
			intent.putExtra("Title", Title);
			intent.putExtra("Time", Time);
			intent.putExtra("Source", Source);
			intent.putExtra("Author", Author);
			intent.putExtra("Pic", Pic);
			intent.putExtra("Content", Content);
			intent.putExtra("Webid", Webid);
			intent.putExtra("Type", Type);
			mContext.startActivity(intent);
		}

	}
}
