package com.lnpdit.babycare;

import java.io.File;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import org.videolan.libvlc.VLCApplication;

import com.easemob.EMConnectionListener;
import com.easemob.EMError;
import com.easemob.chat.EMChat;
import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMContactListener;
import com.easemob.chat.EMContactManager;
import com.easemob.chat.EMConversation;
import com.easemob.chat.EMGroup;
import com.easemob.chat.EMGroupManager;
import com.easemob.chat.EMMessage;
import com.easemob.chat.EMNotifier;
import com.easemob.chat.GroupChangeListener;
import com.easemob.chat.TextMessageBody;
import com.easemob.chat.EMMessage.ChatType;
import com.easemob.chat.EMMessage.Type;
import com.easemob.util.HanziToPinyin;
import com.easemob.util.NetUtils;
import com.lnpdit.babycare.R;
import com.lnpdit.chat.ChatActivity;
import com.lnpdit.chat.ChatAllHistoryActivity;
import com.lnpdit.garden.GardenComActivity;
import com.lnpdit.photo.Constant;
import com.lnpdit.service.NewsPushService;
import com.lnpdit.sqllite.BBGJDB;
import com.lnpdit.sqllite.User;
import com.lnpdit.sqllite.UserDao;
import com.lnpdit.util.DownLoadManager;
import com.lnpdit.util.UpdataInfo;
import com.lnpdit.util.UpdataInfoParser;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.TabActivity;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.AlertDialog.Builder;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

@SuppressWarnings("deprecation")
public class MainActivity extends TabActivity implements OnClickListener {
	public static String TAB_TAG_GARDEN = "garden";
	public static String TAB_TAG_MONITOR = "monitor";
	public static String TAB_TAG_MSG = "message";
	public static String TAB_TAB_MORE = "more";
	public static TabHost mTabHost;
	static final int COLOR1 = Color.parseColor("#838992");
	static final int COLOR2 = Color.parseColor("#b87721");
	ImageView mBut1, mBut2, mBut3, mBut4;
	TextView mCateText1, mCateText2, mCateText3, mCateText4;

	Intent mGardenIntent, mMonitorItent, mMsgIntent, mMoreIntent;
	private ChatAllHistoryActivity chatHistory;

	int mCurTabId = R.id.channel1;

	// Animation
	private Animation left_in, left_out;
	private Animation right_in, right_out;

	Bundle loginBundle;
	public static String ipString = "";
	public static String IP = "";
	private String userId;
	private String comId;
	private String realName;

	private LinearLayout bottomLayout;

	/** Called when the activity is first created. */
	// 环信

	private NewMessageBroadcastReceiver msgReceiver;
	// 未读消息textview
	private TextView unreadLabel;
	// 账号在别处登录
	private boolean isConflict = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		loginBundle = this.getIntent().getExtras();
		ipString = loginBundle.getString("serverIp");
		IP = "http://" + ipString + ":7799";

		chatHistory = new ChatAllHistoryActivity();

		prepareAnim();
		prepareIntent();
		setupIntent();
		prepareView();
		prepareGarden();

		try {
			localVersion = getVersionName();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		CheckVersionTask cv = new CheckVersionTask();
		new Thread(cv).start();

		// 环信

		// 注册一个接收消息的BroadcastReceiver
		msgReceiver = new NewMessageBroadcastReceiver();
		IntentFilter intentFilter = new IntentFilter(EMChatManager
				.getInstance().getNewMessageBroadcastAction());
		intentFilter.setPriority(3);
		registerReceiver(msgReceiver, intentFilter);

		// 注册一个ack回执消息的BroadcastReceiver
		IntentFilter ackMessageIntentFilter = new IntentFilter(EMChatManager
				.getInstance().getAckMessageBroadcastAction());
		ackMessageIntentFilter.setPriority(3);
		registerReceiver(ackMessageReceiver, ackMessageIntentFilter);

		// 注册一个离线消息的BroadcastReceiver
		// IntentFilter offlineMessageIntentFilter = new
		// IntentFilter(EMChatManager.getInstance()
		// .getOfflineMessageBroadcastAction());
		// registerReceiver(offlineMessageReceiver, offlineMessageIntentFilter);

		// 注册一个监听连接状态的listener
		EMChatManager.getInstance().addConnectionListener(
				new MyConnectionListener());
		// 通知sdk，UI 已经初始化完毕，注册了相应的receiver和listener, 可以接受broadcast了
		EMChat.getInstance().setAppInited();

	}

