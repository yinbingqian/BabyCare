package com.lnpdit.monitor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.xmlpull.v1.XmlPullParserException;

import com.lnpdit.babycare.R;
import com.lnpdit.photo.SimpleZoomListener;
import com.lnpdit.photo.ZoomState;
import com.lnpdit.sqllite.ToDoDB;
import com.lnpdit.util.ComUtil;
import com.lnpdit.util.ShakeDetector;
import com.lnpdit.util.ShakeListener;
import com.lnpdit.util.TrafficMonitoring;
import com.lnpdit.util.passwordInterface;
import com.lnpdit.widget.Panel;
import com.lnpdit.widget.Panel.OnPanelListener;
import com.mulong.android.VView;

import android.R.bool;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Bitmap.Config;
import android.media.AudioFormat;
import android.net.TrafficStats;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.util.Log;
import android.view.Display;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class ShowVideo extends passwordInterface implements OnClickListener,
		OnTouchListener, OnPanelListener {

	VView vv;

	private GridView toolbar;

	static int screenHeight;
	static int screenWidth;

	// 视频加载计时器
	private int timerCount = 0;
	private Timer timer;
	private boolean isWaiting;
	private Thread threadLoading;

	int width; // 此处设定初始分辨率
	int height;
	// String dstip = "60.16.32.11";//外包IP
	// String dstip = "200.20.32.200";//200服务器
	// String dstip = "200.20.22.217";// 华仔电脑
	// String dstip = "218.60.13.9";//点9服务器
	// String dstip = "59.46.115.85";//200服务器外网IP
	// String dstip = "218.25.10.52";
	String serverIp = "";// 获取传过来的管理平台IP
	String socketIp = "";// 获取传过来的转发IP
	String port = "";
	String ptz = "";
	String zoom = "";
	String talk = "";
	int chCount = 0;
	int chNo = 0;
	int adapterID = 0;
	int listNo = 0;
	int listCount = 0;

	// 云台按钮
	private Button upButton;
	private Button downButton;
	private Button leftButton;
	private Button rightButton;
	private Button zoominButton;
	private Button zoomoutButton;
	private Button focusinButton;
	private Button focusoutButton;
	private Button talkButton;

	// 图像缩放参数
	private Bitmap bitmap;
	private ZoomState mZoomState;
	private SimpleZoomListener mZoomListener;

	public ComUtil util;

	// 心跳包
	private int heartTimerCount = 0;
	private Timer heartTimer;
	private boolean isAlive = true;
	private Thread heartBeatThread;

	// 摇晃
	private ShakeDetector mShakeDetector;
	private ShakeListener mShaker;
	private Timer switchTimer;
	private boolean isSwitching = true;
	private Thread swiThread;
	private int switchTimerCount = 0;

	// 测试版传参
	private String videoString;

	// 更新UI
	Handler handlerUI;

	private String userId;
	private String webId;
	private String ifRecord;
	private String ifSnap;
	// 检测是否收藏
	private String ifFavor = "0";

	// 数据库
	Button favorButton;
	ToDoDB db;

	int byteFlag;
	int noPtzFlag = 20;
	int ptzFlag = 5;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// 禁止锁屏
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		vv = new VView(this);
		setContentView(vv);

		util = new ComUtil();

		handlerUI = new Handler();
		// 等待提示框
		loadingFlag = true;
		timer = new Timer();
		isWaiting = true;
		showWait("视频载入中，请稍候...");
		threadLoading = new Thread(waitThread);
		threadLoading.start();

		Display display = getWindowManager().getDefaultDisplay();
		screenWidth = display.getWidth();
		screenHeight = display.getHeight();
		bitmap = Bitmap.createBitmap(screenWidth, screenHeight, Config.RGB_565);
		// bitmap=Bitmap.createBitmap((int)width, (int)height, Config.RGB_565);

		socketIp = this.getIntent().getStringExtra("socketIp");// 获取传过来的设备端口
		port = this.getIntent().getStringExtra("port");// 获取传过来的设备端口

		userId = this.getIntent().getStringExtra("userId").toString();

		width = Integer.parseInt(this.getIntent().getStringExtra("width"));// 获取传过来的视频宽度
		height = Integer.parseInt(this.getIntent().getStringExtra("height"));// 获取传过来的视频高度
		ptz = this.getIntent().getStringExtra("ptz");// 获取是否支持云台
		zoom = this.getIntent().getStringExtra("zoom");// 获取是否支持云台
		talk = this.getIntent().getStringExtra("talk");// 获取是否支持云台
		chCount = Integer
				.parseInt(this.getIntent().getStringExtra("listCount"));// 获取通道数量
		chNo = Integer.parseInt(this.getIntent().getStringExtra("chNo"));// 获取通道ID
		adapterID = Integer.parseInt(this.getIntent().getStringExtra(
				"adapterId"));// 获取适配ID

		listNo = Integer.parseInt(this.getIntent().getStringExtra("listNo"));// 获取在设备中排序
		listCount = Integer.parseInt(this.getIntent().getStringExtra(
				"listCount"));// 获取设备总数

		ifRecord = this.getIntent().getStringExtra("ifRecord").toString();
		ifSnap = this.getIntent().getStringExtra("ifSnap").toString();

		videoString = this.getIntent().getStringExtra("devId");

		if (ptz.equals("1")) {
			byteFlag = ptzFlag;
		} else {
			byteFlag = noPtzFlag;
		}
		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				/*
				 * 加载网络图片 load form url
				 */
				vv.InitVideoParam("", (int) width, (int) height, byteFlag);
				vv.NetTest(socketIp, 9332, videoString, chNo, adapterID);
				vv.PlayVideo(screenWidth, screenHeight);

				vv.setContext(ShowVideo.this);
			}
		});
		thread.start();

		vv.setHeart(true);

		// 视频上添加可操作Layout
		// drawWidget();

		// 数据库
		db = new ToDoDB(this);
		Cursor cur = db.select_favor();
		while (cur.moveToNext()) {
			int webIndex = cur.getColumnIndex("DATA_WEBID");
			webId = cur.getString(webIndex);
			int userIndex = cur.getColumnIndex("DATA_USERID");
			String webUserId = cur.getString(userIndex);
			int devIndex = cur.getColumnIndex("DATA_DEVID");
			String favorDevId = cur.getString(devIndex);
			int chIndex = cur.getColumnIndex("DATA_CHNO");
			String favorChNo = cur.getString(chIndex);
			if (favorDevId.equals(videoString)
					&& favorChNo.equals(Integer.toString(chNo))
					&& webUserId.equals(userId)) {
				ifFavor = "1";
			}
		}

	}

	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (msg.what == 0) {
				vv.setImage(bitmap);
				mZoomState = new ZoomState();
				vv.setZoomState(mZoomState);
				mZoomListener = new SimpleZoomListener();
				mZoomListener.setZoomState(mZoomState);
				mZoomListener.setmGestureDetector(new GestureDetector(
						new MyGestureListener()));
				vv.setOnTouchListener(mZoomListener);

				resetZoomState();
			} else if (msg.what == 1) {
				vv.setImage(bitmap);
				mZoomState = new ZoomState();
				vv.setZoomState(mZoomState);
				mZoomListener = new SimpleZoomListener();
				mZoomListener.setZoomState(mZoomState);
				mZoomListener.setmGestureDetector(new GestureDetector(
						new MyGestureListener()));
				vv.setOnTouchListener(mZoomListener);
			}
		}
	};

	private void resetZoomState() {
		mZoomState.setPanX(0.5f);
		mZoomState.setPanY(0.5f);
		mZoomState.setZoom(1f);
		mZoomState.notifyObservers();
	}

	Boolean playBoolean = true;
	Boolean talkBoolean = false;
	Boolean recordBoolean = false;
	static Boolean isRecoding = false;

	private Boolean playOrPause(Boolean bl) {
		if (bl == true)
			return false;
		else
			return true;
	}

	private Boolean recordOrNot(Boolean bl) {
		if (bl == true)
			return false;
		else
			return true;
	}

	// 向视频上添加按钮等Layout
	Runnable runnableDrawWidget = new Runnable() {
		@Override
		public void run() {
			drawWidget();
		}

	};

	private Boolean ifFling = true;

	public void goToZoomPage() {
		handler.sendEmptyMessage(0);
	}

	public void goToSwicherPage() {
		handler.sendEmptyMessage(1);
	}

	private void drawWidget() {

		try {
			Display display = getWindowManager().getDefaultDisplay();
			final LayoutInflater inflater = LayoutInflater.from(this);

			// 显示云台箭头
			LinearLayout layout = (LinearLayout) inflater.inflate(
					R.layout.arrow, null).findViewById(R.id.arrowlayout);
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
					(int) display.getWidth(), (int) display.getHeight(), 200);
			addContentView(layout, params);

			upButton = (Button) findViewById(R.id.btnUp);
			upButton.setOnClickListener(this);
			upButton.setOnTouchListener(new OnTouchListener() {
				public boolean onTouch(View v, MotionEvent event) {
					// TODO Auto-generated method stub
					if (event.getAction() == MotionEvent.ACTION_DOWN) {
						// 更改为按下时的背景图片
						v.setBackgroundResource(R.drawable.uppress);
						vv.videoUP();
					} else if (event.getAction() == MotionEvent.ACTION_UP) {
						// 改为抬起时的图片
						v.setBackgroundResource(R.drawable.up);
						vv.videoUPStop();
					}

					return false;
				}
			});
			downButton = (Button) findViewById(R.id.btnDown);
			downButton.setOnClickListener(this);
			downButton.setOnTouchListener(new OnTouchListener() {
				public boolean onTouch(View v, MotionEvent event) {
					// TODO Auto-generated method stub
					if (event.getAction() == MotionEvent.ACTION_DOWN) {
						// 更改为按下时的背景图片
						v.setBackgroundResource(R.drawable.downpress);
						vv.videoDOWN();
					} else if (event.getAction() == MotionEvent.ACTION_UP) {
						// 改为抬起时的图片
						v.setBackgroundResource(R.drawable.down);
						vv.videoDOWNStop();
					}

					return false;
				}
			});
			leftButton = (Button) findViewById(R.id.btnLeft);
			leftButton.setOnClickListener(this);
			leftButton.setOnTouchListener(new OnTouchListener() {
				public boolean onTouch(View v, MotionEvent event) {
					// TODO Auto-generated method stub
					if (event.getAction() == MotionEvent.ACTION_DOWN) {
						// 更改为按下时的背景图片
						v.setBackgroundResource(R.drawable.leftpress);
						vv.videoLEFT();
					} else if (event.getAction() == MotionEvent.ACTION_UP) {
						// 改为抬起时的图片
						v.setBackgroundResource(R.drawable.left);
						vv.videoLEFTStop();
					}

					return false;
				}
			});
			rightButton = (Button) findViewById(R.id.btnRight);
			rightButton.setOnClickListener(this);
			rightButton.setOnTouchListener(new OnTouchListener() {
				public boolean onTouch(View v, MotionEvent event) {
					// TODO Auto-generated method stub
					if (event.getAction() == MotionEvent.ACTION_DOWN) {
						// 更改为按下时的背景图片
						v.setBackgroundResource(R.drawable.rightpress);
						vv.videoRIGHT();
					} else if (event.getAction() == MotionEvent.ACTION_UP) {
						// 改为抬起时的图片
						v.setBackgroundResource(R.drawable.right);
						vv.videoRIGHTStop();
					}

					return false;
				}
			});

			if (ptz.equals("1")) {
				upButton.setVisibility(Button.VISIBLE);
				downButton.setVisibility(Button.VISIBLE);
				leftButton.setVisibility(Button.VISIBLE);
				rightButton.setVisibility(Button.VISIBLE);
			} else if (ptz.equals("0")) {
				upButton.setVisibility(Button.GONE);
				downButton.setVisibility(Button.GONE);
				leftButton.setVisibility(Button.GONE);
				rightButton.setVisibility(Button.GONE);
			}

			// 显示工具栏
			LinearLayout layoutSD = (LinearLayout) inflater.inflate(
					R.layout.bottomsd, null).findViewById(R.id.bottomlayout);
			LinearLayout.LayoutParams paramsSD = new LinearLayout.LayoutParams(
					(int) display.getWidth(), (int) display.getHeight(), 200);
			addContentView(layoutSD, paramsSD);

			// 上个通道按钮
			Button lastButton = (Button) findViewById(R.id.last);
			lastButton.setVisibility(Button.GONE);
			lastButton.setOnClickListener(this);
			lastButton.setOnTouchListener(new OnTouchListener() {
				public boolean onTouch(View v, MotionEvent event) {
					// TODO Auto-generated method stub
					if (event.getAction() == MotionEvent.ACTION_DOWN) {
						// 更改为按下时的背景图片
						v.setBackgroundResource(R.drawable.lastpress_icon);
					} else if (event.getAction() == MotionEvent.ACTION_UP) {
						// 改为抬起时的图片
						v.setBackgroundResource(R.drawable.last_icon);
					}

					return false;
				}
			});

			// 下个通道按钮
			Button nextButton = (Button) findViewById(R.id.next);
			nextButton.setVisibility(Button.GONE);
			nextButton.setOnClickListener(this);
			nextButton.setOnTouchListener(new OnTouchListener() {
				public boolean onTouch(View v, MotionEvent event) {
					// TODO Auto-generated method stub
					if (event.getAction() == MotionEvent.ACTION_DOWN) {
						// 更改为按下时的背景图片
						v.setBackgroundResource(R.drawable.nextpress_icon);
					} else if (event.getAction() == MotionEvent.ACTION_UP) {
						// 改为抬起时的图片
						v.setBackgroundResource(R.drawable.next_icon);
					}

					return false;
				}
			});

			// 暂停播放按钮
			Button playButton = (Button) findViewById(R.id.play);
			playButton.setOnClickListener(this);
			playButton.setOnTouchListener(new OnTouchListener() {
				public boolean onTouch(View v, MotionEvent event) {
					// TODO Auto-generated method stub
					if (playBoolean) {
						if (event.getAction() == MotionEvent.ACTION_DOWN) {
							// 更改为按下时的背景图片
							v.setBackgroundResource(R.drawable.icon_pausepress);
						} else if (event.getAction() == MotionEvent.ACTION_UP) {
							// 改为抬起时的图片
							v.setBackgroundResource(R.drawable.icon_pause);
						}
					} else {
						if (event.getAction() == MotionEvent.ACTION_DOWN) {
							// 更改为按下时的背景图片
							v.setBackgroundResource(R.drawable.playbtn_iconpress);
						} else if (event.getAction() == MotionEvent.ACTION_UP) {
							// 改为抬起时的图片
							v.setBackgroundResource(R.drawable.playbtn_icon);
						}
					}

					return false;
				}
			});

			// 变倍加
			zoominButton = (Button) findViewById(R.id.zoomin);
			if (zoom.equals("0")) {
				zoominButton.setVisibility(Button.GONE);
			}
			zoominButton.setOnClickListener(this);
			zoominButton.setOnTouchListener(new OnTouchListener() {
				public boolean onTouch(View v, MotionEvent event) {
					// TODO Auto-generated method stub
					if (event.getAction() == MotionEvent.ACTION_DOWN) {
						// 更改为按下时的背景图片
						v.setBackgroundResource(R.drawable.zoom_inpress);
						vv.videoZoomIn();
					} else if (event.getAction() == MotionEvent.ACTION_UP) {
						// 改为抬起时的图片
						v.setBackgroundResource(R.drawable.zoom_in);
						vv.videoZoomInStop();
					}

					return false;
				}
			});
			// 变倍减
			zoomoutButton = (Button) findViewById(R.id.zoomout);
			if (zoom.equals("0")) {
				zoomoutButton.setVisibility(Button.GONE);
			}
			zoomoutButton.setOnClickListener(this);
			zoomoutButton.setOnTouchListener(new OnTouchListener() {
				public boolean onTouch(View v, MotionEvent event) {
					// TODO Auto-generated method stub
					if (event.getAction() == MotionEvent.ACTION_DOWN) {
						// 更改为按下时的背景图片
						v.setBackgroundResource(R.drawable.zoom_outpress);
						vv.videoZoomOut();
					} else if (event.getAction() == MotionEvent.ACTION_UP) {
						// 改为抬起时的图片
						v.setBackgroundResource(R.drawable.zoom_out);
						vv.videoZoomOutStop();
					}

					return false;
				}
			});
			// 变焦加
			focusinButton = (Button) findViewById(R.id.focusin);
			if (zoom.equals("0")) {
				focusinButton.setVisibility(Button.GONE);
			}
			focusinButton.setOnClickListener(this);
			focusinButton.setOnTouchListener(new OnTouchListener() {
				public boolean onTouch(View v, MotionEvent event) {
					// TODO Auto-generated method stub
					if (event.getAction() == MotionEvent.ACTION_DOWN) {
						// 更改为按下时的背景图片
						v.setBackgroundResource(R.drawable.focus_inpress);
						vv.videoFocusIn();
					} else if (event.getAction() == MotionEvent.ACTION_UP) {
						// 改为抬起时的图片
						v.setBackgroundResource(R.drawable.focus_in);
						vv.videoFocusInStop();
					}

					return false;
				}
			});
			// 变焦减
			focusoutButton = (Button) findViewById(R.id.focusout);
			if (zoom.equals("0")) {
				focusoutButton.setVisibility(Button.GONE);
			}
			focusoutButton.setOnClickListener(this);
			focusoutButton.setOnTouchListener(new OnTouchListener() {
				public boolean onTouch(View v, MotionEvent event) {
					// TODO Auto-generated method stub
					if (event.getAction() == MotionEvent.ACTION_DOWN) {
						// 更改为按下时的背景图片
						v.setBackgroundResource(R.drawable.focus_outpress);
						vv.videoFocusOut();
					} else if (event.getAction() == MotionEvent.ACTION_UP) {
						// 改为抬起时的图片
						v.setBackgroundResource(R.drawable.focus_out);
						vv.videoFocusOutStop();
					}

					return false;
				}
			});

			// 截屏按钮
			Button snapButton = (Button) findViewById(R.id.snap);
			if (ifSnap.equals("0")) {
				snapButton.setVisibility(Button.GONE);
			}
			snapButton.setOnClickListener(this);
			snapButton.setOnTouchListener(new OnTouchListener() {
				public boolean onTouch(View v, MotionEvent event) {
					// TODO Auto-generated method stub
					if (event.getAction() == MotionEvent.ACTION_DOWN) {
						// 更改为按下时的背景图片
						v.setBackgroundResource(R.drawable.snapbtn_iconpress);
					} else if (event.getAction() == MotionEvent.ACTION_UP) {
						// 改为抬起时的图片
						v.setBackgroundResource(R.drawable.snapbtn_icon);
					}

					return false;
				}
			});

			// 录像按钮
			Button recordButton = (Button) findViewById(R.id.record);
			if (ifRecord.equals("0")) {
				recordButton.setVisibility(Button.GONE);
			}
			recordButton.setOnClickListener(this);
			recordButton.setOnTouchListener(new OnTouchListener() {
				public boolean onTouch(View v, MotionEvent event) {
					// TODO Auto-generated method stub
					if (event.getAction() == MotionEvent.ACTION_DOWN) {
						// 更改为按下时的背景图片
						// v.setBackgroundResource(R.drawable.icon_recordpress);
					} else if (event.getAction() == MotionEvent.ACTION_UP) {
						// 改为抬起时的图片
						// v.setBackgroundResource(R.drawable.icon_record);
					}

					return false;
				}
			});

			// 对讲按钮
			talkButton = (Button) findViewById(R.id.talk);
			if (talk.equals("0")) {
				talkButton.setVisibility(Button.GONE);
			}
			talkButton.setOnClickListener(this);
			talkButton.setOnTouchListener(new OnTouchListener() {
				public boolean onTouch(View v, MotionEvent event) {
					// TODO Auto-generated method stub
					if (event.getAction() == MotionEvent.ACTION_DOWN) {
						// 更改为按下时的背景图片
						// v.setBackgroundResource(R.drawable.talking);
						// new Thread() {
						// public void run() {
						// handler.post(talkStart);
						// }
						// }.start();

					} else if (event.getAction() == MotionEvent.ACTION_UP) {
						// 改为抬起时的图片
						// v.setBackgroundResource(R.drawable.talk);
						// new Thread() {
						// public void run() {
						// handler.post(talkEnd);
						// }
						// }.start();
					}

					return false;
				}
			});
			// 收藏夹按钮
			favorButton = (Button) findViewById(R.id.favor);
			if (ifFavor.equals("1")) {
				favorButton.setBackgroundResource(R.drawable.icon_favorpress);
			} else if (ifFavor.equals("0")) {
				favorButton.setBackgroundResource(R.drawable.icon_favor);
			}
			favorButton.setOnClickListener(this);
			favorButton.setOnTouchListener(new OnTouchListener() {
				public boolean onTouch(View v, MotionEvent event) {
					// TODO Auto-generated method stub
					if (event.getAction() == MotionEvent.ACTION_DOWN) {
						// 更改为按下时的背景图片
						// if (ifFavor.equals("1")) {
						// v.setBackgroundResource(R.drawable.icon_favor);
						// } else if (ifFavor.equals("0")) {
						// v.setBackgroundResource(R.drawable.icon_favorpress);
						// }
					} else if (event.getAction() == MotionEvent.ACTION_UP) {
						// 改为抬起时的图片
						// if (ifFavor.equals("1")) {
						// v.setBackgroundResource(R.drawable.icon_favorpress);
						// } else if (ifFavor.equals("0")) {
						// v.setBackgroundResource(R.drawable.icon_favor);
						// }
					}

					return false;
				}
			});

			// 返回按钮
			Button backButton = (Button) findViewById(R.id.bottomback);
			backButton.setOnClickListener(this);
			backButton.setOnTouchListener(new OnTouchListener() {
				public boolean onTouch(View v, MotionEvent event) {
					// TODO Auto-generated method stub
					if (event.getAction() == MotionEvent.ACTION_DOWN) {
						// 更改为按下时的背景图片
						v.setBackgroundResource(R.drawable.icon_backpress);
					} else if (event.getAction() == MotionEvent.ACTION_UP) {
						// 改为抬起时的图片
						v.setBackgroundResource(R.drawable.icon_back);
					}

					return false;
				}
			});

		} catch (Exception e) {
			// TODO: handle exception
			if (e != null) {
				System.out.println(e.getMessage());
			}
		}
	}

	// 开始对讲提示
	Runnable talkStart = new Runnable() {
		@Override
		public void run() {
			Toast.makeText(ShowVideo.this, "开始对讲", Toast.LENGTH_SHORT).show();
		}

	};
	// 结束对讲提示
	Runnable talkEnd = new Runnable() {
		@Override
		public void run() {
			Toast.makeText(ShowVideo.this, "对讲结束", Toast.LENGTH_SHORT).show();
		}

	};

	String recordPath;

	// 云台控制
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		// case R.id.btnUp:
		// vv.videoUP();
		// break;
		//
		// case R.id.btnDown:
		// vv.videoDOWN();
		// break;
		//
		// case R.id.btnLeft:
		// vv.videoLEFT();
		// break;
		//
		// case R.id.btnRight:
		// vv.videoRIGHT();
		// break;

		// 播放按钮
		case R.id.play:
			if (playBoolean) {// 正在播放

				if (!recordBoolean) {
					vv.setStopStatus(true);

					vv.setHeart(false);
					vv.closeSocket();
					waitClose();
					isWaiting = false;
					isAlive = false;// 是否观看视频

					v.setBackgroundResource(R.drawable.playbtn_icon);
					playBoolean = playOrPause(playBoolean);
				} else {
					Toast.makeText(this, "正在录像中，请勿停止！", Toast.LENGTH_SHORT)
							.show();
				}
			} else {
				isWaiting = true;
				loadingFlag = true;
				switchTimerCount = 0;

				switchTimer = new Timer();
				showWait("视频载入中，请稍候...");
				swiThread = new Thread(switchThread);
				swiThread.start();

				Display display = getWindowManager().getDefaultDisplay();
				bitmap = Bitmap.createBitmap(display.getWidth(),
						display.getHeight(), Config.RGB_565);

				Thread thread = new Thread(new Runnable() {
					@Override
					public void run() {
						/*
						 * 加载网络图片 load form url
						 */
						vv.InitVideoParam("", (int) width, (int) height,
								byteFlag);
						vv.NetTest(socketIp, 9332, videoString, chNo, adapterID);
						vv.PlayVideo(screenWidth, screenHeight);
						handler.sendEmptyMessage(1);
					}
				});
				thread.start();

				v.setBackgroundResource(R.drawable.icon_pause);

				playBoolean = playOrPause(playBoolean);

				vv.setStopStatus(false);

				vv.setHeart(true);
			}
			break;
		// 上一个通道按钮
		case R.id.last:
			if (!recordBoolean) {
				switchDev("last");
			} else {
				Toast.makeText(this, "正在录像中，不可切换通道！", Toast.LENGTH_SHORT)
						.show();
			}
			break;
		// 下一个通道按钮
		case R.id.next:
			if (!recordBoolean) {
				switchDev("next");
			} else {
				Toast.makeText(this, "正在录像中，不可切换通道！", Toast.LENGTH_SHORT)
						.show();
			}
			break;
		// 抓拍按钮
		case R.id.snap:
			if (playBoolean) {
				// Bitmap snapBitmap = getViewBitmap(vv);
				Bitmap snapBitmap = vv.snapBitmap();
				SimpleDateFormat formatter = new SimpleDateFormat(
						"yyyyMMdd-HHmmss");
				Date curDate = new Date(System.currentTimeMillis());// 获取当前时间
				String str = "Video" + formatter.format(curDate);
				savePic(snapBitmap, str);
				Toast.makeText(this, "抓拍成功", Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(this, "视频已停止，无法抓拍！", Toast.LENGTH_SHORT).show();
			}
			break;
		// 录像按钮
		case R.id.record:
			if (playBoolean) {
				if (!recordBoolean) {
					Thread thread = new Thread(new Runnable() {
						@Override
						public void run() {
							vv.record();
							new Thread() {
								public void run() {
									handler.post(recordWaitting);
								}
							}.start();
						}
					});
					thread.start();

					v.setBackgroundResource(R.drawable.icon_recordpress);
					// Toast.makeText(this, "正在创建录像文件",
					// Toast.LENGTH_SHORT).show();
				} else {
					Thread thread = new Thread(new Runnable() {
						@Override
						public void run() {
							recordPath = vv.stopRecord();

							new Thread() {
								public void run() {
									handler.post(recordStop);
								}
							}.start();
						}
					});
					thread.start();
					v.setBackgroundResource(R.drawable.icon_record);
					// Toast.makeText(this, "正在保存录像",
					// Toast.LENGTH_SHORT).show();
					// isRecoding = false;
				}
				recordBoolean = recordOrNot(recordBoolean);
			} else {
				Toast.makeText(this, "视频已停止，无法录像！", Toast.LENGTH_SHORT).show();
			}

			break;
		// 对讲按钮
		case R.id.talk:
			break;
		// 收藏按钮
		case R.id.favor: {
			if (ifFavor.equals("1")) {
				ifFavor = "0";
				v.setBackgroundResource(R.drawable.icon_favor);

				deleteFavor mThread = new deleteFavor();
				Thread thread = new Thread(mThread);
				thread.start();

				// Toast.makeText(this, "取消收藏！", Toast.LENGTH_SHORT).show();
			} else if (ifFavor.equals("0")) {
				ifFavor = "1";
				v.setBackgroundResource(R.drawable.icon_favorpress);

				AddFavor mThread = new AddFavor();
				Thread thread = new Thread(mThread);
				thread.start();

				// Toast.makeText(this, "添加收藏！", Toast.LENGTH_SHORT).show();
			}
		}
			break;
		// 返回按钮
		case R.id.bottomback:

			if (!recordBoolean) {
				vv.videoStop();
				vv.setStopStatus(true);
				vv.closeSocket();
				waitClose();
				isWaiting = false;
				isAlive = false;// 是否观看视频
				try {
					mShaker.pause();
				} catch (Exception e) {
					// TODO: handle exception
				}
				finish();
			} else {
				Toast.makeText(this, "正在录像中，请勿退出！", Toast.LENGTH_SHORT).show();
			}
			break;
		}
	}

	// 添加收藏显示
	Runnable addFavorRunnable = new Runnable() {
		@Override
		public void run() {
			Toast.makeText(ShowVideo.this, "添加收藏！", Toast.LENGTH_LONG).show();
		}

	};
	// 删除收藏显示
	Runnable deleteFavorRunnable = new Runnable() {
		@Override
		public void run() {
			Toast.makeText(ShowVideo.this, "删除收藏！", Toast.LENGTH_LONG).show();
		}

	};

	private class AddFavor implements Runnable {
		@Override
		public void run() {
			// TODO Auto-generated method stub

			String webAddId = "0";

			// String NAMESPACE = "MobileNewspaper";
			// String METHOD_NAME = "AddFavor";
			// String URL = "http://" + dstip
			// + getResources().getString(R.string.url_server);
			// String SOAP_ACTION = "MobileNewspaper/AddFavor";
			//
			// SoapObject rpc = new SoapObject(NAMESPACE, METHOD_NAME);
			// rpc.addProperty("userId", userId);
			// rpc.addProperty("devId", videoString);
			// rpc.addProperty("chno", chNo);
			//
			// SoapSerializationEnvelope envelope = new
			// SoapSerializationEnvelope(
			// SoapEnvelope.VER11);
			// envelope.bodyOut = rpc;
			// envelope.dotNet = true;
			// envelope.setOutputSoapObject(rpc);
			// HttpTransportSE ht = new HttpTransportSE(URL);
			// ht.debug = true;
			// try {
			// ht.call(SOAP_ACTION, envelope);
			// Object push_soap = envelope.getResponse();
			// webAddId = push_soap.toString().trim();
			// } catch (IOException e) {
			// // TODO Auto-generated catch block
			// e.printStackTrace();
			// } catch (XmlPullParserException e) {
			// // TODO Auto-generated catch block
			// e.printStackTrace();
			// }

			ContentValues values = new ContentValues();
			values.put("DATA_WEBID", webAddId);
			values.put("DATA_USERID", userId);
			values.put("DATA_DEVID", videoString);
			values.put("DATA_CHNO", chNo);
			db.insert_favor(values);
			new Thread() {
				public void run() {
					handler.post(addFavorRunnable);
				}
			}.start();
		}

	}

	private class deleteFavor implements Runnable {
		@Override
		public void run() {
			// TODO Auto-generated method stub

			db.DeleteFavorById(userId, videoString, Integer.toString(chNo));

			// String NAMESPACE = "MobileNewspaper";
			// String METHOD_NAME = "DeleteFavor";
			// String URL = "http://" + dstip
			// + getResources().getString(R.string.url_server);
			// String SOAP_ACTION = "MobileNewspaper/DeleteFavor";
			//
			// SoapObject rpc = new SoapObject(NAMESPACE, METHOD_NAME);
			// rpc.addProperty("favorId", webId);
			//
			// SoapSerializationEnvelope envelope = new
			// SoapSerializationEnvelope(
			// SoapEnvelope.VER11);
			// envelope.bodyOut = rpc;
			// envelope.dotNet = true;
			// envelope.setOutputSoapObject(rpc);
			// HttpTransportSE ht = new HttpTransportSE(URL);
			// ht.debug = true;
			// try {
			// ht.call(SOAP_ACTION, envelope);
			// Object push_soap = envelope.getResponse();
			// String result = push_soap.toString().trim();
			// String result_text = "";
			// } catch (IOException e) {
			// // TODO Auto-generated catch block
			// e.printStackTrace();
			// } catch (XmlPullParserException e) {
			// // TODO Auto-generated catch block
			// e.printStackTrace();
			// }
			new Thread() {
				public void run() {
					handler.post(deleteFavorRunnable);
				}
			}.start();
		}

	}

	private void switchDev(String flag) {
		if (flag.equals("last")) {
			try {

				String nameString = "";
				recordBoolean = false;
				// vv = new VView(ShowVideo.this);
				isWaiting = true;
				loadingFlag = true;
				switchTimerCount = 0;

				vv.videoStop();
				vv.setStopStatus(true);
				vv.closeSocket();
				Thread.sleep(300);

				Bundle bundle = ShowVideo.this.getIntent().getExtras();
				ArrayList devList = bundle.getParcelableArrayList("videoList");

				// 循环查找上一个设备信息
				int i = 0;
				for (i = 0; i < devList.size(); i++) {
					StringBuffer sb = new StringBuffer("");
					HashMap<String, Object> map = (HashMap<String, Object>) devList
							.get(i);
					Integer no = Integer.parseInt(map.get("listNo").toString());
					if (listNo == 1) {
						if (no == listCount) {
							String online = map.get("stayLine").toString();
							if (online.equals("1")) {
								listNo = no;
								nameString = map.get("chName").toString();
								ptz = map.get("ptz").toString();
								zoom = map.get("zoom").toString();
								talk = map.get("talk").toString();
								width = Integer.parseInt(map.get("width")
										.toString());// 获取传过来的视频宽度
								height = Integer.parseInt(map.get("height")
										.toString());// 获取传过来的视频高度
								adapterID = Integer.parseInt(map.get(
										"adapterId").toString());// 获取适配ID
								chNo = Integer.parseInt(map.get("chNo")
										.toString());// 获取通道ID
								videoString = map.get("devId").toString();// 获取设备序列号
								break;
							} else {
								listNo = no;
								i = -1;
							}
						}
					} else {
						if (no == listNo - 1) {
							String online = map.get("stayLine").toString();
							if (online.equals("1")) {
								listNo = no;
								nameString = map.get("chName").toString();
								ptz = map.get("ptz").toString();
								zoom = map.get("zoom").toString();
								talk = map.get("talk").toString();
								width = Integer.parseInt(map.get("width")
										.toString());// 获取传过来的视频宽度
								height = Integer.parseInt(map.get("height")
										.toString());// 获取传过来的视频高度
								adapterID = Integer.parseInt(map.get(
										"adapterId").toString());// 获取适配ID
								chNo = Integer.parseInt(map.get("chNo")
										.toString());// 获取通道ID
								videoString = map.get("devId").toString();// 获取设备序列号
								break;
							} else {
								listNo = no;
								i = -1;
							}
						}
					}

				}

				if (ptz.equals("1")) {
					upButton.setVisibility(Button.VISIBLE);
					downButton.setVisibility(Button.VISIBLE);
					leftButton.setVisibility(Button.VISIBLE);
					rightButton.setVisibility(Button.VISIBLE);
				} else if (ptz.equals("0")) {
					upButton.setVisibility(Button.GONE);
					downButton.setVisibility(Button.GONE);
					leftButton.setVisibility(Button.GONE);
					rightButton.setVisibility(Button.GONE);
				}

				if (zoom.equals("1")) {
					zoominButton.setVisibility(Button.VISIBLE);
					zoomoutButton.setVisibility(Button.VISIBLE);
					focusinButton.setVisibility(Button.VISIBLE);
					focusoutButton.setVisibility(Button.VISIBLE);
				} else if (zoom.equals("0")) {
					zoominButton.setVisibility(Button.GONE);
					zoomoutButton.setVisibility(Button.GONE);
					focusinButton.setVisibility(Button.GONE);
					focusoutButton.setVisibility(Button.GONE);
				}
				if (talk.equals("1")) {
					talkButton.setVisibility(Button.VISIBLE);
				} else if (talk.equals("0")) {
					talkButton.setVisibility(Button.GONE);
				}

				switchTimer = new Timer();
				showWait("切换到设备:" + nameString + "");
				swiThread = new Thread(switchThread);
				swiThread.start();

				Display display = getWindowManager().getDefaultDisplay();
				bitmap = Bitmap.createBitmap(display.getWidth(),
						display.getHeight(), Config.RGB_565);

				if (ptz.equals("1")) {
					byteFlag = ptzFlag;
				} else {
					byteFlag = noPtzFlag;
				}

				Thread thread = new Thread(new Runnable() {
					@Override
					public void run() {
						/*
						 * 加载网络图片 load form url
						 */
						vv.InitVideoParam("", (int) width, (int) height,
								byteFlag);
						vv.NetTest(socketIp, 9332, videoString, chNo, adapterID);
						vv.PlayVideo(screenWidth, screenHeight);

						handler.sendEmptyMessage(1);
					}
				});
				thread.start();

				ifFavor = "0";
				// 数据库
				Cursor cur = db.select_favor();
				while (cur.moveToNext()) {
					int webIndex = cur.getColumnIndex("DATA_WEBID");
					webId = cur.getString(webIndex);
					int userIndex = cur.getColumnIndex("DATA_USERID");
					String webUserId = cur.getString(userIndex);
					int devIndex = cur.getColumnIndex("DATA_DEVID");
					String favorDevId = cur.getString(devIndex);
					int chIndex = cur.getColumnIndex("DATA_CHNO");
					String favorChNo = cur.getString(chIndex);
					if (favorDevId.equals(videoString)
							&& favorChNo.equals(Integer.toString(chNo))
							&& webUserId.equals(userId)) {
						ifFavor = "1";
					}
				}
				if (ifFavor.equals("1")) {
					favorButton
							.setBackgroundResource(R.drawable.icon_favorpress);
				} else if (ifFavor.equals("0")) {
					favorButton.setBackgroundResource(R.drawable.icon_favor);
				}

			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			}
		} else if (flag.equals("next")) {

			try {
				String nameString = "";
				recordBoolean = false;
				// vv = new VView(ShowVideo.this);
				isWaiting = true;
				loadingFlag = true;
				switchTimerCount = 0;

				vv.videoStop();
				vv.setStopStatus(true);
				vv.closeSocket();
				Thread.sleep(300);

				Bundle bundle = ShowVideo.this.getIntent().getExtras();
				ArrayList devList = bundle.getParcelableArrayList("videoList");

				// 循环查找下一个设备信息
				int i = 0;
				for (i = 0; i < devList.size(); i++) {
					StringBuffer sb = new StringBuffer("");
					HashMap<String, Object> map = (HashMap<String, Object>) devList
							.get(i);
					Integer no = Integer.parseInt(map.get("listNo").toString());
					if (listNo == listCount) {
						if (no == 1) {
							String online = map.get("stayLine").toString();
							if (online.equals("1")) {
								listNo = no;
								nameString = map.get("chName").toString();
								ptz = map.get("ptz").toString();
								zoom = map.get("zoom").toString();
								talk = map.get("talk").toString();
								width = Integer.parseInt(map.get("width")
										.toString());// 获取传过来的视频宽度
								height = Integer.parseInt(map.get("height")
										.toString());// 获取传过来的视频高度
								adapterID = Integer.parseInt(map.get(
										"adapterId").toString());// 获取适配ID
								chNo = Integer.parseInt(map.get("chNo")
										.toString());// 获取通道ID
								videoString = map.get("devId").toString();// 获取设备序列号
								break;
							} else {
								listNo = no;
								i = -1;
							}
						}
					} else {
						if (no == listNo + 1) {
							String online = map.get("stayLine").toString();
							if (online.equals("1")) {
								listNo = no;
								nameString = map.get("chName").toString();
								ptz = map.get("ptz").toString();
								zoom = map.get("zoom").toString();
								talk = map.get("talk").toString();
								width = Integer.parseInt(map.get("width")
										.toString());// 获取传过来的视频宽度
								height = Integer.parseInt(map.get("height")
										.toString());// 获取传过来的视频高度
								adapterID = Integer.parseInt(map.get(
										"adapterId").toString());// 获取适配ID
								chNo = Integer.parseInt(map.get("chNo")
										.toString());// 获取通道ID
								videoString = map.get("devId").toString();// 获取设备序列号
								break;
							} else {
								listNo = no;
								i = -1;
							}
						}
					}

				}

				if (ptz.equals("1")) {
					upButton.setVisibility(Button.VISIBLE);
					downButton.setVisibility(Button.VISIBLE);
					leftButton.setVisibility(Button.VISIBLE);
					rightButton.setVisibility(Button.VISIBLE);
				} else if (ptz.equals("0")) {
					upButton.setVisibility(Button.GONE);
					downButton.setVisibility(Button.GONE);
					leftButton.setVisibility(Button.GONE);
					rightButton.setVisibility(Button.GONE);
				}

				if (zoom.equals("1")) {
					zoominButton.setVisibility(Button.VISIBLE);
					zoomoutButton.setVisibility(Button.VISIBLE);
					focusinButton.setVisibility(Button.VISIBLE);
					focusoutButton.setVisibility(Button.VISIBLE);
				} else if (zoom.equals("0")) {
					zoominButton.setVisibility(Button.GONE);
					zoomoutButton.setVisibility(Button.GONE);
					focusinButton.setVisibility(Button.GONE);
					focusoutButton.setVisibility(Button.GONE);
				}
				if (talk.equals("1")) {
					talkButton.setVisibility(Button.VISIBLE);
				} else if (talk.equals("0")) {
					talkButton.setVisibility(Button.GONE);
				}
				switchTimer = new Timer();
				showWait("切换到设备:" + nameString + "");
				swiThread = new Thread(switchThread);
				swiThread.start();

				Display display = getWindowManager().getDefaultDisplay();
				bitmap = Bitmap.createBitmap(display.getWidth(),
						display.getHeight(), Config.RGB_565);

				if (ptz.equals("1")) {
					byteFlag = ptzFlag;
				} else {
					byteFlag = noPtzFlag;
				}
				Thread thread = new Thread(new Runnable() {
					@Override
					public void run() {
						/*
						 * 加载网络图片 load form url
						 */
						vv.InitVideoParam("", (int) width, (int) height,
								byteFlag);
						vv.NetTest(socketIp, 9332, videoString, chNo, adapterID);
						vv.PlayVideo(screenWidth, screenHeight);

						handler.sendEmptyMessage(1);
					}
				});
				thread.start();

				ifFavor = "0";
				// 数据库
				Cursor cur = db.select_favor();
				while (cur.moveToNext()) {
					int webIndex = cur.getColumnIndex("DATA_WEBID");
					webId = cur.getString(webIndex);
					int userIndex = cur.getColumnIndex("DATA_USERID");
					String webUserId = cur.getString(userIndex);
					int devIndex = cur.getColumnIndex("DATA_DEVID");
					String favorDevId = cur.getString(devIndex);
					int chIndex = cur.getColumnIndex("DATA_CHNO");
					String favorChNo = cur.getString(chIndex);
					if (favorDevId.equals(videoString)
							&& favorChNo.equals(Integer.toString(chNo))
							&& webUserId.equals(userId)) {
						ifFavor = "1";
					}
				}
				if (ifFavor.equals("1")) {
					favorButton
							.setBackgroundResource(R.drawable.icon_favorpress);
				} else if (ifFavor.equals("0")) {
					favorButton.setBackgroundResource(R.drawable.icon_favor);
				}
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			}
		}
	}

	// 录像提示
	Runnable recordWaitting = new Runnable() {
		@Override
		public void run() {
			Toast.makeText(ShowVideo.this, "开始录像", Toast.LENGTH_LONG).show();
		}

	};

	// 超时提示
	Runnable recordStop = new Runnable() {
		@Override
		public void run() {
			// Toast.makeText(ShowVideo.this, "录像已保存在" + recordPath,
			// Toast.LENGTH_LONG).show();
			Toast.makeText(ShowVideo.this, "录像文件已保存", Toast.LENGTH_LONG).show();
		}

	};

	// 晃动视频切换
	private void shakeDetector() {
		// 晃动
		mShaker = new ShakeListener(this);
		mShaker.setOnShakeListener(new ShakeListener.OnShakeListener() {
			public void onShake() {
				if (!isWaiting && !recordBoolean) { // 如果正在切换一个视频或者在录像，则不响应摇晃。
					try {

						String nameString = "";
						recordBoolean = false;
						// vv = new VView(ShowVideo.this);
						isWaiting = true;
						loadingFlag = true;
						switchTimerCount = 0;

						vv.videoStop();
						vv.setStopStatus(true);
						vv.closeSocket();
						Thread.sleep(300);

						Bundle bundle = ShowVideo.this.getIntent().getExtras();
						ArrayList devList = bundle
								.getParcelableArrayList("videoList");

						// 循环查找下一个设备信息
						int i = 0;
						for (i = 0; i < devList.size(); i++) {
							StringBuffer sb = new StringBuffer("");
							HashMap<String, Object> map = (HashMap<String, Object>) devList
									.get(i);
							Integer no = Integer.parseInt(map.get("listNo")
									.toString());
							if (listNo == listCount) {
								if (no == 1) {
									String online = map.get("stayLine")
											.toString();
									if (online.equals("1")) {
										listNo = no;
										nameString = map.get("chName")
												.toString();
										ptz = map.get("ptz").toString();
										zoom = map.get("zoom").toString();
										talk = map.get("talk").toString();
										width = Integer.parseInt(map.get(
												"width").toString());// 获取传过来的视频宽度
										height = Integer.parseInt(map.get(
												"height").toString());// 获取传过来的视频高度
										adapterID = Integer.parseInt(map.get(
												"adapterId").toString());// 获取适配ID
										chNo = Integer.parseInt(map.get("chNo")
												.toString());// 获取通道ID
										videoString = map.get("devId")
												.toString();// 获取设备序列号
										break;
									} else {
										listNo = no;
										i = -1;
									}
								}
							} else {
								if (no == listNo + 1) {
									String online = map.get("stayLine")
											.toString();
									if (online.equals("1")) {
										listNo = no;
										nameString = map.get("chName")
												.toString();
										ptz = map.get("ptz").toString();
										zoom = map.get("zoom").toString();
										talk = map.get("talk").toString();
										width = Integer.parseInt(map.get(
												"width").toString());// 获取传过来的视频宽度
										height = Integer.parseInt(map.get(
												"height").toString());// 获取传过来的视频高度
										adapterID = Integer.parseInt(map.get(
												"adapterId").toString());// 获取适配ID
										chNo = Integer.parseInt(map.get("chNo")
												.toString());// 获取通道ID
										videoString = map.get("devId")
												.toString();// 获取设备序列号
										break;
									} else {
										listNo = no;
										i = -1;
									}
								}
							}

						}

						if (ptz.equals("1")) {
							upButton.setVisibility(Button.VISIBLE);
							downButton.setVisibility(Button.VISIBLE);
							leftButton.setVisibility(Button.VISIBLE);
							rightButton.setVisibility(Button.VISIBLE);
						} else if (ptz.equals("0")) {
							upButton.setVisibility(Button.GONE);
							downButton.setVisibility(Button.GONE);
							leftButton.setVisibility(Button.GONE);
							rightButton.setVisibility(Button.GONE);
						}

						if (zoom.equals("1")) {
							zoominButton.setVisibility(Button.VISIBLE);
							zoomoutButton.setVisibility(Button.VISIBLE);
							focusinButton.setVisibility(Button.VISIBLE);
							focusoutButton.setVisibility(Button.VISIBLE);
						} else if (zoom.equals("0")) {
							zoominButton.setVisibility(Button.GONE);
							zoomoutButton.setVisibility(Button.GONE);
							focusinButton.setVisibility(Button.GONE);
							focusoutButton.setVisibility(Button.GONE);
						}

						if (talk.equals("1")) {
							talkButton.setVisibility(Button.VISIBLE);
						} else if (talk.equals("0")) {
							talkButton.setVisibility(Button.GONE);
						}
						switchTimer = new Timer();
						showWait("切换到设备:" + nameString + "");
						swiThread = new Thread(switchThread);
						swiThread.start();

						Display display = getWindowManager()
								.getDefaultDisplay();
						bitmap = Bitmap.createBitmap(display.getWidth(),
								display.getHeight(), Config.RGB_565);

						if (ptz.equals("1")) {
							byteFlag = ptzFlag;
						} else {
							byteFlag = noPtzFlag;
						}
						Thread thread = new Thread(new Runnable() {
							@Override
							public void run() {
								/*
								 * 加载网络图片 load form url
								 */

								vv.InitVideoParam("", (int) width,
										(int) height, byteFlag);
								vv.NetTest(socketIp, 9332, videoString, chNo,
										adapterID);

								vv.PlayVideo(screenWidth, screenHeight);

								handler.sendEmptyMessage(1);
							}
						});
						thread.start();

						ifFavor = "0";
						// 数据库
						Cursor cur = db.select_favor();
						while (cur.moveToNext()) {
							int webIndex = cur.getColumnIndex("DATA_WEBID");
							webId = cur.getString(webIndex);
							int userIndex = cur.getColumnIndex("DATA_USERID");
							String webUserId = cur.getString(userIndex);
							int devIndex = cur.getColumnIndex("DATA_DEVID");
							String favorDevId = cur.getString(devIndex);
							int chIndex = cur.getColumnIndex("DATA_CHNO");
							String favorChNo = cur.getString(chIndex);
							if (favorDevId.equals(videoString)
									&& favorChNo.equals(Integer.toString(chNo))
									&& webUserId.equals(userId)) {
								ifFavor = "1";
							}
						}
						if (ifFavor.equals("1")) {
							favorButton
									.setBackgroundResource(R.drawable.icon_favorpress);
						} else if (ifFavor.equals("0")) {
							favorButton
									.setBackgroundResource(R.drawable.icon_favor);
						}
					} catch (Exception e) {
						// TODO: handle exception
						e.printStackTrace();
					}
				}
			}
		});
	}

	// 退出后停止摇晃监测
	// public void onPause() {
	// mShaker.pause();
	// super.onPause();
	// }

	// 切换提示框操作
	private void SwitchTimerCount(Timer t) {
		Boolean status = vv.getViewStatus();
		if (status) {
			waitClose();
			isWaiting = false;
			loadingFlag = false;
			t.cancel();
			vv.setStopStatus(false);
		} else {
			if (loadingFlag) {
				switchTimerCount++;
				if (switchTimerCount > 240) {
					t.cancel();
					waitClose();
					if (isWaiting) {
						// showWait("网络不稳定，视频暂无响应");
						// try {
						// Thread.sleep(3000);
						// waitClose();
						// isWaiting = false;
						// loadingFlag = false;
						// } catch (InterruptedException e) {
						// e.printStackTrace();
						// }

						isWaiting = false;
						loadingFlag = false;
						new Thread() {
							public void run() {
								handler.post(runnableWaitting);
							}
						}.start();
					}
				}
			} else {
				t.cancel();
			}
		}

	}

	// 超时提示
	Runnable runnableWaitting = new Runnable() {
		@Override
		public void run() {
			Toast.makeText(ShowVideo.this, "网络不稳定，视频暂无响应", Toast.LENGTH_SHORT)
					.show();
		}

	};

	private void SwitchTimerOut() {
		TimerTask timerTask = new TimerTask() {
			@Override
			public void run() {
				SwitchTimerCount(switchTimer);
			}
		};
		switchTimer.schedule(timerTask, 0, 1000);
	}

	Runnable switchThread = new Runnable() {
		public void run() {
			if (isSwitching) {
				vv.setViewStatusFalse();
				SwitchTimerOut();
			}
		}
	};

	// 等待提示框操作
	private void TimerCount(Timer t) {
		Boolean status = vv.getViewStatus();

		if (status) {
			waitClose();
			handlerTraffic.postDelayed(runnable, 1000);
			isWaiting = false;
			t.cancel();
			loadingFlag = false;
			vv.setStopStatus(false);

			new Thread() {
				public void run() {
					handler.post(runnableDrawWidget);
				}
			}.start();

			handler.sendEmptyMessage(1);
			// 开始监测晃动
			shakeDetector();
			// 心跳包
			// heartTimer = new Timer();
			// heartBeatThread = new Thread(heartThread);
			// heartBeatThread.start();
		} else {
			if (loadingFlag) {
				timerCount++;
				if (timerCount > 240) {
					t.cancel();
					waitClose();
					if (isWaiting) {
						// showWait("网络不稳定，视频暂无响应");
						// try {
						// Thread.sleep(3000);
						// waitClose();
						// isWaiting = false;
						// loadingFlag = false;
						// // 开始监测晃动
						// shakeDetector();
						// } catch (InterruptedException e) {
						// e.printStackTrace();
						// }

						try {
							isWaiting = false;
							loadingFlag = false;
							new Thread() {
								public void run() {
									handler.post(runnableWaitting);
								}
							}.start();
							// 开始监测晃动
							// shakeDetector();
						} catch (Exception e) {
							// TODO: handle exception
							e.printStackTrace();
						}
					}
				}
			} else {
				t.cancel();
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

	// 心跳包

	private void heartTimerCount(Timer t) {
		if (isAlive) {
			vv.heartBeat();
		} else {
			t.cancel();
		}

	}

	private void heartTimerOut() {
		TimerTask timerTask = new TimerTask() {
			@Override
			public void run() {
				heartTimerCount(heartTimer);
			}
		};
		heartTimer.schedule(timerTask, 0, 10000);
	}

	Runnable heartThread = new Runnable() {
		public void run() {
			if (isAlive) {
				heartTimerOut();
			}
		}
	};

	// 超时提示
	Runnable recording = new Runnable() {
		@Override
		public void run() {
			Toast.makeText(ShowVideo.this, "正在录像中，不可切换通道", Toast.LENGTH_SHORT)
					.show();
		}

	};

	// 超时提示
	Runnable right = new Runnable() {
		@Override
		public void run() {
			Toast.makeText(ShowVideo.this, "向右", Toast.LENGTH_SHORT).show();
		}

	};

	private class MyGestureListener extends
			GestureDetector.SimpleOnGestureListener {
		@Override
		public boolean onDoubleTap(MotionEvent e) {

			if (ifFling) {
				goToZoomPage();
				ifFling = false;
			} else {
				goToSwicherPage();
				ifFling = true;
			}
			return ifFling;
		}

		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
				float velocityY) {

			if (!isWaiting && !recordBoolean) { // 如果正在切换一个视频或者在录像，则不响应摇晃。
				if (ifFling) {
					try {
						if (e1.getX() - e2.getX() > 120) {
							if (!recordBoolean) {
								switchDev("next");
							} else {
								new Thread() {
									public void run() {
										handler.post(recording);
									}
								}.start();
							}
							return true;
						} else if (e1.getX() - e2.getX() < -120) {
							if (!recordBoolean) {
								switchDev("last");
							} else {
								new Thread() {
									public void run() {
										handler.post(recording);
									}
								}.start();
							}
							return true;
						}
					} catch (Exception e) {
						// TODO: handle exception
						e.printStackTrace();
					}

				}
			}
			return true;
		}
	}

	// @Override
	// protected void onResume() {
	// // TODO Auto-generated method stub
	// super.onResume();
	// vv.setBackRunning(false);
	// }
	// //
	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		// 退出，则设置退出属性为ture
		if (isAlive) {
			vv.videoStop();
			vv.setStopStatus(true);
			vv.setHeart(false);

			vv.closeSocket();
			waitClose();
			isWaiting = false;
			isAlive = false;// 是否观看视频
			try {
				mShaker.pause();
			} catch (Exception e) {
				// TODO: handle exception
			}
			finish();
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK
				&& event.getAction() == KeyEvent.ACTION_DOWN) {

			if (!recordBoolean) {
				// 退出，则设置退出属性为ture
				vv.videoStop();
				vv.setStopStatus(true);
				vv.setHeart(false);

				vv.closeSocket();
				waitClose();
				isWaiting = false;
				isAlive = false;// 是否观看视频
				try {
					mShaker.pause();
				} catch (Exception e) {
					// TODO: handle exception
				}

				finish();
			} else {
				Toast.makeText(this, "正在录像中，请勿退出！", Toast.LENGTH_SHORT).show();
			}
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	public void onPanelClosed(Panel panel) {
		String panelName = getResources().getResourceEntryName(panel.getId());
		Log.d("TestPanels", "Panel [" + panelName + "] closed");
	}

	public void onPanelOpened(Panel panel) {
		String panelName = getResources().getResourceEntryName(panel.getId());
		Log.d("TestPanels", "Panel [" + panelName + "] opened");
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		// TODO Auto-generated method stub
		return false;
	}

	// 截屏代码

	public static Bitmap getViewBitmap(View v) {
		v.clearFocus(); //
		v.setPressed(false); //
		// 能画缓存就返回false
		boolean willNotCache = v.willNotCacheDrawing();
		v.setWillNotCacheDrawing(false);
		int color = v.getDrawingCacheBackgroundColor();
		v.setDrawingCacheBackgroundColor(0);
		if (color != 0) {
			v.destroyDrawingCache();
		}
		v.buildDrawingCache();
		Bitmap cacheBitmap = v.getDrawingCache();
		if (cacheBitmap == null) {
			// Log.e(TAG, "failed getViewBitmap(" + v + ")", new
			// RuntimeException());
			return null;
		}
		Bitmap bitmap = Bitmap.createBitmap(cacheBitmap);
		// Restore the view
		v.destroyDrawingCache();
		v.setWillNotCacheDrawing(willNotCache);
		v.setDrawingCacheBackgroundColor(color);
		return bitmap;
	}

	// 保存到sdcard
	// savePic(getViewBitmap(v), "sdcard/xx.png");
	private static void savePic(Bitmap b, String strFileName) {
		FileOutputStream fos = null;
		try {
			String strFolder = checkSnapPath();

			fos = new FileOutputStream(strFolder + strFileName + ".PNG");
			if (null != fos) {
				b.compress(Bitmap.CompressFormat.PNG, 90, fos);
				fos.flush();
				fos.close();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static String checkSnapPath() {
		String strFolder = Environment.getExternalStorageDirectory().toString()
				+ "/MLVideo/";

		File file = new File(strFolder);

		if (!file.exists()) {
			file.mkdir();
		}

		strFolder = Environment.getExternalStorageDirectory().toString()
				+ "/MLVideo/Snap/";

		file = new File(strFolder);

		if (!file.exists()) {
			file.mkdir();
		}
		return strFolder;
	}

	// 流量
	long old_totalRx = 0;
	long old_totalTx = 0;
	long totalRx = 0;
	long totalTx = 0;

	int count = 0;

	private Handler handlerTraffic = new Handler();

	private Runnable runnable = new Runnable() {
		public void run() {
			this.update();
			handler.postDelayed(this, 1000);// 间隔1秒
			old_totalRx = TrafficStats.getTotalRxBytes();
			old_totalTx = TrafficStats.getTotalTxBytes();
		}

		void update() {
			// 刷新msg的内容
			TextView textTraffic = (TextView) findViewById(R.id.traffic);

			String nowTraffic;
			totalRx = TrafficStats.getTotalRxBytes();
			totalTx = TrafficStats.getTotalTxBytes();
			long mrx = totalRx - old_totalRx;
			old_totalRx = totalRx;
			long mtx = totalTx - old_totalTx;
			old_totalTx = totalTx;

			vv.setTraffic(TrafficMonitoring.convertLongTraffic(mrx));

			if (count > 0) {
				textTraffic.setText(TrafficMonitoring.convertTraffic(mrx)
						+ "/s");
			}
			count++;
		}

	};

}