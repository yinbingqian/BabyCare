package com.lnpdit.garden;

import android.os.Bundle;
import android.app.Activity;
import android.app.ListActivity;
import android.content.SharedPreferences;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebSettings.PluginState;
import android.widget.TextView;
import android.view.KeyEvent;

import com.lnpdit.babycare.MainActivity;
import com.lnpdit.babycare.R;

public class GardenCourseActivity extends Activity implements OnClickListener {

	private TextView gardencourse_back;
	private WebView web_course;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.gardencourse);

		gardencourse_back = (TextView) findViewById(R.id.gardencourse_back);
		gardencourse_back.setOnClickListener(this);

		SharedPreferences share = getSharedPreferences("BBGJ_UserInfo",
				Activity.MODE_WORLD_READABLE);
		int comId = share.getInt("comId", 0);
		web_course = (WebView) findViewById(R.id.web_course);
		web_course.getSettings().setJavaScriptEnabled(true);
		web_course.getSettings().setPluginState(PluginState.ON);
		web_course.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
		web_course.getSettings().setAllowFileAccess(true);
		web_course.getSettings().setDefaultTextEncodingName("UTF-8");
		web_course.getSettings().setLoadWithOverviewMode(true);
		web_course.getSettings().setUseWideViewPort(true);
		web_course.loadUrl(MainActivity.IP + "/mobile/courseWeb.aspx?ComId="
				+ comId);
		web_course.setWebViewClient(new WebViewClient() {
			// 这个方法在用户试图点开页面上的某个链接时被调用
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				if (url != null) {
					// 如果想继续加载目标页面则调用下面的语句
					view.loadUrl(url);
					// 如果不想那url就是目标网址，如果想获取目标网页的内容那你可以用HTTP的API把网页扒下来。
				}
				// 返回true表示停留在本WebView（不跳转到系统的浏览器）
				return true;
			}
		});

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
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.gardencourse_back:

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
