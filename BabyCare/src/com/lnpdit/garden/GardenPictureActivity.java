package com.lnpdit.garden;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import com.lnpdit.babycare.R;
import com.lnpdit.util.PhotoZoomListener;
import com.lnpdit.util.PhotoZoomState;
import com.lnpdit.util.PhotoZoomView;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ZoomControls;


public class GardenPictureActivity extends Activity {

	private PhotoZoomView mZoomView;
	private PhotoZoomState mZoomState;
	private PhotoZoomListener mZoomListener;

	Context context;
	Resources resources;
	PhotoZoomView imageview;
	Bundle bundle;
	String pic_url;
	Bitmap bitmap = null;
	private ProgressDialog dialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_picture);
		context = this;
		resources = this.getResources();
		mZoomView = (PhotoZoomView) findViewById(R.id.picture_imageview);
		Intent intent = getIntent();
		pic_url = intent.getStringExtra("PIC");
		dialog = new ProgressDialog(context);
		dialog.setMessage("正在加载图片,请稍等.");
		dialog.setIndeterminate(true);
		dialog.setCancelable(true);
		dialog.show();
		Thread thread = new Thread(new UpdatePicture());
		thread.start();

		ZoomControls zoomCtrl = (ZoomControls) findViewById(R.id.zoomCtrl);
		zoomCtrl.setOnZoomInClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				float z = mZoomState.getZoom() + 0.25f;
				float up_state = mZoomState.getZoom();
				if (up_state < 4.1) {
					mZoomState.setZoom(z);
					mZoomState.notifyObservers();
				}
			}
		});
		zoomCtrl.setOnZoomOutClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				float z = mZoomState.getZoom() - 0.25f;
				float down_state = mZoomState.getZoom();
				if (down_state > 0.9) {
					mZoomState.setZoom(z);
					mZoomState.notifyObservers();
				}
			}
		});
	}

	Handler threadMessageHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			bitmap = (Bitmap) msg.obj;
			mZoomView.setImage(bitmap);
			mZoomState = new PhotoZoomState();
			mZoomView.setZoomState(mZoomState);
			mZoomListener = new PhotoZoomListener();
			mZoomListener.setZoomState(mZoomState);
			mZoomView.setOnTouchListener(mZoomListener);
			resetZoomState();
			dialog.dismiss();
		}
	};

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (bitmap != null)
			bitmap.recycle();
		// mZoomView.setOnTouchListener(null);
		// mZoomState.deleteObservers();
	}

	private void resetZoomState() {
		mZoomState.setPanX(0.5f);
		mZoomState.setPanY(0.5f);
		mZoomState.setZoom(1f);
		mZoomState.notifyObservers();
	}

	public class UpdatePicture implements Runnable {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			// String url_str = "http://www.linuxidc.com/upload/linuxidc.jpg";
			String url_str = pic_url;
			try {
				URL url = new URL(url_str);
				HttpURLConnection conn = (HttpURLConnection) url
						.openConnection();
				conn.setDoInput(true);
				conn.connect();
				InputStream is = conn.getInputStream();
				Bitmap bmImg = BitmapFactory.decodeStream(is);
				Message msg = new Message();
				msg.obj = bmImg;
				threadMessageHandler.sendMessage(msg);
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
