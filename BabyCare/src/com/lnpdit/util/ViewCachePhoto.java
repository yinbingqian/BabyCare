package com.lnpdit.util;

import com.lnpdit.babycare.R;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;


public class ViewCachePhoto {

	private View baseView;
	private TextView textViewTitle1;
	private TextView textViewTitle2;
	private ImageView imageView1;
	private ImageView imageView2;

	public ViewCachePhoto(View baseView) {
		this.baseView = baseView;
	}

	public ImageView getImageView1() {
		if (imageView1 == null) {
			imageView1 = (ImageView) baseView
					.findViewById(R.id.list_in_journal_img1);
		}
		return imageView1;
	}
	
	public ImageView getImageView2() {
		if(imageView2 == null){
			imageView2 = (ImageView) baseView.findViewById(R.id.list_in_journal_img2);
		}
		return imageView2;
	}

	public TextView getTextViewText1() {
		if (textViewTitle1 == null) {
			textViewTitle1 = (TextView) baseView
					.findViewById(R.id.list_in_journal_text1);
		}
		return textViewTitle1;
	}

	public TextView getTextViewText2() {
		if (textViewTitle2 == null) {
			textViewTitle2 = (TextView) baseView
					.findViewById(R.id.list_in_journal_text2);
		}
		return textViewTitle2;
	}

}
