package com.lnpdit.babycare;

import java.util.ArrayList;
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
import com.lnpdit.sqllite.User;
import com.lnpdit.sqllite.UserDao;
import com.lnpdit.util.ConnectionDetector;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;

public class LoadingActivity extends Activity {
	private SharedPreferences sp;

	public static final String SETTING_INFOS = "SETTING_Infos";
	public static final String SERIP = "serverIp";
	public static final String NAME = "name";
	public static final String PASS = "pass";
	public static final String REMEMBER = "remember";
	public static final String AUTOLOG = "autolog";

	private String serverIp = "";
	private String socketIp = "";
	private String name = "";
	private String pass = "";
	private String remember = "";

	Handler handler;

	// 登陆加载计时器
	private int timerCount = 0;
	private Timer timer;
	private boolean isWaiting = true;
	private Thread threadLoading;
	private Thread webLoading;

	// 登陆状态
	Boolean status = false;
	// 检测网络状态
	Boolean isInternetPresent = false;
	ConnectionDetector cd;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.home_page);

		sp = getSharedPreferences(SETTING_INFOS, 0);

		handler = new Handler();

		cd = new ConnectionDetector(getApplicationContext());
		isInternetPresent = cd.isConnectingToInternet();
		if (!isInternetPresent) {
			Toast.makeText(this, getResources().getString(R.string.loginerror),
					Toast.LENGTH_SHORT).show();
			return;
		} else {

			if (sp != null) {

				socketIp = sp.getString(SERIP, "");
				name = sp.getString(NAME, "");
				pass = sp.getString(PASS, "");
				remember = sp.getString(REMEMBER, "");

				if (remember.equals("yes")) {
					timer = new Timer();
					threadLoading = new Thread(waitThread);
					threadLoading.start();
					webLoading = new Thread(newWebThread);
					webLoading.start();
				} else {
					Intent intent = new Intent(LoadingActivity.this,
							LoginActivity.class);
					startActivity(intent);
					finish();
				}

			} else {
				new Handler().postDelayed(new Runnable() {

					@Override
					public void run() {
						Intent intent = new Intent(LoadingActivity.this,
								LoginActivity.class);
						startActivity(intent);
						finish();
					}
				}, 2000);
			}
		}
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

	// 等待提示框操作
	private void TimerCount(Timer t) {
		if (status) {
			t.cancel();
			// handler.post(runnableHidProgress);
		} else {
			timerCount++;
			if (timerCount > 40) {
				t.cancel();
				runOnUiThread(runnableRunOver);
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

	// 新的登录过程

	Runnable newWebThread = new Runnable() {
		public void run() {
			try {
				String NAMESPACE = "MobileNewspaper";
				String METHOD_NAME = "NewLogin";
				String serverIp = "";

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
				rpc.addProperty("userName", name);
				rpc.addProperty("passWord", pass);

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
				}

				// // 调用sdk登陆方法登陆聊天服务器
				// EMChatManager.getInstance().login(name, pass,
				// new EMCallBack() {
				//
				// @Override
				// public void onSuccess() {
				//
				// // 登陆成功，保存用户名密码
				// VLCApplication.getInstance().setUserName(
				// name);
				// VLCApplication.getInstance().setPassword(
				// pass);
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
				// LoadingActivity.this);
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

				Intent intent = new Intent(LoadingActivity.this,
						MainActivity.class);// 为Intent设置需要激活的组件
				Bundle bundle = new Bundle();
				bundle.putParcelableArrayList("videoList", (ArrayList) listItem);
				bundle.putString("userId", userId);
				bundle.putString("comId", comId);
				bundle.putString("realName", realName);
				bundle.putString("empId", empId);
				bundle.putString("username", name);
				bundle.putString("password", pass);
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
				// handler.post(runnableHidProgress);

				status = true;
				startActivity(intent);
				LoadingActivity.this.finish();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	};

	// 密码提示
	Runnable runnableWaitting = new Runnable() {
		@Override
		public void run() {
			Toast.makeText(LoadingActivity.this, "用户名或者密码错误，请查证后输入！",
					Toast.LENGTH_LONG).show();
		}

	};

	// 锁定提示
	Runnable lockWaitting = new Runnable() {
		@Override
		public void run() {
			Toast.makeText(LoadingActivity.this, "该用户已被锁定，不能登录！",
					Toast.LENGTH_LONG).show();
		}

	};

	// 无法获取运营商提示
	Runnable noYys = new Runnable() {
		@Override
		public void run() {
			Toast.makeText(LoadingActivity.this, "无法获取运营商信息，不能登录！",
					Toast.LENGTH_LONG).show();
		}

	};

	// 运营商不符合提示
	Runnable wrongYys = new Runnable() {
		@Override
		public void run() {
			Toast.makeText(LoadingActivity.this, "您的运营商信息不符合，不能登录！",
					Toast.LENGTH_LONG).show();
		}

	};

	// 超时提示
	Runnable runnableRunOver = new Runnable() {
		@Override
		public void run() {
			Toast.makeText(LoadingActivity.this, "网络不稳定，请稍候重试！",
					Toast.LENGTH_SHORT).show();
		}

	};

	// 环信登录

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

	boolean isExit = false;
	boolean hasExitTask = false;
	Timer exitTimer = new Timer();
	TimerTask exitTask = new TimerTask() {
		public void run() {
			isExit = false;
			hasExitTask = true;
		}
	};

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

	@Override
	protected void onDestroy() {
		super.onDestroy();
		sp = null;
	}

}
