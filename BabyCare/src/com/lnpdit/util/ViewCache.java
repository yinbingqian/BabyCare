package com.lnpdit.util;

import com.lnpdit.babycare.R;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;


public class ViewCache {

	private View baseView;
	private TextView textViewContent;
	private TextView textViewTime;
	private ImageView imageView;

	public ViewCache(View baseView) {
		this.baseView = baseView;
	}

	public ImageView getImageView() {
		if (imageView == null) {
			imageView = (ImageView) baseView
					.findViewById(R.id.list_in_news_icon);
		}
		return imageView;
	}

	public TextView getTextViewContent() {
		if (textViewContent == null) {
			textViewContent = (TextView) baseView
					.findViewById(R.id.list_in_news_content);
		}
		return textViewContent;
	}

	public TextView getTextViewTime() {
		if (textViewTime == null) {
			textViewTime = (TextView) baseView
					.findViewById(R.id.list_in_news_time);
		}
		return textViewTime;
	}

}
