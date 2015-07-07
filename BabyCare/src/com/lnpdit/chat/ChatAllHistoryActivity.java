package com.lnpdit.chat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.List;

import org.bitlet.weupnp.Main;
import org.videolan.libvlc.VLCApplication;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMContact;
import com.easemob.chat.EMConversation;
import com.easemob.chat.EMGroup;
import com.easemob.chat.EMGroupManager;
import com.easemob.chat.EMMessage;
import com.lnpdit.babycare.MainActivity;
import com.lnpdit.babycare.R;
import com.lnpdit.garden.GardenComActivity;
import com.lnpdit.garden.GardenPushTempActivity;
import com.lnpdit.sqllite.InviteMessgeDao;
import com.lnpdit.util.adapter.ChatAllHistoryAdapter;

/**
 * 显示所有会话记录，比较简单的实现，更好的可能是把陌生人存入本地，这样取到的聊天记录是可控的
 * 
 */
public class ChatAllHistoryActivity extends Activity implements OnClickListener {

	private InputMethodManager inputMethodManager;
	private ListView listView;
	private ChatAllHistoryAdapter adapter;
	private EditText query;
	private ImageButton clearSearch;
	public RelativeLayout errorItem;
	public TextView errorText;
	private boolean hidden;
	private List<EMGroup> groups;
	private List<EMConversation> conversationList;
	private TextView gocontact;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.chat_history);

		inputMethodManager = (InputMethodManager) this
				.getSystemService(Context.INPUT_METHOD_SERVICE);
		errorItem = (RelativeLayout) this.findViewById(R.id.rl_error_item);
		errorText = (TextView) errorItem.findViewById(R.id.tv_connect_errormsg);

		conversationList = loadConversationsWithRecentChat();
		listView = (ListView) this.findViewById(R.id.list);
		adapter = new ChatAllHistoryAdapter(this, 1, conversationList);
		// 设置adapter
		listView.setAdapter(adapter);
		

		gocontact = (TextView) findViewById(R.id.gocontact);
		gocontact.setOnClickListener(this);

		groups = EMGroupManager.getInstance().getAllGroups();

		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				EMConversation conversation = adapter.getItem(position);
				String username = conversation.getUserName();
				if (username.equals(VLCApplication.getInstance().getUserName()))
					Toast.makeText(ChatAllHistoryActivity.this, "不能和自己聊天", 0)
							.show();
				else {
					// 进入聊天页面
					Intent intent = new Intent(ChatAllHistoryActivity.this,
							ChatActivity.class);
					EMContact emContact = null;
					groups = EMGroupManager.getInstance().getAllGroups();
					for (EMGroup group : groups) {
						if (group.getGroupId().equals(username)) {
							emContact = group;
							break;
						}
					}
					if (emContact != null && emContact instanceof EMGroup) {
						// it is group chat
						intent.putExtra("chatType", ChatActivity.CHATTYPE_GROUP);
						intent.putExtra("groupId",
								((EMGroup) emContact).getGroupId());
					} else {
						// it is single chat
						intent.putExtra("userId", username);
					}
					startActivity(intent);
				}
			}
		});
		// 注册上下文菜单
		registerForContextMenu(listView);

		listView.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// 隐藏软键盘
				hideSoftKeyboard();
				return false;
			}

		});
		// 搜索框
		query = (EditText) this.findViewById(R.id.query);
		// 搜索框中清除button
		clearSearch = (ImageButton) this.findViewById(R.id.search_clear);
		query.addTextChangedListener(new TextWatcher() {
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {

				adapter.getFilter().filter(s);
				if (s.length() > 0) {
					clearSearch.setVisibility(View.VISIBLE);
				} else {
					clearSearch.setVisibility(View.INVISIBLE);
				}
			}

			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			public void afterTextChanged(Editable s) {
			}
		});
		clearSearch.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				query.getText().clear();
				hideSoftKeyboard();
			}
		});
	}

	void hideSoftKeyboard() {
		if (this.getWindow().getAttributes().softInputMode != WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN) {
			if (this.getCurrentFocus() != null)
				inputMethodManager.hideSoftInputFromWindow(this
						.getCurrentFocus().getWindowToken(),
						InputMethodManager.HIDE_NOT_ALWAYS);
		}
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		// if(((AdapterContextMenuInfo)menuInfo).position > 0){ m,
		this.getMenuInflater().inflate(R.menu.delete_message, menu);
		// }
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.delete_message) {
			EMConversation tobeDeleteCons = adapter
					.getItem(((AdapterContextMenuInfo) item.getMenuInfo()).position);
			// 删除此会话
			EMChatManager.getInstance().deleteConversation(
					tobeDeleteCons.getUserName(), tobeDeleteCons.isGroup());
			InviteMessgeDao inviteMessgeDao = new InviteMessgeDao(this);
			inviteMessgeDao.deleteMessage(tobeDeleteCons.getUserName());
			adapter.remove(tobeDeleteCons);
			adapter.notifyDataSetChanged();

			// // 更新消息未读数
			// new MainActivity().updateUnreadLabel();
			return true;
		}
		return super.onContextItemSelected(item);
	}

	/**
	 * 刷新页面
	 */
	public void refresh() {
		conversationList.clear();
		conversationList.addAll(loadConversationsWithRecentChat());
		adapter.notifyDataSetChanged();
	}

	/**
	 * 获取所有会话
	 * 
	 * @param context
	 * @return
	 */
	private List<EMConversation> loadConversationsWithRecentChat() {
		// 获取所有会话，包括陌生人
		Hashtable<String, EMConversation> conversations = EMChatManager
				.getInstance().getAllConversations();
		List<EMConversation> list = new ArrayList<EMConversation>();
		// 过滤掉messages seize为0的conversation
		for (EMConversation conversation : conversations.values()) {
			if (conversation.getAllMessages().size() != 0)
				list.add(conversation);
		}
		// 排序
		sortConversationByLastChatTime(list);
		return list;
	}

	/**
	 * 根据最后一条消息的时间排序
	 * 
	 * @param usernames
	 */
	private void sortConversationByLastChatTime(
			List<EMConversation> conversationList) {
		Collections.sort(conversationList, new Comparator<EMConversation>() {
			@Override
			public int compare(final EMConversation con1,
					final EMConversation con2) {

				EMMessage con2LastMessage = con2.getLastMessage();
				EMMessage con1LastMessage = con1.getLastMessage();
				if (con2LastMessage.getMsgTime() == con1LastMessage
						.getMsgTime()) {
					return 0;
				} else if (con2LastMessage.getMsgTime() > con1LastMessage
						.getMsgTime()) {
					return 1;
				} else {
					return -1;
				}
			}

		});
	}
	

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.gocontact:

			Intent intent = new Intent();
			intent.setClass(ChatAllHistoryActivity.this,
					ContactListActivity.class);
			// intent.setClass(TabComActivity.this, PushEditActivity.class);
			startActivity(intent);

			break;

		default:
			break;
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		if (!hidden) {
			refresh();
		}
	}

}
