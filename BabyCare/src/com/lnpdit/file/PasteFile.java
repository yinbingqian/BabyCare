package com.lnpdit.file;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import com.lnpdit.babycare.R;

import android.app.Activity;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

/** 粘贴文件 **/
public class PasteFile extends ListActivity {
	private TextView _filePath;
	private List<FileInfo> _files;
	private String _rootPath = FileUtil.getSDPath();
	private String _currentPath = _rootPath;
	private final String TAG = "PasteFile";
	private String _currentPasteFilePath = "";
	private String _action = "";
	private ProgressDialog progressDialog;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.file_paste);

		// 获取从Intent传递过来的参数
		Bundle bundle = getIntent().getExtras();
		_currentPasteFilePath = bundle.getString("CURRENTPASTEFILEPATH");
		_action = bundle.getString("ACTION");

		_filePath = (TextView) findViewById(R.id.file_path);

		// 绑定事件
		((Button) findViewById(R.id.cancel)).setOnClickListener(fun_Cancel);

		// 默认获取根目录的文件列表
		viewFiles(_rootPath);
	}

	/** 行被点击事件处理 **/
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		FileInfo f = _files.get(position);

		if (f.IsDirectory) {
			viewFiles(f.Path);
		}
	}

	/** 重定义返回键事件 **/
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// 拦截back按键
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			File f = new File(_currentPath);
			String parentPath = f.getParent();
			if (parentPath != null) {
				viewFiles(parentPath);
			}
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	/** 获取该目录下所有文件 **/
	private void viewFiles(String filePath) {
		ArrayList<FileInfo> tmp = FileActivityHelper.getFiles(PasteFile.this, filePath);
		if (tmp != null) {
			// 清空数据
			if (_files != null) {
				_files.clear();
			}

			_files = tmp;
			// 设置当前目录
			_currentPath = filePath;
			_filePath.setText(filePath);
			// 绑定数据
			setListAdapter(new FileAdapter(this, _files));
		}
	}

	/** 创建文件夹回调委托 **/
	private final Handler createDirHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (msg.what == 0)
				viewFiles(_currentPath);
		}
	};


	/**
	 * 用Handler来更新UI
	 */
	private final Handler progressHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			// 关闭ProgressDialog
			progressDialog.dismiss();

			Intent intent = new Intent();
			Bundle bundle = new Bundle();
			bundle.putString("CURRENTPATH", _currentPath);
			intent.putExtras(bundle);
			setResult(Activity.RESULT_OK, intent);

			finish();
		}
	};

	private Button.OnClickListener fun_Paste = new Button.OnClickListener() {
		public void onClick(View v) {

			final File src = new File(_currentPasteFilePath);
			if (!src.exists()) {
//				Toast.makeText(getApplicationContext(), R.string.file_notexists, Toast.LENGTH_SHORT)
//						.show();
				return;
			}
			String newPath = FileUtil.combinPath(_currentPath, src.getName());
			final File tar = new File(newPath);
			if (tar.exists()) {
//				Toast.makeText(getApplicationContext(), R.string.file_exists, Toast.LENGTH_SHORT)
//						.show();
				return;
			}

			progressDialog = ProgressDialog.show(PasteFile.this, "", "Please wait...", true, false);

			new Thread() {
				@Override
				public void run() {
					if ("MOVE".equals(_action)) { // 移动文件
					} else { // 复制文件
					}

					progressHandler.sendEmptyMessage(0);
				}
			}.start();
		}
	};

	private Button.OnClickListener fun_Cancel = new Button.OnClickListener() {
		public void onClick(View v) {
			setResult(Activity.RESULT_CANCELED);
			finish();
		}
	};
}
