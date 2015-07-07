/*****************Copyright (C), 2010-2015, FORYOU Tech. Co., Ltd.********************/
package com.lnpdit.monitor;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.lnpdit.babycare.R;
import com.lnpdit.file.FileInfo;
import com.lnpdit.file.FileUtil;
import com.lnpdit.photo.Constant;
import com.lnpdit.photo.Constant.ImageFolderInfo;
import com.lnpdit.photo.Constant.gridItemEntity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Bitmap.Config;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * @Filename: GridImageView.java
 * @Author: wanghb
 * @Email: wanghb@foryouge.com.cn
 * @CreateDate: 2011-7-14
 * @Description: description of the new class
 * @Others: comments
 * @ModifyHistory:
 */
public class SnapList extends Activity implements OnClickListener {
	private LayoutInflater mInflater;
	private int currentConlumID = -1;
	private int currentCount = 1;
	private int displayHeight;
	private LinearLayout data;

	static int screenHeight;
	static int screenWidth;
	private int itemh;
	private int itemw;

	private ArrayList<String> imagePathes;

	private boolean exit;
	private boolean isWait;

	private boolean firstRun = true;

	private final String TAG = "Main";
	private final int MENU_DELETE = Menu.FIRST + 5;
	private List<FileInfo> _files;
	private TextView snaplist_back;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.snaplist);

		Display display = getWindowManager().getDefaultDisplay();
		screenWidth = display.getWidth();
		screenHeight = display.getHeight();

		itemw = screenHeight / 5;
		itemh = screenWidth / 5;

		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		displayHeight = dm.heightPixels;

		mInflater = LayoutInflater.from(this);

		final String mCardPath = checkSnapPath();

		ImageFolderInfo holder = getFiles(mCardPath);

		imagePathes = holder.filePathes;
		data = (LinearLayout) findViewById(R.id.layout_webnav);
		// mAdapter.getFilePathes().set(index, object);

		snaplist_back = (TextView) findViewById(R.id.snaplist_back);
		snaplist_back.setOnClickListener(this);

	}

	private Thread mThread = new Thread() {
		public void run() {
			for (int i = 0; i < imagePathes.size() && !exit; i++) {
				if (isWait) {
					isWait = !isWait;
					synchronized (this) {
						try {
							this.wait();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
				String path = imagePathes.get(i);
				if (new File(path).exists()) {

					gridItemEntity gie = new gridItemEntity();
					Bitmap bm = getDrawable(i, 2);
					if (bm != null) {
						if (isWait) {
							isWait = !isWait;
							synchronized (this) {
								try {
									this.wait();
								} catch (InterruptedException e) {
									e.printStackTrace();
								}
							}
						}
						gie.image = new BitmapDrawable(bm);
						gie.path = path;
						gie.index = i;
						android.os.Message msg = new Message();
						msg = new Message();
						msg.what = 0;
						msg.obj = gie;
						mHandler.sendMessage(msg);
					}
				}
			}
		}
	};

	private Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case 0:
				gridItemEntity gie = (gridItemEntity) msg.obj;
				if (gie != null) {

					int num = displayHeight / itemh - 1;
					num = num == 0 ? 1 : num;

					LinearLayout ll;
					if ((currentCount - 1) % num > 0) {
						ll = (LinearLayout) data.findViewWithTag("columnId_"
								+ currentConlumID);
					} else {
						ll = (LinearLayout) mInflater.inflate(
								R.layout.snapcolumn, null);
						currentConlumID--;
						ll.setTag("columnId_" + currentConlumID);
						for (int j = 0; j < num; j++) {
							LinearLayout child = new LinearLayout(SnapList.this);
							child.setLayoutParams(new LayoutParams(itemw, itemh));
							child.setTag("item_" + j);
							ll.addView(child);
						}
						data.addView(ll);
					}

					int step = currentCount % num - 1;
					if (step == -1) {
						step = num - 1;
					}
					LinearLayout child = (LinearLayout) ll
							.findViewWithTag("item_" + step);
					// child.setBackgroundColor(R.color.bright_text_dark_focused);
					child.setBackgroundResource(R.drawable.grid_selector);
					child.setTag(gie);
					child.setOnClickListener(imageClick);
					child.setOnLongClickListener(longClickListener);
					child.setPadding(10, 10, 10, 10);
					//
					ImageView v = new ImageView(SnapList.this);
					v.setLayoutParams(new LayoutParams(itemw, itemh));
					v.setImageDrawable(gie.image);
					child.addView(v);
					currentCount++;
				}
				break;

			default:
				break;
			}
			// removeMessages(msg.what);
		}
	};

	private OnClickListener imageClick = new OnClickListener() {

		@Override
		public void onClick(View view) {
			gridItemEntity gie = (gridItemEntity) view.getTag();
			// Intent it = new Intent(GridImageView.this, ImageSwitcher.class);
			// it.putStringArrayListExtra("pathes", imagePathes);
			// it.putExtra("index", gie.index);
			// startActivity(it);

			String name = imagePathes.get(gie.index);
			Intent intent = new Intent(Intent.ACTION_VIEW);
			Uri mUri = Uri.parse("file://" + name);
			intent.setDataAndType(mUri, "image/*");
			startActivity(intent);

			if (mThread.isAlive()) {
				isWait = true;
			}
		}
	};

	// 一个特殊的对象数组的比较.使用冒泡法为原型.
	public static void sort(File[] array) {
		File temp = null;
		boolean condition = false;
		for (int i = 0; i < array.length; i++) {
			for (int j = array.length - 1; j > i; j--) {
				condition = array[j].lastModified() > array[j - 1]
						.lastModified();
				if (condition) {
					temp = array[j];
					array[j] = array[j - 1];
					array[j - 1] = temp;
				}
			}
		}
	}

	// 查看图片获取
	private ImageFolderInfo getFiles(String path) {
		File f = new File(path);
		File[] files = f.listFiles();
		if (files != null) {
			sort(files);
		}
		ImageFolderInfo ifi = new ImageFolderInfo();
		ifi.path = path;
		if (files != null) {
			for (int i = 0; i < files.length; i++) {
				final File ff = files[i];
				if (ff.isDirectory()) {
					getFiles(ff.getPath());
				} else {
					String fName = ff.getName();
					if (fName.indexOf(".") > -1) {
						String end = fName.substring(
								fName.lastIndexOf(".") + 1, fName.length())
								.toUpperCase();
						if (Constant.getExtens().contains(end)) {
							ifi.filePathes.add(ff.getPath());
						}
					}
				}
			}
		}
		if (!ifi.filePathes.isEmpty()) {
			ifi.pisNum = ifi.filePathes.size();
			String imagePath = ifi.filePathes.get(0);
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inSampleSize = 7;
			Bitmap bm = BitmapFactory.decodeFile(imagePath, options);
			ifi.image = new BitmapDrawable(bm);
		}
		return ifi;
	}

	protected OnLongClickListener longClickListener = new OnLongClickListener() {

		@Override
		public boolean onLongClick(View v) {
			// TODO Auto-generated method stub

			gridItemEntity gie = (gridItemEntity) v.getTag();

			String path = imagePathes.get(gie.index);
			final File f = new File(path);

			new AlertDialog.Builder(SnapList.this)
					.setTitle("提示")
					.setMessage("确定删除吗?")
					.setPositiveButton("确定",
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface arg0,
										int arg1) {
									FileUtil.deleteFile(f);

									// String mCardPath = checkSnapPath();
									// ImageFolderInfo holder =
									// getFiles(mCardPath);
									//
									// imagePathes = holder.filePathes;

									Intent intent = new Intent(SnapList.this,
											SnapList.class);
									startActivity(intent);
									SnapList.this.finish();
								}
							})
					.setNegativeButton("取消",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface arg0,
										int arg1) {
									// TODO Auto-generated method stub

								}
							}).show();
			return false;
		}
	};

	@Override
	protected void onResume() {
		if (mThread.isAlive()) {
			synchronized (mThread) {
				mThread.notify();
			}
		} else {
			if (firstRun) {
				firstRun = !firstRun;
				mThread.start();
			}
		}
		super.onResume();
	}

	@Override
	protected void onDestroy() {
		exit = true;
		super.onDestroy();
	}

	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.snaplist_back:

			this.finish();

			break;

		default:
			break;
		}
	}

	/** 重定义返回键事件 **/
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// 拦截back按键
		if (keyCode == KeyEvent.KEYCODE_BACK
				&& event.getAction() == KeyEvent.ACTION_DOWN) {
			finish();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	private Bitmap getDrawable(int index, int zoom) {
		if (index >= 0 && index < imagePathes.size()) {
			String path = imagePathes.get(index);

			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inJustDecodeBounds = true;
			BitmapFactory.decodeFile(path, options);
			int mWidth = options.outWidth;
			int mHeight = options.outHeight;
			int s = 1;
			while ((mWidth / s > itemw * 2 * zoom)
					|| (mHeight / s > itemh * 2 * zoom)) {
				s *= 2;
			}

			options = new BitmapFactory.Options();
			options.inSampleSize = s;
			options.inPreferredConfig = Config.ARGB_8888;
			Bitmap bm = BitmapFactory.decodeFile(path, options);

			if (bm != null) {
				int h = bm.getHeight();
				int w = bm.getWidth();

				float ft = (float) ((float) w / (float) h);
				float fs = (float) ((float) itemw / (float) itemh);

				int neww = ft >= fs ? itemw * zoom : (int) (itemh * zoom * ft);
				int newh = ft >= fs ? (int) (itemw * zoom / ft) : itemh * zoom;

				float scaleWidth = ((float) neww) / w;
				float scaleHeight = ((float) newh) / h;

				Matrix matrix = new Matrix();
				matrix.postScale(scaleWidth, scaleHeight);
				bm = Bitmap.createBitmap(bm, 0, 0, w, h, matrix, true);

				// Bitmap bm1 = Bitmap.createScaledBitmap(bm, w, h, true);
				// if (!bm.isRecycled()) {// 先判断图片是否已释放了
				// bm.recycle();
				// }
				return bm;
			}
		}
		return null;
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
}
