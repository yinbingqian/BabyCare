package com.lnpdit.garden;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.HashMap;


import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.xmlpull.v1.XmlPullParserException;

import com.lnpdit.babycare.R;
import com.lnpdit.service.MessengerService;
import com.lnpdit.sqllite.BBGJDB;
import com.lnpdit.util.adapter.TopicAdapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;


public class GardenPushReplyActivity extends Activity {

	Context context;
	Resources resources;

	ToggleButton audio_bt;
	ImageView audio_icon;
	TextView audio_title_text;
	TextView audio_time_text;
	TextView contact_text;
	EditText remark_edit;
	Button audio_file_bt;
	Button camera_bt;
	Button commit_bt;
	ImageView camera_img;
	private int recLen = 0;
	TimerRunnable timeRunnable;
	Thread timeThread;
	boolean timeState;
	String _min;
	String _sec;

	String usrID;
	String contact_id = "";
	String picPath = "";
	Bitmap bitmap;
	private MediaRecorder mediaRecorder;
	private File file;
	private SoundPool sp;
	private HashMap<Integer, Integer> spMap;
	String audio_path_str = "";
	String audio_base64_str = "";

	String rcv_user_name = "";
	String rcv_user_tel = "";
	String rcv_user_type = "";
	String rcv_remark = "";
	mContactBroadcast mReceiver = null;
	private ProgressDialog dialog;
	private TextView gardenpushedit_back;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_push_edit);
		context = this;
		resources = this.getResources();
		
		Intent intent = this.getIntent();
		usrID = intent.getStringExtra("ID").toString();
		contact_id = intent.getStringExtra("USERID").toString();
		rcv_user_name = intent.getStringExtra("USERNAME").toString();
		timeState = false;
		viewInit();
		contact_text.setText(rcv_user_name);
		try {
			BBGJDB tdd = new BBGJDB(context);
			Cursor cursor = tdd.selectcontact();
			tdd.close();
			if (cursor.getCount() == 0) {
				mGetContactDataThread mThread = new mGetContactDataThread();
				mThread.getSyncState(context);
				Thread thread = new Thread(mThread);
				thread.start();
			}
			cursor.close();
		} catch (Exception e) {
			// TODO: handle exception
		}
		sp = new SoundPool(2, AudioManager.STREAM_MUSIC, 0);
		spMap = new HashMap<Integer, Integer>();
		spMap.put(1, sp.load(this, R.raw.voice_start, 1));
		spMap.put(2, sp.load(this, R.raw.voice_stop, 1));
		mReceiver = new mContactBroadcast();
		IntentFilter mFilter = new IntentFilter(
				MessengerService.CONTACT_CHOOSE_CONTACT);
		context.registerReceiver(mReceiver, mFilter);
	}

	private void viewInit() {
		RelativeLayout contact_layout = (RelativeLayout) this
				.findViewById(R.id.push_edit_content1);
		audio_bt = (ToggleButton) this.findViewById(R.id.layout_push_record_d);
		audio_icon = (ImageView) this.findViewById(R.id.push_edit_record_icon);
		gardenpushedit_back = (TextView) findViewById(R.id.gardenpushedit_back);
		commit_bt = (Button) findViewById(R.id.layout_push_commit_d);
		audio_file_bt = (Button) this
				.findViewById(R.id.layout_push_audio_file_d);
		camera_bt = (Button) this.findViewById(R.id.layout_push_camera_d);
		camera_img = (ImageView) this.findViewById(R.id.layout_push_img_d);
		remark_edit = (EditText) findViewById(R.id.layout_push_remark_d);
		audio_time_text = (TextView) this
				.findViewById(R.id.push_edit_record_time_text);
		audio_title_text = (TextView) this
				.findViewById(R.id.push_edit_record_text);
		contact_text = (TextView) this
				.findViewById(R.id.layout_push_contact_text);
		audio_bt.setOnCheckedChangeListener(tgListener);
		audio_file_bt.setOnClickListener(btListener);
		camera_bt.setOnClickListener(btListener);
		contact_layout.setClickable(true);
		contact_layout.setOnClickListener(btListener);
		gardenpushedit_back.setOnClickListener(btListener);
		commit_bt.setOnClickListener(btListener);
	}

	private OnCheckedChangeListener tgListener = new OnCheckedChangeListener() {

		@Override
		public void onCheckedChanged(CompoundButton buttonView,
				boolean isChecked) {
			// TODO Auto-generated method stub
			if (isChecked == true) {
				if (Environment.MEDIA_MOUNTED.equals(Environment
						.getExternalStorageState())) {
					audio_icon
							.setBackgroundResource(R.drawable.push_edit_voice_icon_start);
					audio_title_text.setText("正在录音...");
					timeThread = new Thread(new TimerRunnable());
					timeState = true;
					timeThread.start();
					audio_time_text.setText("00:00");
					audio_time_text
							.setVisibility(MessengerService.VISIBILITY_TRUE);
					startRecord();
				} else {
					Toast.makeText(context, "未检测到SD卡，无法使用此功能。",
							Toast.LENGTH_SHORT).show();
				}
			} else {
				audio_icon
						.setBackgroundResource(R.drawable.push_edit_voice_icon);
				audio_title_text.setText("录音结束,点击文件可进行试听播放");
				audio_bt.setVisibility(MessengerService.VISIBILITY_FALSE);
				timeState = false;
				audio_file_bt.setVisibility(MessengerService.VISIBILITY_TRUE);
				stopRecord();
			}
		}
	};

	private android.view.View.OnClickListener btListener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			switch (v.getId()) {
			case R.id.layout_push_audio_file_d:
				try {
					mPlayAudio(audio_path_str);
					int _time = Integer.parseInt(_min) * 60
							+ Integer.parseInt(_sec);
					TimeMinRunnable timeMinRunnable = new TimeMinRunnable();
					timeMinRunnable.setTime(_time);
					Thread thread = new Thread(timeMinRunnable);
					thread.start();
					audio_file_bt
							.setBackgroundResource(R.drawable.audio_file_playing);
					audio_file_bt.setClickable(false);
				} catch (Exception e) {
					// TODO: handle exception
				}
				break;
			case R.id.layout_push_camera_d:
				try {
					if (Environment.MEDIA_MOUNTED.equals(Environment
							.getExternalStorageState())) {
						Intent intent = new Intent(
								MediaStore.ACTION_IMAGE_CAPTURE);
						startActivityForResult(intent, 1);
					} else {
						Toast.makeText(context, "未检测到SD卡，无法使用此功能。",
								Toast.LENGTH_SHORT).show();
					}
				} catch (Exception e) {
					// TODO: handle exception
					Toast.makeText(context, "未检测到相机，无法使用此功能",
							Toast.LENGTH_SHORT).show();
				}
				break;
			case R.id.push_edit_content1:
				try {
					Intent intent = new Intent();
					intent.setClass(GardenPushReplyActivity.this,
							GardenContactActivity.class);
					intent.putExtra("USERID", usrID);
					startActivity(intent);
				} catch (Exception e) {
					// TODO: handle exception
				}
				break;
			case R.id.gardenpushedit_back:
				finish();
				break;
			case R.id.layout_push_commit_d:
				push_msg();
				break;
			default:
				break;
			}
		}
	};

	private void push_msg() {
		if (contact_id.equals("")) {
			Toast.makeText(context, "请选择联系人", Toast.LENGTH_SHORT).show();
			return;
		}

		String remark_str = remark_edit.getText().toString();
		if (remark_str.equals("")) {
			Toast.makeText(context, "输入评语", Toast.LENGTH_SHORT).show();
			return;
		}
		String uploadBuffer = null;
		try {
			FileInputStream fis = new FileInputStream(picPath);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			byte[] buffer = new byte[1024];
			int count = 0;
			while ((count = fis.read(buffer)) >= 0) {
				baos.write(buffer, 0, count);
			}
			uploadBuffer = new String(Base64.encode(baos.toByteArray(),
					Base64.DEFAULT));
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		if (!audio_path_str.equals("")) {
			try {
				FileInputStream fis = new FileInputStream(audio_path_str);
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				byte[] buffer = new byte[1024];
				int count = 0;
				while ((count = fis.read(buffer)) >= 0) {
					baos.write(buffer, 0, count);
				}

				audio_base64_str = new String(Base64.encode(baos.toByteArray(),
						Base64.DEFAULT));
			} catch (Exception e) {
				// TODO: handle exception
			}
		}

		dialog = new ProgressDialog(context);
		dialog.setMessage("正在进行推送,请稍等.");
		dialog.setIndeterminate(true);
		dialog.setCancelable(true);
		dialog.show();
		mPushMsg pm = new mPushMsg();
		pm.getMsg(remark_str, uploadBuffer, audio_base64_str);
		Thread thread = new Thread(pm);
		thread.start();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == Activity.RESULT_OK) {

			String sdStatus = Environment.getExternalStorageState();
			if (!sdStatus.equals(Environment.MEDIA_MOUNTED)) { // 检测sd是否可用
				Log.v("TestFile",
						"SD card is not avaiable/writeable right now.");
				return;
			}
			camera_img.setVisibility(MessengerService.VISIBILITY_TRUE);
			camera_bt.setVisibility(MessengerService.VISIBILITY_FALSE);

			Bundle bundle = data.getExtras();
			bitmap = (Bitmap) bundle.get("data");// 获取相机返回的数据，并转换为Bitmap图片格式
			FileOutputStream b = null;
			File file = new File("/sdcard/newsmobileImg/");
			file.mkdirs();// 创建文件夹

			SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
			Long time = new Long(445555555);
			String d = format.format(time);
			picPath = "/sdcard/newsmobileImg/" + d + ".jpg";

			try {
				b = new FileOutputStream(picPath);
				bitmap.compress(Bitmap.CompressFormat.JPEG, 100, b);// 把数据写入文件
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} finally {
				try {
					b.flush();
					b.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			camera_img.setImageBitmap(bitmap);// 将图片显示在ImageView里
		}
	}

	Handler threadMessageHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			Toast.makeText(context, "联系人同步完成", Toast.LENGTH_SHORT).show();
		}
	};

	final Handler handler = new Handler() { // handle
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 1:
				recLen++;
				_min = String.valueOf(recLen / 60);
				_sec = String.valueOf(recLen % 60);
				if (_min.length() < 2) {
					_min = "0" + _min;
				}
				if (_sec.length() < 2) {
					_sec = "0" + _sec;
				}
				audio_time_text.setText(_min + ":" + _sec);
				break;
			case 2:
				int _time = (Integer) msg.obj;
				String _min_min = String.valueOf(_time / 60);
				String _min_sec = String.valueOf(_time % 60);
				if (_min_min.length() < 2) {
					_min_min = "0" + _min_min;
				}
				if (_min_sec.length() < 2) {
					_min_sec = "0" + _min_sec;
				}
				audio_time_text.setText(_min_min + ":" + _min_sec);
				break;
			case 3:
				audio_time_text.setText(_min + ":" + _sec);
				audio_file_bt.setBackgroundResource(R.drawable.audio_file_play);
				audio_file_bt.setClickable(true);
				break;
			default:
				break;
			}
			super.handleMessage(msg);
		}
	};

	public class TimeMinRunnable implements Runnable { // thread

		int mTime = 0;

		public void setTime(int _time) {
			this.mTime = _time;
		}

		@Override
		public void run() {
			try {
				for (int i = mTime; i > 0; i--) {
					Thread.sleep(1000); // sleep 1000ms
					mTime = mTime - 1;
					Message message = new Message();
					message.what = 2;
					message.obj = mTime;
					handler.sendMessage(message);
				}
				Message message = new Message();
				message.what = 3;
				handler.sendMessage(message);
			} catch (Exception e) {
			}
		}
	}

	public class TimerRunnable implements Runnable { // thread
		@Override
		public void run() {
			while (timeState) {
				try {
					Thread.sleep(1000); // sleep 1000ms
					Message message = new Message();
					message.what = 1;
					handler.sendMessage(message);
				} catch (Exception e) {
				}
			}
		}
	}

	Handler pushthreadMessageHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			dialog.dismiss();
			String push_res = (String) msg.obj;
			String[] push_res_array = new String[2];
			push_res_array = push_res.split("\\|");
			try {

			} catch (Exception e) {
				// TODO: handle exception
			}
			saveSendData(contact_id, push_res_array[0], rcv_remark,
					rcv_user_name, rcv_user_tel, rcv_user_type,
					push_res_array[1]);
			Toast.makeText(context, "信息推送成功", Toast.LENGTH_SHORT).show();
			bitmap = null;
			camera_img
					.setBackgroundResource(R.drawable.v5_0_1_profile_headphoto);
			remark_edit.setText("");
			finish();
		}
	};

	private void saveSendData(String webid, String pic, String remark,
			String sendername, String tel, String type, String audio) {
		try {
			SimpleDateFormat formatter = new SimpleDateFormat(
					"yyyy-MM-dd HH:mm:ss");
			Date curDate = new Date(System.currentTimeMillis());// 获取当前时间
			String time_str = formatter.format(curDate);
			BBGJDB tdd = new BBGJDB(context);
			ContentValues cv = new ContentValues();
			cv.put(tdd.PUSH_WEBID, webid);
			cv.put(tdd.PUSH_PIC, pic);
			cv.put(tdd.PUSH_REMARK, remark);
			cv.put(tdd.PUSH_TIME, time_str);
			String _name = "";
			Cursor cursor = tdd.selectuser();
			if (cursor.getCount() != 0) {
				cursor.moveToFirst();
				_name = cursor.getString(2);
			}
			cv.put(tdd.PUSH_SENDER_NAME, _name);
			cv.put(tdd.PUSH_SENDER_TEL, tel);
			cv.put(tdd.PUSH_SENDER_TYPE, type);
			cv.put(tdd.PUSH_AUDIO, audio);
			cv.put(tdd.PUSH_AUDIO_LENGTH, recLen);
			cv.put(tdd.PUSH_DEV_TYPE, "1");
			cv.put(tdd.PUSH_RCVER_NAME, sendername);
			cv.put(tdd.PUSH_SENDER_ID, usrID);
			tdd.insertpush(cv);
		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	private void startRecord() {
		try {
			File old_file = new File(Environment.getExternalStorageDirectory(),
					"lnpditNewsTempAudio" + ".amr");
			old_file.delete();
		} catch (Exception e) {
			// TODO: handle exception
		}
		try {
			// String file_name = String.valueOf(System.currentTimeMillis());
			String file_name = "lnpditNewsTempAudio";
			file = new File(Environment.getExternalStorageDirectory(),
					file_name + ".amr");
			playSounds(1, 0);
			mediaRecorder = new MediaRecorder();
			mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
			mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.RAW_AMR);
			mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
			mediaRecorder.setOutputFile(file.getAbsolutePath());
			mediaRecorder.prepare();
			mediaRecorder.start();
			audio_path_str = Environment.getExternalStorageDirectory() + "/"
					+ file_name + ".amr";
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO: handle exception
			Toast.makeText(context, "录制失败", Toast.LENGTH_SHORT).show();
		}
	}

	private void stopRecord() {
		try {
			if (mediaRecorder != null) {
				mediaRecorder.stop();
				mediaRecorder.release();
				mediaRecorder = null;
				playSounds(2, 0);
				audio_file_bt.setVisibility(1);
			}
		} catch (Exception e) {
			// TODO: handle exception
			Toast.makeText(context, "录制失败，请检查音频设置并重试。", Toast.LENGTH_SHORT)
					.show();
		}
	}

	public void mPlayAudio(String url_str) {
		MediaPlayer mediaPlayer = new MediaPlayer();
		try {
			File file = new File(url_str);
			FileInputStream fis = new FileInputStream(file);
			mediaPlayer.setDataSource(fis.getFD());
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
		} catch (Exception e) {
			// TODO: handle exception
			Toast.makeText(context, "播放异常", Toast.LENGTH_SHORT).show();
		}
	}

	private class mPushMsg implements Runnable {

		String img = "";
		String remark = "";
		String audioBase64 = "";

		public void getMsg(String remark_temp, String img_temp,
				String audio_temp) {
			this.img = img_temp;
			this.remark = remark_temp;
			this.audioBase64 = audio_temp;
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			String qurl = MessengerService.URL;
			String qmethodname = MessengerService.METHOD_INTERACTIONSUBMIT;
			String qnamespace = MessengerService.NAMESPACE;
			String qsoapaction = qnamespace + "/" + qmethodname;

			SoapObject rpc = new SoapObject(qnamespace, qmethodname);
			rpc.addProperty("fromUser", usrID);
			rpc.addProperty("toUser", Integer.valueOf(contact_id));
			rpc.addProperty("content", remark);
			rcv_remark = remark;
			rpc.addProperty("images", img);
			rpc.addProperty("audio", audioBase64);
			rpc.addProperty("audioLength", recLen);
			rpc.addProperty("devType", 1);
			rpc.addProperty("namelist", rcv_user_name);
			SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
					SoapEnvelope.VER11);
			envelope.bodyOut = rpc;
			envelope.dotNet = true;
			envelope.setOutputSoapObject(rpc);
			HttpTransportSE ht = new HttpTransportSE(qurl);
			ht.debug = true;
			try {
				ht.call(qsoapaction, envelope);
				Object push_soap = envelope.getResponse();
				String result = push_soap.toString().trim();
				Message msg = new Message();
				msg.obj = result;
				pushthreadMessageHandler.sendMessage(msg);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (XmlPullParserException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	private class mContactBroadcast extends BroadcastReceiver {

		@Override
		public void onReceive(Context _context, Intent intent) {
			// TODO Auto-generated method stub
			if (intent.getAction().toString()
					.equals(MessengerService.CONTACT_CHOOSE_CONTACT)) {
				String _id = intent.getStringExtra("USRID");
				String _name = intent.getStringExtra("USRNAME");
				String _tel = intent.getStringExtra("USRTEL");
				String _type = intent.getStringExtra("USRTYPE");

				contact_id = _id;
				rcv_user_name = _name;
				rcv_user_tel = _tel;
				rcv_user_type = _type;
				contact_text.setText(rcv_user_name);
			}
		}

	}

	@SuppressLint("HandlerLeak")
	private class mGetContactDataThread implements Runnable {

		Context _context;
		TopicAdapter lvbt;
		ListView _listview;

		public void getSyncState(Context c) {
			this._context = c;
		}

		public void setListAdapter(ListView l) {
			this._listview = l;
			_listview.setAdapter(lvbt);
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			BBGJDB tdd = new BBGJDB(_context);
			// tdd.clearcontact();
			String curl = MessengerService.URL_WITHOUT_WSDL;
			String cmethodname = MessengerService.METHOD_GETUSERINFOBYCLASS;
			String cnamespace = MessengerService.NAMESPACE;
			String csoapaction = cnamespace + "/" + cmethodname;

			SoapObject rpc = new SoapObject(cnamespace, cmethodname);
			rpc.addProperty("userid", usrID);
			rpc.addProperty("pagesize", 1000);
			rpc.addProperty("pageindex", 1);
			SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
					SoapEnvelope.VER11);
			envelope.bodyOut = rpc;
			envelope.dotNet = true;
			envelope.setOutputSoapObject(rpc);
			HttpTransportSE ht = new HttpTransportSE(curl);
			ht.debug = true;

			try {
				ht.call(csoapaction, envelope);
				SoapObject contactlist = (SoapObject) envelope.bodyIn;
				for (int i = 0; i < contactlist.getPropertyCount(); i++) {
					SoapObject soapchilds = (SoapObject) contactlist
							.getProperty(i);
					for (int j = 0; j < soapchilds.getPropertyCount(); j++) {
						SoapObject soapchildsson = (SoapObject) soapchilds
								.getProperty(j);

						String webid = soapchildsson.getProperty("Id")
								.toString();
						String name = soapchildsson.getProperty("RealName")
								.toString();
						String tel = soapchildsson.getProperty("Sim")
								.toString();
						String type = soapchildsson.getProperty("Type")
								.toString();

						ContentValues values = new ContentValues();
						values.put(tdd.CONTACT_WEBID, webid);
						values.put(tdd.CONTACT_NAME, name);
						values.put(tdd.CONTACT_TEL, tel);
						values.put(tdd.CONTACT_TYPE, type);
						tdd.insertcontact(values);

					}
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (XmlPullParserException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Message msg = new Message();
			msg.arg1 = 1;
			threadMessageHandler.sendMessage(msg);

		}
	}

	public void playSounds(int sound, int number) {
		try {
			AudioManager am = (AudioManager) this
					.getSystemService(this.AUDIO_SERVICE);
			float audioMaxVolumn = am
					.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
			float audioCurrentVolumn = am
					.getStreamVolume(AudioManager.STREAM_MUSIC);
			float volumnRatio = audioCurrentVolumn / audioMaxVolumn;

			sp.play(spMap.get(sound), volumnRatio, volumnRatio, 1, number, 1);
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
}
