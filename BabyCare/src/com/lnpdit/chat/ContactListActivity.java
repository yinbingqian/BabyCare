package com.lnpdit.chat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.videolan.libvlc.VLCApplication;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.easemob.chat.EMContactManager;
import com.easemob.exceptions.EaseMobException;
import com.lnpdit.babycare.R;
import com.lnpdit.photo.Constant;
import com.lnpdit.sqllite.User;
import com.lnpdit.sqllite.UserDao;
import com.lnpdit.util.adapter.ContactAdapter;
import com.lnpdit.widget.Sidebar;

/**
 * 联系人列表页
 * 
 */
public class ContactListActivity extends Activity implements OnClickListener {
	private ContactAdapter adapter;
	private List<User> contactList;
	private ListView listView;
	private boolean hidden;
	private Sidebar sidebar;
	private InputMethodManager inputMethodManager;
	private List<String> blackList;
	private TextView contactlist_back;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.contact_list);
		inputMethodManager = (InputMethodManager) this
				.getSystemService(Context.INPUT_METHOD_SERVICE);
		listView = (ListView) this.findViewById(R.id.list);
		sidebar = (Sidebar) this.findViewById(R.id.sidebar);
		sidebar.setListView(listView);
		contactlist_back = (TextView) findViewById(R.id.contactlist_back);
		contactlist_back.setOnClickListener(this);
		// 黑名单列表
		blackList = EMContactManager.getInstance().getBlackListUsernames();
		contactList = new ArrayList<User>();
		// 获取设置contactlist
		getContactList();
		// 设置adapter
		adapter = new ContactAdapter(this, R.layout.row_contact, contactList,
				sidebar);
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				String username = adapter.getItem(position).getUsername();

				// demo中直接进入聊天页面，实际一般是进入用户详情页
				startActivity(new Intent(ContactListActivity.this,
						ChatActivity.class).putExtra("userId",
						adapter.getItem(position).getUsername()));
			}
		});
		listView.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// 隐藏软键盘
				if (ContactListActivity.this.getWindow().getAttributes().softInputMode != WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN) {
					if (ContactListActivity.this.getCurrentFocus() != null)
						inputMethodManager.hideSoftInputFromWindow(
								ContactListActivity.this.getCurrentFocus()
										.getWindowToken(),
								InputMethodManager.HIDE_NOT_ALWAYS);
				}
				return false;
			}
		});

		registerForContextMenu(listView);

	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		// 长按前两个不弹menu
		if (((AdapterContextMenuInfo) menuInfo).position > 2) {
			this.getMenuInflater().inflate(R.menu.context_contact_list, menu);
		}
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.delete_contact) {
			User tobeDeleteUser = adapter
					.getItem(((AdapterContextMenuInfo) item.getMenuInfo()).position);
			// 删除此联系人
			deleteContact(tobeDeleteUser);
			return true;
		} else if (item.getItemId() == R.id.add_to_blacklist) {
			User user = adapter.getItem(((AdapterContextMenuInfo) item
					.getMenuInfo()).position);
			moveToBlacklist(user.getUsername());
			return true;
		}
		return super.onContextItemSelected(item);
	}

	@Override
	public void onResume() {
		super.onResume();
		if (!hidden) {
			refresh();
		}
	}

	/**
	 * 删除联系人
	 * 
	 * @param toDeleteUser
	 */
	public void deleteContact(final User tobeDeleteUser) {
		final ProgressDialog pd = new ProgressDialog(this);
		pd.setMessage("正在删除...");
		pd.setCanceledOnTouchOutside(false);
		pd.show();
		new Thread(new Runnable() {
			public void run() {
				try {
					EMContactManager.getInstance().deleteContact(
							tobeDeleteUser.getUsername());
					// 删除db和内存中此用户的数据
					UserDao dao = new UserDao(ContactListActivity.this);
					dao.deleteContact(tobeDeleteUser.getUsername());
					VLCApplication.getInstance().getContactList()
							.remove(tobeDeleteUser.getUsername());
					ContactListActivity.this.runOnUiThread(new Runnable() {
						public void run() {
							pd.dismiss();
							adapter.remove(tobeDeleteUser);
							adapter.notifyDataSetChanged();

						}
					});
				} catch (final Exception e) {
					ContactListActivity.this.runOnUiThread(new Runnable() {
						public void run() {
							pd.dismiss();
							Toast.makeText(ContactListActivity.this,
									"删除失败: " + e.getMessage(), 1).show();
						}
					});

				}

			}
		}).start();

	}

	/**
	 * 把user移入到黑名单
	 */
	private void moveToBlacklist(final String username) {
		final ProgressDialog pd = new ProgressDialog(this);
		pd.setMessage("正在移入黑名单...");
		pd.setCanceledOnTouchOutside(false);
		pd.show();
		new Thread(new Runnable() {
			public void run() {
				try {
					// 加入到黑名单
					EMContactManager.getInstance().addUserToBlackList(username,
							false);
					ContactListActivity.this.runOnUiThread(new Runnable() {
						public void run() {
							pd.dismiss();
							Toast.makeText(ContactListActivity.this, "移入黑名单成功",
									0).show();
							refresh();
						}
					});
				} catch (EaseMobException e) {
					e.printStackTrace();
					ContactListActivity.this.runOnUiThread(new Runnable() {
						public void run() {
							pd.dismiss();
							Toast.makeText(ContactListActivity.this, "移入黑名单失败",
									0).show();
						}
					});
				}
			}
		}).start();

	}

	// 刷新ui
	public void refresh() {
		try {
			// 可能会在子线程中调到这方法
			this.runOnUiThread(new Runnable() {
				public void run() {
					getContactList();
					adapter.notifyDataSetChanged();

				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 获取联系人列表，并过滤掉黑名单和排序
	 */
	private void getContactList() {
		contactList.clear();
		// 获取本地好友列表
		Map<String, User> users = VLCApplication.getInstance().getContactList();
		Iterator<Entry<String, User>> iterator = users.entrySet().iterator();
		while (iterator.hasNext()) {
			Entry<String, User> entry = iterator.next();
			if (!entry.getKey().equals(Constant.NEW_FRIENDS_USERNAME)
					&& !entry.getKey().equals(Constant.GROUP_USERNAME)
					&& !blackList.contains(entry.getKey()))
				contactList.add(entry.getValue());
		}
		// 排序
		Collections.sort(contactList, new Comparator<User>() {

			@Override
			public int compare(User lhs, User rhs) {
				return lhs.getUsername().compareTo(rhs.getUsername());
			}
		});
	}

	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {

		case R.id.contactlist_back:
			this.finish();
			break;

		}
	}

	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		// 退出，则设置退出属性为ture
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
