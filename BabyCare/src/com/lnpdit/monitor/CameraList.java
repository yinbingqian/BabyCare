package com.lnpdit.monitor;

import java.io.DataInputStream;
import java.io.IOException;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.SoapFault;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.LibVlcException;
import org.videolan.libvlc.Util;
import org.xmlpull.v1.XmlPullParserException;

import android.R.bool;
import android.R.integer;
import android.R.string;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
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
import android.view.Menu;
import android.view.MenuItem;
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
import com.company.Demo.ToolKits;
import com.company.NetSDK.CB_fHaveReConnect;
import com.company.NetSDK.CB_fSubDisConnect;
import com.company.NetSDK.INetSDK;
import com.company.NetSDK.NET_DEVICEINFO;
import com.lnpdit.babycare.R;
import com.lnpdit.util.NetUtil;
import com.lnpdit.util.passwordInterface;

public class CameraList extends passwordInterface implements
		OnItemClickListener, OnClickListener {

	protected static final int REQUEST_CODE = 0;
	private ListView cameralist;
	private ArrayList<HashMap<String, Object>> cameraItem;
	private SimpleAdapter cameraAdapter;
	// private ViewCenter videoView;
	private GestureDetector gestureScanner;
	private TextView cameralist_back;
//	private TextView cameralist_map;

	private Button btnRefresh;
	private TextView tip;
	public NetUtil netUtil = new NetUtil();

	public int screenWidth = 0;
	public int screenHeight = 0;
	public static int clear = 0;

	private String userName = "";// ���������û���
	private String passWord = "";// ������������
	private String serverIp = "";// ��ȡ�������Ĺ���ƽ̨IP
	private String socketIp = "";// ��ȡ��������ת��IP
	private String userId;
	private String ifPtz;
	private String ifMap;
	private String ifRecord;
	private String ifSnap;
	private Handler handler = null;

	Bundle loginBundle;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// ��ֹ����
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		setContentView(R.layout.cameralist);

		cameralist_back = (TextView) findViewById(R.id.cameralist_back);
		cameralist_back.setOnClickListener(this);
//		cameralist_map = (TextView) findViewById(R.id.cameralist_map);
//		cameralist_map.setOnClickListener(this);

		loginBundle = this.getIntent().getExtras();

		userId = loginBundle.getString("userId").toString();
		userName = loginBundle.getString("userName");// ��ȡ���������û���
		passWord = loginBundle.getString("passWord");// ��ȡ������������
		ifPtz = loginBundle.getString("ifPtz").toString();
		ifMap = loginBundle.getString("ifMap").toString();
		ifRecord = loginBundle.getString("ifRecord").toString();
		ifSnap = loginBundle.getString("ifSnap").toString();
//		if (ifMap.equals("0")) {
//			cameralist_map.setVisibility(TextView.GONE);
//		}
		// �����������̵߳�handler
		handler = new Handler();

		initDevList();
	}

	private void initDevList() {

		cameralist = (ListView) findViewById(R.id.camera_list);
		cameraItem = new ArrayList<HashMap<String, Object>>();

		ArrayList devList = loginBundle.getParcelableArrayList("videoList");
		ArrayList phoneList = devList;
		serverIp = loginBundle.getString("serverIp");
		socketIp = loginBundle.getString("socketIp");

		int i = 0;
		HashMap<String, Object> map = null;
		for (i = 0; i < devList.size(); i++) {
			StringBuffer sb = new StringBuffer("");
			map = (HashMap<String, Object>) devList.get(i);
			String stayLine = map.get("stayLine").toString();
			if (stayLine.equals("1")) {
				map.put("image", R.drawable.deviceicon);
				map.put("title", map.get("chName").toString());
			} else if (stayLine.equals("0")) {
				map.put("image", R.drawable.deviceicon_grey);
				map.put("title", map.get("chName").toString() + "(�豸����)");
			}

			map.put("devName", map.get("devName").toString());// �б���ʾ���豸���ƣ����硰չ�����豸��
			map.put("chName", map.get("chName").toString()); // ͨ�����ƣ�д������ͷ1������
			map.put("devId", map.get("devId").toString()); // �豸���кţ��������Ҫ��ÿ���豸��ͬ��������д�롰DHPZB2LN26800002������
			map.put("ip", map.get("ip").toString()); // �����������IP����59.46.115.85,������д�롰200.20.32.200������
			map.put("port", map.get("port").toString()); // �˿ںţ���9332����
			map.put("chNo", map.get("chNo").toString());// ��1����
			map.put("listCount", map.get("listCount").toString());// �豸����
			map.put("listNo", map.get("listNo").toString());// ���豸�е�˳��
			map.put("width", map.get("width").toString()); // д352
			map.put("height", map.get("height").toString()); // д288
			map.put("longitude", map.get("longitude").toString());// ���ȣ������Ǹ�չ���ľ���
			map.put("latitude", map.get("latitude").toString());// γ�ȣ������Ǹ�չ����γ��
			map.put("adapterId", map.get("adapterId").toString());// γ�ȣ������Ǹ�չ����γ��
			map.put("stayLine", map.get("stayLine").toString());// ��1
			map.put("rtsp", map.get("rtsp").toString());//
			map.put("devUserName", map.get("devUserName").toString());//
			map.put("devPassWord", map.get("devPassWord").toString());//
			map.put("devType", map.get("type").toString());//
			if (ifPtz.equals("1")) { // ���Ȩ����������̨������� ʵ������ж�
				map.put("ptz", map.get("ptz").toString());// �Ƿ�֧����̨�����֧�֣���1����֧�֣���0
				map.put("zoom", map.get("zoom").toString());// �Ƿ�֧����̨�����֧�֣���1����֧�֣���0
				map.put("talk", map.get("talk").toString());// �Ƿ�֧����̨�����֧�֣���1����֧�֣���0
			} else {// ���Ȩ������û����̨����0
				map.put("ptz", "0");// �Ƿ�֧����̨�����֧�֣���1����֧�֣���0
				map.put("zoom", "0");// �Ƿ�֧����̨�����֧�֣���1����֧�֣���0
				map.put("talk", "0");// �Ƿ�֧����̨�����֧�֣���1����֧�֣���0
			}
			map.put("type", "dev");// ��1

			cameraItem.add(map);
		}

		cameraAdapter = new SimpleAdapter(this, cameraItem,
				R.layout.camera_row, new String[] { "image", "title" },
				new int[] { R.id.ItemImage, R.id.ItemTitle });
		cameralist.setAdapter(cameraAdapter);
		cameralist.setOnItemClickListener(this);
	}

	public static final int UPDATE_ID = Menu.FIRST;

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		// menu.add(0, UPDATE_ID, 0, R.string.refreshvideo);
		return true;
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
		if (R.id.camera_list == arg0.getId()) {

			final String selectName = cameraItem.get(arg2).get("title")
					.toString();
			final String selectType = cameraItem.get(arg2).get("type")
					.toString();
			final String rtsp = cameraItem.get(arg2).get("rtsp").toString();
			final String devType = cameraItem.get(arg2).get("devType")
					.toString();

			if (cameraItem.get(arg2).get("stayLine").toString().equals("0")) {
				Toast toast = Toast.makeText(CameraList.this, "��Ƶ�����޷��ۿ���",
						Toast.LENGTH_SHORT);
				toast.show();
				return;
			}
			if (devType.equals("DHZL")) {
				Bundle bundle = this.getIntent().getExtras();
				Intent intent = new Intent(CameraList.this,
						DhPlayerActivity.class);
				intent.putExtra("devName", selectName);
				intent.putExtra("userId", userId);
				intent.putExtra("chName", cameraItem.get(arg2).get("chName")
						.toString());
				intent.putExtra("devId", cameraItem.get(arg2).get("devId")
						.toString());
				intent.putExtra("ip", cameraItem.get(arg2).get("ip").toString());
				intent.putExtra("port", cameraItem.get(arg2).get("port")
						.toString());
				intent.putExtra("devUserName",
						cameraItem.get(arg2).get("devUserName").toString());
				intent.putExtra("devPassWord",
						cameraItem.get(arg2).get("devPassWord").toString());
				intent.putExtra("chNo", cameraItem.get(arg2).get("chNo")
						.toString());
				intent.putExtra("listCount",
						cameraItem.get(arg2).get("listCount").toString());
				intent.putExtra("listNo", cameraItem.get(arg2).get("listNo")
						.toString());
				intent.putExtra("width", cameraItem.get(arg2).get("width")
						.toString());
				intent.putExtra("height", cameraItem.get(arg2).get("height")
						.toString());
				intent.putExtra("adapterId",
						cameraItem.get(arg2).get("adapterId").toString());
				intent.putExtra("ptz", cameraItem.get(arg2).get("ptz")
						.toString());
				intent.putExtra("zoom", cameraItem.get(arg2).get("zoom")
						.toString());
				intent.putExtra("talk", cameraItem.get(arg2).get("talk")
						.toString());
				intent.putExtra("socketIp", socketIp);
				intent.putExtra("ifRecord", ifRecord);
				intent.putExtra("ifSnap", ifSnap);
				intent.putExtras(bundle);

				startActivity(intent);
			} else {
				if (selectType.equals("dev")) {
					if (rtsp.equals("0")) {

						Bundle bundle = this.getIntent().getExtras();

						Intent intent = new Intent(CameraList.this,
								ShowVideo.class);
						intent.putExtra("devName", selectName);
						intent.putExtra("userId", userId);
						intent.putExtra("chName",
								cameraItem.get(arg2).get("chName").toString());
						intent.putExtra("devId",
								cameraItem.get(arg2).get("devId").toString());
						intent.putExtra("ip", cameraItem.get(arg2).get("ip")
								.toString());
						intent.putExtra("port", cameraItem.get(arg2)
								.get("port").toString());
						intent.putExtra("chNo", cameraItem.get(arg2)
								.get("chNo").toString());
						intent.putExtra("listCount",
								cameraItem.get(arg2).get("listCount")
										.toString());
						intent.putExtra("listNo",
								cameraItem.get(arg2).get("listNo").toString());
						intent.putExtra("width",
								cameraItem.get(arg2).get("width").toString());
						intent.putExtra("height",
								cameraItem.get(arg2).get("height").toString());
						intent.putExtra("adapterId",
								cameraItem.get(arg2).get("adapterId")
										.toString());
						intent.putExtra("ptz", cameraItem.get(arg2).get("ptz")
								.toString());
						intent.putExtra("zoom", cameraItem.get(arg2)
								.get("zoom").toString());
						intent.putExtra("talk", cameraItem.get(arg2)
								.get("talk").toString());
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
						String chNo = cameraItem.get(arg2).get("chNo")
								.toString();
						int addOneNo = Integer.parseInt(chNo) + 1;
						String rtspUrl = "";
						if (socketIp.contains(".net:")
								|| socketIp.contains(".com:")) {
							rtspUrl = "rtsp://"
									+ socketIp
									+ "/"
									+ cameraItem.get(arg2).get("devId")
											.toString() + "_"
									+ Integer.toString(addOneNo);
						} else {
							rtspUrl = "rtsp://"
									+ socketIp
									+ ":554/"
									+ cameraItem.get(arg2).get("devId")
											.toString() + "_"
									+ Integer.toString(addOneNo);
						}
						System.out.println("rtspUrl:" + rtspUrl);
						// String rtspUrl =
						// "rtsp://admin:12345@200.20.36.105/h264/ch1/sub/av_stream";
						Bundle bundle = this.getIntent().getExtras();
						Intent intent = new Intent(CameraList.this,
								VideoPlayerActivity.class);
						intent.putExtra("rtspUrl", rtspUrl);
						intent.putExtra("devName", selectName);
						intent.putExtra("userId", userId);
						intent.putExtra("chName",
								cameraItem.get(arg2).get("chName").toString());
						intent.putExtra("devId",
								cameraItem.get(arg2).get("devId").toString());
						intent.putExtra("ip", cameraItem.get(arg2).get("ip")
								.toString());
						intent.putExtra("port", cameraItem.get(arg2)
								.get("port").toString());
						intent.putExtra("chNo", cameraItem.get(arg2)
								.get("chNo").toString());
						intent.putExtra("listCount",
								cameraItem.get(arg2).get("listCount")
										.toString());
						intent.putExtra("listNo",
								cameraItem.get(arg2).get("listNo").toString());
						intent.putExtra("width",
								cameraItem.get(arg2).get("width").toString());
						intent.putExtra("height",
								cameraItem.get(arg2).get("height").toString());
						intent.putExtra("adapterId",
								cameraItem.get(arg2).get("adapterId")
										.toString());
						intent.putExtra("ptz", cameraItem.get(arg2).get("ptz")
								.toString());
						intent.putExtra("zoom", cameraItem.get(arg2)
								.get("zoom").toString());
						intent.putExtra("talk", cameraItem.get(arg2)
								.get("talk").toString());
						intent.putExtra("socketIp", socketIp);
						intent.putExtra("ifRecord", ifRecord);
						intent.putExtra("ifSnap", ifSnap);
						intent.putExtras(bundle);

						startActivity(intent);

					} else if (rtsp.equals("2")) {

						Bundle bundle = this.getIntent().getExtras();
						Intent intent = new Intent(CameraList.this,
								DhPlayerActivity.class);
						intent.putExtra("devName", selectName);
						intent.putExtra("userId", userId);
						intent.putExtra("chName",
								cameraItem.get(arg2).get("chName").toString());
						intent.putExtra("devId",
								cameraItem.get(arg2).get("devId").toString());
						intent.putExtra("ip", cameraItem.get(arg2).get("ip")
								.toString());
						intent.putExtra("port", cameraItem.get(arg2)
								.get("port").toString());
						intent.putExtra("devUserName", cameraItem.get(arg2)
								.get("devUserName").toString());
						intent.putExtra("devPassWord", cameraItem.get(arg2)
								.get("devPassWord").toString());
						intent.putExtra("chNo", cameraItem.get(arg2)
								.get("chNo").toString());
						intent.putExtra("listCount",
								cameraItem.get(arg2).get("listCount")
										.toString());
						intent.putExtra("listNo",
								cameraItem.get(arg2).get("listNo").toString());
						intent.putExtra("width",
								cameraItem.get(arg2).get("width").toString());
						intent.putExtra("height",
								cameraItem.get(arg2).get("height").toString());
						intent.putExtra("adapterId",
								cameraItem.get(arg2).get("adapterId")
										.toString());
						intent.putExtra("ptz", cameraItem.get(arg2).get("ptz")
								.toString());
						intent.putExtra("zoom", cameraItem.get(arg2)
								.get("zoom").toString());
						intent.putExtra("talk", cameraItem.get(arg2)
								.get("talk").toString());
						intent.putExtra("socketIp", socketIp);
						intent.putExtra("ifRecord", ifRecord);
						intent.putExtra("ifSnap", ifSnap);
						intent.putExtras(bundle);

						startActivity(intent);

					}
				} else if (selectType.equals("rtsp")) {

					try {
						mLibVLC = Util.getLibVlcInstance();
					} catch (LibVlcException e) {
						e.printStackTrace();
					}

					Intent intent = new Intent(CameraList.this,
							VideoPlayerActivity.class);
					intent.putExtra("rtspUrl",
							cameraItem.get(arg2).get("chName").toString());
					intent.putExtra("width", cameraItem.get(arg2).get("width")
							.toString());
					intent.putExtra("height", cameraItem.get(arg2)
							.get("height").toString());
					intent.putExtra("devName", selectName);
					intent.putExtra("userId", userId);
					intent.putExtra("devId", cameraItem.get(arg2).get("devId")
							.toString());
					intent.putExtra("chName", "0");
					intent.putExtra("ip", "0");
					intent.putExtra("port", "0");
					intent.putExtra("chNo", "0");
					intent.putExtra("listCount", "0");
					intent.putExtra("listNo", "0");
					intent.putExtra("adapterId", "1");
					intent.putExtra("ptz", cameraItem.get(arg2).get("ptz")
							.toString());
					intent.putExtra("zoom", cameraItem.get(arg2).get("zoom")
							.toString());
					intent.putExtra("talk", "0");
					intent.putExtra("socketIp", socketIp);
					intent.putExtra("ifRecord", ifRecord);
					intent.putExtra("ifSnap", ifSnap);

					startActivity(intent);
				}
			}
		}

	}

	private Thread threadLoading;

	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {

		case R.id.cameralist_back:
			this.finish();
			break;
//		case R.id.cameralist_map:
//			Intent intent = new Intent(CameraList.this, MapDialog.class);
//			intent.putExtras(loginBundle);
//
//			startActivity(intent);
//			break;

		}
	}
	Runnable runnableUi = new Runnable() {
		@Override
		public void run() {
			try {
				cameraAdapter.notifyDataSetChanged();
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			}
			waitClose();
		}

	};

	private void refreshDevList(SoapObject service) {

		Bundle bundle = this.getIntent().getExtras();
		ArrayList devList = bundle.getParcelableArrayList("videoList");

		cameraItem.clear();
		int count = service.getPropertyCount();
		for (int i = 0; i < count; i++) {
			SoapObject soapchilds = (SoapObject) service.getProperty(i);
			String ID = soapchilds.getProperty("Id").toString();
			String devName = soapchilds.getProperty("DevName").toString();
			String chName = soapchilds.getProperty("ChName").toString();
			String devId = soapchilds.getProperty("DevId").toString();
			String ip = soapchilds.getProperty("Ip").toString();
			String port = soapchilds.getProperty("Port").toString();
			String chNo = soapchilds.getProperty("ChNo").toString();
			String listCount = soapchilds.getProperty("ListCount").toString();
			String listNo = soapchilds.getProperty("ListNo").toString();
			String width = soapchilds.getProperty("Width").toString();
			String height = soapchilds.getProperty("Height").toString();
			String longitude = soapchilds.getProperty("Longitude").toString();
			String latitude = soapchilds.getProperty("Latitude").toString();
			String adapterId = soapchilds.getProperty("AdapterId").toString();
			String ptz = soapchilds.getProperty("Ptz").toString();
			String zoom = soapchilds.getProperty("Zoom").toString();
			String talk = soapchilds.getProperty("Talk").toString();
			String rtsp = soapchilds.getProperty("rtsp").toString();
			String stayLine = soapchilds.getProperty("Stayline").toString();

			HashMap<String, Object> map = new HashMap<String, Object>();

			if (stayLine.equals("1")) {
				map.put("image", R.drawable.deviceicon);
				map.put("title", chName);
			} else if (stayLine.equals("0")) {
				map.put("image", R.drawable.deviceicon_grey);
				map.put("title", chName + "(�豸����)");
			}

			map.put("devName", devName);// �б���ʾ���豸���ƣ����硰չ�����豸��
			map.put("chName", chName); // ͨ�����ƣ�д������ͷ1������
			map.put("devId", devId); // �豸���кţ��������Ҫ��ÿ���豸��ͬ��������д�롰DHPZB2LN26800002������
			map.put("ip", ip); // �����������IP����59.46.115.85,������д�롰200.20.32.200������
			map.put("port", port); // �˿ںţ���9332����
			map.put("chNo", chNo);// ��1����
			map.put("listCount", listCount);// �豸����
			map.put("listNo", listNo);// ���豸�е�˳��
			map.put("width", width); // д352
			map.put("height", height); // д288
			map.put("longitude", longitude);// ���ȣ������Ǹ�չ���ľ���
			map.put("latitude", latitude);// γ�ȣ������Ǹ�չ����γ��
			map.put("adapterId", adapterId);// γ�ȣ������Ǹ�չ����γ��
			map.put("stayLine", stayLine);// ��1
			map.put("rtsp", rtsp);// ��1
			if (ifPtz.equals("1")) { // ���Ȩ����������̨������� ʵ������ж�
				map.put("ptz", ptz);// �Ƿ�֧����̨�����֧�֣���1����֧�֣���0
				map.put("zoom", zoom);// �Ƿ�֧����̨�����֧�֣���1����֧�֣���0
				map.put("talk", talk);// �Ƿ�֧����̨�����֧�֣���1����֧�֣���0
			} else {// ���Ȩ������û����̨����0
				map.put("ptz", "0");// �Ƿ�֧����̨�����֧�֣���1����֧�֣���0
				map.put("zoom", "0");// �Ƿ�֧����̨�����֧�֣���1����֧�֣���0
				map.put("talk", "0");// �Ƿ�֧����̨�����֧�֣���1����֧�֣���0
			}

			cameraItem.add(map);
			bundle.remove("videoList");
			bundle.putParcelableArrayList("videoList", (ArrayList) cameraItem);
		}

		new Thread() {
			public void run() {
				handler.post(runnableUi);
			}
		}.start();
		waitClose();

	}

	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		// �˳����������˳�����Ϊture
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (KeyEvent.KEYCODE_BACK == event.getKeyCode()) {
			this.finish();
			return true;
		}
		return super.onKeyUp(keyCode, event);
	}

}
