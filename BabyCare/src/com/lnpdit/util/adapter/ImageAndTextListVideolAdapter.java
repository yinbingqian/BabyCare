package com.lnpdit.util.adapter;



import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.lnpdit.babycare.R;
import com.lnpdit.util.AsyncImageLoader;
import com.lnpdit.util.AsyncImageLoader.ImageCallback;
import com.lnpdit.util.ImageAndTextVideo;
import com.lnpdit.util.ViewCacheVideo;

public class ImageAndTextListVideolAdapter extends
		ArrayAdapter<ImageAndTextVideo> {

	private ListView listView;
	private AsyncImageLoader asyncImageLoader;
	Context mContext;

	// Resources resources;

	public ImageAndTextListVideolAdapter(Activity activity,
			List<ImageAndTextVideo> imageAndTexts, ListView listView,
			Context context) {
		super(activity, 0, imageAndTexts);
		this.listView = listView;
		this.mContext = context;
		// this.resources = mContext.getResources();
		asyncImageLoader = new AsyncImageLoader();
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		Activity activity = (Activity) getContext();

		// Inflate the views from XML
		View rowView = convertView;
		ViewCacheVideo viewCache;
		if (rowView == null) {
			LayoutInflater inflater = activity.getLayoutInflater();
			rowView = inflater.inflate(R.layout.list_in_videolist, null);
			viewCache = new ViewCacheVideo(rowView);
			rowView.setTag(viewCache);
		} else {
			viewCache = (ViewCacheVideo) rowView.getTag();
		}
		ImageAndTextVideo imageAndText = getItem(position);

		// Load the image and set it on the ImageView
		String imageUrl1 = imageAndText.getImageUrl1();
		ImageView imageView1 = viewCache.getImageView1();
		imageView1.setTag(imageUrl1);
		Drawable cachedImage1 = asyncImageLoader.loadDrawable(imageUrl1,
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
		if (cachedImage1 == null) {
			imageView1.setImageResource(R.drawable.default_microhot);
		} else {
			imageView1.setImageDrawable(cachedImage1);
		}
		// Set the text on the TextView
		TextView textViewTitle1 = viewCache.getTextViewText1();
		TextView textViewTitle2 = viewCache.getTextViewText2();
		textViewTitle1.setText(imageAndText.getTitle1());
		textViewTitle2.setText(imageAndText.getCrtime1());

		imageView1.setOnClickListener(new AdapterListener(position, imageView1,
				imageUrl1, imageAndText.getWebId1(), imageAndText.getVideo1()));

		return rowView;
	}

	class AdapterListener implements OnClickListener {
		private int position;
		private ImageView iv;
		private String p;
		private String id;
		private String video;

		public AdapterListener(int pos, ImageView _iv, String _p, String _id,
				String _video) {
			// TODO Auto-generated constructor stub
			this.position = pos;
			this.iv = _iv;
			this.p = _p;
			this.id = _id;
			this.video = _video;
		}

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			Intent intent = new Intent(Intent.ACTION_VIEW);
			Uri uri = Uri.parse(video);
			intent.setDataAndType(uri, "video/mp4");
			mContext.startActivity(intent);
		}

	}

}
