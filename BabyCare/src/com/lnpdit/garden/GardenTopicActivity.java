package com.lnpdit.garden;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.xmlpull.v1.XmlPullParserException;

import com.lnpdit.babycare.R;
import com.lnpdit.service.MessengerService;
import com.lnpdit.util.PullDownView;
import com.lnpdit.util.PullDownView.OnPullDownListener;
import com.lnpdit.util.adapter.TopicAdapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class GardenTopicActivity extends Activity implements OnPullDownListener, OnClickListener {
	private Resources resources;
	private Context context;
	ListView listview;
	private PullDownView mPullDownView;
	ProgressBar progressbar;
	RelativeLayout progresslayout;
	RelativeLayout timeoutlayout;
	Button refresh_bt;

	private SoapObject commu;
	private String comid;
	private String comtitle;
	private String comcontent;
	private String comnumber;
	private String comtime;

	public boolean sync_state;
	private Animation animation;
	TopicAdapter topicAdapter;

	int pagesize;

	private TextView gardentopic_back;
	public static boolean isAlive;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.gardentopic);

		gardentopic_back = (TextView) findViewById(R.id.gardentopic_back);
		gardentopic_back.setOnClickListener(this);

		resources = this.getResources();
		context = this;
		pagesize = 1;
		WidgetInit();
		getNewData();
		isAlive = true;
	}

	private void getNewData() {
		try {
			sync_state = false;
			Timer timer = new Timer();
			final Thread thread = new Thread(new mGetTopicDataThread());
			thread.start();
			timer.schedule(new TimerTask() {

				@Override
				public void run() {
					// TODO Auto-generated method stub
					if (sync_state == false) {
						thread.interrupt();
						Message msg = new Message();
						msg.arg1 = 1;
						threadMessageHandler.sendMessage(msg);
					}
				}
			}, MessengerService.LOADING_TIME);
		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	private void WidgetInit() {
		mPullDownView = (PullDownView) this.findViewById(R.id.topic_list);
		mPullDownView.setOnPullDownListener(this);
		listview = mPullDownView.getListView();
		listview.setDivider(resources.getDrawable(R.drawable.listview_line4));
		Animation listview_anim = AnimationUtils.loadAnimation(this,
				R.anim.fade);
		listview.setAnimation(listview_anim);
		progressbar = (ProgressBar) this.findViewById(R.id.topic_progressbar);
		progresslayout = (RelativeLayout) this
				.findViewById(R.id.topic_progress_layout);
		timeoutlayout = (RelativeLayout) this
				.findViewById(R.id.topic_timeout_layout);
		refresh_bt = (Button) this.findViewById(R.id.topic_timeout_bt);
		refresh_bt.setOnClickListener(btListener);
		animation = AnimationUtils.loadAnimation(this, R.anim.rotate);
		try {
			progressbar.setVisibility(MessengerService.VISIBILITY_TRUE);
			progressbar.setAnimation(animation);
		} catch (Exception e) {
			// TODO: handle exception
			new AlertDialog.Builder(context).setMessage(e.toString()).show();
		}
		mPullDownView.setHideHeader();
		mPullDownView.setShowHeader();
	}

	Handler threadMessageHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			try {
				switch (msg.arg1) {
				case 0:
					listview.setVisibility(MessengerService.VISIBILITY_TRUE);
					ArrayList<HashMap<String, Object>> remoteWindowItem = (ArrayList<HashMap<String, Object>>) msg.obj;
					if (pagesize == 1) {
						topicAdapter = new TopicAdapter(context,
								remoteWindowItem, R.layout.list_in_question,
								new String[] { "Webid", "Title", "Content",
										"Time", "Number" }, new int[] {
										R.id.list_question_title,
										R.id.list_question_title,
										R.id.list_question_title,
										R.id.list_question_title,
										R.id.list_question_title }, resources);
						listview.setAdapter(topicAdapter);
						mPullDownView.setShowFooter();
					} else {
						topicAdapter.addItem(remoteWindowItem);
						topicAdapter.notifyDataSetChanged();
					}
					progresslayout
							.setVisibility(MessengerService.VISIBILITY_FALSE);
					timeoutlayout
							.setVisibility(MessengerService.VISIBILITY_FALSE);
					break;
				case 1:
					listview.setVisibility(MessengerService.VISIBILITY_FALSE);
					progresslayout
							.setVisibility(MessengerService.VISIBILITY_FALSE);
					timeoutlayout
							.setVisibility(MessengerService.VISIBILITY_TRUE);
					break;
				case 2:
					mPullDownView.setHideFooter();
					break;
				default:
					break;
				}
			} catch (Exception e) {
				// TODO: handle exception
				Log.e("Topic threadMessageHandler error : ", e.toString());
			}
		}
	};

	private android.view.View.OnClickListener btListener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			switch (v.getId()) {
			case R.id.topic_timeout_bt:
				timeoutlayout.setVisibility(MessengerService.VISIBILITY_FALSE);
				progresslayout.setVisibility(MessengerService.VISIBILITY_TRUE);
				pagesize = 1;
				getNewData();
				break;
			default:
				break;
			}
		}
	};

	@Override
	public void onRefresh() {
		// TODO Auto-generated method stub
		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					pagesize = 1;
					getNewData();
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				/** 关闭 刷新完毕 ***/
				mPullDownView.RefreshComplete();// 这个事线程安全的 可看源代码
			}
		}).start();
	}

	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		if (!isAlive) {
			new Thread(new Runnable() {

				@Override
				public void run() {
					try {
						pagesize = 1;
						getNewData();
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}

					/** 关闭 刷新完毕 ***/
					mPullDownView.RefreshComplete();// 这个事线程安全的 可看源代码
				}
			}).start();
			isAlive = true;
		}
	}

	@Override
	public void onMore() {
		// TODO Auto-generated method stub
		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					pagesize = pagesize + 1;
					getNewData();
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				// 告诉它获取更多完毕 这个事线程安全的 可看源代码
				mPullDownView.notifyDidMore();
			}
		}).start();
	}

	@SuppressLint("HandlerLeak")
	private class mGetTopicDataThread implements Runnable {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			SharedPreferences share = getSharedPreferences("BBGJ_UserInfo",
					Activity.MODE_WORLD_READABLE);
			int comId = share.getInt("comId", 0);
			String comurl = MessengerService.URL;
			String commethodname = MessengerService.METHOD_GETCOMMUNICATION;
			String comnamespace = MessengerService.NAMESPACE;
			String comsoapaction = comnamespace + "/" + commethodname;

			SoapObject rpc = new SoapObject(comnamespace, commethodname);
			rpc.addProperty("pagesize", 10);
			rpc.addProperty("pageindex", pagesize);
			rpc.addProperty("comId", comId);
			SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
					SoapEnvelope.VER11);
			envelope.bodyOut = rpc;
			envelope.dotNet = true;
			envelope.setOutputSoapObject(rpc);
			HttpTransportSE ht = new HttpTransportSE(comurl);
			ht.debug = true;
			try {
				ht.call(comsoapaction, envelope);
				commu = (SoapObject) envelope.bodyIn;
				ArrayList<HashMap<String, Object>> remoteWindowItem = new ArrayList<HashMap<String, Object>>();
				SoapObject soapchilds = (SoapObject) commu.getProperty(0);
				SoapObject soapchildss = (SoapObject) soapchilds.getProperty(1);
				SoapObject soapchildtemp = (SoapObject) soapchildss
						.getProperty(0);
				sync_state = true;
				int _item_count = soapchildtemp.getPropertyCount();
				if (_item_count < 10) {
					Message msg = new Message();
					msg.arg1 = 2;
					threadMessageHandler.sendMessage(msg);
				}
				for (int j = 0; j < _item_count; j++) {
					SoapObject soapchildsson = (SoapObject) soapchildtemp
							.getProperty(j);

					comid = soapchildsson.getProperty("id").toString();
					comtitle = soapchildsson.getProperty("title").toString();
					comcontent = soapchildsson.getProperty("content")
							.toString();
					comtime = soapchildsson.getProperty("crtime").toString();
					comnumber = soapchildsson.getProperty("count").toString();

					HashMap<String, Object> mapdevinfo = new HashMap<String, Object>();
					mapdevinfo.put("Webid", comid);
					mapdevinfo.put("Title", comtitle);
					mapdevinfo.put("Content", comcontent);
					mapdevinfo.put("Time", comtime);
					mapdevinfo.put("Number", comnumber);
					remoteWindowItem.add(mapdevinfo);
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
			} catch (Exception e) {
				// TODO: handle exception
				Log.e("TOPIC GET DATA ERROR : ", e.toString());
			}
		}
	}


	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.gardentopic_back:

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

}
