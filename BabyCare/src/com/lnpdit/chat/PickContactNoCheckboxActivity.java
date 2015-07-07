package com.lnpdit.chat;



import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.videolan.libvlc.VLCApplication;

import com.lnpdit.babycare.BaseActivity;
import com.lnpdit.babycare.R;
import com.lnpdit.photo.Constant;
import com.lnpdit.sqllite.User;
import com.lnpdit.util.adapter.ContactAdapter;
import com.lnpdit.widget.Sidebar;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;


public class PickContactNoCheckboxActivity extends BaseActivity {

	private ListView listView;
	private Sidebar sidebar;
	protected ContactAdapter contactAdapter;
	private List<User> contactList;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_pick_contact_no_checkbox);
		listView = (ListView) findViewById(R.id.list);
		sidebar = (Sidebar) findViewById(R.id.sidebar);
		sidebar.setListView(listView);
		contactList = new ArrayList<User>();
		// 获取设置contactlist
		getContactList();
		// 设置adapter
		contactAdapter = new ContactAdapter(this, R.layout.row_contact, contactList, sidebar);
		listView.setAdapter(contactAdapter);
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				onListItemClick(position);
			}
		});

	}

	protected void onListItemClick(int position) {
		if (position != 0) {
			setResult(RESULT_OK, new Intent().putExtra("username", contactAdapter.getItem(position)
					.getUsername()));
			finish();
		}
	}

	public void back(View view) {
		finish();
	}

	private void getContactList() {
		contactList.clear();
		Map<String, User> users = VLCApplication.getInstance().getContactList();
		Iterator<Entry<String, User>> iterator = users.entrySet().iterator();
		while (iterator.hasNext()) {
			Entry<String, User> entry = iterator.next();
			if (!entry.getKey().equals(Constant.NEW_FRIENDS_USERNAME) && !entry.getKey().equals(Constant.GROUP_USERNAME))
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

}
