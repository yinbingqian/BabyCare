package com.lnpdit.babycare;

import java.io.File;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import com.lnpdit.garden.GardenReplyManageActivity;
import com.lnpdit.sqllite.BBGJDB;
import com.lnpdit.util.DownLoadManager;
import com.lnpdit.util.UpdataInfo;
import com.lnpdit.util.UpdataInfoParser;
import com.lnpdit.util.passwordInterface;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MimePageActivity extends passwordInterface implements
		OnClickListener {

	private TextView txtRealName;
	private TextView txtPhoneNumber;
	private TextView txtGardenName;

	private RelativeLayout mime_layout_04;
	private RelativeLayout mime_layout_05;
	private RelativeLayout mime_layout_06;
	private Button exitBtn;

	public static final String SETTING_INFOS = "SETTING_Infos";
	public static final String SERIP = "serverIp";
	public static final String NAME = "name";
	public static final String PASS = "pass";
	public static final String REMEMBER = "remember";
	public static final String AUTOLOG = "autolog";

	Bundle loginBundle;
	private static String userName = "";
	private static String realName = "";
	private static String ipString = "";
	private static String gardenName = "";

	int user_state;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.mimepage);

		init();

		mime_layout_04 = (RelativeLayout) findViewById(R.id.mime_layout_04);
		mime_layout_04.setOnClickListener(this);

		mime_layout_05 = (RelativeLayout) findViewById(R.id.mime_layout_05);
		mime_layout_05.setOnClickListener(this);

		mime_layout_06 = (RelativeLayout) findViewById(R.id.mime_layout_06);
		mime_layout_06.setOnClickListener(this);

		exitBtn = (Button) findViewById(R.id.exit_login_btn);
		exitBtn.setOnClickListener(this);
	}

	private void init() {
		loginBundle = this.getIntent().getExtras();

		ipString = loginBundle.getString("serverIp").toString();
		userName = loginBundle.getString("username").toString();
		realName = loginBundle.getString("realName").toString();
		gardenName = loginBundle.getString("kinderName").toString();

		txtRealName = (TextView) findViewById(R.id.realname);
		txtPhoneNumber = (TextView) findViewById(R.id.phonenum);
		txtGardenName = (TextView) findViewById(R.id.garden_text);

		txtRealName.setText(realName);
		txtPhoneNumber.setText(userName);
		txtGardenName.setText(gardenName);

		int res = checkUser();

	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {

		// case R.id.mime_layout_01:
		//
		// Intent mimeIntent=new Intent(MimePageActivity.this,
		// TestActivity.class);
		// startActivity(mimeIntent);
		//
		// break;

		case R.id.mime_layout_04:

			if (user_state == 0) {
				Intent intent = new Intent();
				intent.setClass(MimePageActivity.this, LoginActivity.class);
				startActivity(intent);
				Toast.makeText(this, "请先进行登录操作", Toast.LENGTH_SHORT).show();
			} else {
				Intent intent = new Intent();
				intent.setClass(MimePageActivity.this,
						GardenReplyManageActivity.class);
				BBGJDB tdd = new BBGJDB(this);
				Cursor cursor = tdd.selectuser();
				cursor.moveToFirst();
				intent.putExtra("ID", cursor.getString(1)).toString();
				startActivity(intent);
				break;
			}

			break;

		case R.id.mime_layout_05:

			// 检查更新
			try {
				showWait(getResources().getString(R.string.updating));
				localVersion = getVersionName();
				CheckVersionTask cv = new CheckVersionTask();
				new Thread(cv).start();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			break;

		case R.id.mime_layout_06:

			Intent aboutIntent = new Intent(MimePageActivity.this,
					AboutActivity.class);
			startActivity(aboutIntent);

			break;

		case R.id.exit_login_btn:

			new AlertDialog.Builder(this)
					.setMessage("确定退出登录吗？")
					.setPositiveButton("确定",
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface arg0,
										int arg1) {
									Intent intent = new Intent(
											MimePageActivity.this,
											LoginActivity.class);
									startActivity(intent);
									finish();
								}

							}).setNegativeButton("取消", null).create().show();
			break;

		default:
			break;
		}
	}

	private int checkUser() {
		BBGJDB tdd = new BBGJDB(this);
		Cursor cursor = tdd.selectuser();
		if (cursor.getCount() == 0) {
			user_state = 0;
		} else {
			cursor.moveToFirst();
			String usr_name = cursor.getString(2);
			user_state = 1;
		}
		return user_state;
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
				Toast.makeText(getApplicationContext(), "您的软件是最新版本，无需升级",
						Toast.LENGTH_SHORT).show();
				waitClose();
				break;
			case UPDATA_CLIENT:
				// 对话框通知用户升级程序
				// Toast.makeText(getApplicationContext(), "可以升级程序啦~",
				// 1).show();
				showUpdataDialog();
				waitClose();
				break;
			case GET_UNDATAINFO_ERROR:
				// 服务器超时
				Toast.makeText(getApplicationContext(), "获取服务器更新信息失败", 1)
						.show();
				waitClose();
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
		pd = new ProgressDialog(MimePageActivity.this);
		pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		pd.setMessage("正在下载更新");
		pd.setCanceledOnTouchOutside(false);
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

}