	private void prepareGarden() {

		userId = loginBundle.getString("userId");// 用户ID
		comId = loginBundle.getString("comId");// 幼儿园ID
		realName = loginBundle.getString("realName");// 用户姓名

		// 手机报
		SharedPreferences sjbSsharedPreferences = getSharedPreferences(
				"BBGJ_UserInfo", Context.MODE_PRIVATE); // 私有数据
		Editor sjbEditor = sjbSsharedPreferences.edit();// 获取编辑器

		sjbEditor.putInt("userId", Integer.parseInt(userId));
		sjbEditor.putInt("comId", Integer.parseInt(comId));
		sjbEditor.putString("realName", realName);
		sjbEditor.commit();// 提交修改

		try {
			BBGJDB tdd = new BBGJDB(this);
			tdd.cleardbusr();
			tdd.close();
			userLogin();
		} catch (Exception e) {
			// TODO: handle exception
		}

		boolean pushservicestate = isPushServiceWork();
		if (pushservicestate == false) {
			Intent i = new Intent(this, NewsPushService.class);
			this.startService(i);
		}

	}

	public boolean isPushServiceWork() {
		ActivityManager myManager = (ActivityManager) this
				.getSystemService(Context.ACTIVITY_SERVICE);
		ArrayList<RunningServiceInfo> runningService = (ArrayList<RunningServiceInfo>) myManager
				.getRunningServices(30);
		for (int i = 0; i < runningService.size(); i++) {
			if (runningService.get(i).service.getClassName().toString()
					.equals("lnpdit.babycare.pushservice")) {
				return true;
			}
		}
		return false;
	}

	private void userLogin() {
		SharedPreferences share = getSharedPreferences("BBGJ_UserInfo",
				Activity.MODE_WORLD_READABLE);

		BBGJDB tdd = new BBGJDB(this);
		int userid = share.getInt("userId", 0);
		String username = share.getString("realName", "");
		String phonenum = "";
		ContentValues values = new ContentValues();
		values.put(tdd.USER_WEBID, userid);
		values.put(tdd.USER_IMSI, phonenum);
		values.put(tdd.USER_NAME, username);
		values.put(tdd.USER_PUSHID, "0");
		values.put(tdd.USER_VERSION, "0");
		values.put(tdd.USER_REFRESH_RATE, "10000");
		tdd.insertuser(values);
	}

	private void prepareAnim() {
		left_in = AnimationUtils.loadAnimation(this, R.anim.left_in);
		left_out = AnimationUtils.loadAnimation(this, R.anim.left_out);
		right_in = AnimationUtils.loadAnimation(this, R.anim.right_in);
		right_out = AnimationUtils.loadAnimation(this, R.anim.right_out);
	}

	private void prepareView() {
		bottomLayout = (LinearLayout) findViewById(R.id.mainbottom);
		mBut1 = (ImageView) findViewById(R.id.imageView1);
		mBut2 = (ImageView) findViewById(R.id.imageView2);
		mBut3 = (ImageView) findViewById(R.id.imageView3);
		mBut4 = (ImageView) findViewById(R.id.imageView4);
		findViewById(R.id.channel1).setOnClickListener(this);
		findViewById(R.id.channel2).setOnClickListener(this);
		findViewById(R.id.channel3).setOnClickListener(this);
		findViewById(R.id.channel4).setOnClickListener(this);
		mCateText1 = (TextView) findViewById(R.id.textView1);
		mCateText2 = (TextView) findViewById(R.id.textView2);
		mCateText3 = (TextView) findViewById(R.id.textView3);
		mCateText4 = (TextView) findViewById(R.id.textView4);

		unreadLabel = (TextView) findViewById(R.id.unread_msg_number);

	}

