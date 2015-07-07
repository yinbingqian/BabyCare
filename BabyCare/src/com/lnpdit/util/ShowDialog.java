package com.lnpdit.util;


import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;

public class ShowDialog {
	public void mShowDialog(Context mContext, String mTitle, String mMsg) {
		String title = mTitle;
		String msg = mMsg;
		Context context = mContext;
		AlertDialog.Builder mBuilder = new Builder(context);
		mBuilder.setTitle(title);
		mBuilder.setMessage(msg);
		mBuilder.setPositiveButton("х╥хо",
				new android.content.DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						dialog.dismiss();
					}
				});
		mBuilder.show();
	}
}
