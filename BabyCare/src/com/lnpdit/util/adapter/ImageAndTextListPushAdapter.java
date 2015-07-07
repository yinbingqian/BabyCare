package com.lnpdit.util.adapter;

import java.io.IOException;
import java.util.List;

import com.lnpdit.babycare.LoginActivity;
import com.lnpdit.babycare.R;
import com.lnpdit.garden.GardenPictureActivity;
import com.lnpdit.garden.GardenPushReplyActivity;
import com.lnpdit.garden.GardenPushTransActivity;
import com.lnpdit.service.MessengerService;
import com.lnpdit.sqllite.BBGJDB;
import com.lnpdit.util.AsyncImageLoader;
import com.lnpdit.util.AsyncImageLoader.ImageCallback;
import com.lnpdit.util.ImageAndTextPush;
import com.lnpdit.util.ViewCachePush;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


public class ImageAndTextListPushAdapter extends ArrayAdapter<ImageAndTextPush> {

	private ListView listView;
	private AsyncImageLoader asyncImageLoader;
	Context mContext;

	public ImageAndTextListPushAdapter(Activity activity,
			List<ImageAndTextPush> imageAndTexts, ListView listView,
			Context context) {
		super(activity, 0, imageAndTexts);
		this.listView = listView;
		this.mContext = context;
		asyncImageLoader = new AsyncImageLoader();
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		Activity activity = (Activity) getContext();

		// Inflate the views from XML
		View rowView = convertView;
		ViewCachePush viewCache;
		if (rowView == null) {
			LayoutInflater inflater = activity.getLayoutInflater();
			rowView = inflater.inflate(R.layout.list_in_com, null);
			viewCache = new ViewCachePush(rowView);
			rowView.setTag(viewCache);
		} else {
			viewCache = (ViewCachePush) rowView.getTag();
		}
		ImageAndTextPush imageAndText = getItem(position);

		// Load the image and set it on the ImageView
		String imageUrl = imageAndText.getImageUrl();
		ImageView imageView = viewCache.getImageView();
		imageView.setTag(imageUrl);
		Drawable cachedImage1 = asyncImageLoader.loadDrawable(imageUrl,
				new ImageCallback() {
					public void imageLoaded(Drawable imageDrawable,
							String imageUrl) {
						ImageView imageViewByTag = (ImageView) listView
								.findViewWithTag(imageUrl);
						if (imageViewByTag != null) {
							imageViewByTag.setImageDrawable(imageDrawable);
						}
					}
				});
		if (cachedImage1 == null) {
			// imageView.setImageResource(R.drawable.default_microhot);
		} else {
			imageView.setImageDrawable(cachedImage1);
		}
		// Set the text on the TextView
		TextView textViewName = viewCache.getTextViewName();
		textViewName.setText(imageAndText.getName());
		TextView textViewTime = viewCache.getTextViewTime();
		textViewTime.setText(imageAndText.getTime());
		TextView textViewDevType = viewCache.getTextViewDevType();
		String devTypeText = "";
		switch (Integer.parseInt(imageAndText.getDevType())) {
		case 0:
			devTypeText = "来自：系统平台";
			break;
		case 1:
			devTypeText = "来自：Android客户端";
			break;
		case 2:
			devTypeText = "来自：Iphone客户端";
			break;
		default:
			devTypeText = "来自：未知设备";
			break;
		}
		textViewDevType.setText(devTypeText);
		TextView textViewRcverName = viewCache.getTextViewRcverName();
		textViewRcverName.setText("@" + imageAndText.getRcverName());
		TextView textViewRemark = viewCache.getTextViewRemark();
		textViewRemark.setText(imageAndText.getRemark());
		Button audio_bt = viewCache.getButtonAudio();
		if (imageAndText.getAudio().startsWith("no")) {
			audio_bt.setBackgroundResource(R.drawable.list_no_audio);
			audio_bt.setTag("0");
		} else {
			audio_bt.setBackgroundResource(R.drawable.list_play_audio);
			audio_bt.setTag("1");
		}
		if (imageAndText.getImageUrl().endsWith("no.jpg")) {
			imageView.setBackgroundResource(R.drawable.push_img_blank_no);
		}
		// RelativeLayout transLayout = viewCache.getLayoutTrans();
		RelativeLayout replyLayout = viewCache.getLayoutReply();
		RelativeLayout deleteLayout = viewCache.getLayoutDelete();
		// transLayout.setClickable(true);
		deleteLayout.setClickable(true);

		imageView.setOnClickListener(new ImageListener(position, imageAndText
				.getImageUrl()));
		audio_bt.setOnClickListener(new AdapterListener(position, imageAndText
				.getWebId(), imageAndText.getRemark(), imageAndText
				.getImageUrl(), imageAndText.getTime(), imageAndText.getName(),
				imageAndText.getTel(), imageAndText.getType(), imageAndText
						.getAudio(), audio_bt.getTag().toString(), imageAndText
						.getAudioLength(), audio_bt));

		// rowView.setOnLongClickListener(new AdapterRemoveListener(position,
		// imageAndText.getWebId(), imageAndText.getRemark(), imageAndText
		// .getImageUrl(), imageAndText.getTime(), imageAndText
		// .getName(), imageAndText.getTel(), imageAndText
		// .getType(), imageAndText.getAudio()));
		deleteLayout.setOnClickListener(new AdapterRemoveListener(position,
				imageAndText.getWebId(), imageAndText.getRemark(), imageAndText
						.getImageUrl(), imageAndText.getTime(), imageAndText
						.getName(), imageAndText.getTel(), imageAndText
						.getType(), imageAndText.getAudio(), imageAndText
						.getLocalId()));
		replyLayout.setClickable(true);
		replyLayout.setOnClickListener(new AdapterReplyListener(position,
				imageAndText.getSenderId(), imageAndText.getName(),
				imageAndText.getUserId()));

		return rowView;
	}