	private void prepareIntent() {

		mGardenIntent = new Intent(this, GardenPageActivity.class);
		mGardenIntent.putExtras(loginBundle);

		mMonitorItent = new Intent(this, FrontPageActivity.class);
		mMonitorItent.putExtras(loginBundle);

		mMsgIntent = new Intent(this, ChatAllHistoryActivity.class);
		mMsgIntent.putExtras(loginBundle);
		// mMsgIntent = new Intent(this, GardenComActivity.class);
		// mMsgIntent.putExtras(loginBundle);

		mMoreIntent = new Intent(this, MimePageActivity.class);
		mMoreIntent.putExtras(loginBundle);

	}

	private void setupIntent() {

		mTabHost = getTabHost();

		mTabHost.addTab(buildTabSpec(TAB_TAG_GARDEN, R.string.bottomtitle01,
				R.drawable.garden, mGardenIntent));

		mTabHost.addTab(buildTabSpec(TAB_TAG_MONITOR, R.string.bottomtitle02,
				R.drawable.monitor1, mMonitorItent));

		mTabHost.addTab(buildTabSpec(TAB_TAG_MSG, R.string.bottomtitle03,
				R.drawable.msg1, mMsgIntent));

		mTabHost.addTab(buildTabSpec(TAB_TAB_MORE, R.string.bottomtitle04,
				R.drawable.mime1, mMoreIntent));

	}

	public void showBottom() {
		bottomLayout.setVisibility(TabHost.VISIBLE);
	}

	public void hidBottom() {
		bottomLayout.setVisibility(TabHost.GONE);
	}

	private TabHost.TabSpec buildTabSpec(String tag, int resLabel, int resIcon,
			final Intent content) {
		return mTabHost
				.newTabSpec(tag)
				.setIndicator(getString(resLabel),
						getResources().getDrawable(resIcon))
				.setContent(content);
	}

	public static void setCurrentTabByTag(String tab) {
		mTabHost.setCurrentTabByTag(tab);
	}

	@Override
	public void onClick(View v) {

		// TODO Auto-generated method stub
		if (mCurTabId == v.getId()) {

			return;
		}

		mBut1.setImageResource(R.drawable.tab1_off);
		mBut2.setImageResource(R.drawable.tab2_off);
		mBut3.setImageResource(R.drawable.tab3_off);
		mBut4.setImageResource(R.drawable.tab4_off);
		mCateText1.setTextColor(COLOR1);
		mCateText2.setTextColor(COLOR1);
		mCateText3.setTextColor(COLOR1);
		mCateText4.setTextColor(COLOR1);
		int checkedId = v.getId();
		final boolean o;
		if (mCurTabId < checkedId)
			o = true;
		else
			o = false;
		if (o)
			mTabHost.getCurrentView().startAnimation(left_out);
		else
			mTabHost.getCurrentView().startAnimation(right_out);
		switch (checkedId) {

		case R.id.channel1:
			mTabHost.setCurrentTabByTag(TAB_TAG_GARDEN);
			mBut1.setImageResource(R.drawable.tab1_on);
			mCateText1.setTextColor(COLOR2);

			break;

		case R.id.channel2:
			mTabHost.setCurrentTabByTag(TAB_TAG_MONITOR);
			mBut2.setImageResource(R.drawable.tab2_on);
			mCateText2.setTextColor(COLOR2);

			break;
		case R.id.channel3:
			mTabHost.setCurrentTabByTag(TAB_TAG_MSG);
			mBut3.setImageResource(R.drawable.tab3_on);
			mCateText3.setTextColor(COLOR2);

			break;

		case R.id.channel4:
			mTabHost.setCurrentTabByTag(TAB_TAB_MORE);
			mBut4.setImageResource(R.drawable.tab4_on);
			mCateText4.setTextColor(COLOR2);

			break;
		default:
			break;
		}

		if (o)
			mTabHost.getCurrentView().startAnimation(left_in);
		else
			mTabHost.getCurrentView().startAnimation(right_in);
		mCurTabId = checkedId;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		// 注销广播接收者
		try {
			unregisterReceiver(msgReceiver);
		} catch (Exception e) {
		}
		try {
			unregisterReceiver(ackMessageReceiver);
		} catch (Exception e) {
		}
		// try {
		// unregisterReceiver(offlineMessageReceiver);
		// } catch (Exception e) {
		// }

		if (conflictBuilder != null) {
			conflictBuilder.create().dismiss();
			conflictBuilder = null;
		}

	}

