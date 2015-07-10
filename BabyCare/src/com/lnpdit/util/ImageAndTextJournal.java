package com.lnpdit.util;

public class ImageAndTextJournal {

	private String imageUrl1;
	private String text_webid1;
	private String text_title1;
	private String text_pic1;
	private String text_content1;
	private String text_crtime1;

	public ImageAndTextJournal(String imageUrl1, String textwebid1,
			String texttitle1, String textpic1, String textcontent1,
			String textcrtime1) {
		this.imageUrl1 = imageUrl1;
		this.text_webid1 = textwebid1;
		this.text_title1 = texttitle1;
		this.text_pic1 = textpic1;
		this.text_content1 = textcontent1;
		this.text_crtime1 = textcrtime1;

	}

	public String getImageUrl1() {
		return imageUrl1;
	}

	public String getWebId1() {
		return text_webid1;
	}

	public String getTitle1() {
		return text_title1;
	}

	public String getPic1() {
		return text_pic1;
	}

	public String getContent1() {
		return text_content1;
	}

	public String getCrtime1() {
		return text_crtime1;
	}

}
