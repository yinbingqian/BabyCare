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
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.lnpdit.babycare.R;
import com.lnpdit.service.MessengerService;
import com.lnpdit.util.adapter.CourseAdapter;
import com.lnpdit.util.adapter.CourseMonthAdapter;
import com.lnpdit.widget.RightMenuLinearLayout;
import com.lnpdit.widget.RightMenuLinearLayout.OnScrollListener;

public class GardenCourseActivity extends Activity implements OnTouchListener,
		GestureDetector.OnGestureListener, OnItemClickListener, OnClickListener {

	// private WebView web_course;
	Context context;
	private Button gardencourse_back;
	private boolean hasMeasured = false;// �Ƿ�Measured.
	private LinearLayout layout_left;// ��߲���
	private LinearLayout layout_right;// �ұ߲���
	private ImageView iv_set;// ͼƬ
	private ListView lv_set;// ���ò˵�
	private ListView lv_news;

	public static final String BBGJ_USERINFO = "BBGJ_UserInfo";
	public static final String NAME = "name";

	/** ÿ���Զ�չ��/�����ķ�Χ */
	private int MAX_WIDTH = 0;
	/** ÿ���Զ�չ��/�������ٶ� */
	private final static int SPEED = 30;

	private final static int sleep_time = 5;

	private GestureDetector mGestureDetector;// ����
	private boolean isScrolling = false;
	private float mScrollX; // ���黬������
	private int window_width;// ��Ļ�Ŀ��

	private String TAG = "jj";

	private View view = null;// �����view
	String comId = "";

	private RightMenuLinearLayout mylaout;
	private RightMenuLinearLayout mywebviewlayout;
	ArrayList<HashMap<String, Object>> remoteWindowItem = null;
	ArrayList<HashMap<String, Object>> remoteWindowItem1 = null;
	ArrayList<String> data_array = new ArrayList<String>();

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.gardencourse);
		context = this;

		InitView();
		getName();

		mGetDisplayDataThread runnable = new mGetDisplayDataThread();
		Thread thread = new Thread(runnable);
		thread.start();
	}

	void InitView() {
		gardencourse_back = (Button) this.findViewById(R.id.gardencourse_back);
		layout_left = (LinearLayout) findViewById(R.id.layout_left);
		layout_right = (LinearLayout) findViewById(R.id.layout_right);
		lv_set = (ListView) findViewById(R.id.lv_set2);
		lv_news = (ListView) findViewById(R.id.course_list);
		iv_set = (ImageView) findViewById(R.id.iv2_set);
		mylaout = (RightMenuLinearLayout) findViewById(R.id.mylaout);
		mywebviewlayout = (RightMenuLinearLayout) findViewById(R.id.mywebviewlaout);

		gardencourse_back.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				finish();
			}
		});

		mylaout.setOnScrollListener(new OnScrollListener() {
			@Override
			public void doScroll(float distanceX) {
				doScrolling(distanceX);
			}

			@Override
			public void doLoosen() {
				RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) layout_left
						.getLayoutParams();
				Log.e("jj", "layoutParams.leftMargin="
						+ layoutParams.leftMargin);
				// ����ȥ
				if (layoutParams.leftMargin < -window_width / 3) {
					new AsynMove().execute(-SPEED);
				} else {
					new AsynMove().execute(SPEED);
				}
			}
		});

		mywebviewlayout.setOnScrollListener(new OnScrollListener() {
			@Override
			public void doScroll(float distanceX) {
				doScrolling(distanceX);
			}

			@Override
			public void doLoosen() {
				RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) layout_left
						.getLayoutParams();
				Log.e("jj", "layoutParams.leftMargin="
						+ layoutParams.leftMargin);
				// ����ȥ
				if (layoutParams.leftMargin < -window_width / 3) {
					new AsynMove().execute(-SPEED);
				} else {
					new AsynMove().execute(SPEED);
				}
			}
		});

		// �������
		lv_set.setOnItemClickListener(this);
		iv_set.setOnTouchListener(this);
		layout_right.setOnTouchListener(this);
		layout_left.setOnTouchListener(this);
		mGestureDetector = new GestureDetector(this);
		// ���ó�������
		mGestureDetector.setIsLongpressEnabled(false);
		getMAX_WIDTH();
	}

	void getName() {
		SharedPreferences sp = getSharedPreferences(BBGJ_USERINFO, 0);
		int comId_int = sp.getInt("comId", 0);
		comId = String.valueOf(comId_int);
	}

	private class mGetDisplayDataThread implements Runnable {
		@Override
		public void run() {
			// TODO Auto-generated method stub
			String qurl = MessengerService.URL;
			String qmethodname = MessengerService.METHOD_GETSOURCEFORCLASSALL;
			String qnamespace = MessengerService.NAMESPACE;
			String qsoapaction = qnamespace + "/" + qmethodname;

			SoapObject rpc = new SoapObject(qnamespace, qmethodname);
			rpc.addProperty("comId", comId);
			SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
					SoapEnvelope.VER11);
			envelope.bodyOut = rpc;
			envelope.dotNet = true;
			envelope.setOutputSoapObject(rpc);
			HttpTransportSE ht = new HttpTransportSE(qurl);
			ht.debug = true;
			try {
				ht.call(qsoapaction, envelope);

				remoteWindowItem = new ArrayList<HashMap<String, Object>>();

				SoapObject journal = (SoapObject) envelope.bodyIn;
				SoapObject soapchilds = (SoapObject) journal.getProperty(0);
				SoapObject soapchildss = (SoapObject) soapchilds.getProperty(1);
				SoapObject soapchildsss = (SoapObject) soapchildss
						.getProperty(0);

				for (int i = 0; i < soapchildsss.getPropertyCount(); i++) {
					SoapObject soapfinal = (SoapObject) soapchildsss
							.getProperty(i);

					String comId = soapfinal.getProperty("comId")
							.toString();
					String className = soapfinal.getProperty("className")
							.toString();
					String classId = soapfinal.getProperty("classId").toString();

					HashMap<String, Object> mapdevinfo = new HashMap<String, Object>();
					mapdevinfo.put("comId", comId);
					mapdevinfo.put("className", className);
					mapdevinfo.put("classId", classId);

//					if (exist.equals("1")) {
						remoteWindowItem.add(mapdevinfo);
//					}
				}
				Message msg = new Message();
				msg.arg1 = 0;
				msg.obj = remoteWindowItem;
				threadMessageHandler.sendMessage(msg);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (XmlPullParserException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	@SuppressLint("HandlerLeak")
	private class mGetTopicDataThread implements Runnable {


		String classId = "";
		
		public void setParser(String _classId){
			classId = _classId;
		}
		
		@Override
		public void run() {
			// TODO Auto-generated method stub
			String comurl = MessengerService.URL;
			String commethodname = MessengerService.METHOD_GETSOURCEFORCLASSSINGLE;
			String comnamespace = MessengerService.NAMESPACE;
			String comsoapaction = comnamespace + "/" + commethodname;

			SoapObject rpc = new SoapObject(comnamespace, commethodname);
			rpc.addProperty("comId", comId);
			rpc.addProperty("classId", classId);
			SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
					SoapEnvelope.VER11);
			envelope.bodyOut = rpc;
			envelope.dotNet = true;
			envelope.setOutputSoapObject(rpc);
			HttpTransportSE ht = new HttpTransportSE(comurl);
			ht.debug = true;
			try {
				ht.call(comsoapaction, envelope);

				remoteWindowItem1 = new ArrayList<HashMap<String, Object>>();

				SoapObject journal = (SoapObject) envelope.bodyIn;
				SoapObject soapchilds = (SoapObject) journal.getProperty(0);
				SoapObject soapchildss = (SoapObject) soapchilds.getProperty(1);
				SoapObject soapchildsss = (SoapObject) soapchildss
						.getProperty(0);

				String week_str = "";
				String course_str = "";
				String time_str = "";
				int signal = 1;
				for (int i = 0; i < soapchildsss.getPropertyCount(); i++) {
					SoapObject soapfinal = (SoapObject) soapchildsss
							.getProperty(i);

					String comId = soapfinal.getProperty("comId")
							.toString();
					String classId = soapfinal.getProperty("classId").toString();
					String className = soapfinal.getProperty("className")
							.toString();
					String tmStart = soapfinal.getProperty("tmStart").toString();
					String tmEnd = soapfinal.getProperty("tmEnd").toString();
					String week = soapfinal.getProperty("week").toString();
					String lesson = soapfinal.getProperty("lesson").toString();
					String courseName = soapfinal.getProperty("courseName").toString();

//					HashMap<String, Object> mapdevinfo = new HashMap<String, Object>();
//					mapdevinfo.put("comId", comId);
//					mapdevinfo.put("classId", classId);
//					mapdevinfo.put("className", className);
//					mapdevinfo.put("tmStart", tmStart);
//					mapdevinfo.put("tmEnd", tmEnd);
//					mapdevinfo.put("week", week);
//					mapdevinfo.put("lesson", lesson);
//					mapdevinfo.put("courseName", courseName);
					
					if(!week.equals(week_str)){
						HashMap<String, Object> mapdevinfo = new HashMap<String, Object>();
//						mapdevinfo.put("comId", comId);
//						mapdevinfo.put("classId", classId);
//						mapdevinfo.put("className", className);
//						mapdevinfo.put("tmStart", tmStart);
//						mapdevinfo.put("tmEnd", tmEnd);
//						mapdevinfo.put("week", week);
//						mapdevinfo.put("lesson", lesson);
//						mapdevinfo.put("courseName", courseName);
						mapdevinfo.put("week_str", week_str);
						mapdevinfo.put("course_str", course_str);
						mapdevinfo.put("time_str", time_str);
						if(!week_str.equals("")){							
							remoteWindowItem1.add(mapdevinfo);
						}
						
						week_str = week;
						course_str = lesson + "��" + courseName + "%";
						time_str = tmStart + "-" + tmEnd + "%";
						
					}else{
						course_str = course_str + lesson + "��" + courseName + "%";
						time_str = time_str + tmStart + "-" + tmEnd + "%";
					}
				}
				
				Message msg = new Message();
				msg.arg1 = 1;
				msg.obj = remoteWindowItem1;
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

	/***
	 * listview ���ڻ���ʱִ��.
	 */
	void doScrolling(float distanceX) {
		isScrolling = true;
		mScrollX += distanceX;// distanceX:����Ϊ������Ϊ��

		RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) layout_left
				.getLayoutParams();
		RelativeLayout.LayoutParams layoutParams_1 = (RelativeLayout.LayoutParams) layout_right
				.getLayoutParams();
		layoutParams.leftMargin -= mScrollX;
		layoutParams_1.leftMargin = window_width + layoutParams.leftMargin;
		if (layoutParams.leftMargin >= 0) {
			isScrolling = false;// �Ϲ�ͷ�˲���Ҫ��ִ��AsynMove��
			layoutParams.leftMargin = 0;
			layoutParams_1.leftMargin = window_width;

		} else if (layoutParams.leftMargin <= -MAX_WIDTH) {
			// �Ϲ�ͷ�˲���Ҫ��ִ��AsynMove��
			isScrolling = false;
			layoutParams.leftMargin = -MAX_WIDTH;
			layoutParams_1.leftMargin = window_width - MAX_WIDTH;
		}
		Log.v(TAG, "layoutParams.leftMargin=" + layoutParams.leftMargin
				+ ",layoutParams_1.leftMargin =" + layoutParams_1.leftMargin);

		layout_left.setLayoutParams(layoutParams);
		layout_right.setLayoutParams(layoutParams_1);
	}

	void getMAX_WIDTH() {
		ViewTreeObserver viewTreeObserver = layout_left.getViewTreeObserver();
		// ��ȡ�ؼ����
		viewTreeObserver.addOnPreDrawListener(new OnPreDrawListener() {
			@Override
			public boolean onPreDraw() {
				if (!hasMeasured) {
					window_width = getWindowManager().getDefaultDisplay()
							.getWidth();
					MAX_WIDTH = layout_right.getWidth();
					RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) layout_left
							.getLayoutParams();
					RelativeLayout.LayoutParams layoutParams_1 = (RelativeLayout.LayoutParams) layout_right
							.getLayoutParams();
					ViewGroup.LayoutParams layoutParams_2 = mylaout
							.getLayoutParams();
					// ע�⣺ ����layout_left�Ŀ�ȡ���ֹ�����ƶ���ʱ��ؼ�����ѹ
					layoutParams.width = window_width;
					layout_left.setLayoutParams(layoutParams);

					// ����layout_right�ĳ�ʼλ��.
					layoutParams_1.leftMargin = window_width;
					layout_right.setLayoutParams(layoutParams_1);
					// ע�⣺����lv_set�Ŀ�ȷ�ֹ�����ƶ���ʱ��ؼ�����ѹ
					layoutParams_2.width = MAX_WIDTH;
					mylaout.setLayoutParams(layoutParams_2);

					Log.v(TAG, "MAX_WIDTH=" + MAX_WIDTH + "width="
							+ window_width);
					hasMeasured = true;
				}
				return true;
			}
		});

	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) layout_left
					.getLayoutParams();
			if (layoutParams.leftMargin < 0) {
				new AsynMove().execute(SPEED);
				return false;
			}
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {

		view = v;// ��¼����Ŀؼ�

		// �ɿ���ʱ��Ҫ�жϣ������������Ļλ��������ȥ��
		if (MotionEvent.ACTION_UP == event.getAction() && isScrolling == true) {
			RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) layout_left
					.getLayoutParams();
			// ����ȥ
			if (layoutParams.leftMargin < -window_width / 3) {
				new AsynMove().execute(-SPEED);
			} else {
				new AsynMove().execute(SPEED);
			}
		}

		return mGestureDetector.onTouchEvent(event);
	}

	@Override
	public boolean onDown(MotionEvent e) {

		int position = lv_set.pointToPosition((int) e.getX(), (int) e.getY());
		if (position != ListView.INVALID_POSITION) {
			View child = lv_set.getChildAt(position
					- lv_set.getFirstVisiblePosition());
			if (child != null)
				child.setPressed(true);
		}

		mScrollX = 0;
		isScrolling = false;
		// ��֮��Ϊtrue���Żᴫ�ݸ�onSingleTapUp,��Ȼ�¼��������´���.
		return true;
	}

	@Override
	public void onShowPress(MotionEvent e) {

	}

	/***
	 * ����ɿ�ִ��
	 */
	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		// ����Ĳ���layout_left
		if (view != null && view == iv_set) {
			RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) layout_left
					.getLayoutParams();
			// ���ƶ�
			if (layoutParams.leftMargin >= 0) {
				new AsynMove().execute(-SPEED);
				lv_set.setSelection(0);// ����Ϊ��λ.
			} else {
				// ���ƶ�
				new AsynMove().execute(SPEED);
			}
		} else if (view != null && view == layout_left) {
			RelativeLayout.LayoutParams layoutParams = (android.widget.RelativeLayout.LayoutParams) layout_left
					.getLayoutParams();
			if (layoutParams.leftMargin < 0) {
				// ˵��layout_left�����ƶ������״̬�����ʱ��������layout_leftӦ��ֱ������ԭ��״̬.(�����Ի�)
				// ���ƶ�
				new AsynMove().execute(SPEED);
			}
		}

		return true;
	}

	/***
	 * �������� ����һ�����ƶ�������һ����. distanceX=�����x-ǰ���x���������0��˵���������ǰ�����ұ߼����һ���
	 */
	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
			float distanceY) {
		// ִ�л���.
		doScrolling(distanceX);
		return false;
	}

	@Override
	public void onLongPress(MotionEvent e) {

	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		return false;
	}

	class AsynMove extends AsyncTask<Integer, Integer, Void> {

		@Override
		protected Void doInBackground(Integer... params) {
			int times = 0;
			if (MAX_WIDTH % Math.abs(params[0]) == 0)// ����
				times = MAX_WIDTH / Math.abs(params[0]);
			else
				times = MAX_WIDTH / Math.abs(params[0]) + 1;// ������

			for (int i = 0; i < times; i++) {
				publishProgress(params[0]);
				try {
					Thread.sleep(sleep_time);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

			return null;
		}

		/**
		 * update UI
		 */
		@Override
		protected void onProgressUpdate(Integer... values) {
			RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) layout_left
					.getLayoutParams();
			RelativeLayout.LayoutParams layoutParams_1 = (RelativeLayout.LayoutParams) layout_right
					.getLayoutParams();
			// ���ƶ�
			if (values[0] > 0) {
				layoutParams.leftMargin = Math.min(layoutParams.leftMargin
						+ values[0], 0);
				layoutParams_1.leftMargin = Math.min(layoutParams_1.leftMargin
						+ values[0], window_width);
				Log.v(TAG, "layout_left��" + layoutParams.leftMargin
						+ ",layout_right��" + layoutParams_1.leftMargin);
			} else {
				// ���ƶ�
				layoutParams.leftMargin = Math.max(layoutParams.leftMargin
						+ values[0], -MAX_WIDTH);
				layoutParams_1.leftMargin = Math.max(layoutParams_1.leftMargin
						+ values[0], window_width - MAX_WIDTH);
				Log.v(TAG, "layout_left��" + layoutParams.leftMargin
						+ ",layout_right��" + layoutParams_1.leftMargin);
			}
			layout_right.setLayoutParams(layoutParams_1);
			layout_left.setLayoutParams(layoutParams);

		}

	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) layout_left
				.getLayoutParams();
		// ֻҪû�л��������ڵ��
		// Toast.makeText(context, title[position], 1).show();
//
		String classId = remoteWindowItem.get(position).get("classId").toString();
//		Toast.makeText(context, month, Toast.LENGTH_SHORT).show();
		mGetTopicDataThread runnable = new mGetTopicDataThread();
		runnable.setParser(classId);
		Thread thread = new Thread(runnable);
		thread.start();

		if (layoutParams.leftMargin < 0) {
			new AsynMove().execute(SPEED);
		}
	}

	Handler threadMessageHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (msg.arg1 == 0) {
				ArrayList<HashMap<String, Object>> remoteWindowItem = (ArrayList<HashMap<String, Object>>) msg.obj;
				CourseMonthAdapter topicAdapter = new CourseMonthAdapter(
						context, remoteWindowItem,
						R.layout.list_in_coursemonth, new String[] {
								"comId", "className", "classId"}, new int[] { R.id.textview,
								R.id.textview,
								R.id.textview });
				lv_set.setAdapter(topicAdapter);
			} else if (msg.arg1 == 1) {
				ArrayList<HashMap<String, Object>> remoteWindowItem1 = (ArrayList<HashMap<String, Object>>) msg.obj;

				CourseAdapter displayAdapter = new CourseAdapter(context,
						remoteWindowItem1, R.layout.list_in_course,
						new String[] { "week_str", "course_str", "time_str" }, new int[] {
								R.id.textview, R.id.textview, R.id.textview });
				lv_news.setAdapter(displayAdapter);
			}
		}
	};

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
//		Toast.makeText(context, comId, Toast.LENGTH_LONG).show();
	}
}
