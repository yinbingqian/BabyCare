package com.lnpdit.monitor;

import java.io.File;
import java.sql.Date;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import org.videolan.libvlc.EventManager;
import org.videolan.libvlc.IVideoPlayer;
import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.LibVlcException;
import org.videolan.libvlc.MediaList;
import org.videolan.libvlc.Util;
import org.videolan.libvlc.WeakHandler;
import org.videolan.vlc.Media;
import org.videolan.vlc.MediaDatabase;

import com.lnpdit.babycare.R;
import com.lnpdit.photo.SimpleZoomListener;
import com.lnpdit.photo.ZoomState;
import com.lnpdit.sqllite.ToDoDB;
import com.lnpdit.util.ShakeDetector;
import com.lnpdit.util.ShakeListener;
import com.lnpdit.util.TrafficMonitoring;
import com.lnpdit.util.passwordInterface;
import com.mulong.android.RtspView;

import android.app.Activity;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ImageFormat;
import android.graphics.PixelFormat;
import android.graphics.Bitmap.Config;
import android.net.TrafficStats;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.Display;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.View.OnSystemUiVisibilityChangeListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.Toast;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class VideoPlayerActivity extends passwordInterface implements
		IVideoPlayer, OnClickListener {

	public final static String TAG = "DEBUG/VideoPlayerActivity";

	private SurfaceHolder surfaceHolder = null;
	private LibVLC mLibVLC = null;

	private FrameLayout mSurfaceFrame;
	private int mVideoHeight;
	private int mVideoWidth;
	private int mVideoVisibleHeight;
	private int mVideoVisibleWidth;
	private int mSarDen;
	private int mSarNum;
	private int mUiVisibility = -1;
	private static final int SURFACE_SIZE = 3;

	private static final int SURFACE_BEST_FIT = 0;
	private static final int SURFACE_FIT_HORIZONTAL = 1;
	private static final int SURFACE_FIT_VERTICAL = 2;
	private static final int SURFACE_FILL = 3;
	private static final int SURFACE_16_9 = 4;
	private static final int SURFACE_4_3 = 5;
	private static final int SURFACE_ORIGINAL = 6;
	private int mCurrentSize = SURFACE_BEST_FIT;

	// 视频加载计时器
	private int timerCount = 0;
	private Timer timer;
	private boolean isWaiting;
	private Thread threadLoading;
	private boolean isAlive = true;

	RtspView vv;
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
	private Button favorButton;
	String port = "";
	String ptz = "";
	String zoom = "";
	String talk = "";
	private String ifRecord;
	private String ifSnap;
	private String ifFavor = "0";
	private String userId;
	private String webId;
	private String videoString;

	String serverIp = "";// 获取传过来的管理平台IP
	String socketIp = "";// 获取传过来的转发IP
	int chCount = 0;
	int chNo = 0;
	int adapterID = 0;
	int listNo = 0;
	int listCount = 0;

	int byteFlag = 5;
	int noPtzFlag = 20;
	int ptzFlag = 5;

	// 图像缩放参数
	// private Bitmap bitmap;
	private ZoomState mZoomState;
	private SimpleZoomListener mZoomListener;

	// 数据库
	ToDoDB db;

	int width; // 此处设定初始分辨率
	int height;
	static int screenHeight;
	static int screenWidth;

	ProgressBar progressBar;
	TextView progressTextView;

	int waitTime = 0;

	// private ArrayList<Media> mMetadataCache;

	// private String[] mAudioTracks;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// 禁止锁屏
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.video_player);
		setupView();

		getVideoIntent();

		progressBar = (ProgressBar) findViewById(R.id.loadprocess);
		progressTextView = (TextView) findViewById(R.id.progresstext);

		if (Util.isICSOrLater())
			getWindow()
					.getDecorView()
					.findViewById(android.R.id.content)
					.setOnSystemUiVisibilityChangeListener(
							new OnSystemUiVisibilityChangeListener() {

								@Override
								public void onSystemUiVisibilityChange(
										int visibility) {
									if (visibility == mUiVisibility)
										return;
									setSurfaceSize(mVideoWidth, mVideoHeight,
											mVideoVisibleWidth,
											mVideoVisibleHeight, mSarNum,
											mSarDen);
									if (visibility == View.SYSTEM_UI_FLAG_VISIBLE) {
										Log.d(TAG, "onSystemUiVisibilityChange");
									}
									mUiVisibility = visibility;
								}
							});

		try {
			mLibVLC = mLibVLC.getInstance();
			// MediaList mediaList = new MediaList(mLibVLC);
			// mMetadataCache = new ArrayList<Media>();

			if (mLibVLC != null) {
				// String path = getIntent().getStringExtra("rtspUrl");

				// String pathUri = LibVLC.getInstance().nativeToURI(path);
				// enableIOMX(true);

				// mLibVLC.setIomx(true);

				String pathUri = getIntent().getStringExtra("rtspUrl");

				// if (pathUri.equals("rtsp://211.149.156.38:9000/47_0")
				// || pathUri.equals("rtsp://211.149.156.38:9000/47_5")) {
				// waitTime = 4000;
				//
				// } else {

				waitTime = 4000;
				// }
				System.out.println("WaitTime:" + waitTime);
				// MediaDatabase db = MediaDatabase
				// .getInstance(VideoPlayerActivity.this);
				// Media media = db.getMedia(VideoPlayerActivity.this,
				// pathUri);
				// if (media == null) {
				// media = new Media(pathUri, false);
				// }
				// mMetadataCache.add(media);
				// mediaList.add(pathUri, false);
				// mLibVLC.setMediaList(mediaList);

				// Add handler after loading the list

				// mLibVLC.playIndex(0);

				mLibVLC.readMediaML(pathUri);

				handler.sendEmptyMessageDelayed(0, 1000);
			}
		} catch (LibVlcException e) {
			e.printStackTrace();
		}

		mCurrentSize = 3;
		changeSurfaceSize();

		Display display = getWindowManager().getDefaultDisplay();
		screenWidth = display.getWidth();
		screenHeight = display.getHeight();

		vv = new RtspView(this);

		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				/*
				 * 加载网络图片 load form url
				 */
				vv.InitVideoParam("", (int) width, (int) height, byteFlag);
				vv.NetTest(socketIp, 9332, videoString, chNo, adapterID);
				vv.PlayVideo(screenWidth, screenHeight);
			}
		});
		thread.start();
		// 等待提示框
		loadingFlag = true;
		timer = new Timer();
		isWaiting = true;
		progressTextView.setText("视频载入中，请稍候...");
		threadLoading = new Thread(waitThread);
		threadLoading.start();

		// width = Integer.parseInt(this.getIntent().getStringExtra("width"));//
		// 获取传过来的视频宽度
		// height =
		// Integer.parseInt(this.getIntent().getStringExtra("height"));//
		// 获取传过来的视频高度
		//
		//
		// surfaceHolder.setFixedSize(width, height);
		// LayoutParams lp = surfaceView.getLayoutParams();
		// lp.width = screenWidth;
		// lp.height = screenHeight;
		// surfaceView.setLayoutParams(lp);
		// surfaceView.invalidate();

	}

	// private void enableIOMX(boolean enableIomx) {
	// SharedPreferences p = PreferenceManager
	// .getDefaultSharedPreferences(this);
	// Editor e = p.edit();
	// e.putBoolean("enable_iomx", enableIomx);
	// mLibVLC.restart(this);
	// }

	private void getVideoIntent() {
		// 原有视频播放参数获取
		socketIp = this.getIntent().getStringExtra("socketIp");// 获取传过来的设备端口
		port = this.getIntent().getStringExtra("port");// 获取传过来的设备端口

		userId = this.getIntent().getStringExtra("userId").toString();

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

	// 向视频上添加按钮等Layout
	Runnable runnableDrawWidget = new Runnable() {
		@Override
		public void run() {
			drawWidget();
		}

	};

	// 显示progressBar
	Runnable runnableShowProgress = new Runnable() {
		@Override
		public void run() {
			progressBar.setVisibility(ProgressBar.VISIBLE);
			progressTextView.setVisibility(TextView.VISIBLE);
		}

	};
	// 隐藏progressBar
	Runnable runnableHidProgress = new Runnable() {
		@Override
		public void run() {
			progressBar.setVisibility(ProgressBar.GONE);
			progressTextView.setVisibility(TextView.GONE);
		}

	};

	// 录像开始提示
	Runnable recordWaitting = new Runnable() {
		@Override
		public void run() {
			Toast.makeText(VideoPlayerActivity.this, "开始录像", Toast.LENGTH_LONG)
					.show();
		}

	};

	// 录像结束提示
	Runnable recordStop = new Runnable() {
		@Override
		public void run() {
			// Toast.makeText(ShowVideo.this, "录像已保存在" + recordPath,
			// Toast.LENGTH_LONG).show();
			Toast.makeText(VideoPlayerActivity.this, "录像文件已保存",
					Toast.LENGTH_LONG).show();
		}

	};

	Boolean status = false;

	// 等待提示框操作
	private void TimerCount(Timer t) {

		if (isAlive) {
			// vlcTime = mLibVLC.getTime();
			// System.out.println("vlcTime:" + vlcTime);
			// int playing = mLibVLC.getState();
			// System.out.println("isPlaying:" + playing);
			int videoStatus = mLibVLC.getVideoStatus();
			System.out.println("videoStatus:" + videoStatus);
			if (videoStatus == 1) {

				// vlcTime = mLibVLC.getTime();
				// System.out.println("vlcTime:" + vlcTime);
				// if (mLibVLC.isPlaying() && vlcTime != 0 && vlcTime != -1) {
				// waitClose();
				new Thread() {
					public void run() {
						try {
							Thread.sleep(waitTime);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						handler.post(runnableHidProgress);
						isWaiting = false;
						loadingFlag = false;

						new Thread() {
							public void run() {
								handler.post(runnableDrawWidget);
							}
						}.start();

						// 开始监测晃动
						// shakeDetector();
					}
				}.start();
				t.cancel();
				handlerTraffic.postDelayed(runnable, 1000);

			} else {
				if (loadingFlag) {
					timerCount++;
					if (timerCount > 240) {
						t.cancel();
						// waitClose();

						handler.post(runnableHidProgress);
						if (isWaiting) {
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

	// 超时提示
	Runnable runnableWaitting = new Runnable() {
		@Override
		public void run() {
			Toast.makeText(VideoPlayerActivity.this, "由于网络原因，暂时无法获取视频，请稍候再试！",
					Toast.LENGTH_SHORT).show();
		}

	};

	@Override
	public void onClick(View v) {

		switch (v.getId()) {
		case R.id.play:
			if (mLibVLC.isPlaying()) {
				mLibVLC.pause();
				v.setBackgroundResource(R.drawable.playbtn_icon);
			} else {
				mLibVLC.play();
				v.setBackgroundResource(R.drawable.icon_pause);
			}

			playBoolean = playOrPause(playBoolean);
			break;
		// case R.id.video_player_size:
		// if (mCurrentSize < SURFACE_ORIGINAL) {
		// mCurrentSize++;
		// } else {
		// mCurrentSize = 0;
		// }
		// changeSurfaceSize();
		// break;
		// 抓拍按钮
		case R.id.snap:
			if (playBoolean) {
				String strFolder = checkSnapPath();
				takeSnapShot(strFolder, screenWidth, screenHeight);
				Toast.makeText(this, "抓拍成功", Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(this, "视频已停止，无法抓拍！", Toast.LENGTH_SHORT).show();
			}
			break;
		// 录像按钮
		case R.id.record:

			if (playBoolean) {
				if (!recordBoolean) {
					new Thread() {
						public void run() {
							handler.post(recordWaitting);
						}
					}.start();

					String recordPath = checkRecordPath();
					mLibVLC.videoRecordStart(recordPath);

					v.setBackgroundResource(R.drawable.icon_recordpress);
					// Toast.makeText(this, "正在创建录像文件",
					// Toast.LENGTH_SHORT).show();
				} else {
					new Thread() {
						public void run() {
							handler.post(recordStop);
						}
					}.start();

					mLibVLC.videoRecordStop();

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
				// waitClose();

				handler.post(runnableHidProgress);
				isAlive = false;// 是否观看视频
				if (mLibVLC != null) {
					mLibVLC.stop();
				}
				finish();
			} else {
				Toast.makeText(this, "正在录像中，请勿退出！", Toast.LENGTH_SHORT).show();
			}
			break;
		}

	}

	private void drawWidget() {

		try {

			// btnSize.setVisibility(Button.VISIBLE);
			// btnPlayPause.setVisibility(Button.VISIBLE);

			Display display = getWindowManager().getDefaultDisplay();
			final LayoutInflater inflater = LayoutInflater.from(this);
			//
			// // 显示云台箭头
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
			//
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
					if (mLibVLC.isPlaying()) {
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

	private Boolean ifFling = true;

	public void goToZoomPage() {
		handler.sendEmptyMessage(0);
	}

	public void goToSwicherPage() {
		handler.sendEmptyMessage(1);
	}

	private class MyGestureListener extends
			GestureDetector.SimpleOnGestureListener {
		// @Override
		// public boolean onDoubleTap(MotionEvent e) {
		//
		// if (ifFling) {
		// goToZoomPage();
		// ifFling = false;
		// } else {
		// goToSwicherPage();
		// ifFling = true;
		// }
		// return ifFling;
		// }

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
										// handler.post(recording);
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
										// handler.post(recording);
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

	// 摇晃
	private ShakeDetector mShakeDetector;
	private ShakeListener mShaker;
	private Timer switchTimer;
	private boolean isSwitching = true;
	private Thread swiThread;
	private int switchTimerCount = 0;
	private String switchRtspUrl = "";

	private void switchDev(String flag) {

		Bundle bundle = VideoPlayerActivity.this.getIntent().getExtras();
		ArrayList devList = bundle.getParcelableArrayList("videoList");
		if (devList.size() == 1) {
			return;
		}

		if (flag.equals("last")) {
			try {

				String nameString = "";

				isWaiting = true;
				loadingFlag = true;

				switchTimerCount = 0;

				status = false;
				// surfaceView.draw(canvas);

				if (mLibVLC != null) {
					mLibVLC.stop();
				}

				Thread.sleep(300);

				// if (chNo.length() == 1) {
				// chNo = "0" + chNo;
				// }

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

								int addOneNo = chNo + 1;
								if (socketIp.contains(".net:")
										|| socketIp.contains(".com:")) {
									switchRtspUrl = "rtsp://" + socketIp + "/"
											+ videoString + "_" + addOneNo;
								} else {
									switchRtspUrl = "rtsp://" + socketIp
											+ ":554/" + videoString + "_"
											+ addOneNo;
								}
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

								int addOneNo = chNo + 1;
								if (socketIp.contains(".net:")
										|| socketIp.contains(".com:")) {
									switchRtspUrl = "rtsp://" + socketIp + "/"
											+ videoString + "_" + addOneNo;
								} else {
									switchRtspUrl = "rtsp://" + socketIp
											+ ":554/" + videoString + "_"
											+ addOneNo;
								}
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

				// showWait("切换到设备:" + nameString + "");
				progressTextView.setText("切换到设备:" + nameString);
				handler.post(runnableShowProgress);
				swiThread = new Thread(switchThread);
				swiThread.start();

				if (ptz.equals("1")) {
					byteFlag = ptzFlag;
				} else {
					byteFlag = noPtzFlag;
				}

				Thread thread = new Thread(new Runnable() {
					@Override
					public void run() {

						try {
							mLibVLC = LibVLC.getInstance();
						} catch (LibVlcException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

						// MediaList mediaList = new MediaList(mLibVLC);
						if (mLibVLC != null) {

							// MediaDatabase db = MediaDatabase
							// .getInstance(VideoPlayerActivity.this);
							// MediaDatabase db = MediaDatabase
							// .getInstance(VideoPlayerActivity.this);
							// Media media =
							// db.getMedia(VideoPlayerActivity.this, pathUri);
							// if (media == null) {
							// media = new Media(pathUri, false);
							// }
							// mMetadataCache.add(media);
							// mediaList.add(pathUri, false);
							// mLibVLC.setMediaList(mediaList);
							//
							// // Add handler after loading the list
							//
							// mLibVLC.playIndex(0);

							mLibVLC.readMediaML(switchRtspUrl);

							handler.sendEmptyMessageDelayed(0, 1000);
						}

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
				final String rtspUrl = "";
				String nameString = "";
				recordBoolean = false;
				// vv = new VView(ShowVideo.this);
				isWaiting = true;
				loadingFlag = true;
				switchTimerCount = 0;

				status = false;
				// surfaceView.draw(canvas);
				if (mLibVLC != null) {
					mLibVLC.stop();
				}

				Thread.sleep(300);

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

								int addOneNo = chNo + 1;
								if (socketIp.contains(".net:")
										|| socketIp.contains(".com:")) {
									switchRtspUrl = "rtsp://" + socketIp + "/"
											+ videoString + "_" + addOneNo;
								} else {
									switchRtspUrl = "rtsp://" + socketIp
											+ ":554/" + videoString + "_"
											+ addOneNo;
								}
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

								int addOneNo = chNo + 1;
								if (socketIp.contains(".net:")
										|| socketIp.contains(".com:")) {
									switchRtspUrl = "rtsp://" + socketIp + "/"
											+ videoString + "_" + addOneNo;
								} else {
									switchRtspUrl = "rtsp://" + socketIp
											+ ":554/" + videoString + "_"
											+ addOneNo;
								}
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
				// showWait("切换到设备:" + nameString + "");
				progressTextView.setText("切换到设备:" + nameString);
				handler.post(runnableShowProgress);
				swiThread = new Thread(switchThread);
				swiThread.start();

				if (ptz.equals("1")) {
					byteFlag = ptzFlag;
				} else {
					byteFlag = noPtzFlag;
				}
				Thread thread = new Thread(new Runnable() {
					@Override
					public void run() {

						try {
							mLibVLC = LibVLC.getInstance();
						} catch (LibVlcException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						MediaList mediaList = new MediaList(mLibVLC);
						if (mLibVLC != null) {
							// MediaDatabase db = MediaDatabase
							// .getInstance(VideoPlayerActivity.this);
							// MediaDatabase db = MediaDatabase
							// .getInstance(VideoPlayerActivity.this);
							// Media media =
							// db.getMedia(VideoPlayerActivity.this, pathUri);
							// if (media == null) {
							// media = new Media(pathUri, false);
							// }
							// mMetadataCache.add(media);
							// mediaList.add(pathUri, false);
							// mLibVLC.setMediaList(mediaList);
							//
							// // Add handler after loading the list
							//
							// mLibVLC.playIndex(0);

							mLibVLC.readMediaML(switchRtspUrl);

							handler.sendEmptyMessageDelayed(0, 1000);
						}

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

	// 晃动视频切换
	private void shakeDetector() {
		// 晃动
		mShaker = new ShakeListener(this);
		mShaker.setOnShakeListener(new ShakeListener.OnShakeListener() {
			public void onShake() {

				Bundle bundle = VideoPlayerActivity.this.getIntent()
						.getExtras();
				ArrayList devList = bundle.getParcelableArrayList("videoList");
				if (!isWaiting && !recordBoolean) { // 如果正在切换一个视频或者在录像，则不响应摇晃。
					try {
						System.out.println("shake");
						final String rtspUrl = "";
						String nameString = "";
						recordBoolean = false;
						// vv = new VView(ShowVideo.this);
						isWaiting = true;
						loadingFlag = true;
						switchTimerCount = 0;

						status = false;
						// surfaceView.draw(canvas);
						if (mLibVLC != null) {
							mLibVLC.stop();
						}

						Thread.sleep(300);

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

										int addOneNo = chNo + 1;
										if (socketIp.contains(".net:")
												|| socketIp.contains(".com:")) {
											switchRtspUrl = "rtsp://"
													+ socketIp + "/"
													+ videoString + "_"
													+ addOneNo;
										} else {
											switchRtspUrl = "rtsp://"
													+ socketIp + ":554/"
													+ videoString + "_"
													+ addOneNo;
										}
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

										int addOneNo = chNo + 1;
										if (socketIp.contains(".net")
												|| socketIp.contains(".com")) {
											switchRtspUrl = "rtsp://"
													+ socketIp + "/"
													+ videoString + "_"
													+ addOneNo;
										} else {
											switchRtspUrl = "rtsp://"
													+ socketIp + ":554/"
													+ videoString + "_"
													+ addOneNo;
										}
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
						// showWait("切换到设备:" + nameString + "");
						progressTextView.setText("切换到设备:" + nameString);
						handler.post(runnableShowProgress);
						swiThread = new Thread(switchThread);
						swiThread.start();

						if (ptz.equals("1")) {
							byteFlag = ptzFlag;
						} else {
							byteFlag = noPtzFlag;
						}
						Thread thread = new Thread(new Runnable() {
							@Override
							public void run() {

								try {
									mLibVLC = LibVLC.getInstance();
								} catch (LibVlcException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								// MediaList mediaList = new MediaList(mLibVLC);
								if (mLibVLC != null) {

									// MediaDatabase db = MediaDatabase
									// .getInstance(VideoPlayerActivity.this);
									// MediaDatabase db = MediaDatabase
									// .getInstance(VideoPlayerActivity.this);
									// Media media =
									// db.getMedia(VideoPlayerActivity.this,
									// pathUri);
									// if (media == null) {
									// media = new Media(pathUri, false);
									// }
									// mMetadataCache.add(media);
									// mediaList.add(pathUri, false);
									// mLibVLC.setMediaList(mediaList);
									//
									// // Add handler after loading the list
									//
									// mLibVLC.playIndex(0);

									mLibVLC.readMediaML(switchRtspUrl);

									handler.sendEmptyMessageDelayed(0, 1000);
								}

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

	long lastVlcTime = 0;
	long vlcTime = 0;

	// 切换提示框操作
	private void SwitchTimerCount(Timer t) {

		if (isAlive) {
			// vlcTime = mLibVLC.getTime();
			// System.out.println("vlcTime:" + vlcTime);
			// int playing = mLibVLC.getState();
			// System.out.println("isPlaying:" + playing);
			int videoStatus = mLibVLC.getVideoStatus();
			System.out.println("videoStatus:" + videoStatus);
			if (videoStatus == 1) {

				// vlcTime = mLibVLC.getTime();
				// System.out.println("vlcTime:" + vlcTime);
				// if (mLibVLC.isPlaying() && vlcTime != 0 && vlcTime != -1) {
				// waitClose();

				new Thread() {
					public void run() {
						try {
							Thread.sleep(waitTime);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						handler.post(runnableHidProgress);
						isWaiting = false;
						loadingFlag = false;
					}
				}.start();
				t.cancel();
			} else {
				if (loadingFlag) {
					switchTimerCount++;
					if (switchTimerCount > 240) {
						t.cancel();
						// waitClose();

						handler.post(runnableHidProgress);
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
	}

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

	private SurfaceView surfaceView = null;
	private FrameLayout mLayout;
	private TextView mTextTitle;

	// private Spinner mAudioTrackSpinner;

	private void setupView() {

		SharedPreferences pref = PreferenceManager
				.getDefaultSharedPreferences(this);

		surfaceView = (SurfaceView) findViewById(R.id.main_surface);
		surfaceHolder = surfaceView.getHolder();

		String chroma = pref.getString("chroma_format", "");
		if (Util.isGingerbreadOrLater() && chroma.equals("YV12")) {
			surfaceHolder.setFormat(ImageFormat.YV12);
		} else if (chroma.equals("RV16")) {
			surfaceHolder.setFormat(PixelFormat.RGB_565);
		} else {
			surfaceHolder.setFormat(PixelFormat.RGBX_8888);
		}
		surfaceHolder.addCallback(mSurfaceCallback);

		mSurfaceFrame = (FrameLayout) findViewById(R.id.player_surface_frame);
		mLayout = (FrameLayout) findViewById(R.id.video_player_overlay);
		mTextTitle = (TextView) findViewById(R.id.video_player_title);

		// btnPlayPause = (ImageView) findViewById(R.id.video_player_playpause);
		// btnSize = (ImageView) findViewById(R.id.video_player_size);
		// mTextTime = (TextView) findViewById(R.id.video_player_time);
		// mTextLength = (TextView) findViewById(R.id.video_player_length);
		// mSeekBar = (SeekBar) findViewById(R.id.video_player_seekbar);
		// mTextShowInfo = (TextView) findViewById(R.id.video_player_showinfo);
		//
		// mSeekBar.setOnSeekBarChangeListener(seekBarChangeListener);
		//
		// btnPlayPause.setOnClickListener(this);
		// btnSize.setOnClickListener(this);

		mTextTitle.setText(getIntent().getStringExtra("name"));

		// btnSize.setVisibility(Button.GONE);
		// mTextTime.setVisibility(TextView.GONE);
		// mTextLength.setVisibility(TextView.GONE);
		// mTextShowInfo.setVisibility(TextView.GONE);
		// btnPlayPause.setVisibility(Button.GONE);
		// mSeekBar.setVisibility(SeekBar.GONE);
	}

	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);

			int time = (int) mLibVLC.getTime();
			int length = (int) mLibVLC.getLength();
			// Log.d(TAG, "handleMessage length: " + length + "; time: " +
			// time);
			handler.sendEmptyMessageDelayed(0, 1000);

			if (msg.what == 0) {
				mZoomState = new ZoomState();
				mZoomListener = new SimpleZoomListener();
				mZoomListener.setZoomState(mZoomState);
				mZoomListener.setmGestureDetector(new GestureDetector(
						new MyGestureListener()));
				surfaceView.setOnTouchListener(mZoomListener);

				resetZoomState();
			} else if (msg.what == 1) {
				mZoomState = new ZoomState();
				mZoomListener = new SimpleZoomListener();
				mZoomListener.setZoomState(mZoomState);
				mZoomListener.setmGestureDetector(new GestureDetector(
						new MyGestureListener()));
				surfaceView.setOnTouchListener(mZoomListener);
			}
		}
	};

	private void resetZoomState() {
		mZoomState.setPanX(0.5f);
		mZoomState.setPanY(0.5f);
		mZoomState.setZoom(1f);
		mZoomState.notifyObservers();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		setSurfaceSize(mVideoWidth, mVideoHeight, mVideoVisibleHeight,
				mVideoVisibleWidth, mSarNum, mSarDen);
		super.onConfigurationChanged(newConfig);
	}

	@Override
	public void setSurfaceSize(int width, int height, int visible_width,
			int visible_height, int sar_num, int sar_den) {
		if (width * height == 0)
			return;

		// store video size
		mVideoHeight = height;
		mVideoWidth = width;
		mVideoVisibleHeight = visible_height;
		mVideoVisibleWidth = visible_width;
		mSarNum = sar_num;
		mSarDen = sar_den;
		Message msg = mHandler.obtainMessage(SURFACE_SIZE);
		mHandler.sendMessage(msg);
	}

	// public void setSurfaceSize(int width, int height) {
	// // store video size
	// mVideoHeight = height;
	// mVideoWidth = width;
	// Message msg = mHandler.obtainMessage(SURFACE_SIZE);
	// mHandler.sendMessage(msg);
	// }

	private final Handler mHandler = new VideoPlayerHandler(this);

	private static class VideoPlayerHandler extends
			WeakHandler<VideoPlayerActivity> {
		public VideoPlayerHandler(VideoPlayerActivity owner) {
			super(owner);
		}

		@Override
		public void handleMessage(Message msg) {
			VideoPlayerActivity activity = getOwner();
			if (activity == null) // WeakReference could be GC'ed early
				return;

			switch (msg.what) {
			case SURFACE_SIZE:
				activity.changeSurfaceSize();
				break;
			}
		}
	};

	private void changeSurfaceSize() {
		// get screen size
		int dw = getWindow().getDecorView().getWidth();
		int dh = getWindow().getDecorView().getHeight();

		// getWindow().getDecorView() doesn't always take orientation into
		// account, we have to correct the values
		boolean isPortrait = getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
		if (dw > dh && isPortrait || dw < dh && !isPortrait) {
			int d = dw;
			dw = dh;
			dh = d;
		}

		// sanity check
		if (dw * dh == 0 || mVideoWidth * mVideoHeight == 0) {
			Log.e(TAG, "Invalid surface size");
			return;
		}

		// compute the aspect ratio
		double ar, vw;
		double density = (double) mSarNum / (double) mSarDen;
		if (density == 1.0) {
			/* No indication about the density, assuming 1:1 */
			vw = mVideoVisibleWidth;
			ar = (double) mVideoVisibleWidth / (double) mVideoVisibleHeight;
		} else {
			/* Use the specified aspect ratio */
			vw = mVideoVisibleWidth * density;
			ar = vw / mVideoVisibleHeight;
		}

		// compute the display aspect ratio
		double dar = (double) dw / (double) dh;

		switch (mCurrentSize) {
		case SURFACE_BEST_FIT:
			if (dar < ar)
				dh = (int) (dw / ar);
			else
				dw = (int) (dh * ar);
			break;
		case SURFACE_FIT_HORIZONTAL:
			dh = (int) (dw / ar);
			break;
		case SURFACE_FIT_VERTICAL:
			dw = (int) (dh * ar);
			break;
		case SURFACE_FILL:
			break;
		case SURFACE_16_9:
			ar = 16.0 / 9.0;
			if (dar < ar)
				dh = (int) (dw / ar);
			else
				dw = (int) (dh * ar);
			break;
		case SURFACE_4_3:
			ar = 4.0 / 3.0;
			if (dar < ar)
				dh = (int) (dw / ar);
			else
				dw = (int) (dh * ar);
			break;
		case SURFACE_ORIGINAL:
			dh = mVideoVisibleHeight;
			dw = (int) vw;
			break;
		}

		// force surface buffer size
		surfaceHolder.setFixedSize(mVideoWidth, mVideoHeight);

		// set display size
		LayoutParams lp = surfaceView.getLayoutParams();
		lp.width = dw * mVideoWidth / mVideoVisibleWidth;
		lp.height = dh * mVideoHeight / mVideoVisibleHeight;
		surfaceView.setLayoutParams(lp);

		// set frame size (crop if necessary)
		lp = mSurfaceFrame.getLayoutParams();
		lp.width = dw;
		lp.height = dh;
		mSurfaceFrame.setLayoutParams(lp);

		surfaceView.invalidate();
	}

	/**
	 * attach and disattach surface to the lib
	 */
	private final SurfaceHolder.Callback mSurfaceCallback = new Callback() {
		@Override
		public void surfaceChanged(SurfaceHolder holder, int format, int width,
				int height) {
			if (format == PixelFormat.RGBX_8888)
				Log.d(TAG, "Pixel format is RGBX_8888");
			else if (format == PixelFormat.RGB_565)
				Log.d(TAG, "Pixel format is RGB_565");
			else if (format == ImageFormat.YV12)
				Log.d(TAG, "Pixel format is YV12");
			else
				Log.d(TAG, "Pixel format is other/unknown");
			mLibVLC.attachSurface(holder.getSurface(),
					VideoPlayerActivity.this, width, height);
		}

		@Override
		public void surfaceCreated(SurfaceHolder holder) {
		}

		@Override
		public void surfaceDestroyed(SurfaceHolder holder) {
			mLibVLC.detachSurface();
		}
	};

	private final Handler eventHandler = new VideoPlayerEventHandler(this);

	private static class VideoPlayerEventHandler extends
			WeakHandler<VideoPlayerActivity> {
		public VideoPlayerEventHandler(VideoPlayerActivity owner) {
			super(owner);
		}

		@Override
		public void handleMessage(Message msg) {
			VideoPlayerActivity activity = getOwner();
			if (activity == null)
				return;

			switch (msg.getData().getInt("event")) {
			case EventManager.MediaPlayerPlaying:
				Log.i(TAG, "MediaPlayerPlaying");
				break;
			case EventManager.MediaPlayerPaused:
				Log.i(TAG, "MediaPlayerPaused");
				break;
			case EventManager.MediaPlayerStopped:
				Log.i(TAG, "MediaPlayerStopped");
				break;
			case EventManager.MediaPlayerEndReached:
				Log.i(TAG, "MediaPlayerEndReached");
				activity.finish();
				break;
			case EventManager.MediaPlayerVout:
				activity.finish();
				break;
			default:
				Log.e(TAG, "Event not handled");
				break;
			}
			// activity.updateOverlayPausePlay();
		}
	}

	@Override
	protected void onDestroy() {
		if (mLibVLC != null) {
			mLibVLC.stop();
			System.out.println("onDestroystop");
		}

		EventManager em = EventManager.getInstance();
		em.removeHandler(eventHandler);
		System.out.println("eventHandler");

		super.onDestroy();
	};

	/**
	 * Convert time to a string
	 * 
	 * @param millis
	 *            e.g.time/length from file
	 * @return formated string (hh:)mm:ss
	 */
	public static String millisToString(long millis) {
		boolean negative = millis < 0;
		millis = java.lang.Math.abs(millis);

		millis /= 1000;
		int sec = (int) (millis % 60);
		millis /= 60;
		int min = (int) (millis % 60);
		millis /= 60;
		int hours = (int) millis;

		String time;
		DecimalFormat format = (DecimalFormat) NumberFormat
				.getInstance(Locale.US);
		format.applyPattern("00");
		if (millis > 0) {
			time = (negative ? "-" : "") + hours + ":" + format.format(min)
					+ ":" + format.format(sec);
		} else {
			time = (negative ? "-" : "") + min + ":" + format.format(sec);
		}
		return time;
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

			// waitClose();
			if (recordBoolean) {
				mLibVLC.videoRecordStop();
				System.out.println("videoRecordStop");
				try {
					Thread.sleep(300);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			handler.post(runnableHidProgress);
			System.out.println("onStoprunnableHidProgress");
			isWaiting = false;
			isAlive = false;// 是否观看视频

			finish();
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK
				&& event.getAction() == KeyEvent.ACTION_DOWN) {

			if (!recordBoolean) {
				// 退出，则设置退出属性为ture

				// waitClose();

				handler.post(runnableHidProgress);

				System.out.println("runnableHidProgress");
				isWaiting = false;
				isAlive = false;// 是否观看视频
				// if (mLibVLC != null) {
				// mLibVLC.stop();
				// }
				System.out.println("stop");

				finish();
				System.out.println("finish");
			} else {
				Toast.makeText(this, "正在录像中，请勿退出！", Toast.LENGTH_SHORT).show();
			}
			return true;
		}
		return super.onKeyDown(keyCode, event);
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

	// 添加收藏显示
	Runnable addFavorRunnable = new Runnable() {
		@Override
		public void run() {
			Toast.makeText(VideoPlayerActivity.this, "添加收藏！", Toast.LENGTH_LONG)
					.show();
		}

	};
	// 删除收藏显示
	Runnable deleteFavorRunnable = new Runnable() {
		@Override
		public void run() {
			Toast.makeText(VideoPlayerActivity.this, "删除收藏！", Toast.LENGTH_LONG)
					.show();
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

	// 抓拍

	public boolean takeSnapShot(String file, int width, int height) {

		return mLibVLC.takeSnapShot(0, file, width, height);
	}

	// 保存到sdcard
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

	private static String checkRecordPath() {
		String strFolder = Environment.getExternalStorageDirectory().toString()
				+ "/MLVideo/";

		File file = new File(strFolder);

		if (!file.exists()) {
			file.mkdir();
		}

		strFolder = Environment.getExternalStorageDirectory().toString()
				+ "/MLVideo/Record/";

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

			if (count > 0) {
				try {
					textTraffic.setText(TrafficMonitoring.convertTraffic(mrx)
							+ "/s");
				} catch (Exception e) {
					// TODO: handle exception
					e.printStackTrace();
				}
			}
			count++;
		}

	};

}
