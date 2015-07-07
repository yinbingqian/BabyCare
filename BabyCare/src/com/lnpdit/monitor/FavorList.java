package com.lnpdit.monitor;

import java.io.DataInputStream;
import java.io.IOException;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.LibVlcException;
import org.videolan.libvlc.Util;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.Display;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.GestureDetector.OnGestureListener;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;
import android.widget.AdapterView.OnItemClickListener;

import com.company.Demo.DhPlayerActivity;
import com.lnpdit.babycare.R;
import com.lnpdit.sqllite.ToDoDB;
import com.lnpdit.util.NetUtil;
import com.lnpdit.util.passwordInterface;

public class FavorList extends passwordInterface implements
		OnItemClickListener, OnClickListener {

	protected static final int REQUEST_CODE = 0;
	private ListView favorlist;
	private ArrayList<HashMap<String, Object>> favorItem;
	private SimpleAdapter favorAdapter;
	// private ViewCenter videoView;
	private TextView tip;
	public NetUtil netUtil = new NetUtil();

	public int screenWidth = 0;
	public int screenHeight = 0;
	public static int clear = 0;

	private String serverIp = "";// 获取传过来的管理平台IP
	private String socketIp = "";// 获取传过来的转发IP
	private String userId;
	private String ifPtz;
	private String ifMap;
	private String ifRecord;
	private String ifSnap;
	private TextView favorlist_back;

	// 数据库
	ToDoDB db;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// 禁止锁屏
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		setContentView(R.layout.favorlist);

		favorlist_back = (TextView) findViewById(R.id.favorlist_back);
		favorlist_back.setOnClickListener(this);

		userId = this.getIntent().getStringExtra("userId").toString();
		ifPtz = this.getIntent().getStringExtra("ifPtz").toString();
		ifMap = this.getIntent().getStringExtra("ifMap").toString();
		ifRecord = this.getIntent().getStringExtra("ifRecord").toString();
		ifSnap = this.getIntent().getStringExtra("ifSnap").toString();

		initDevList();
	}

	private void initDevList() {

		favorlist = (ListView) findViewById(R.id.favor_list);
		favorItem = new ArrayList<HashMap<String, Object>>();

		Bundle bundle = this.getIntent().getExtras();
		ArrayList devList = bundle.getParcelableArrayList("videoList");
		serverIp = bundle.getString("serverIp");
		socketIp = bundle.getString("socketIp");

		int i = 0;
		for (i = 0; i < devList.size(); i++) {
			StringBuffer sb = new StringBuffer("");
			HashMap<String, Object> map = (HashMap<String, Object>) devList
					.get(i);

			// 数据库
			db = new ToDoDB(this);
			Cursor cur = db.select_favor();
			while (cur.moveToNext()) {
				int userIndex = cur.getColumnIndex("DATA_USERID");
				String webUserId = cur.getString(userIndex);
				int devIndex = cur.getColumnIndex("DATA_DEVID");
				String favorDevId = cur.getString(devIndex);
				int chIndex = cur.getColumnIndex("DATA_CHNO");
				String favorChNo = cur.getString(chIndex);
				if (favorDevId.equals(map.get("devId").toString())
						&& favorChNo.equals(map.get("chNo").toString())
						&& webUserId.equals(userId)) {

					String stayLine = map.get("stayLine").toString();
					if (stayLine.equals("1")) {
						map.put("image", R.drawable.camera);
						map.put("title", map.get("chName").toString());
					} else if (stayLine.equals("0")) {
						map.put("image", R.drawable.cameraoffline);
						map.put("title", map.get("chName").toString()
								+ "(设备离线)");
					}

					map.put("devName", map.get("devName").toString());// 列表显示的设备名称，比如“展厅大华设备”
					map.put("chName", map.get("chName").toString()); // 通道名称，写“摄像头1”即可
					map.put("devId", map.get("devId").toString()); // 设备序列号，这个很重要，每个设备不同，你现在写入“DHPZB2LN26800002”即可
					map.put("ip", map.get("ip").toString()); // 传服务的外网IP，如59.46.115.85,你现在写入“200.20.32.200”即可
					map.put("port", map.get("port").toString()); // 端口号，传9332即可
					map.put("chNo", map.get("chNo").toString());// 传1即可
					map.put("listCount", map.get("listCount").toString());// 设备总数
					map.put("listNo", map.get("listNo").toString());// 在设备中的顺序
					map.put("width", map.get("width").toString()); // 写352
					map.put("height", map.get("height").toString()); // 写288
					map.put("longitude", map.get("longitude").toString());// 经度，传你那个展厅的经度
					map.put("latitude", map.get("latitude").toString());// 纬度，传你那个展厅的纬度
					map.put("adapterId", map.get("adapterId").toString());// 纬度，传你那个展厅的纬度
					map.put("stayLine", map.get("stayLine").toString());// 传1
					map.put("rtsp", map.get("rtsp").toString());// 传1
					map.put("devUserName", map.get("devUserName").toString());//
					map.put("devPassWord", map.get("devPassWord").toString());//
					map.put("devType", map.get("type").toString());//
					if (ifPtz.equals("1")) { // 如果权限里面有云台，则根据 实际情况判断
						map.put("ptz", map.get("ptz").toString());// 是否支持云台，如果支持，传1，不支持，传0
						map.put("zoom", map.get("zoom").toString());// 是否支持云台，如果支持，传1，不支持，传0
						map.put("talk", map.get("talk").toString());// 是否支持云台，如果支持，传1，不支持，传0
					} else {// 如果权限里面没有云台，则传0
						map.put("ptz", "0");// 是否支持云台，如果支持，传1，不支持，传0
						map.put("zoom", "0");// 是否支持云台，如果支持，传1，不支持，传0
						map.put("talk", "0");// 是否支持云台，如果支持，传1，不支持，传0
					}

					favorItem.add(map);
				}
			}
		}

		favorAdapter = new SimpleAdapter(this, favorItem, R.layout.camera_row,
				new String[] { "image", "title" }, new int[] { R.id.ItemImage,
						R.id.ItemTitle });
		favorlist.setAdapter(favorAdapter);
		favorlist.setOnItemClickListener(this);
	}

	/*
	 * start to view
	 */
	String port = "";
	String stayLine = "";
	Boolean ifCanBoolean = true;
	public final static String TAG = "VLC/VideoPlayerActivity";

	private LibVLC mLibVLC = null;

	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long id) {
		if (R.id.favor_list == arg0.getId()) {

			final String selectName = favorItem.get(arg2).get("title")
					.toString();
			final String rtsp = favorItem.get(arg2).get("rtsp").toString();

			final String devType = favorItem.get(arg2).get("devType")
					.toString();

			if (devType.equals("LDH")) {
			} else {

				if (favorItem.get(arg2).get("stayLine").toString().equals("0")) {
					Toast toast = Toast.makeText(FavorList.this, "视频离线无法观看！",
							Toast.LENGTH_SHORT);
					toast.show();
					return;
				}

				if (rtsp.equals("0")) {

					Bundle bundle = this.getIntent().getExtras();

					Intent intent = new Intent(FavorList.this, ShowVideo.class);
					intent.putExtra("devName", selectName);
					intent.putExtra("userId", userId);
					intent.putExtra("chName", favorItem.get(arg2).get("chName")
							.toString());
					intent.putExtra("devId", favorItem.get(arg2).get("devId")
							.toString());
					intent.putExtra("ip", favorItem.get(arg2).get("ip")
							.toString());
					intent.putExtra("port", favorItem.get(arg2).get("port")
							.toString());
					intent.putExtra("chNo", favorItem.get(arg2).get("chNo")
							.toString());
					intent.putExtra("listCount",
							favorItem.get(arg2).get("listCount").toString());
					intent.putExtra("listNo", favorItem.get(arg2).get("listNo")
							.toString());
					intent.putExtra("width", favorItem.get(arg2).get("width")
							.toString());
					intent.putExtra("height", favorItem.get(arg2).get("height")
							.toString());
					intent.putExtra("adapterId",
							favorItem.get(arg2).get("adapterId").toString());
					intent.putExtra("ptz", favorItem.get(arg2).get("ptz")
							.toString());
					intent.putExtra("zoom", favorItem.get(arg2).get("zoom")
							.toString());
					intent.putExtra("talk", favorItem.get(arg2).get("talk")
							.toString());
					intent.putExtra("socketIp", socketIp);
					intent.putExtra("ifRecord", ifRecord);
					intent.putExtra("ifSnap", ifSnap);
					intent.putExtras(bundle);

					startActivity(intent);
				} else if (rtsp.equals("1")) {

					try {
						mLibVLC = Util.getLibVlcInstance();
					} catch (LibVlcException e) {
						e.printStackTrace();
					}
					String chNo = favorItem.get(arg2).get("chNo").toString();
					int addOneNo = Integer.parseInt(chNo) + 1;
					String rtspUrl = "";
					if (socketIp.contains(".net") || socketIp.contains(".com")) {
						rtspUrl = "rtsp://" + socketIp + "/"
								+ favorItem.get(arg2).get("devId").toString()
								+ "_" + Integer.toString(addOneNo);
					} else {
						rtspUrl = "rtsp://" + socketIp + ":554/"
								+ favorItem.get(arg2).get("devId").toString()
								+ "_" + Integer.toString(addOneNo);
					}
					// String rtspUrl =
					// "rtsp://admin:12345@200.20.36.105/h264/ch1/sub/av_stream";
					Bundle bundle = this.getIntent().getExtras();
					Intent intent = new Intent(FavorList.this,
							VideoPlayerActivity.class);
					intent.putExtra("rtspUrl", rtspUrl);
					intent.putExtra("devName", selectName);
					intent.putExtra("userId", userId);
					intent.putExtra("chName", favorItem.get(arg2).get("chName")
							.toString());
					intent.putExtra("devId", favorItem.get(arg2).get("devId")
							.toString());
					intent.putExtra("ip", favorItem.get(arg2).get("ip")
							.toString());
					intent.putExtra("port", favorItem.get(arg2).get("port")
							.toString());
					intent.putExtra("chNo", favorItem.get(arg2).get("chNo")
							.toString());
					intent.putExtra("listCount",
							favorItem.get(arg2).get("listCount").toString());
					intent.putExtra("listNo", favorItem.get(arg2).get("listNo")
							.toString());
					intent.putExtra("width", favorItem.get(arg2).get("width")
							.toString());
					intent.putExtra("height", favorItem.get(arg2).get("height")
							.toString());
					intent.putExtra("adapterId",
							favorItem.get(arg2).get("adapterId").toString());
					intent.putExtra("ptz", favorItem.get(arg2).get("ptz")
							.toString());
					intent.putExtra("zoom", favorItem.get(arg2).get("zoom")
							.toString());
					intent.putExtra("talk", favorItem.get(arg2).get("talk")
							.toString());
					intent.putExtra("socketIp", socketIp);
					intent.putExtra("ifRecord", ifRecord);
					intent.putExtra("ifSnap", ifSnap);
					intent.putExtras(bundle);

					startActivity(intent);

				} else if (rtsp.equals("2")) {

					Bundle bundle = this.getIntent().getExtras();
					Intent intent = new Intent(FavorList.this,
							DhPlayerActivity.class);
					intent.putExtra("devName", selectName);
					intent.putExtra("userId", userId);
					intent.putExtra("chName", favorItem.get(arg2).get("chName")
							.toString());
					intent.putExtra("devId", favorItem.get(arg2).get("devId")
							.toString());
					intent.putExtra("ip", favorItem.get(arg2).get("ip")
							.toString());
					intent.putExtra("port", favorItem.get(arg2).get("port")
							.toString());
					intent.putExtra("devUserName",
							favorItem.get(arg2).get("devUserName").toString());
					intent.putExtra("devPassWord",
							favorItem.get(arg2).get("devPassWord").toString());
					intent.putExtra("chNo", favorItem.get(arg2).get("chNo")
							.toString());
					intent.putExtra("listCount",
							favorItem.get(arg2).get("listCount").toString());
					intent.putExtra("listNo", favorItem.get(arg2).get("listNo")
							.toString());
					intent.putExtra("width", favorItem.get(arg2).get("width")
							.toString());
					intent.putExtra("height", favorItem.get(arg2).get("height")
							.toString());
					intent.putExtra("adapterId",
							favorItem.get(arg2).get("adapterId").toString());
					intent.putExtra("ptz", favorItem.get(arg2).get("ptz")
							.toString());
					intent.putExtra("zoom", favorItem.get(arg2).get("zoom")
							.toString());
					intent.putExtra("talk", favorItem.get(arg2).get("talk")
							.toString());
					intent.putExtra("socketIp", socketIp);
					intent.putExtra("ifRecord", ifRecord);
					intent.putExtra("ifSnap", ifSnap);
					intent.putExtras(bundle);

					startActivity(intent);

				}
			}
		}

	}

	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {

		case R.id.favorlist_back:
			this.finish();
			break;

		}
	}

	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		// 退出，则设置退出属性为ture
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (KeyEvent.KEYCODE_BACK == event.getKeyCode()) {
			this.finish();
			return true;
		}
		return super.onKeyUp(keyCode, event);
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}

}
