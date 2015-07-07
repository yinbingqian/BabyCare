package com.lnpdit.monitor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.lnpdit.babycare.R;
import com.lnpdit.util.AlbumHelper;
import com.lnpdit.util.Bimp;
import com.lnpdit.util.FileUtils;
import com.lnpdit.util.ImageItem;
import com.lnpdit.util.adapter.ImageGridAdapter;
import com.lnpdit.util.adapter.ImageGridAdapter.TextCallback;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

public class SnapGridActivity extends Activity {
	public static final String EXTRA_IMAGE_LIST = "imagelist";

	// ArrayList<Entity> dataList;
	String imageExist;
	List<ImageItem> dataList;
	GridView gridView;
	ImageGridAdapter adapter;
	AlbumHelper helper;
	TextView bt;

	TextView snapgrid_back;

	Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 0:
				Toast.makeText(SnapGridActivity.this, "最多选择9张图片", 400).show();
				break;

			default:
				break;
			}
		}
	};

	@SuppressWarnings("unchecked")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.snapgrid);

		helper = AlbumHelper.getHelper();
		helper.init(getApplicationContext());

		initButton();
		imageExist = this.getIntent().getStringExtra("imageExist");
		if (imageExist.equals("yes")) {
			dataList = (List<ImageItem>) getIntent().getSerializableExtra(
					"imagelist");

			initView();
		}
	}

	private void initButton() {

		snapgrid_back = (TextView) findViewById(R.id.snapgrid_back);
		snapgrid_back.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

				Bimp.bmp.clear();
				Bimp.drr.clear();
				Bimp.max = 0;
				FileUtils.deleteDir();

				finish();

			}
		});
	}

	private void initView() {

		gridView = (GridView) findViewById(R.id.gridview);
		gridView.setSelector(new ColorDrawable(Color.TRANSPARENT));
		adapter = new ImageGridAdapter(SnapGridActivity.this, dataList,
				mHandler);
		gridView.setAdapter(adapter);
		// adapter.setTextCallback(new TextCallback() {
		// public void onListen(int count) {
		// // if (count == 0) {
		// //
		// // bt.setText("完成");
		// //
		// // } else {
		// // bt.setText("完成" + "(" + count + ")");
		// // }
		//
		// }
		// });
		//
		// gridView.setOnItemClickListener(new OnItemClickListener() {
		//
		// @Override
		// public void onItemClick(AdapterView<?> parent, View view,
		// int position, long id) {
		//
		// // if(dataList.get(position).isSelected()){
		// // dataList.get(position).setSelected(false);
		// // }else{
		// // dataList.get(position).setSelected(true);
		// // }
		//
		// adapter.notifyDataSetChanged();
		// }
		//
		// });

	}
}
