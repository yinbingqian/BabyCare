package com.lnpdit.util.adapter;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import com.lnpdit.babycare.R;
import com.lnpdit.garden.GardenPictureActivity;
import com.lnpdit.service.MessengerService;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.WebSettings.LayoutAlgorithm;
import android.webkit.WebView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;


public class NewsDetailAdapter extends BaseAdapter {
	private class buttonViewHolder {
		TextView apptitle;
		// TextView appsource;
		// TextView appauthor;
		TextView apptime;
		ImageView apppic;
		WebView appcontent;
	}

	private ArrayList<HashMap<String, Object>> mAppList;
	private LayoutInflater mInflater;
	private Context mContext;
	private Resources resources;
	private String[] keyString;
	private buttonViewHolder holder;
	public String pic;
	public ImageView imageview;
	public static Bitmap bmImg;
	private Bitmap _bmp;
	private int bmp_state;

	public static final String SERVICEUI_REFRESHPIC = "com.syml.mobilenewspaper.refreshpicture";

	public NewsDetailAdapter(Context c,
			ArrayList<HashMap<String, Object>> appList, int resource,
			String[] from, int[] to, Resources r, Bitmap bitmap, int s) {
		mAppList = appList;
		mContext = c;
		resources = r;
		mInflater = (LayoutInflater) mContext
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		keyString = new String[from.length];
		_bmp = bitmap;
		bmp_state = s;
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
			convertView = mInflater.inflate(R.layout.list_in_newsdetail, null);
			holder = new buttonViewHolder();
			holder.apptitle = (TextView) convertView
					.findViewById(R.id.newsdetail_title);
//			holder.apptime = (TextView) convertView
//					.findViewById(R.id.newsdetail_time);
//			holder.apppic = (ImageView) convertView
//					.findViewById(R.id.newsdetail_pic);
//			holder.appcontent = (WebView) convertView
//					.findViewById(R.id.newsdetail_content);
			convertView.setTag(holder);
		}
		HashMap<String, Object> appInfo = mAppList.get(position);
		if (appInfo != null) {
			String title = (String) appInfo.get(keyString[0]);
			String time = (String) appInfo.get(keyString[1]);
			String source = (String) appInfo.get(keyString[2]);
			String author = (String) appInfo.get(keyString[3]);
			pic = (String) appInfo.get(keyString[4]);
			String content = (String) appInfo.get(keyString[5]);
			String webid = (String) appInfo.get(keyString[6]);

			holder.apptitle.setText(title);
			// holder.appsource.setText(source);
			// holder.appsource.setText("");
			// holder.appauthor.setText("");
			// holder.appauthor.setText(author);
			holder.apptime.setText(time);
			// holder.appcontent.setText(content);
			holder.appcontent.getSettings().setSupportZoom(true);
			holder.appcontent.clearCache(true);
			holder.appcontent.getSettings().setDefaultTextEncodingName("utf-8");
			holder.appcontent.getSettings().setLayoutAlgorithm(LayoutAlgorithm.SINGLE_COLUMN);
			holder.appcontent.loadDataWithBaseURL(null, content, "text/html",
					"utf-8", null);
			imageview = holder.apppic;

			if (bmp_state == 1) {
				// imageview.setImageBitmap(_bmp);
				imageview.setImageBitmap(bmImg);
			} else if (bmp_state == 0) {
				if (!pic.equals("null")) {
					Thread updatepic = new Thread(new UpdatePicture());
					updatepic.start();
				}
			}
			imageview.setOnClickListener(new AdapterListener(position,
					imageview, pic));
		}
		return convertView;
	}

	class AdapterListener implements OnClickListener {
		private int position;
		private ImageView iv;
		private String p;

		public AdapterListener(int pos, ImageView _iv, String _p) {
			// TODO Auto-generated constructor stub
			position = pos;
			iv = _iv;
			p = _p;
		}

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			Intent intent = new Intent();
			intent.setClass(mContext, GardenPictureActivity.class);
			intent.putExtra("PIC", MessengerService.PIC_FILE + p);
			mContext.startActivity(intent);
		}

	}

	public class UpdatePicture implements Runnable {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			// String url_str = "http://www.linuxidc.com/upload/linuxidc.jpg";
			String url_str = pic;
			try {
				URL url = new URL(MessengerService.PIC_FILE + url_str);
				HttpURLConnection conn = (HttpURLConnection) url
						.openConnection();
				conn.setDoInput(true);
				conn.connect();
				InputStream is = conn.getInputStream();
				bmImg = BitmapFactory.decodeStream(is);
				// NewsDetailActivity.newsInstance.finish();
				// imageview.setImageBitmap(bmImg);
				Intent intent = new Intent(SERVICEUI_REFRESHPIC);
				mContext.sendBroadcast(intent);
				Thread.sleep(300);
				is.close();

			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
