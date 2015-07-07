package com.lnpdit.util.adapter;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.lnpdit.babycare.R;
import com.lnpdit.garden.GardenNewsDetailActivity;
import com.lnpdit.util.AsyncImageLoader;
import com.lnpdit.util.AsyncImageLoader.ImageCallback;
import com.lnpdit.util.ViewCache;

public class ImageAndTextListAdapter extends ArrayAdapter<ImageAndText> {

	private ListView listView;
	private AsyncImageLoader asyncImageLoader;
	Context mContext;

	public ImageAndTextListAdapter(Activity activity,
			List<ImageAndText> imageAndTexts, ListView listView, Context context) {
		super(activity, 0, imageAndTexts);
		this.listView = listView;
		this.mContext = context;
		asyncImageLoader = new AsyncImageLoader();
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		Activity activity = (Activity) getContext();

		// Inflate the views from XML
		View rowView = convertView;
		ViewCache viewCache;
		if (rowView == null) {
			LayoutInflater inflater = activity.getLayoutInflater();
			rowView = inflater.inflate(R.layout.list_in_news, null);
			viewCache = new ViewCache(rowView);
			rowView.setTag(viewCache);
		} else {
			viewCache = (ViewCache) rowView.getTag();
		}
		ImageAndText imageAndText = getItem(position);

		// Load the image and set it on the ImageView
		String imageUrl = imageAndText.getImageUrl();
		ImageView imageView = viewCache.getImageView();
		imageView.setTag(imageUrl);
		Drawable cachedImage = asyncImageLoader.loadDrawable(imageUrl,
				new ImageCallback() {
					public void imageLoaded(Drawable imageDrawable,
							String imageUrl) {
						ImageView imageViewByTag = (ImageView) listView
								.findViewWithTag(imageUrl);
						if (imageViewByTag != null) {
							imageViewByTag.setImageDrawable(imageDrawable);
						}
					}
				});
		if (cachedImage == null) {
			imageView.setImageResource(R.drawable.default_microhot);
		} else {
			imageView.setImageDrawable(cachedImage);
		}
		// Set the text on the TextView
		TextView textViewTitle = viewCache.getTextViewContent();
		textViewTitle.setText(imageAndText.getTitle());

		TextView textViewtime = viewCache.getTextViewTime();
		textViewtime.setText(imageAndText.getTextTime());

		rowView.setOnClickListener(new newsAdapterListener(position,
				imageAndText.getTitle(), imageAndText.getTextTime(),
				imageAndText.getSource(), imageAndText.getAuthor(),
				imageAndText.getPic(), imageAndText.getTextContent(),
				imageAndText.getId(), imageAndText.getType()));

		return rowView;
	}

	class newsAdapterListener implements OnClickListener {
		private int position;
		private String Title;
		private String Time;
		private String Source;
		private String Author;
		private String Pic;
		private String Content;
		private String Webid;
		private String Type;

		public newsAdapterListener(int pos, String title, String time,
				String source, String author, String pic, String content,
				String webid, String type) {
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
			// text_title = texttitle;
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
