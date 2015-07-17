package com.lnpdit.babycare;

import java.io.File;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.videolan.libvlc.VLCApplication;

import com.easemob.EMCallBack;
import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMContactManager;
import com.easemob.chat.EMGroupManager;
import com.easemob.util.EMLog;
import com.easemob.util.HanziToPinyin;
import com.lnpdit.photo.Constant;
import com.lnpdit.sqllite.ToDoDB;
import com.lnpdit.sqllite.User;
import com.lnpdit.sqllite.UserDao;
import com.lnpdit.util.ConnectionDetector;
import com.lnpdit.util.DownLoadManager;
import com.lnpdit.util.UpdataInfo;
import com.lnpdit.util.UpdataInfoParser;
import com.lnpdit.util.passwordInterface;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class LoginActivity extends passwordInterface implements OnClickListener {

	private EditText field_address;
	private EditText field_name;
	private EditText field_pass;
	private boolean isConnect;

	public static final String SETTING_INFOS = "SETTING_Infos";
	public static final String SERIP = "serverIp";
	public static final String NAME = "name";
	public static final String PASS = "pass";
	public static final String REMEMBER = "remember";
	public static final String AUTOLOG = "autolog";

	// 登陆加载计时器
	private int timerCount = 0;
	private Timer timer;
	private boolean isWaiting = true;
	private Thread threadLoading;
	private Thread webLoading;

	// 检测网络状态
	Boolean isInternetPresent = false;
	ConnectionDetector cd;

	Button btnLogin;
	ImageView btnDelAddress;
	ImageView btnDelUsername;
	ImageView btnDelPassword;
	ProgressBar loginProgressBar;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		// 禁止锁屏
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		// String strOpt = dm.widthPixels + "*" + dm.heightPixels; 
		// ShowMessage(strOpt);
		setContentView(R.layout.login);

		tdd = new ToDoDB(this);

		loginProgressBar = (ProgressBar) findViewById(R.id.login_progressbar);
		handler.post(runnableHidProgress);

		btnLogin = (Button) findViewById(R.id.login_bt);
		btnLogin.setOnClickListener(this);
		btnDelAddress = (ImageView) findViewById(R.id.deleteaddress);
		btnDelAddress.setOnClickListener(this);
		btnDelAddress.setVisibility(ImageView.GONE);
		btnDelUsername = (ImageView) findViewById(R.id.deleteusername);
		btnDelUsername.setOnClickListener(this);
		btnDelUsername.setVisibility(ImageView.GONE);
		btnDelPassword = (ImageView) findViewById(R.id.deletepassword);
		btnDelPassword.setOnClickListener(this);
		btnDelPassword.setVisibility(ImageView.GONE);

		field_address = (EditText) findViewById(R.id.address_edit);
		field_name = (EditText) findViewById(R.id.username_edit);
		field_pass = (EditText) findViewById(R.id.password_edit);
		SharedPreferences settings = getSharedPreferences(SETTING_INFOS, 0);
//		String ip = settings.getString(SERIP, "");
		String name = settings.getString(NAME, "");
		String pass = settings.getString(PASS, "");
		String remember = settings.getString(REMEMBER, "");
		field_address.setText("221.180.149.201");
//		field_address.setText("221.180.149.151");
		field_name.setText(name);
		field_pass.setText(pass);

		if (!field_address.getText().toString().equals("")) {
			btnDelAddress.setVisibility(ImageView.VISIBLE);
		} else {
			btnDelAddress.setVisibility(ImageView.GONE);
		}
//		if (!field_name.getText().toString().equals("")) {
//			btnDelUsername.setVisibility(ImageView.VISIBLE);
//		} else {
//			btnDelUsername.setVisibility(ImageView.GONE);
//		}
//		if (!field_pass.getText().toString().equals("")) {
//			btnDelPassword.setVisibility(ImageView.VISIBLE);
//		} else {
//			btnDelPassword.setVisibility(ImageView.GONE);
//		}

		getPhoneNumberType();

		checkVersion();

		field_address.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				if (!field_address.getText().toString().equals("")) {
					btnDelAddress.setVisibility(ImageView.VISIBLE);
				} else {
					btnDelAddress.setVisibility(ImageView.GONE);
				}
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				if (!field_address.getText().toString().equals("")) {
					btnDelAddress.setVisibility(ImageView.VISIBLE);
				} else {
					btnDelAddress.setVisibility(ImageView.GONE);
				}
			}

			@Override
			public void afterTextChanged(Editable s) {
				if (!field_address.getText().toString().equals("")) {
					btnDelAddress.setVisibility(ImageView.VISIBLE);
				} else {
					btnDelAddress.setVisibility(ImageView.GONE);
				}
			}
		});

		field_name.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
