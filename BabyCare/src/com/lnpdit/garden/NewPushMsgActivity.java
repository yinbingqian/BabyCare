package com.lnpdit.garden;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import com.lnpdit.babycare.R;
import com.lnpdit.service.MessengerService;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Base64;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class NewPushMsgActivity extends Activity implements OnClickListener {

	private Context context;
	private Resources resources;

	private String _id;
	private String _remark;
	private String _pic;
	private String _time;
	private String _name;
	private String _tel;
	private String _type;
	private String _audio;

	private TextView name_text;
	private TextView time_text;
	private TextView content_text;
	private TextView type_text;
	private TextView tel_text;
	private ImageView image;
	private Button audio_bt;
	private Bitmap bitmap = null;

	private static final String TAG = "LNPDIT_news_uploadFile";
	private static final int TIME_OUT = 10 * 1000;
	private static final String CHARSET = "utf-8";
	String uploadBufferAudio = null;
	private TextView pushrcv_back;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_push_rcv);

		context = this;
		resources = this.getResources();

		Intent intent = getIntent();
		_id = intent.getStringExtra("_id");
		_remark = intent.getStringExtra("_remark");
		_pic = intent.getStringExtra("_pic");
		_time = intent.getStringExtra("_time");
		_name = intent.getStringExtra("_name");
		_tel = intent.getStringExtra("_tel");
		_type = intent.getStringExtra("_type");
		_audio = intent.getStringExtra("_audio");

		viewInit();
	}

	private void viewInit() {
		pushrcv_back = (TextView) findViewById(R.id.pushrcv_back);
		pushrcv_back.setOnClickListener(this);
		name_text = (TextView) findViewById(R.id.push_rcv_name);
		time_text = (TextView) findViewById(R.id.push_rcv_time);
		content_text = (TextView) findViewById(R.id.push_rcv_content);
		image = (ImageView) findViewById(R.id.push_rcv_img);
		type_text = (TextView) findViewById(R.id.push_rcv_type);
		tel_text = (TextView) findViewById(R.id.push_rcv_tel);
		audio_bt = (Button) this.findViewById(R.id.push_audio_file_bt);

		if (!_audio.startsWith("no")) {
			audio_bt.setVisibility(1);
			final String audio_path_str = MessengerService.AUDIO_PATH + _audio;
			audio_bt.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					MediaPlayer mediaPlayer = new MediaPlayer();
					try {
						mediaPlayer.setDataSource(audio_path_str);
						mediaPlayer.prepare();
						mediaPlayer.start();
					} catch (IllegalArgumentException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IllegalStateException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					// Thread thread = new Thread(new mPlayAudioFileThread());
					// thread.start();
				}
			});
		}

		type_text.setText(_remark);
		String sender_type = "";
		if (_type.equals("1")) {
			sender_type = "特殊分组";
		} else if (_type.equals("2")) {
			sender_type = "普通";
		} else if (_type.equals("3")) {
			sender_type = "其他";
		} else {
			sender_type = "未知";
		}
		name_text.setText(_name);
		time_text.setText(_time);
		content_text.setText(_remark);
		type_text.setText("用户类型： " + sender_type);
		tel_text.setText("电话： " + _tel);
		Thread thread = new Thread(new mImageThread());
		thread.start();

		image.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent();
				intent.setClass(context, GardenPictureActivity.class);
				intent.putExtra("PIC", _pic);
				context.startActivity(intent);
			}
		});
	}

	Handler remarkthreadMessageHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			Bitmap temp_bmp = null;
			temp_bmp = (Bitmap) msg.obj;
			image.setImageBitmap(temp_bmp);
			bitmap = null;
			temp_bmp = null;
		}
	};

	public void uploadByString(String filePath) {
		try {
			FileInputStream fis = new FileInputStream(filePath);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			byte[] buffer = new byte[1024];
			int count = 0;
			while ((count = fis.read(buffer)) >= 0) {
				baos.write(buffer, 0, count);
			}
			uploadBufferAudio = new String(Base64.encode(baos.toByteArray(),
					Base64.DEFAULT));
		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	class mPlayAudioFileThread implements Runnable {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			MediaPlayer mediaPlayer = new MediaPlayer();
			try {
				mediaPlayer.setDataSource(_audio);
				mediaPlayer.prepare();
				mediaPlayer.start();
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalStateException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	class mImageThread implements Runnable {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			String url_str = _pic;
			try {
				URL url = new URL(url_str);
				HttpURLConnection conn = (HttpURLConnection) url
						.openConnection();
				conn.setDoInput(true);
				conn.connect();
				InputStream is = conn.getInputStream();
				bitmap = BitmapFactory.decodeStream(is);
				Message msg = new Message();
				msg.obj = bitmap;
				remarkthreadMessageHandler.sendMessage(msg);
				is.close();

			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.pushrcv_back:

			this.finish();

			break;

		default:
			break;
		}
	}

	/** 重定义返回键事件 **/
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// 拦截back按键
		if (keyCode == KeyEvent.KEYCODE_BACK
				&& event.getAction() == KeyEvent.ACTION_DOWN) {
			finish();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
}
