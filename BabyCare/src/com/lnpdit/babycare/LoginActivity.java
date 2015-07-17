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

	// ��½���ؼ�ʱ��
	private int timerCount = 0;
	private Timer timer;
	private boolean isWaiting = true;
	private Thread threadLoading;
	private Thread webLoading;

	// �������״̬
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

		// ��ֹ����
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
			// imsi �����ƶ��û�ʶ����
			String imsi = tm.getSubscriberId();

			if (imsi != null) {
				if (imsi.startsWith("46000") || imsi.startsWith("46002")
						|| imsi.startsWith("46007")) {
					// �й��ƶ�
					Log.v("��Ӫ��:", "�й��ƶ�" + " " + imsi);
					return "1";
				} else if (imsi.startsWith("46001") || imsi.startsWith("46006")) {
					// �й���ͨ
					Log.v("��Ӫ��:", "�й���ͨ" + " " + imsi);
					return "2";
				} else if (imsi.startsWith("46003") || imsi.startsWith("46005")) {
					// �й�����
					Log.v("��Ӫ��:", "�й�����" + " " + imsi);
					return "3";
				} else {
					// �޷��ж�
					Log.v("��Ӫ��", "�޷��ж���Ӫ����Ϣ" + " " + imsi);
					return "0";
				}

			} else {
				// imsiΪ��
				return "0";
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			return "0";
		}
	}

	// ��ʾprogressBar
	Runnable runnableShowProgress = new Runnable() {
		@Override
		public void run() {
			loginProgressBar.setVisibility(ProgressBar.VISIBLE);
			btnLogin.setVisibility(Button.GONE);
		}

	};
	// ����progressBar
	Runnable runnableHidProgress = new Runnable() {
		@Override
		public void run() {
			loginProgressBar.setVisibility(ProgressBar.GONE);
			btnLogin.setVisibility(Button.VISIBLE);
		}

	};

	// ��½״̬
	Boolean status = false;

	public static final int UPDATE_ID = Menu.FIRST;
	public static final int ABOUT_ID = Menu.FIRST + 1;

	public void showToast(String sc) {
		Toast toast = Toast
				.makeText(LoginActivity.this, sc, Toast.LENGTH_SHORT);
		toast.show();
	}

	private String serverIp = "";// ��ȡ�������Ĺ���ƽ̨IP
	private String socketIp = "";// ��ȡ��������ת��IP
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
					// ��½�ȴ���ʾ��
					// ���ݳ�ʼ��
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

					// �ȴ������
					handler.post(runnableShowProgress);
					threadLoading = new Thread(waitThread);
					threadLoading.start();

					// WebService����
					webLoading = new Thread(newWebThread);
					webLoading.start();
					// this.logToServer();// �������ʽ�����������ת
					break;
				} catch (Exception e) {
					e.printStackTrace();
					handler.post(runnableHidProgress);
					showToast("��������ʧ��");
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

	// ������ʾ
	Runnable runnableWaitting = new Runnable() {
		@Override
		public void run() {
			isWaiting = false;

			handler.post(runnableHidProgress);
			Toast.makeText(LoginActivity.this, "�û�����������������֤�����룡",
					Toast.LENGTH_LONG).show();
		}

	};

	// ������ʾ
	Runnable lockWaitting = new Runnable() {
		@Override
		public void run() {
			isWaiting = false;

			handler.post(runnableHidProgress);
			Toast.makeText(LoginActivity.this, "���û��ѱ����������ܵ�¼��",
					Toast.LENGTH_LONG).show();
		}

	};

	// �޷���ȡ��Ӫ����ʾ
	Runnable noYys = new Runnable() {
		@Override
		public void run() {
			isWaiting = false;

			handler.post(runnableHidProgress);
			Toast.makeText(LoginActivity.this, "�޷���ȡ��Ӫ����Ϣ�����ܵ�¼��",
					Toast.LENGTH_LONG).show();
		}

	};

	// ��Ӫ�̲�������ʾ
	Runnable wrongYys = new Runnable() {
		@Override
		public void run() {
			isWaiting = false;

			handler.post(runnableHidProgress);
			Toast.makeText(LoginActivity.this, "������Ӫ����Ϣ�����ϣ����ܵ�¼��",
					Toast.LENGTH_LONG).show();
		}

	};

	// ��ʱ��ʾ
	Runnable runnableRunOver = new Runnable() {
		@Override
		public void run() {
			Toast.makeText(LoginActivity.this, "���粻�ȶ������Ժ����ԣ�",
					Toast.LENGTH_SHORT).show();
		}

	};

	// �û��Ѿ���¼��ʾ
	Runnable onlyWaitting = new Runnable() {
		@Override
		public void run() {
			isWaiting = false;

			handler.post(runnableHidProgress);
			Toast.makeText(LoginActivity.this, "���û�����ʹ���У����ܵ�¼��",
					Toast.LENGTH_LONG).show();
		}

	};
	// webService���̽��е�½

	ToDoDB tdd;

	// �µĵ�¼����

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
				String userType = "�ҳ�";
				String kinderName = "С��ͯ�׶�԰";

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

					if (!yys.equals("0")) { // ���������Ӫ�̣����ж�
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
					map.put("devName", devName);// �б���ʾ���豸���ƣ����硰չ�����豸��
					map.put("chName", chName); // ͨ�����ƣ�д������ͷ1������
					map.put("devId", devId); // �豸���кţ��������Ҫ��ÿ���豸��ͬ��������д�롰DHPZB2LN26800002������
					map.put("ip", ip); // �����������IP����59.46.115.85,������д�롰200.20.32.200������
					map.put("port", port); //
					map.put("chNo", chNo);// ��1����
					map.put("listCount", listCount);// �豸����
					map.put("listNo", listNo);// ���豸�е�˳��
					map.put("width", width); // д352
					map.put("height", height); // д288
					map.put("longitude", longitude);// ���ȣ������Ǹ�չ���ľ���
					map.put("latitude", latitude);// γ�ȣ������Ǹ�չ����γ��
					map.put("adapterId", adapterId);// ����ID
					map.put("stayLine", stayLine);// ��1
					map.put("ptz", ptz);// �Ƿ�֧����̨�����֧�֣���1����֧�֣���0
					map.put("zoom", zoom);// �Ƿ�֧����̨�����֧�֣���1����֧�֣���0
					map.put("talk", talk);// �Ƿ�֧����̨�����֧�֣���1����֧�֣���0
					map.put("rtsp", rtsp);// �Ƿ�֧����̨�����֧�֣���1����֧�֣���0
					map.put("devUserName", devUserName);
					map.put("devPassWord", devPassWord);
					map.put("type", type);
					listItem.add(map);
					isConnect = true;
				}
				//
				// // ����sdk��½������½���������
				// EMChatManager.getInstance().login(nameString, passWordString,
				// new EMCallBack() {
				//
				// @Override
				// public void onSuccess() {
				//
				// // ��½�ɹ��������û�������
				// VLCApplication.getInstance().setUserName(
				// nameString);
				// VLCApplication.getInstance().setPassword(
				// passWordString);
				// // runOnUiThread(new Runnable() {
				// // public void run() {
				// // pd.setMessage("���ڻ�ȡ���Ѻ�Ⱥ���б�...");
				// // }
				// // });
				// try {
				// // ** ��һ�ε�¼����֮ǰlogout�󣬼������б���Ⱥ�ͻػ�
				// // ** manually load all local groups and
				// // conversations in case we are auto login
				// EMGroupManager.getInstance()
				// .loadAllGroups();
				// EMChatManager.getInstance()
				// .loadAllConversations();
				//
				// // demo�м򵥵Ĵ����ÿ�ε�½��ȥ��ȡ����username���������Լ������������
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
				// // ���user"������֪ͨ"
				// User newFriends = new User();
				// newFriends
				// .setUsername(Constant.NEW_FRIENDS_USERNAME);
				// newFriends.setNick("������֪ͨ");
				// newFriends.setHeader("");
				// userlist.put(Constant.NEW_FRIENDS_USERNAME,
				// newFriends);
				// // ���"Ⱥ��"
				// User groupUser = new User();
				// groupUser
				// .setUsername(Constant.GROUP_USERNAME);
				// groupUser.setNick("Ⱥ��");
				// groupUser.setHeader("");
				// userlist.put(Constant.GROUP_USERNAME,
				// groupUser);
				//
				// // �����ڴ�
				// VLCApplication.getInstance()
				// .setContactList(userlist);
				// // ����db
				// UserDao dao = new UserDao(
				// LoginActivity.this);
				// List<User> users = new ArrayList<User>(
				// userlist.values());
				// dao.saveContactList(users);
				//
				// // ��ȡȺ���б�(Ⱥ����ֻ��groupid��groupname�ļ���Ϣ),sdk���Ⱥ����뵽�ڴ��db��
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
				// // "��¼ʧ��: " + message, 0).show();
				// //
				// // }
				// // });
				// }
				// });

				// Intent intent = new Intent(Landing.this,
				// ShowView.class);// ΪIntent������Ҫ��������

				Intent intent = new Intent(LoginActivity.this,
						MainActivity.class);// ΪIntent������Ҫ��������
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
	 * ����hearder���ԣ�����ͨѶ�ж���ϵ�˰�header������ʾ���Լ�ͨ���Ҳ�ABCD...��ĸ�����ٶ�λ��ϵ��
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

	// �ȴ���ʾ�����
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
					// showWait("���粻�ȶ������Ժ�����");
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

	/** ���ؼ����ؼ��� */
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

	/** �˳� */
	void exit() {
		finish();
		java.lang.System.exit(0);
	}

	// ������

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
	 * ��ȡ��ǰ����İ汾��
	 */
	private String getVersionName() throws Exception {
		// ��ȡpackagemanager��ʵ��
		PackageManager packageManager = getPackageManager();
		// getPackageName()���㵱ǰ��İ�����0�����ǻ�ȡ�汾��Ϣ
		PackageInfo packInfo = packageManager.getPackageInfo(getPackageName(),
				0);
		return packInfo.versionName;
	}

	/*
	 * �ӷ�������ȡxml���������бȶ԰汾��
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
				// ����Դ�ļ���ȡ������ ��ַ
				String path = "";
				if (serverIp.equals("218.60.13.9:7799")) {
					path = "http://" + serverIp
							+ getResources().getString(R.string.url_update);
				} else {
					path = "http://" + serverIp + ":7799"
							+ getResources().getString(R.string.url_update);
				}
				// ��װ��url�Ķ���
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
					Log.i(TAG, "�汾����ͬ��������");
					Message msg = new Message();
					msg.what = UPDATA_NONEED;
					handler.sendMessage(msg);
					// LoginMain();
				} else {
					Log.i(TAG, "�汾�Ų�ͬ ,��ʾ�û����� ");
					Message msg = new Message();
					msg.what = UPDATA_CLIENT;
					handler.sendMessage(msg);
				}
			} catch (Exception e) {
				// ������
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
				// Toast.makeText(getApplicationContext(), "������������°汾����������",
				// Toast.LENGTH_SHORT).show();
				// waitClose();
				break;
			case UPDATA_CLIENT:
				// �Ի���֪ͨ�û���������
				// Toast.makeText(getApplicationContext(), "����������°汾����������",
				// 1).show();
				showUpdataDialog();
				waitClose();
				break;
			case GET_UNDATAINFO_ERROR:
				// ��������ʱ
				// Toast.makeText(getApplicationContext(), "��ȡ������������Ϣʧ��", 1)
				// .show();
				// waitClose();
				// LoginMain();
				break;
			case SDCARD_NOMOUNTED:
				// sdcard������
				Toast.makeText(getApplicationContext(), "SD��������", 1).show();
				break;
			case DOWN_ERROR:
				// ����apkʧ��
				Toast.makeText(getApplicationContext(), "�����°汾ʧ��", 1).show();
				// LoginMain();
				break;
			}
		}
	};

	/*
	 * 
	 * �����Ի���֪ͨ�û����³���
	 * 
	 * �����Ի���Ĳ��裺 1.����alertDialog��builder. 2.Ҫ��builder��������, �Ի��������,��ʽ,��ť
	 * 3.ͨ��builder ����һ���Ի��� 4.�Ի���show()����
	 */
	protected void showUpdataDialog() {
		AlertDialog.Builder builer = new Builder(this);
		builer.setTitle("�汾����");
		builer.setMessage(info.getDescription());
		// ����ȷ����ťʱ�ӷ����������� �µ�apk Ȼ��װ
		builer.setPositiveButton("ȷ��", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				Log.i(TAG, "����apk,����");
				downLoadApk();
			}
		});
		// ����ȡ����ťʱ���е�¼
		builer.setNegativeButton("ȡ��", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				// LoginMain();
			}
		});
		AlertDialog dialog = builer.create();
		dialog.show();
	}

	/*
	 * �ӷ�����������APK
	 */
	protected void downLoadApk() {
		final ProgressDialog pd; // �������Ի���
		pd = new ProgressDialog(LoginActivity.this);
		pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		pd.setMessage("�������ظ���");
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
						pd.dismiss(); // �������������Ի���

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

	// ��װapk
	protected void installApk(File file) {
		Intent intent = new Intent();
		// ִ�ж���
		intent.setAction(Intent.ACTION_VIEW);
		// ִ�е���������
		intent.setDataAndType(Uri.fromFile(file),
				"application/vnd.android.package-archive");
		startActivity(intent);
	}

	// ��ȡ�ֻ�IP
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