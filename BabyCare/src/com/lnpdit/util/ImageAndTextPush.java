package com.lnpdit.util;

public class ImageAndTextPush {

	private String imageUrl;
	private String text_webid;
	private String text_remark;
	private String text_time;
	private String text_name;
	private String text_tel;
	private String text_type;
	private String text_audio;
	private String text_audio_length;
	private String text_dev_type;
	private String text_rcver_name;
	private String text_sender_id;
	private String text_local_id;
	private String text_user_id;

	public ImageAndTextPush(String imageUrl, String textwebid,
			String textremark, String texttime, String textname,
			String texttel, String texttype, String textaudio,
			String textaudioLength, String textdevType, String textrcverName,
			String textsenderId, String textlocalId, String textuserId) {
		this.imageUrl = imageUrl;
		this.text_webid = textwebid;
		this.text_remark = textremark;
		this.text_time = texttime;
		this.text_name = textname;
		this.text_tel = texttel;
		this.text_type = texttype;
		this.text_audio = textaudio;
		this.text_audio_length = textaudioLength;
		this.text_dev_type = textdevType;
		this.text_rcver_name = textrcverName;
		this.text_sender_id = textsenderId;
		this.text_local_id = textlocalId;
		this.text_user_id = textuserId;
	}

	public String getImageUrl() {
		return imageUrl;
	}

	public String getWebId() {
		return text_webid;
	}

	public String getRemark() {
		return text_remark;
	}

	public String getTime() {
		return text_time;
	}

	public String getName() {
		return text_name;
	}

	public String getTel() {
		return text_tel;
	}

	public String getType() {
		return text_type;
	}

	public String getAudio() {
		return text_audio;
	}

	public String getAudioLength() {
		return text_audio_length;
	}

	public String getDevType() {
		return text_dev_type;
	}

	public String getRcverName() {
		return text_rcver_name;
	}

	public String getSenderId() {
		return text_sender_id;
	}

	public String getLocalId() {
		return text_local_id;
	}

	public String getUserId() {
		return text_user_id;
	}
}
