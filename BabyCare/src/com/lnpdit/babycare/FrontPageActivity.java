package com.lnpdit.babycare;

import java.io.File;
import java.io.Serializable;
import java.util.List;

import com.lnpdit.monitor.CameraList;
import com.lnpdit.monitor.FavorList;
import com.lnpdit.monitor.GetPicActivity;
import com.lnpdit.monitor.RecordList;
import com.lnpdit.monitor.SnapGridActivity;
import com.lnpdit.monitor.SnapList;
import com.lnpdit.util.AlbumHelper;
import com.lnpdit.util.ImageBucket;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;

public class FrontPageActivity extends Activity implements OnClickListener {

	View front_layout_camera;
	View front_layout_record;
	View front_layout_snap;
	View front_layout_favor;
//	ImageView imageCamera;
//	ImageView imageRecord;
//	ImageView imageSnap;
//	ImageView imageFavor;
	// ArrayList<Entity> dataList;//用来装载数据源的列表
	List<ImageBucket> dataList;
	AlbumHelper helper;

	// 权限

	private String ifCamera = "";
	private String ifRecord = "";
	private String ifSnap = "";
	private String ifFavor = "";

	Bundle loginBundle;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.frontpage);

		front_layout_camera = findViewById(R.id.front_layout_camera);
		front_layout_record = findViewById(R.id.front_layout_record);
		front_layout_snap = findViewById(R.id.front_layout_snap);
		front_layout_favor = findViewById(R.id.front_layout_favor);
		front_layout_camera.setOnClickListener(this);
		front_layout_record.setOnClickListener(this);
		front_layout_snap.setOnClickListener(this);
		front_layout_favor.setOnClickListener(this);

//		imageCamera = (ImageView) findViewById(R.id.front_image_camera);
//		imageRecord = (ImageView) findViewById(R.id.front_image_record);
//		imageSnap = (ImageView) findViewById(R.id.front_image_snap);

		// checkSnapPath();
		// helper = AlbumHelper.getHelper();
		// helper.init(getApplicationContext());
		// dataList = helper.getImagesBucketList(false);

		initView();
	}

	private void initView() {

		loginBundle = this.getIntent().getExtras();

		ifCamera = loginBundle.getString("ifVideo").toString();
		ifRecord = loginBundle.getString("ifRecord").toString();
		ifSnap = loginBundle.getString("ifSnap").toString();
		ifFavor = loginBundle.getString("ifFavor").toString();
		if (!ifCamera.equals("1")) {
			front_layout_camera.setVisibility(View.GONE);
//			imageCamera.setVisibility(ImageView.GONE);
		}
		if (!ifRecord.equals("1")) {
			front_layout_record.setVisibility(View.GONE);
//			imageRecord.setVisibility(ImageView.GONE);
		}
		if (!ifSnap.equals("1")) {
			front_layout_snap.setVisibility(View.GONE);
//			imageSnap.setVisibility(ImageView.GONE);
		}
		if (!ifFavor.equals("1")) {
			front_layout_favor.setVisibility(View.GONE);
//			imageFavor.setVisibility(ImageView.GONE);
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.front_layout_camera:
			Intent cameraIntent = new Intent(FrontPageActivity.this,
					CameraList.class);
			cameraIntent.putExtras(loginBundle);
			startActivity(cameraIntent);
			break;
		case R.id.front_layout_record:
			startActivity(new Intent(FrontPageActivity.this, RecordList.class));
			break;
		case R.id.front_layout_snap:

			Intent snapIntent = new Intent(FrontPageActivity.this,
					SnapList.class);
			startActivity(snapIntent);
			// if (dataList.size() != 0) {
			// snapIntent.putExtra("imageExist", "yes");
			// snapIntent.putExtra("imagelist",
			// (Serializable) dataList.get(0).imageList);
			// } else {
			// snapIntent.putExtra("imageExist", "no");
			// snapIntent.putExtra("imagelist", "");
			// }
			// Intent snapIntent = new Intent(FrontPageActivity.this,
			// SnapList.class);
			// startActivity(snapIntent);
			break;
		case R.id.front_layout_favor:
			Intent favorIntent = new Intent(FrontPageActivity.this,
					FavorList.class);
			favorIntent.putExtras(loginBundle);
			startActivity(favorIntent);
			break;

		default:
			break;
		}
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