	class AdapterListener implements OnClickListener {
		private int position;
		String t_id;
		String t_remark;
		String t_pic;
		String t_time;
		String t_name;
		String t_tel;
		String t_type;
		String t_audio;
		String t_audio_tag;
		String t_audio_length;
		Button t_audio_bt;

		public AdapterListener(int pos, String a_id, String a_remark,
				String a_pic, String a_time, String a_name, String a_tel,
				String a_type, String a_audio, String a_audio_tag,
				String a_audio_length, Button a_audio_bt) {
			// TODO Auto-generated constructor stub
			position = pos;
			t_id = a_id;
			t_remark = a_remark;
			t_pic = a_pic;
			t_time = a_time;
			t_name = a_name;
			t_tel = a_tel;
			t_type = a_type;
			t_audio = a_audio;
			t_audio_tag = a_audio_tag;
			t_audio_length = a_audio_length;
			t_audio_bt = a_audio_bt;
		}

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			if (t_audio_tag.equals("1")) {
				MediaPlayer mediaPlayer = new MediaPlayer();
				mediaPlayer
						.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

							@Override
							public void onCompletion(MediaPlayer mp) {
								// TODO Auto-generated method stub
								// Toast.makeText(mContext, "Media finished!",
								// Toast.LENGTH_SHORT).show();
								t_audio_bt.setClickable(true);
								t_audio_bt
										.setBackgroundResource(R.drawable.list_play_audio);
							}
						});
				try {
					mediaPlayer.setDataSource(MessengerService.AUDIO_PATH
							+ t_audio);
					mediaPlayer.prepare();
					mediaPlayer.start();
					// Toast.makeText(mContext, "start", Toast.LENGTH_SHORT)
					// .show();
					t_audio_bt.setClickable(false);
					t_audio_bt
							.setBackgroundResource(R.drawable.list_play_audio_playing);
				} catch (IllegalArgumentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalStateException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (Exception e) {
					// TODO: handle exception
				}
			}
		}

	}

	class ImageListener implements OnClickListener {
		private int position;
		String t_pic;

		public ImageListener(int pos, String a_pic) {
			// TODO Auto-generated constructor stub
			position = pos;
			t_pic = a_pic;
		}

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			Intent intent = new Intent();
			intent.setClass(mContext, GardenPictureActivity.class);
			intent.putExtra("PIC", t_pic);
			mContext.startActivity(intent);
		}

	}

	class AdapterTransListener implements OnClickListener {

		private int position;
		String t_pic;
		String t_audio;
		String t_audio_length;
		String t_content;

		public AdapterTransListener(int pos, String a_pic, String a_audio,
				String a_audio_length, String a_content) {
			// TODO Auto-generated constructor stub
			position = pos;
			this.t_pic = a_pic;
			this.t_audio = a_audio;
			this.t_audio_length = a_audio_length;
			this.t_content = a_content;
		}

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			BBGJDB tdd = new BBGJDB(mContext);
			Cursor cursor = tdd.selectuser();
			if (cursor.getCount() == 0) {
				Toast.makeText(mContext, "请先进行登录", Toast.LENGTH_SHORT).show();
				Intent intent = new Intent();
				intent.setClass(mContext, LoginActivity.class);
				mContext.startActivity(intent);
			} else {
				cursor.moveToFirst();
				Intent intent = new Intent();
				intent.putExtra("ID", cursor.getString(1)).toString();
				intent.putExtra("PIC", t_pic);
				intent.putExtra("AUDIO", t_audio);
				intent.putExtra("AUDIOLENGTH", t_audio_length);
				intent.putExtra("CONTENT", t_content);
				intent.setClass(mContext, GardenPushTransActivity.class);
				mContext.startActivity(intent);
			}
		}

	}

	class AdapterReplyListener implements OnClickListener {

		private int position;
		String t_senderid;
		String t_sendername;
		String t_userid;

		public AdapterReplyListener(int pos, String a_senderid,
				String a_sendername, String a_userid) {
			// TODO Auto-generated constructor stub
			position = pos;
			this.t_senderid = a_senderid;
			this.t_sendername = a_sendername;
			this.t_userid = a_userid;
		}

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			if(t_userid.equals(t_senderid)){
				Toast.makeText(mContext, "不能对自己进行回复", Toast.LENGTH_SHORT).show();
			}else{				
				BBGJDB tdd = new BBGJDB(mContext);
				Cursor cursor = tdd.selectuser();
				if (cursor.getCount() == 0) {
					Toast.makeText(mContext, "请先进行登录", Toast.LENGTH_SHORT).show();
					Intent intent = new Intent();
					intent.setClass(mContext, LoginActivity.class);
					mContext.startActivity(intent);
				} else {
					cursor.moveToFirst();
					Intent intent = new Intent();
					intent.putExtra("ID", cursor.getString(1)).toString();
					intent.putExtra("USERID", t_senderid);
					intent.putExtra("USERNAME", t_sendername);
					intent.setClass(mContext, GardenPushReplyActivity.class);
					mContext.startActivity(intent);
				}
			}
		}

	}

	class AdapterRemoveListener implements OnClickListener {
		private int position;
		String t_id;
		String t_remark;
		String t_pic;
		String t_time;
		String t_name;
		String t_tel;
		String t_type;
		String t_audio;
		String t_localId;

		public AdapterRemoveListener(int pos, String a_id, String a_remark,
				String a_pic, String a_time, String a_name, String a_tel,
				String a_type, String a_audio, String a_localId) {
			// TODO Auto-generated constructor stub
			position = pos;
			t_id = a_id;
			t_remark = a_remark;
			t_pic = a_pic;
			t_time = a_time;
			t_name = a_name;
			t_tel = a_tel;
			t_type = a_type;
			t_audio = a_audio;
			t_localId = a_localId;
		}

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			try {

				AlertDialog.Builder exitbuilder = new Builder(mContext);
				exitbuilder.setMessage("确定删除该条信息？");
				exitbuilder.setTitle("系统提示");
				exitbuilder.setPositiveButton("确定",
						new android.content.DialogInterface.OnClickListener() {

							public void onClick(DialogInterface dialog,
									int which) {
								// TODO Auto-generated method stub
								BBGJDB tdd = new BBGJDB(mContext);
								tdd.ClearOldPushById(t_localId);
								Toast.makeText(mContext, "删除成功",
										Toast.LENGTH_SHORT).show();
								dialog.dismiss();
								Intent intent = new Intent(
										"mulongtec.lnpditnews.and.com.list.refresh");
								mContext.sendBroadcast(intent);
							}
						});
				exitbuilder.setNegativeButton("取消",
						new android.content.DialogInterface.OnClickListener() {

							public void onClick(DialogInterface dialog,
									int which) {
								// TODO Auto-generated method stub
								dialog.dismiss();
							}
						});
				exitbuilder.show();
			} catch (Exception e) {
				// TODO: handle exception
			}
		}

	}

}
