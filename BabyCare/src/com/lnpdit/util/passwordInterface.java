package com.lnpdit.util;

import android.app.Activity;
import android.app.ProgressDialog;
import android.app.AlertDialog.Builder;
import android.view.Gravity;
import android.widget.TextView;

public class passwordInterface extends Activity {
	public ProgressDialog processDialog;
	protected boolean loadingFlag = true;

	/**
	 * 显示的一段字符串的对话框 参数message：需要显示的字符串
	 * */
	public void showResult(final String message) {

		passwordInterface.this.runOnUiThread(new Runnable() {

			public void run() {
				// TODO Auto-generated method stub
				// show
				Builder builder = new Builder(passwordInterface.this);
				TextView tv = new TextView(passwordInterface.this);
				tv.setTextSize(20);
				tv.setGravity(Gravity.CENTER_HORIZONTAL);
				tv.setText(message);
				builder.setView(tv);
				builder.setPositiveButton("确定", null);
				builder.create().show();
			}
		});
	}

	/**
	 * 显示的一段字符串并且带标题的对话框
	 * 
	 * @param title
	 *            标题
	 * @param message
	 *            需要显示的字符串
	 * */
	public void showResult(final String title, final String message) {

		passwordInterface.this.runOnUiThread(new Runnable() {

			public void run() {
				// TODO Auto-generated method stub
				// show
				Builder builder = new Builder(passwordInterface.this);
				TextView tv = new TextView(passwordInterface.this);
				tv.setTextSize(20);
				tv.setGravity(Gravity.CENTER_HORIZONTAL);
				tv.setText(message);
				builder.setTitle(title);
				builder.setView(tv);
				builder.setPositiveButton("确定", null);
				builder.create().show();
			}
		});
	}

	/**
	 * 带等待图标的等待框 参数message：需要显示给用户的字符串
	 * */
	public void showWait(final String message) {
		passwordInterface.this.runOnUiThread(new Runnable() {

			public void run() {
				processDialog = new ProgressDialog(passwordInterface.this);
				processDialog.setMessage(message);
				processDialog.setIndeterminate(true);
				processDialog.setCancelable(true);
				processDialog.setCanceledOnTouchOutside(false);
				processDialog.show();
			}
		});

	}

	/**
	 * 关闭等待框
	 * */
	public void waitClose() {
		passwordInterface.this.runOnUiThread(new Runnable() {

			public void run() {
				if (processDialog != null && processDialog.isShowing()) {
					processDialog.dismiss();
				}
			}
		});
		loadingFlag = false;
	}

}
