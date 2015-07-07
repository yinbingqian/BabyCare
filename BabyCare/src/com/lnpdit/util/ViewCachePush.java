package com.lnpdit.util;

import com.lnpdit.babycare.R;

import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;


public class ViewCachePush {

	private View baseView;
	private TextView textViewName;
	private TextView textViewTime;
	private TextView textViewRemark;
	private Button buttonAudio;
	private ImageView imageView;
	private TextView textViewDevType;
	private TextView textViewRcverName;
	// private RelativeLayout layoutTrans;
	private RelativeLayout layoutReply;
	private RelativeLayout layoutDelete;

	public ViewCachePush(View baseView) {
		this.baseView = baseView;
	}

	public ImageView getImageView() {
		if (imageView == null) {
			imageView = (ImageView) baseView.findViewById(R.id.list_com_img);
		}
		return imageView;
	}

	public TextView getTextViewName() {
		if (textViewName == null) {
			textViewName = (TextView) baseView.findViewById(R.id.list_com_name);
		}
		return textViewName;
	}

	public TextView getTextViewTime() {
		if (textViewTime == null) {
			textViewTime = (TextView) baseView.findViewById(R.id.list_com_time);
		}
		return textViewTime;
	}

	public TextView getTextViewRemark() {
		if (textViewRemark == null) {
			textViewRemark = (TextView) baseView
					.findViewById(R.id.list_com_remark);
		}
		return textViewRemark;
	}

	public Button getButtonAudio() {
		if (buttonAudio == null) {
			buttonAudio = (Button) baseView
					.findViewById(R.id.list_play_audio_bt);
		}
		return buttonAudio;
	}

	public TextView getTextViewDevType() {
		if (textViewDevType == null) {
			textViewDevType = (TextView) baseView
					.findViewById(R.id.list_com_dev_type);
		}
		return textViewDevType;
	}

	public TextView getTextViewRcverName() {
		if (textViewRcverName == null) {
			textViewRcverName = (TextView) baseView
					.findViewById(R.id.list_com_reciver);
		}
		return textViewRcverName;
	}

	// public RelativeLayout getLayoutTrans() {
	// if (layoutTrans == null) {
	// layoutTrans = (RelativeLayout) baseView
	// .findViewById(R.id.list_com_bottom_trans_layout);
	// }
	// return layoutTrans;
	// }

	public RelativeLayout getLayoutReply() {
		if (layoutReply == null) {
			layoutReply = (RelativeLayout) baseView
					.findViewById(R.id.list_com_bottom_reply_layout);
		}
		return layoutReply;
	}

	public RelativeLayout getLayoutDelete() {
		if (layoutDelete == null) {
			layoutDelete = (RelativeLayout) baseView
					.findViewById(R.id.list_com_bottom_delete_layout);
		}
		return layoutDelete;
	}

}
