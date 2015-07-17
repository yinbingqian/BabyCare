package com.lnpdit.garden;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.xmlpull.v1.XmlPullParserException;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.lnpdit.babycare.R;
import com.lnpdit.service.MessengerService;
import com.lnpdit.util.adapter.FoodAdapter;

public class GardenFoodActivity extends Activity implements OnTouchListener,
		GestureDetector.OnGestureListener, OnItemClickListener, OnClickListener {

	Context context;
	private Button gardenfood_back;
	private boolean hasMeasured = false;// 是否Measured.
	private LinearLayout layout_left;// 左边布局
	private ListView lv_news;
	public static final String SETTING_INFOS = "SETTING_Infos";
	public static final String BBGJ_USERINFO = "BBGJ_UserInfo";
	public static final String NAME = "name";
	private View view = null;// 点击的view
	String phonecall = "";
	String comId = "";

	ArrayList<HashMap<String, Object>> remoteWindowItem = null;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		context = this;
		setContentView(R.layout.gardenfood);
		InitView();
		getName();
		mGetTopicDataThread runnable = new mGetTopicDataThread();
		Thread thread = new Thread(runnable);
		thread.start();

	}

	void InitView() {
		gardenfood_back = (Button) this.findViewById(R.id.gardenfood_back);
		layout_left = (LinearLayout) findViewById(R.id.layout_left);
		lv_news = (ListView) findViewById(R.id.food_list);

		gardenfood_back.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				finish();
			}
		});

	}

	void getName() {
		SharedPreferences sp = getSharedPreferences(SETTING_INFOS, 0);
		phonecall = sp.getString(NAME, "");
		SharedPreferences sp1 = getSharedPreferences(BBGJ_USERINFO, 0);
		int comId_int = sp1.getInt("comId", 0);
		comId = String.valueOf(comId_int);
	}

	@SuppressLint("HandlerLeak")
	private class mGetTopicDataThread implements Runnable {

		String item = "";

		public void setParser(String _item) {
			item = _item;
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			String comurl = MessengerService.URL;
			String commethodname = MessengerService.METHOD_GETFOODFIRSTSHOW;
			String comnamespace = MessengerService.NAMESPACE;
			String comsoapaction = comnamespace + "/" + commethodname;

			SoapObject rpc = new SoapObject(comnamespace, commethodname);
			rpc.addProperty("phonecall", phonecall);
			rpc.addProperty("comId", comId);
			rpc.addProperty("item", "food");
			SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
					SoapEnvelope.VER11);
			envelope.bodyOut = rpc;
			envelope.dotNet = true;
			envelope.setOutputSoapObject(rpc);
			HttpTransportSE ht = new HttpTransportSE(comurl);
			ht.debug = true;
			try {
				ht.call(comsoapaction, envelope);

				remoteWindowItem = new ArrayList<HashMap<String, Object>>();

				SoapObject journal = (SoapObject) envelope.bodyIn;
				SoapObject soapchilds = (SoapObject) journal.getProperty(0);
				SoapObject soapchildss = (SoapObject) soapchilds.getProperty(1);
				SoapObject soapchildsss = (SoapObject) soapchildss
						.getProperty(0);

				// String foodid_str = "";
				// String week_str = "";
				// String food_str = "";
				// String laud_str = "";
				// String countcomment = "";
				// int signal = 1;
				for (int i = 0; i < soapchildsss.getPropertyCount(); i++) {
					SoapObject soapfinal = (SoapObject) soapchildsss
							.getProperty(i);

					String foodid = soapfinal.getProperty("foodId").toString();
					String comId = soapfinal.getProperty("comId").toString();
					String data = soapfinal.getProperty("data").toString();
					String week = soapfinal.getProperty("week").toString();
					String breakfast = soapfinal.getProperty("breakfast")
							.toString();
					String lunch = soapfinal.getProperty("lunch").toString();
					String diner = soapfinal.getProperty("diner").toString();
					String foodadd = soapfinal.getProperty("foodadd")
							.toString();
					String laud = soapfinal.getProperty("laud").toString();
					String countcomment = soapfinal.getProperty("countcomment")
							.toString();

					String food_str = "早餐：" + breakfast + "\n" + "午餐：" + lunch
							+ "\n" + "晚餐：" + diner + "\n" + "加餐：" + foodadd
							+ "\n";

					// if (!week.equals(week_str)) {
					// HashMap<String, Object> mapdevinfo = new HashMap<String,
					// Object>();
					// mapdevinfo.put("foodid_str", foodid_str);
					// mapdevinfo.put("week_str", week_str);
					// mapdevinfo.put("food_str", food_str);
					// mapdevinfo.put("laud", laud);
					// mapdevinfo.put("countcomment", countcomment);
					// if (!week_str.equals("")) {
					// remoteWindowItem.add(mapdevinfo);
					// }
					//
					// week_str = week;
					// foodid_str = foodid;
					// laud_str = laud;
					// food_str = "早餐：" + breakfast + "%" + "午餐：" + lunch + "%"
					// +"晚餐：" + diner + "%"+"加餐：" + foodadd + "%";
					//
					// } else {
					// foodid_str = foodid;
					// laud_str = laud;
					// food_str = food_str + "早餐：" + breakfast + "%" + "午餐：" +
					// lunch + "%" +"晚餐："
					// + diner + "%"+"加餐：" + foodadd + "%";
					// }
					HashMap<String, Object> mapdevinfo = new HashMap<String, Object>();
					mapdevinfo.put("foodid_str", foodid);
					mapdevinfo.put("week_str", week);
					mapdevinfo.put("food_str", food_str);
					mapdevinfo.put("laud", laud);
					mapdevinfo.put("countcomment", countcomment);
					remoteWindowItem.add(mapdevinfo);
				}

				Message msg = new Message();
				msg.arg1 = 1;
				msg.obj = remoteWindowItem;
				threadMessageHandler.sendMessage(msg);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (XmlPullParserException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Exception e) {
				// TODO: handle exception
				Log.e("TOPIC GET DATA ERROR : ", e.toString());
			}
		}
	}

	Handler threadMessageHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (msg.arg1 == 1) {
				ArrayList<HashMap<String, Object>> remoteWindowItem = (ArrayList<HashMap<String, Object>>) msg.obj;

				FoodAdapter displayAdapter = new FoodAdapter(context,
						remoteWindowItem, R.layout.list_in_food, new String[] {
								"week_str", "food_str", "laud", "countcomment",
								"foodid_str" }, new int[] { R.id.textview,
								R.id.textview, R.id.textview, R.id.textview },
						phonecall);
				lv_news.setAdapter(displayAdapter);
			}
		}
	};

	//
	// /***
	// * listview 正在滑动时执行.
	// */
	// void doScrolling(float distanceX) {
	// isScrolling = true;
	// mScrollX += distanceX;// distanceX:向左为正，右为负
	//
	// RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams)
	// layout_left
	// .getLayoutParams();
	// RelativeLayout.LayoutParams layoutParams_1 =
	// (RelativeLayout.LayoutParams) layout_right
	// .getLayoutParams();
	// layoutParams.leftMargin -= mScrollX;
	// layoutParams_1.leftMargin = window_width + layoutParams.leftMargin;
	// if (layoutParams.leftMargin >= 0) {
	// isScrolling = false;// 拖过头了不需要再执行AsynMove了
	// layoutParams.leftMargin = 0;
	// layoutParams_1.leftMargin = window_width;
	//
	// } else if (layoutParams.leftMargin <= -MAX_WIDTH) {
	// // 拖过头了不需要再执行AsynMove了
	// isScrolling = false;
	// layoutParams.leftMargin = -MAX_WIDTH;
	// layoutParams_1.leftMargin = window_width - MAX_WIDTH;
	// }
	// Log.v(TAG, "layoutParams.leftMargin=" + layoutParams.leftMargin
	// + ",layoutParams_1.leftMargin =" + layoutParams_1.leftMargin);
	//
	// layout_left.setLayoutParams(layoutParams);
	// layout_right.setLayoutParams(layoutParams_1);
	// }

	// void getMAX_WIDTH() {
	// ViewTreeObserver viewTreeObserver = layout_left.getViewTreeObserver();
	// // 获取控件宽度
	// viewTreeObserver.addOnPreDrawListener(new OnPreDrawListener() {
	// @Override
	// public boolean onPreDraw() {
	// if (!hasMeasured) {
	// window_width = getWindowManager().getDefaultDisplay()
	// .getWidth();
	// MAX_WIDTH = layout_right.getWidth();
	// RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams)
	// layout_left
	// .getLayoutParams();
	// RelativeLayout.LayoutParams layoutParams_1 =
	// (RelativeLayout.LayoutParams) layout_right
	// .getLayoutParams();
	// ViewGroup.LayoutParams layoutParams_2 = mylaout
	// .getLayoutParams();
	// // 注意： 设置layout_left的宽度。防止被在移动的时候控件被挤压
	// layoutParams.width = window_width;
	// layout_left.setLayoutParams(layoutParams);
	//
	// // 设置layout_right的初始位置.
	// layoutParams_1.leftMargin = window_width;
	// layout_right.setLayoutParams(layoutParams_1);
	// // 注意：设置lv_set的宽度防止被在移动的时候控件被挤压
	// layoutParams_2.width = MAX_WIDTH;
	// mylaout.setLayoutParams(layoutParams_2);
	//
	// Log.v(TAG, "MAX_WIDTH=" + MAX_WIDTH + "width="
	// + window_width);
	// hasMeasured = true;
	// }
	// return true;
	// }
	// });
	//
	// }
	//
	// @Override
	// public boolean onKeyDown(int keyCode, KeyEvent event) {
	// if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
	// RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams)
	// layout_left
	// .getLayoutParams();
	// if (layoutParams.leftMargin < 0) {
	// new AsynMove().execute(SPEED);
	// return false;
	// }
	// }
	// return super.onKeyDown(keyCode, event);
	// }

	// @Override
	// public boolean onTouch(View v, MotionEvent event) {
	//
	// view = v;// 记录点击的控件
	//
	// // 松开的时候要判断，如果不到半屏幕位子则缩回去，
	// if (MotionEvent.ACTION_UP == event.getAction() && isScrolling == true) {
	// RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams)
	// layout_left
	// .getLayoutParams();
	// // 缩回去
	// if (layoutParams.leftMargin < -window_width / 3) {
	// new AsynMove().execute(-SPEED);
	// } else {
	// new AsynMove().execute(SPEED);
	// }
	// }
	//
	// return mGestureDetector.onTouchEvent(event);
	// }

	// @Override
	// public boolean onDown(MotionEvent e) {
	//
	// int position = lv_set.pointToPosition((int) e.getX(), (int) e.getY());
	// if (position != ListView.INVALID_POSITION) {
	// View child = lv_set.getChildAt(position
	// - lv_set.getFirstVisiblePosition());
	// if (child != null)
	// child.setPressed(true);
	// }
	//
	// mScrollX = 0;
	// isScrolling = false;
	// // 将之改为true，才会传递给onSingleTapUp,不然事件不会向下传递.
	// return true;
	// }

	@Override
	public void onShowPress(MotionEvent e) {

	}

	/***
	 * 点击松开执行
	 */
	// @Override
	// public boolean onSingleTapUp(MotionEvent e) {
	// // 点击的不是layout_left
	// if (view != null && view == iv_set) {
	// RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams)
	// layout_left
	// .getLayoutParams();
	// // 左移动
	// if (layoutParams.leftMargin >= 0) {
	// new AsynMove().execute(-SPEED);
	// lv_set.setSelection(0);// 设置为首位.
	// } else {
	// // 右移动
	// new AsynMove().execute(SPEED);
	// }
	// } else if (view != null && view == layout_left) {
	// RelativeLayout.LayoutParams layoutParams =
	// (android.widget.RelativeLayout.LayoutParams) layout_left
	// .getLayoutParams();
	// if (layoutParams.leftMargin < 0) {
	// // 说明layout_left处于移动最左端状态，这个时候如果点击layout_left应该直接所以原有状态.(更人性化)
	// // 右移动
	// new AsynMove().execute(SPEED);
	// }
	// }
	//
	// return true;
	// }

	/***
	 * 滑动监听 就是一个点移动到另外一个点. distanceX=后面点x-前面点x，如果大于0，说明后面点在前面点的右边及向右滑动
	 */
	// @Override
	// public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
	// float distanceY) {
	// // 执行滑动.
	// doScrolling(distanceX);
	// return false;
	// }

	@Override
	public void onLongPress(MotionEvent e) {

	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		return false;
	}

	// class AsynMove extends AsyncTask<Integer, Integer, Void> {
	//
	// @Override
	// protected Void doInBackground(Integer... params) {
	// int times = 0;
	// if (MAX_WIDTH % Math.abs(params[0]) == 0)// 整除
	// times = MAX_WIDTH / Math.abs(params[0]);
	// else
	// times = MAX_WIDTH / Math.abs(params[0]) + 1;// 有余数
	//
	// for (int i = 0; i < times; i++) {
	// publishProgress(params[0]);
	// try {
	// Thread.sleep(sleep_time);
	// } catch (InterruptedException e) {
	// e.printStackTrace();
	// }
	// }
	//
	// return null;
	// }

	/**
	 * update UI
	 */
	// @Override
	// protected void onProgressUpdate(Integer... values) {
	// RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams)
	// layout_left
	// .getLayoutParams();
	// RelativeLayout.LayoutParams layoutParams_1 =
	// (RelativeLayout.LayoutParams) layout_right
	// .getLayoutParams();
	// // 右移动
	// if (values[0] > 0) {
	// layoutParams.leftMargin = Math.min(layoutParams.leftMargin
	// + values[0], 0);
	// layoutParams_1.leftMargin = Math.min(layoutParams_1.leftMargin
	// + values[0], window_width);
	// Log.v(TAG, "layout_left右" + layoutParams.leftMargin
	// + ",layout_right右" + layoutParams_1.leftMargin);
	// } else {
	// // 左移动
	// layoutParams.leftMargin = Math.max(layoutParams.leftMargin
	// + values[0], -MAX_WIDTH);
	// layoutParams_1.leftMargin = Math.max(layoutParams_1.leftMargin
	// + values[0], window_width - MAX_WIDTH);
	// Log.v(TAG, "layout_left左" + layoutParams.leftMargin
	// + ",layout_right左" + layoutParams_1.leftMargin);
	// }
	// layout_right.setLayoutParams(layoutParams_1);
	// layout_left.setLayoutParams(layoutParams);
	//
	// }
	//
	// }

	// @Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) layout_left
				.getLayoutParams();
		// 只要没有滑动则都属于点击
		// Toast.makeText(context, title[position], 1).show();

		// dn_text.setText(remoteWindowItem.get(position).get("Class").toString());
		// dept_id = remoteWindowItem.get(position).get("Id").toString();
		// mGetTopicDataThread runnable = new mGetTopicDataThread();
		// Thread thread = new Thread(runnable);
		// thread.start();
		//
		// if (layoutParams.leftMargin < 0) {
		// new AsynMove().execute(SPEED);
		// }
	}

	// @Override
	public void onClick(View v) {
		// TODO Auto-generated method stub

	}

	// }

	// @Override
	// public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long
	// arg3) {
	// // TODO Auto-generated method stub
	//
	// }

	@Override
	public boolean onDown(MotionEvent e) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
			float distanceY) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		// TODO Auto-generated method stub
		return false;
	}
}
