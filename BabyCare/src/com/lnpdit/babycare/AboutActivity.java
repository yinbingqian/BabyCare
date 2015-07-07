package com.lnpdit.babycare;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.LibVlcException;
import org.videolan.libvlc.Util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.sax.StartElementListener;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.TextView;

import com.company.Demo.DhPlayerActivity;
import com.lnpdit.babycare.R;
import com.lnpdit.util.TipItemizedOverlay;
import com.mapbar.android.maps.GeoPoint;
import com.mapbar.android.maps.ItemizedOverlay;
import com.mapbar.android.maps.MapActivity;
import com.mapbar.android.maps.MapController;
import com.mapbar.android.maps.MapView;
import com.mapbar.android.maps.MyLocationOverlay;
import com.mapbar.android.maps.OverlayItem;
import com.mapbar.android.maps.Projection;

public class AboutActivity extends MapActivity implements OnClickListener {

	private TextView about_back;

	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub

		super.onCreate(savedInstanceState);

		// ½ûÖ¹ËøÆÁ
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		setContentView(R.layout.about);
		about_back = (TextView) findViewById(R.id.about_back);
		about_back.setOnClickListener(this);
	}

	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {

		case R.id.about_back:
			this.finish();
			break;
		}
	}

	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (KeyEvent.KEYCODE_BACK == event.getKeyCode()) {
			this.finish();
			return true;
		}
		return super.onKeyUp(keyCode, event);
	}
}
