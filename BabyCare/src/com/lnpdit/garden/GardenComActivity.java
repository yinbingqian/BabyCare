package com.lnpdit.garden;

import java.util.ArrayList;
import java.util.List;

import com.lnpdit.babycare.LoginActivity;
import com.lnpdit.babycare.R;
import com.lnpdit.service.MessengerService;
import com.lnpdit.sqllite.BBGJDB;
import com.lnpdit.util.ImageAndTextPush;
import com.lnpdit.util.adapter.ImageAndTextListPushAdapter;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class GardenComActivity extends Activity implements OnClickListener {
	Resources resources;
	Context context;
	ListView listview;
	private mServiceRemoveProcessDialog mReceiver = null;

	int startX = 0;
	int avg_width = 0;
	private TextView garden_pushmsg;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.gardencom);
		resources = this.getResources();
		context = this;

		garden_pushmsg = (TextView) findViewById(R.id.pushmsg);
		garden_pushmsg.setOnClickListener(this);

		getRcvData();

		mReceiver = new mServiceRemoveProcessDialog();
		IntentFilter mFilter = new IntentFilter(
				"mulongtec.lnpditnews.and.com.list.refresh");
		registerReceiver(mReceiver, mFilter);
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		getRcvData();
	}

	public void getRcvData() {
		listview = (ListView) findViewById(R.id.com_list);
		BBGJDB tdd = new BBGJDB(context);
		Cursor cursor = tdd.selectpush();
		Cursor cursor_userid = tdd.selectuser();
		String _userid = "";
		if (cursor_userid.getCount() != 0) {
			cursor_userid.moveToFirst();
			_userid = cursor_userid.getString(1);
		}
		List<ImageAndTextPush> imageAndTexts = new ArrayList<ImageAndTextPush>();
		if (cursor.moveToLast()) {
			for (int i = 0; i < cursor.getCount(); i++) {
				String _id = cursor.getString(1);
				String _remark = cursor.getString(2);
				String _pic = cursor.getString(3);
				String _time = cursor.getString(4);
				String _name = cursor.getString(5);
				String _tel = cursor.getString(6);
				String _type = cursor.getString(7);
				String _audio = cursor.getString(8);
				String _audioLength = cursor.getString(9);
				String _devType = cursor.getString(10);
				String _rcverName = cursor.getString(11);
				String _senderId = cursor.getString(12);
				String _localId = cursor.getString(0);
				ImageAndTextPush it = new ImageAndTextPush(
						MessengerService.PIC_PUSH + _pic, _id, _remark, _time,
						_name, _tel, _type, _audio, _audioLength, _devType,
						_rcverName, _senderId, _localId, _userid);
				imageAndTexts.add(it);
				cursor.moveToPrevious();
			}
		}

		ImageAndTextListPushAdapter ia = new ImageAndTextListPushAdapter(
				GardenComActivity.this, imageAndTexts, listview, context);
		// ia.notifyDataSetChanged();
		listview.setAdapter(ia);

	}

	private class mServiceRemoveProcessDialog extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			try {
				if (intent.getAction().equals(
						"mulongtec.lnpditnews.and.com.list.refresh")) {
					getRcvData();
				}
			} catch (Exception e) {
				// TODO: handle exception
			}
		}

	}

	public void pushEdit() {
		BBGJDB tdd = new BBGJDB(context);
		Cursor cursor = tdd.selectuser();
		if (cursor.getCount() == 0) {
			Toast.makeText(context, "请先进行登录", Toast.LENGTH_SHORT).show();
			Intent intent = new Intent();
			intent.setClass(GardenComActivity.this, LoginActivity.class);
			startActivity(intent);
		} else {
			cursor.moveToFirst();
			Intent intent = new Intent();
			intent.putExtra("ID", cursor.getString(1)).toString();
			intent.setClass(GardenComActivity.this,
					GardenPushTempActivity.class);
			// intent.setClass(TabComActivity.this, PushEditActivity.class);
			startActivity(intent);
		}
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.pushmsg:

			pushEdit();

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
