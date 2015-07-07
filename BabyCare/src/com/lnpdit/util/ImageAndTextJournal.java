package com.lnpdit.util;



public class ImageAndTextJournal {

	private String imageUrl1;
	private String text_webid1;
	private String text_title1;
	private String text_pic1;
	private String text_content1;
	private String text_crtime1;
	
	private String imageUrl2;
	private String text_webid2;
	private String text_title2;
	private String text_pic2;
	private String text_content2;
	private String text_crtime2;

	public ImageAndTextJournal(String imageUrl1, String textwebid1,
			String texttitle1, String textpic1, String textcontent1,
			String textcrtime1, String imageUrl2, String textwebid2,
			String texttitle2, String textpic2, String textcontent2,
			String textcrtime2) {
		this.imageUrl1 = imageUrl1;
		this.text_webid1 = textwebid1;
		this.text_title1 = texttitle1;
		this.text_pic1 = textpic1;
		this.text_content1 = textcontent1;
		this.text_crtime1 = textcrtime1;
		
		this.imageUrl2 = imageUrl2;
		this.text_webid2 = textwebid2;
		this.text_title2 = texttitle2;
		this.text_pic2 = textpic2;
		this.text_content2 = textcontent2;
		this.text_crtime2 = textcrtime2;
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
	
	public String getImageUrl2() {
		return imageUrl2;
	}

	public String getWebId2() {
		return text_webid2;
	}

	public String getTitle2() {
		return text_title2;
	}

	public String getPic2() {
		return text_pic2;
	}

	public String getContent2() {
		return text_content2;
	}

	public String getCrtime2() {
		return text_crtime2;
	}
}