	// /**
	// * 刷新未读消息数
	// */
	// public void updateUnreadLabel() {
	// int count = getUnreadMsgCountTotal();
	// if (count > 0) {
	// unreadLabel.setText(String.valueOf(count));
	// unreadLabel.setVisibility(View.VISIBLE);
	// } else {
	// unreadLabel.setVisibility(View.INVISIBLE);
	// }
	// }
	//
	// /**
	// * 获取未读消息数
	// *
	// * @return
	// */
	// public int getUnreadMsgCountTotal() {
	// int unreadMsgCountTotal = 0;
	// unreadMsgCountTotal = EMChatManager.getInstance().getUnreadMsgsCount();
	// return unreadMsgCountTotal;
	// }

	/**
	 * 新消息广播接收者
	 * 
	 * 
	 */
	private class NewMessageBroadcastReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			// 主页面收到消息后，主要为了提示未读，实际消息内容需要到chat页面查看

			String from = intent.getStringExtra("from");
			// 消息id
			String msgId = intent.getStringExtra("msgid");
			EMMessage message = EMChatManager.getInstance().getMessage(msgId);
			// 2014-10-22 修复在某些机器上，在聊天页面对方发消息过来时不立即显示内容的bug
			if (ChatActivity.activityInstance != null) {
				if (message.getChatType() == ChatType.GroupChat) {
					if (message.getTo().equals(
							ChatActivity.activityInstance.getToChatUsername()))
						return;
				} else {
					if (from.equals(ChatActivity.activityInstance
							.getToChatUsername()))
						return;
				}
			}

			// 注销广播接收者，否则在ChatActivity中会收到这个广播
			abortBroadcast();

