package com.company.Demo;

import android.R.string;
import android.app.Activity;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.Environment;

import android.util.Log;
import android.widget.Button;

import android.view.KeyEvent;
import android.view.View;
import android.view.SurfaceView;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.View.OnClickListener;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import com.company.PlaySDK.BasicSurfaceView;
import com.company.PlaySDK.IPlaySDK;
import com.lnpdit.babycare.R;

public class RecordPlayActivity extends Activity {

	static int port = 0;
	static IPlaySDK playsdk = new IPlaySDK();
	BasicSurfaceView m_PlayView;

	String recordPath;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dhrecordplay);

		recordPath = this.getIntent().getStringExtra("path");

		m_PlayView = (BasicSurfaceView) findViewById(R.id.surfaceView1);
		m_PlayView.Init(port);

		SurfaceHolder holder = m_PlayView.getHolder();
		holder.setFormat(PixelFormat.RGBA_8888);

		holder.addCallback(new Callback() {
			public void surfaceCreated(SurfaceHolder holder) {
				Log.e("[playsdk]surfaceCreated", "surfaceCreated");
				StartPlay();
			}

			public void surfaceChanged(SurfaceHolder holder, int format,
					int width, int height) {
				Log.d("[playsdk]surfaceChanged", "surfaceChanged");
			}

			public void surfaceDestroyed(SurfaceHolder holder) {
				Log.e("[playsdk]surfaceDestroyed", "surfaceDestroyed");
				StopPlay();
			}
		});

	}

	public boolean StartPlay() {

		playsdk.PLAYSetStreamOpenMode(port, 1);

		if (1 == playsdk.PLAYOpenStream(port, null, 0, 5 * 1024 * 1024)) {
			int ret = playsdk.PLAYPlay(port);
			if (0 == ret) {
				return false;
			}

			ret = playsdk.PLAYPlaySound(port);
			if (ret == 0) {
				return false;
			}
			// 文件流读取文件
			FileInputStream fin;
			int length = 0;
			byte[] buffer = null;
			try {
				fin = new FileInputStream(recordPath);
				// 获得字符长度
				length = fin.available();
				// 创建字节数组
				buffer = new byte[length];
				// 把字节流读入数组中
				fin.read(buffer);
				// 关闭文件流
				fin.close();
			} catch (Exception e) {

			}
			ret = playsdk.PLAYInputData(port, buffer, length);
			if (ret == 0) {
				Log.e("[playsdk]PLAYInputData", "Failed.");
				return false;
			}

			return true;
		} else {
			return false;
		}
	}

	public boolean StopPlay() {
		playsdk.PLAYStopSound();
		playsdk.PLAYStop(port);
		playsdk.PLAYCloseStream(port);

		return true;
	}

	boolean isAlive = true;

	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		// 退出，则设置退出属性为ture
		if (isAlive) {
			finish();
		}
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		isAlive = true;
	}

	/** 重定义返回键事件 **/
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// 拦截back按键
		if (keyCode == KeyEvent.KEYCODE_BACK
				&& event.getAction() == KeyEvent.ACTION_DOWN) {
			isAlive = false;
			finish();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

}