//				if (!field_name.getText().toString().equals("")) {
//					btnDelUsername.setVisibility(ImageView.VISIBLE);
//				} else {
//					btnDelUsername.setVisibility(ImageView.GONE);
//				}
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
//				if (!field_name.getText().toString().equals("")) {
//					btnDelUsername.setVisibility(ImageView.VISIBLE);
//				} else {
//					btnDelUsername.setVisibility(ImageView.GONE);
//				}
			}

			@Override
			public void afterTextChanged(Editable s) {
//				if (!field_name.getText().toString().equals("")) {
//					btnDelUsername.setVisibility(ImageView.VISIBLE);
//				} else {
//					btnDelUsername.setVisibility(ImageView.GONE);
//				}
			}
		});

		field_pass.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
//				if (!field_pass.getText().toString().equals("")) {
//					btnDelPassword.setVisibility(ImageView.VISIBLE);
//				} else {
//					btnDelPassword.setVisibility(ImageView.GONE);
//				}
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
//				if (!field_pass.getText().toString().equals("")) {
//					btnDelPassword.setVisibility(ImageView.VISIBLE);
//				} else {
//					btnDelPassword.setVisibility(ImageView.GONE);
//				}
			}

			@Override
			public void afterTextChanged(Editable s) {
//				if (!field_pass.getText().toString().equals("")) {
//					btnDelPassword.setVisibility(ImageView.VISIBLE);
//				} else {
//					btnDelPassword.setVisibility(ImageView.GONE);
//				}
			}
		});

	}

	private void checkVersion() {

		// int times = 0;
		SharedPreferences usedInfo = getSharedPreferences("use_info", 0);
		// String usedTimes = usedInfo.getString("use_times", "");
		String version = usedInfo.getString("use_version", "");

		SharedPreferences.Editor editor = usedInfo.edit();

		String usedVersion = "";
		try {
			usedVersion = getVersionName();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (!version.equals(usedVersion)) {
			tdd.clearfavor();
		}
		editor.putString("use_version", usedVersion).commit();
		editor.commit();
	}

	private String getPhoneNumberType() {

		try {

			TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
			// imsi 国际移动用户识别码
			String imsi = tm.getSubscriberId();

			if (imsi != null) {
				if (imsi.startsWith("46000") || imsi.startsWith("46002")
						|| imsi.startsWith("46007")) {
					// 中国移动
					Log.v("运营商:", "中国移动" + " " + imsi);
					return "1";
				} else if (imsi.startsWith("46001") || imsi.startsWith("46006")) {
					// 中国联通
					Log.v("运营商:", "中国联通" + " " + imsi);
					return "2";
				} else if (imsi.startsWith("46003") || imsi.startsWith("46005")) {
					// 中国电信
					Log.v("运营商:", "中国电信" + " " + imsi);
					return "3";
				} else {
					// 无法判断
					Log.v("运营商", "无法判断运营商信息" + " " + imsi);
					return "0";
				}

			} else {
				// imsi为空
				return "0";
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			return "0";
		}
	}

	// 显示progressBar
	Runnable runnableShowProgress = new Runnable() {
		@Override
		public void run() {
			loginProgressBar.setVisibility(ProgressBar.VISIBLE);
			btnLogin.setVisibility(Button.GONE);
		}

	};
	// 隐藏progressBar
	Runnable runnableHidProgress = new Runnable() {
		@Override
		public void run() {
			loginProgressBar.setVisibility(ProgressBar.GONE);
			btnLogin.setVisibility(Button.VISIBLE);
		}

	};

	// 登陆状态
	Boolean status = false;

	public static final int UPDATE_ID = Menu.FIRST;
	public static final int ABOUT_ID = Menu.FIRST + 1;

	public void showToast(String sc) {
		Toast toast = Toast
				.makeText(LoginActivity.this, sc, Toast.LENGTH_SHORT);
		toast.show();
	}

	private String serverIp = "";// 获取传过来的管理平台IP
	private String socketIp = "";// 获取传过来的转发IP
	private String nameString = "";
	private String passWordString = "";
	private String phoneIp = "";

	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.login_bt:

			cd = new ConnectionDetector(getApplicationContext());
			isInternetPresent = cd.isConnectingToInternet();
			if (!isInternetPresent) {
				Toast.makeText(this,
						getResources().getString(R.string.loginerror),
						Toast.LENGTH_SHORT).show();
				break;
			} else {
				try {
					// 登陆等待提示框
					// 数据初始化
					loadingFlag = true;
					isWaiting = true;
					status = false;
					timerCount = 0;
					timer = new Timer();

					socketIp = field_address.getText().toString().trim();
					nameString = field_name.getText().toString().trim();
					passWordString = field_pass.getText().toString().trim();
					//
					if (socketIp.equals("")) {
						Toast.makeText(getApplicationContext(),
								getResources().getString(R.string.ipempty), 1)
								.show();
						return;
					}

					if (nameString.equals("")) {
						Toast.makeText(
								getApplicationContext(),
								getResources()
										.getString(R.string.usernameempty), 1)
								.show();
						return;
					}
					if (passWordString.equals("")) {
						Toast.makeText(
								getApplicationContext(),
								getResources()
										.getString(R.string.passwordempty), 1)
								.show();
						return;
					}

					// if (!ipString.contains(":")) {
					// field_serverip.setText(ipString + ":7799");
					// ipString += ":7799";
					// } else {
					// String[] strings = ipString.split(":");
					// if (strings.length == 1) {
					// field_serverip.setText(ipString + "7799");
					// ipString += "7799";
					// }
					// }

					// 等待框进程
					handler.post(runnableShowProgress);
					threadLoading = new Thread(waitThread);
					threadLoading.start();

					// WebService进程
					webLoading = new Thread(newWebThread);
					webLoading.start();
					// this.logToServer();// 这个是正式程序里面的跳转
					break;
				} catch (Exception e) {
					e.printStackTrace();
					handler.post(runnableHidProgress);
					showToast("网络连接失败");
					break;
				}
			}

		case R.id.deleteaddress:
			field_address.setText("");
			break;
		case R.id.deleteusername:
			field_name.setText("");
			break;

		case R.id.deletepassword:
			field_pass.setText("");
			break;
		}
	}

	// 密码提示
	Runnable runnableWaitting = new Runnable() {
		@Override
		public void run() {
			isWaiting = false;

			handler.post(runnableHidProgress);
			Toast.makeText(LoginActivity.this, "用户名或者密码错误，请查证后输入！",
					Toast.LENGTH_LONG).show();
		}

	};

	// 锁定提示
	Runnable lockWaitting = new Runnable() {
		@Override
		public void run() {
			isWaiting = false;

			handler.post(runnableHidProgress);
			Toast.makeText(LoginActivity.this, "该用户已被锁定，不能登录！",
					Toast.LENGTH_LONG).show();
		}

	};

	// 无法获取运营商提示
	Runnable noYys = new Runnable() {
		@Override
		public void run() {
			isWaiting = false;

			handler.post(runnableHidProgress);
			Toast.makeText(LoginActivity.this, "无法获取运营商信息，不能登录！",
					Toast.LENGTH_LONG).show();
		}

	};

	// 运营商不符合提示
	Runnable wrongYys = new Runnable() {
		@Override
		public void run() {
			isWaiting = false;

			handler.post(runnableHidProgress);
			Toast.makeText(LoginActivity.this, "您的运营商信息不符合，不能登录！",
					Toast.LENGTH_LONG).show();
		}

	};

	// 超时提示
	Runnable runnableRunOver = new Runnable() {
		@Override
		public void run() {
			Toast.makeText(LoginActivity.this, "网络不稳定，请稍候重试！",
					Toast.LENGTH_SHORT).show();
		}

	};

	// 用户已经登录提示
	Runnable onlyWaitting = new Runnable() {
		@Override
		public void run() {
			isWaiting = false;

			handler.post(runnableHidProgress);
			Toast.makeText(LoginActivity.this, "该用户正在使用中，不能登录！",
					Toast.LENGTH_LONG).show();
		}

	};
	// webService进程进行登陆

	ToDoDB tdd;

	// 新的登录过程

	Runnable newWebThread = new Runnable() {
		public void run() {
			try {
				String NAMESPACE = "MobileNewspaper";
				String METHOD_NAME = "NewLogin";
				String serverIp = "";

				// socketIp = "221.180.149.151";
				serverIp = socketIp;

				String URL = "";
				if (serverIp.equals("218.60.13.9:7799")) {
					URL = "http://" + serverIp
							+ getResources().getString(R.string.url_server);
				} else {
					URL = "http://" + serverIp + ":7799"
							+ getResources().getString(R.string.url_server);
				}
				String SOAP_ACTION = "MobileNewspaper/NewLogin";

				SoapObject rpc = new SoapObject(NAMESPACE, METHOD_NAME);
				rpc.addProperty("userName", nameString);
				rpc.addProperty("passWord", passWordString);

				SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
						SoapEnvelope.VER11);
				envelope.bodyOut = rpc;
				envelope.dotNet = true;
				envelope.setOutputSoapObject(rpc);

				HttpTransportSE ht = new HttpTransportSE(URL);

				ht.debug = true;
				ht.call(SOAP_ACTION, envelope);
				// Object object = envelope.getResponse();
				// System.out.print("rpc:");
				// System.out.println(rpc);
				//
				// String result = object.toString();
				String yysString = getPhoneNumberType();
				if (yysString.equals("0")) {

				}
				SoapObject login = (SoapObject) envelope.getResponse();

				int count = login.getPropertyCount();

				String userId = "0";
				String comId = "0";
				String realName = "0";
				String empId = "0";

				String ifExist = "0";
				String ifBbyy = "1";
				String ifBbgj = "1";
				String ifKqgl = "1";
				String ifBbhy = "1";

				String ifVideo = "0";
				String ifPtz = "0";
				String ifRecord = "0";
				String ifSnap = "0";
				String ifMap = "0";

				String ifFavor = "0";
				String ifDistance = "0";
				String ifKinder = "0";
				String ifUpload = "0";
				String ifNews = "0";
				String yys = "0";
				String ifPay = "0";
				String endDate = "";
				String payStatus = "0";
				String ifonly = "0";
				String userType = "家长";
				String kinderName = "小天童幼儿园";

				ArrayList<HashMap<String, Object>> listItem = new ArrayList<HashMap<String, Object>>();
				for (int i = 0; i < count; i++) {
					SoapObject soapchilds = (SoapObject) login.getProperty(i);

					try {

						ifExist = soapchilds.getProperty("Id").toString();
						ifVideo = soapchilds.getProperty("Video").toString();
						ifPtz = soapchilds.getProperty("Userptz").toString();
						ifRecord = soapchilds.getProperty("Record").toString();
						ifSnap = soapchilds.getProperty("Snap").toString();
						ifMap = soapchilds.getProperty("Map").toString();
						yys = soapchilds.getProperty("Yys").toString();
						ifFavor = soapchilds.getProperty("Favor").toString();
						ifDistance = soapchilds.getProperty("Distance")
								.toString();
						ifKinder = soapchilds.getProperty("Kinder").toString();
						ifUpload = soapchilds.getProperty("Upload").toString();
						ifNews = soapchilds.getProperty("News").toString();
						ifonly = soapchilds.getProperty("Status").toString();
						ifPay = soapchilds.getProperty("Pay").toString();
						endDate = soapchilds.getProperty("Enddate").toString();
						payStatus = soapchilds.getProperty("PayStatus")
								.toString();
						userId = soapchilds.getProperty("UserId").toString();
						userType = soapchilds.getProperty("KinderType")
								.toString();
						kinderName = soapchilds.getProperty("KinderName")
								.toString();
						comId = soapchilds.getProperty("ComId").toString();
						realName = soapchilds.getProperty("RealName")
								.toString();
						empId = soapchilds.getProperty("EmpId").toString();
						ifBbyy = soapchilds.getProperty("Bbyy").toString();
						ifBbgj = soapchilds.getProperty("Bbgj").toString();
						ifKqgl = soapchilds.getProperty("Kqgl").toString();
						ifBbhy = soapchilds.getProperty("Bbhy").toString();
					} catch (Exception e) {
						// TODO: handle exception
						ifBbyy = "1";
						ifBbgj = "1";
						ifKqgl = "1";
						ifBbhy = "1";
					}
					if (ifExist.equals("0")) {
						runOnUiThread(runnableWaitting);
						return;
					}

					if (ifExist.equals("2")) {
						runOnUiThread(lockWaitting);
						return;
					}

					if (!yys.equals("0")) { // 如果限制运营商，则判断
						if (yysString.equals("0")) {
							runOnUiThread(noYys);
							return;
						}

						if (!yysString.equals(yys)) {
							runOnUiThread(wrongYys);
							return;
						}
					}

					if (ifonly.equals("1")) {
						runOnUiThread(onlyWaitting);
						return;
					}
					isConnect = true;

					phoneIp = getLocalIpAddress();

					String ID = soapchilds.getProperty("Id").toString();
					String devName = soapchilds.getProperty("DevName")
							.toString();
					String chName = soapchilds.getProperty("ChName").toString();
					String devId = soapchilds.getProperty("DevId").toString();
					String ip = soapchilds.getProperty("Ip").toString();
					String port = soapchilds.getProperty("DevPort").toString();
					String chNo = soapchilds.getProperty("ChNo").toString();
					String listCount = soapchilds.getProperty("ListCount")
							.toString();
					String listNo = soapchilds.getProperty("ListNo").toString();
					String width = soapchilds.getProperty("Width").toString();
					String height = soapchilds.getProperty("Height").toString();
					String longitude = soapchilds.getProperty("Longitude")
							.toString();
					String latitude = soapchilds.getProperty("Latitude")
							.toString();
					String adapterId = soapchilds.getProperty("AdapterId")
							.toString();
					String ptz = soapchilds.getProperty("Ptz").toString();
					String zoom = "0";
					String talk = "0";
					String rtsp = "0";
					String devUserName = "admin";
					String devPassWord = "admin";
					String type = "DH";
					try {
						zoom = soapchilds.getProperty("Zoom").toString();
						talk = soapchilds.getProperty("Talk").toString();
						rtsp = soapchilds.getProperty("Rtsp").toString();
						devUserName = soapchilds.getProperty("Username")
								.toString();
						devPassWord = soapchilds.getProperty("Password")
								.toString();
						type = soapchilds.getProperty("Type").toString();
					} catch (Exception e) {
						// TODO: handle exception
					}
					String stayLine = soapchilds.getProperty("Stayline")
							.toString();

					HashMap<String, Object> map = new HashMap<String, Object>();
					map.put("ID", ID); // ID
					map.put("devName", devName);// 列表显示的设备名称，比如“展厅大华设备”
					map.put("chName", chName); // 通道名称，写“摄像头1”即可
					map.put("devId", devId); // 设备序列号，这个很重要，每个设备不同，你现在写入“DHPZB2LN26800002”即可
					map.put("ip", ip); // 传服务的外网IP，如59.46.115.85,你现在写入“200.20.32.200”即可
					map.put("port", port); //
					map.put("chNo", chNo);// 传1即可
					map.put("listCount", listCount);// 设备总数
					map.put("listNo", listNo);// 在设备中的顺序
					map.put("width", width); // 写352
					map.put("height", height); // 写288
					map.put("longitude", longitude);// 经度，传你那个展厅的经度
					map.put("latitude", latitude);// 纬度，传你那个展厅的纬度
					map.put("adapterId", adapterId);// 适配ID
					map.put("stayLine", stayLine);// 传1
					map.put("ptz", ptz);// 是否支持云台，如果支持，传1，不支持，传0
					map.put("zoom", zoom);// 是否支持云台，如果支持，传1，不支持，传0
					map.put("talk", talk);// 是否支持云台，如果支持，传1，不支持，传0
					map.put("rtsp", rtsp);// 是否支持云台，如果支持，传1，不支持，传0
					map.put("devUserName", devUserName);
					map.put("devPassWord", devPassWord);
					map.put("type", type);
					listItem.add(map);
					isConnect = true;
				}
				//
				// // 调用sdk登陆方法登陆聊天服务器
				// EMChatManager.getInstance().login(nameString, passWordString,
				// new EMCallBack() {
				//
				// @Override
				// public void onSuccess() {
				//
				// // 登陆成功，保存用户名密码
				// VLCApplication.getInstance().setUserName(
				// nameString);
				// VLCApplication.getInstance().setPassword(
				// passWordString);
				// // runOnUiThread(new Runnable() {
				// // public void run() {
				// // pd.setMessage("正在获取好友和群聊列表...");
				// // }
				// // });
				// try {
				// // ** 第一次登录或者之前logout后，加载所有本地群和回话
				// // ** manually load all local groups and
				// // conversations in case we are auto login
				// EMGroupManager.getInstance()
				// .loadAllGroups();
				// EMChatManager.getInstance()
				// .loadAllConversations();
				//
				// // demo中简单的处理成每次登陆都去获取好友username，开发者自己根据情况而定
				// List<String> usernames = EMContactManager
				// .getInstance()
				// .getContactUserNames();
				// EMLog.d("roster", "contacts size: "
				// + usernames.size());
				// Map<String, User> userlist = new HashMap<String, User>();
				// for (String username : usernames) {
				// User user = new User();
				// user.setUsername(username);
				// setUserHearder(username, user);
				// userlist.put(username, user);
				// }
				// // 添加user"申请与通知"
				// User newFriends = new User();
				// newFriends
				// .setUsername(Constant.NEW_FRIENDS_USERNAME);
				// newFriends.setNick("申请与通知");
				// newFriends.setHeader("");
				// userlist.put(Constant.NEW_FRIENDS_USERNAME,
				// newFriends);
				// // 添加"群聊"
				// User groupUser = new User();
				// groupUser
				// .setUsername(Constant.GROUP_USERNAME);
				// groupUser.setNick("群聊");
				// groupUser.setHeader("");
				// userlist.put(Constant.GROUP_USERNAME,
				// groupUser);
				//
				// // 存入内存
				// VLCApplication.getInstance()
				// .setContactList(userlist);
				// // 存入db
				// UserDao dao = new UserDao(
				// LoginActivity.this);
				// List<User> users = new ArrayList<User>(
				// userlist.values());
				// dao.saveContactList(users);
				//
				// // 获取群聊列表(群聊里只有groupid和groupname的简单信息),sdk会把群组存入到内存和db中
				// EMGroupManager.getInstance()
				// .getGroupsFromServer();
				// } catch (Exception e) {
				// e.printStackTrace();
				// }
				// boolean updatenick = EMChatManager
				// .getInstance().updateCurrentUserNick(
				// VLCApplication.currentUserNick);
				// if (!updatenick) {
				// EMLog.e("LoginActivity",
				// "update current user nick fail");
				// }
				// }
				//
				// @Override
				// public void onProgress(int progress, String status) {
				//
				// }
				//
				// @Override
				// public void onError(int code, final String message) {
				//
				// // runOnUiThread(new Runnable() {
				// // public void run() {
				// // pd.dismiss();
				// // Toast.makeText(getApplicationContext(),
				// // "登录失败: " + message, 0).show();
				// //
				// // }
				// // });
				// }
				// });

				// Intent intent = new Intent(Landing.this,
				// ShowView.class);// 为Intent设置需要激活的组件

				Intent intent = new Intent(LoginActivity.this,
						MainActivity.class);// 为Intent设置需要激活的组件
				Bundle bundle = new Bundle();
				bundle.putParcelableArrayList("videoList", (ArrayList) listItem);
				bundle.putString("userId", userId);
				bundle.putString("comId", comId);
				bundle.putString("realName", realName);
				bundle.putString("empId", empId);
				bundle.putString("username", nameString);
				bundle.putString("password", passWordString);
				bundle.putString("socketIp", socketIp);
				bundle.putString("serverIp", serverIp);
				bundle.putString("ifBbyy", ifBbyy);
				bundle.putString("ifBbgj", ifBbgj);
				bundle.putString("ifKqgl", ifKqgl);
				bundle.putString("ifBbhy", ifBbhy);
				bundle.putString("ifVideo", ifVideo);
				bundle.putString("ifPtz", ifPtz);
				bundle.putString("ifRecord", ifRecord);
				bundle.putString("ifSnap", ifSnap);
				bundle.putString("ifMap", ifMap);
				bundle.putString("ifFavor", ifFavor);
				bundle.putString("ifDistance", ifDistance);
				bundle.putString("ifKinder", ifKinder);
				bundle.putString("ifUpload", ifUpload);
				bundle.putString("ifNews", ifNews);
				bundle.putString("ifPay", ifPay);
				bundle.putString("endDate", endDate);
				bundle.putString("payStatus", payStatus);
				bundle.putString("userType", userType);
				bundle.putString("kinderName", kinderName);
				// bundle.putString("name", nameString);
				// bundle.putString("passWord", passWordString);
				intent.putExtras(bundle);
				status = true;
				// handler.post(runnableHidProgress);

				startActivity(intent);
				// this.finish();
				if (isConnect) {
					SharedPreferences settings = getSharedPreferences(
							SETTING_INFOS, 0);
					SharedPreferences.Editor editor = settings.edit();

					editor.putString(AUTOLOG, "yes");
					editor.putString(SERIP, field_address.getText().toString()
							.trim());
					editor.putString(NAME, field_name.getText().toString()
							.trim());
					editor.putString(PASS, field_pass.getText().toString()
							.trim());
					editor.putString(REMEMBER, "yes");
					editor.commit();
					LoginActivity.this.finish();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	};

	/**
	 * 设置hearder属性，方便通讯中对联系人按header分类显示，以及通过右侧ABCD...字母栏快速定位联系人
	 * 
	 * @param username
	 * @param user
	 */
	protected void setUserHearder(String username, User user) {
		String headerName = null;
		if (!TextUtils.isEmpty(user.getNick())) {
			headerName = user.getNick();
		} else {
			headerName = user.getUsername();
		}
		if (username.equals(Constant.NEW_FRIENDS_USERNAME)) {
			user.setHeader("");
		} else if (Character.isDigit(headerName.charAt(0))) {
			user.setHeader("#");
		} else {
			user.setHeader(HanziToPinyin.getInstance()
					.get(headerName.substring(0, 1)).get(0).target.substring(0,
					1).toUpperCase());
			char header = user.getHeader().toLowerCase().charAt(0);
			if (header < 'a' || header > 'z') {
				user.setHeader("#");
			}
		}
	}

	// 等待提示框操作
	private void TimerCount(Timer t) {
		if (status) {
			isWaiting = false;
			t.cancel();
			// handler.post(runnableHidProgress);
		} else {
			if (loadingFlag) {
				timerCount++;
			} else {
				t.cancel();
			}
			if (timerCount > 40) {
				t.cancel();
				if (isWaiting) {
					// showWait("网络不稳定，请稍候重试");
					handler.post(runnableHidProgress);
					isWaiting = false;
					new Thread() {
						public void run() {
							handler.post(runnableRunOver);
						}
					}.start();
				}
			}
		}

	}

	private void TimerOut() {
		TimerTask timerTask = new TimerTask() {
			@Override
			public void run() {
				TimerCount(timer);
			}
		};
		timer.schedule(timerTask, 0, 1000);
	}

	Runnable waitThread = new Runnable() {
		public void run() {
			if (isWaiting) {
				TimerOut();
			}
		}
	};

	boolean isExit = false;
	boolean hasExitTask = false;
	Timer exitTimer = new Timer();
	TimerTask exitTask = new TimerTask() {
		public void run() {
			isExit = false;
			hasExitTask = true;
		}
	};

	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		tdd.close();
	}

	/** 返回键拦截监听 */
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK
				&& event.getAction() == KeyEvent.ACTION_DOWN) {
			if (isExit) {
				exit();
			} else {
				isExit = true;
				Toast.makeText(this,
						getResources().getString(R.string.oneclickback),
						Toast.LENGTH_SHORT).show();
				if (!hasExitTask) {
					exitTimer.schedule(exitTask, 3000);
				}
			}
		}
		return false;
	}

	/** 退出 */
	void exit() {
		finish();
		java.lang.System.exit(0);
	}

	// 检查更新

	private final String TAG = this.getClass().getName();

	private final int UPDATA_NONEED = 0;
	private final int UPDATA_CLIENT = 1;
	private final int GET_UNDATAINFO_ERROR = 2;
	private final int SDCARD_NOMOUNTED = 3;
	private final int DOWN_ERROR = 4;
	private TextView textView;
	private Button getVersion;

	private UpdataInfo info;
	private String localVersion;

	/*
	 * 获取当前程序的版本号
	 */
	private String getVersionName() throws Exception {
		// 获取packagemanager的实例
		PackageManager packageManager = getPackageManager();
		// getPackageName()是你当前类的包名，0代表是获取版本信息
		PackageInfo packInfo = packageManager.getPackageInfo(getPackageName(),
				0);
		return packInfo.versionName;
	}

	/*
	 * 从服务器获取xml解析并进行比对版本号
	 */
	public class CheckVersionTask implements Runnable {

		public void run() {
			try {

				if (socketIp.equals("123.244.30.58")) {
					serverIp = "218.60.13.16";
				} else if (socketIp.equals("200.20.36.150")) {
					serverIp = "200.20.32.120";
				} else if (socketIp.equals("200.20.22.217")) {
					serverIp = "200.20.32.120";
				} else if (socketIp.equals("200.20.32.217")) {
					serverIp = "200.20.32.120";
				} else if (socketIp.equals("tianbianxue.oicp.net")) {
					serverIp = "59.46.115.85";
				} else if (socketIp.equals("192.168.1.15")) {
					serverIp = "200.20.32.200";
				} else {
					serverIp = socketIp;
				}
				// 从资源文件获取服务器 地址
				String path = "";
				if (serverIp.equals("218.60.13.9:7799")) {
					path = "http://" + serverIp
							+ getResources().getString(R.string.url_update);
				} else {
					path = "http://" + serverIp + ":7799"
							+ getResources().getString(R.string.url_update);
				}
				// 包装成url的对象
				URL url = new URL(path);
				HttpURLConnection conn = (HttpURLConnection) url
						.openConnection();
				conn.setConnectTimeout(5000);
				InputStream is = conn.getInputStream();
				info = UpdataInfoParser.getUpdataInfo(is);
				System.out.println("VersionActivity----------->info = " + info);
				Double webVersion = Double.parseDouble(info.getVersion());
				Double phoneVersion = Double.parseDouble(localVersion);
				if (webVersion <= phoneVersion) {
					Log.i(TAG, "版本号相同无需升级");
					Message msg = new Message();
					msg.what = UPDATA_NONEED;
					handler.sendMessage(msg);
					// LoginMain();
				} else {
					Log.i(TAG, "版本号不同 ,提示用户升级 ");
					Message msg = new Message();
					msg.what = UPDATA_CLIENT;
					handler.sendMessage(msg);
				}
			} catch (Exception e) {
				// 待处理
				Message msg = new Message();
				msg.what = GET_UNDATAINFO_ERROR;
				handler.sendMessage(msg);
				e.printStackTrace();
			}
		}
	}

	Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			switch (msg.what) {
			case UPDATA_NONEED:
				// Toast.makeText(getApplicationContext(), "您的软件是最新版本，无需升级",
				// Toast.LENGTH_SHORT).show();
				// waitClose();
				break;
			case UPDATA_CLIENT:
				// 对话框通知用户升级程序
				// Toast.makeText(getApplicationContext(), "您的软件有新版本，请升级！",
				// 1).show();
				showUpdataDialog();
				waitClose();
				break;
			case GET_UNDATAINFO_ERROR:
				// 服务器超时
				// Toast.makeText(getApplicationContext(), "获取服务器更新信息失败", 1)
				// .show();
				// waitClose();
				// LoginMain();
				break;
			case SDCARD_NOMOUNTED:
				// sdcard不可用
				Toast.makeText(getApplicationContext(), "SD卡不可用", 1).show();
				break;
			case DOWN_ERROR:
				// 下载apk失败
				Toast.makeText(getApplicationContext(), "下载新版本失败", 1).show();
				// LoginMain();
				break;
			}
		}
	};

	/*
	 * 
	 * 弹出对话框通知用户更新程序
	 * 
	 * 弹出对话框的步骤： 1.创建alertDialog的builder. 2.要给builder设置属性, 对话框的内容,样式,按钮
	 * 3.通过builder 创建一个对话框 4.对话框show()出来
	 */
	protected void showUpdataDialog() {
		AlertDialog.Builder builer = new Builder(this);
		builer.setTitle("版本升级");
		builer.setMessage(info.getDescription());
		// 当点确定按钮时从服务器上下载 新的apk 然后安装
		builer.setPositiveButton("确定", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				Log.i(TAG, "下载apk,更新");
				downLoadApk();
			}
		});
		// 当点取消按钮时进行登录
		builer.setNegativeButton("取消", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				// LoginMain();
			}
		});
		AlertDialog dialog = builer.create();
		dialog.show();
	}

	/*
	 * 从服务器中下载APK
	 */
	protected void downLoadApk() {
		final ProgressDialog pd; // 进度条对话框
		pd = new ProgressDialog(LoginActivity.this);
		pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		pd.setMessage("正在下载更新");
		if (!Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {
			Message msg = new Message();
			msg.what = SDCARD_NOMOUNTED;
			handler.sendMessage(msg);
		} else {
			pd.show();
			new Thread() {
				@Override
				public void run() {
					try {
						File file = DownLoadManager.getFileFromServer(
								info.getUrl(), pd);
						sleep(1000);
						installApk(file);
						pd.dismiss(); // 结束掉进度条对话框

					} catch (Exception e) {
						Message msg = new Message();
						msg.what = DOWN_ERROR;
						handler.sendMessage(msg);
						e.printStackTrace();
					}
				}
			}.start();
		}
	}

	// 安装apk
	protected void installApk(File file) {
		Intent intent = new Intent();
		// 执行动作
		intent.setAction(Intent.ACTION_VIEW);
		// 执行的数据类型
		intent.setDataAndType(Uri.fromFile(file),
				"application/vnd.android.package-archive");
		startActivity(intent);
	}

	// 获取手机IP
	public String getLocalIpAddress() {
		try {
			for (Enumeration<NetworkInterface> en = NetworkInterface
					.getNetworkInterfaces(); en.hasMoreElements();) {
				NetworkInterface intf = en.nextElement();
				for (Enumeration<InetAddress> enumIpAddr = intf
						.getInetAddresses(); enumIpAddr.hasMoreElements();) {
					InetAddress inetAddress = enumIpAddr.nextElement();
					if (!inetAddress.isLoopbackAddress()) {
						return inetAddress.getHostAddress().toString();
					}
				}
			}
		} catch (SocketException ex) {
			Log.e("log", ex.toString());
		}
		return null;
	}
}