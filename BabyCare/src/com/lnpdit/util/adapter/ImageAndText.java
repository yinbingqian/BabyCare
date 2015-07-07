package com.lnpdit.util.adapter;



public class ImageAndText {

	private String imageUrl;
	private String text_content;
	private String text_time;
	private String text_title;
	private String text_source;
	private String text_author;
	private String text_pic;
	private String text_type;
	private String text_id;

	public ImageAndText(String imageUrl, String texttitle, String texttime,
			String textcontent, String textsource, String textauthor,
			String textpic, String texttype, String textid) {
		this.imageUrl = imageUrl;
		this.text_title = texttitle;
		this.text_time = texttime;
		this.text_content = textcontent;
		this.text_source = textsource;
		this.text_author = textauthor;
		this.text_pic = textpic;
		this.text_type = texttype;
		this.text_id = textid;
	}

	public String getImageUrl() {
		return imageUrl;
	}

	public String getTitle() {
		return text_title;
	}

	public String getTextContent() {
		return text_content;
	}

	public String getTextTime() {
		return text_time;
	}

	public String getSource() {
		return text_source;
	}

	public String getAuthor() {
		return text_author;
	}

	public String getPic() {
		return text_pic;
	}

	public String getType() {
		return text_type;
	}

	public String getId() {
		return text_id;
	}
}
