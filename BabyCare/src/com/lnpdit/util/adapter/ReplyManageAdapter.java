package com.lnpdit.util.adapter;


import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.xmlpull.v1.XmlPullParserException;

import com.lnpdit.babycare.R;
import com.lnpdit.service.MessengerService;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;


public class ReplyManageAdapter extends BaseAdapter {
	private class buttonViewHolder {
		TextView apptitle;
		TextView appreply;
		TextView appuser;
		TextView apptime;
		Button appremove;
	}

	private ArrayList<HashMap<String, Object>> mAppList;
	private LayoutInflater mInflater;
	private Context mContext;
	private String[] keyString;
	private buttonViewHolder holder;
	private Resources resources;

	public ReplyManageAdapter(Context c,
			ArrayList<HashMap<String, Object>> appList, int resource,
			String[] from, int[] to, Resources mResources) {
		mAppList = appList;
		mContext = c;
		resources = mResources;
		mInflater = (LayoutInflater) mContext
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		keyString = new String[from.length];
		System.arraycopy(from, 0, keyString, 0, from.length);
	}

	@Override
	public int getCount() {
		return mAppList.size();
	}

	@Override
	public Object getItem(int position) {
		return mAppList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	public void removeItem(int positon) {
		mAppList.remove(positon);
		this.notifyDataSetChanged();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView != null) {
			holder = (buttonViewHolder) convertView.getTag();
		} else {
			convertView = mInflater
					.inflate(R.layout.list_in_reply_manage, null);
			holder = new buttonViewHolder();
			holder.apptitle = (TextView) convertView
					.findViewById(R.id.list_in_reply_manage_topic_text);
			holder.appreply = (TextView) convertView
					.findViewById(R.id.list_in_reply_manage_reply_text);
			holder.appuser = (TextView) convertView
					.findViewById(R.id.list_in_reply_manage_user_text);
			holder.apptime = (TextView) convertView
					.findViewById(R.id.list_in_reply_manage_time_text);
			holder.appremove = (Button) convertView
					.findViewById(R.id.list_in_reply_manage_delete_bt);
			convertView.setTag(holder);
		}
		HashMap<String, Object> appInfo = mAppList.get(position);
		if (appInfo != null) {
			String replyid = (String) appInfo.get(keyString[0]);
			String topicid = (String) appInfo.get(keyString[1]);
			String topic = (String) appInfo.get(keyString[2]);
			String userid = (String) appInfo.get(keyString[3]);
			String type = (String) appInfo.get(keyString[4]);
			String time = (String) appInfo.get(keyString[5]);
			String content = (String) appInfo.get(keyString[6]);
			String user = (String) appInfo.get(keyString[7]);

			holder.apptitle.setText(topic);
			holder.appreply.setText(content);
			holder.appuser.setText(user);
			holder.apptime.setText(time);
			holder.appremove
					.setOnClickListener(new AdapterListener(position, replyid,
							topicid, topic, userid, type, time, content, user));
		}
		return convertView;
	}

	class AdapterListener implements OnClickListener {
		private int position;
		private String replyid;
		private String topicid;
		private String topic;
		private String userid;
		private String type;
		private String time;
		private String content;
		private String user;

		public AdapterListener(int pos, String _replyid, String _topicid,
				String _topic, String _userid, String _type, String _time,
				String _content, String _user) {
			// TODO Auto-generated constructor stub
			position = pos;
			replyid = _replyid;
			topicid = _topicid;
			topic = _topic;
			userid = _userid;
			type = _type;
			time = _time;
			content = _content;
			user = _user;
		}

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			try {
				AlertDialog.Builder exitbuilder = new Builder(mContext);
				exitbuilder.setMessage(content);
				exitbuilder.setTitle("确认删除此条回复？");
				exitbuilder.setPositiveButton(
						resources.getString(R.string.ensure),
						new android.content.DialogInterface.OnClickListener() {

							public void onClick(DialogInterface dialog,
									int which) {
								// TODO Auto-generated method stub

								mRemoveItemThread removeRunnable = new mRemoveItemThread();
								removeRunnable.setReplyId(replyid);
								Thread thread = new Thread(removeRunnable);
								thread.start();
								removeItem(position);
							}
						});
				exitbuilder.setNegativeButton(
						resources.getString(R.string.cancel),
						new android.content.DialogInterface.OnClickListener() {

							public void onClick(DialogInterface dialog,
									int which) {
								// TODO Auto-generated method stub
								dialog.dismiss();
							}
						});
				exitbuilder.show();
			} catch (Exception e) {
				// TODO: handle exception
			}
		}
	}

	private class mRemoveItemThread implements Runnable {

		private String mId = "";

		public void setReplyId(String _id) {
			this.mId = _id;
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			String url = MessengerService.URL;
			String methodname = MessengerService.METHOD_COMMUNREPLYREMOVE;
			String namespace = MessengerService.NAMESPACE;
			String soapaction = namespace + "/" + methodname;

			SoapObject rpc = new SoapObject(namespace, methodname);

			rpc.addProperty("id", mId);
			SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
					SoapEnvelope.VER11);
			envelope.bodyOut = rpc;
			envelope.dotNet = true;
			envelope.setOutputSoapObject(rpc);
			HttpTransportSE ht = new HttpTransportSE(url);
			ht.debug = true;
			try {
				ht.call(soapaction, envelope);
				// SoapObject resSoap = (SoapObject) envelope.bodyIn;
				String result = envelope.getResponse().toString();
				Message msg = new Message();
				msg.obj = result;
			} catch (IOException e) {
				// TODO Auto-generated catch blockan
				e.printStackTrace();
			} catch (XmlPullParserException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}
}