			// 刷新bottom bar消息未读数
			// updateUnreadLabel();

		}
	}

	/**
	 * 消息回执BroadcastReceiver
	 */
	private BroadcastReceiver ackMessageReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			abortBroadcast();

			String msgid = intent.getStringExtra("msgid");
			String from = intent.getStringExtra("from");

			EMConversation conversation = EMChatManager.getInstance()
					.getConversation(from);
			if (conversation != null) {
				// 把message设为已读
				EMMessage msg = conversation.getMessage(msgid);

				if (msg != null) {

					// 2014-11-5 修复在某些机器上，在聊天页面对方发送已读回执时不立即显示已读的bug
					if (ChatActivity.activityInstance != null) {
						if (msg.getChatType() == ChatType.Chat) {
							if (from.equals(ChatActivity.activityInstance
									.getToChatUsername()))
								return;
						}
					}

					msg.isAcked = true;
				}
			}

		}
	};

	/**
	 * 离线消息BroadcastReceiver sdk 登录后，服务器会推送离线消息到client，这个receiver，是通知UI
	 * 有哪些人发来了离线消息 UI 可以做相应的操作，比如下载用户信息
	 */
	// private BroadcastReceiver offlineMessageReceiver = new
	// BroadcastReceiver() {
	//
	// @Override
	// public void onReceive(Context context, Intent intent) {
	// String[] users = intent.getStringArrayExtra("fromuser");
	// String[] groups = intent.getStringArrayExtra("fromgroup");
	// if (users != null) {
	// for (String user : users) {
	// System.out.println("收到user离线消息：" + user);
	// }
	// }
	// if (groups != null) {
	// for (String group : groups) {
	// System.out.println("收到group离线消息：" + group);
	// }
	// }
	// }
	// };

	private UserDao userDao;

	/**
	 * set head
	 * 
	 * @param username
	 * @return
	 */
	User setUserHead(String username) {
		User user = new User();
		user.setUsername(username);
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
		return user;
	}

	/**
	 * 连接监听listener
	 * 
	 */
	private class MyConnectionListener implements EMConnectionListener {

		@Override
		public void onConnected() {
			runOnUiThread(new Runnable() {

				@Override
				public void run() {
					// chatHistory.errorItem.setVisibility(View.GONE);
				}

			});
		}

		@Override
		public void onDisconnected(final int error) {
			runOnUiThread(new Runnable() {

				@Override
				public void run() {
					if (error == EMError.CONNECTION_CONFLICT) {
						// 显示帐号在其他设备登陆dialog
						showConflictDialog();
					} else {
						// chatHistory.errorItem.setVisibility(View.VISIBLE);
						// if (NetUtils.hasNetwork(MainActivity.this))
						// chatHistory.errorText.setText("连接不到聊天服务器");
						// else
						// chatHistory.errorText.setText("当前网络不可用，请检查网络设置");

					}
				}

			});
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (!isConflict) {
			// updateUnreadLabel();
			EMChatManager.getInstance().activityResumed();
		}

	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			moveTaskToBack(false);
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	private android.app.AlertDialog.Builder conflictBuilder;
	private boolean isConflictDialogShow;

	/**
	 * 显示帐号在别处登录dialog
	 */
	private void showConflictDialog() {
		isConflictDialogShow = true;
		VLCApplication.getInstance().logout();

		if (!MainActivity.this.isFinishing()) {
			// clear up global variables
			try {
				if (conflictBuilder == null)
					conflictBuilder = new android.app.AlertDialog.Builder(
							MainActivity.this);
				conflictBuilder.setTitle("下线通知");
				conflictBuilder.setMessage(R.string.connect_conflict);
				conflictBuilder.setPositiveButton(R.string.yes,
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								dialog.dismiss();
								conflictBuilder = null;
								finish();
								startActivity(new Intent(MainActivity.this,
										LoginActivity.class));
							}
						});
				conflictBuilder.setCancelable(false);
				conflictBuilder.create().show();
				isConflict = true;
			} catch (Exception e) {
				Log.e("###",
						"---------color conflictBuilder error" + e.getMessage());
			}

		}

	}

	// 检查更新

	private final String TAG = this.getClass().getName();

	private final int UPDATA_NONEED = 0;
	private final int UPDATA_CLIENT = 1;
	private final int GET_UNDATAINFO_ERROR = 2;
	private final int SDCARD_NOMOUNTED = 3;
	private final int DOWN_ERROR = 4;

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

				// 从资源文件获取服务器 地址
				String path = "";
				if (ipString.equals("218.60.13.9:7799")) {
					path = "http://" + ipString
							+ getResources().getString(R.string.url_update);
				} else {
					path = "http://" + ipString + ":7799"
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
					updateHandler.sendMessage(msg);
					// LoginMain();
				} else {
					Log.i(TAG, "版本号不同 ,提示用户升级 ");
					Message msg = new Message();
					msg.what = UPDATA_CLIENT;
					updateHandler.sendMessage(msg);
				}
			} catch (Exception e) {
				// 待处理
				Message msg = new Message();
				msg.what = GET_UNDATAINFO_ERROR;
				updateHandler.sendMessage(msg);
				e.printStackTrace();
			}
		}
	}

	Handler updateHandler = new Handler() {

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
		builer.setCancelable(false);
		builer.setMessage(info.getDescription());
		// 当点确定按钮时从服务器上下载 新的apk 然后安装
		builer.setPositiveButton("确定", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				Log.i(TAG, "下载apk,更新");
				updateApk();
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
	protected void updateApk() {
		final ProgressDialog pd; // 进度条对话框
		pd = new ProgressDialog(MainActivity.this);
		pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		pd.setMessage("正在下载更新");
		if (!Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {
			Message msg = new Message();
			msg.what = SDCARD_NOMOUNTED;
			updateHandler.sendMessage(msg);
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
						updateHandler.sendMessage(msg);
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

	boolean isExit = false;
	boolean hasExitTask = false;
	Timer exitTimer = new Timer();
	TimerTask exitTask = new TimerTask() {
		public void run() {
			isExit = false;
			hasExitTask = true;
		}
	};

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		if (event.getKeyCode() == KeyEvent.KEYCODE_BACK
				&& event.getRepeatCount() == 0
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
			return false;
		} else {
			return super.dispatchKeyEvent(event);
		}
	}

	/** 退出 */
	void exit() {
		finish();
		java.lang.System.exit(0);
	}

}