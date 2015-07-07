package com.lnpdit.babycare;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

public class MsgPageActivity extends Activity implements OnClickListener {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.msgpage);

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {

		default:
			break;
		}
	}
}
