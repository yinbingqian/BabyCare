package com.lnpdit.util;

import java.io.InputStream;

import org.xmlpull.v1.XmlPullParser;

import android.util.Xml;

public class UpdataInfoParser {

	/*
	 * 用pull解析器解析服务器返回的xml文件 (xml封装了版本号)
	 */
	public static UpdataInfo getUpdataInfo(InputStream is) throws Exception{
		XmlPullParser  parser = Xml.newPullParser();  
		parser.setInput(is, "utf-8");//设置解析的数据源 
		int type = parser.getEventType();
		UpdataInfo info = new UpdataInfo();//实体
		while(type != XmlPullParser.END_DOCUMENT ){
			switch (type) {
			case XmlPullParser.START_TAG:
				if("version".equals(parser.getName())){
					info.setVersion(parser.nextText());	//获取版本号
				}else if ("url".equals(parser.getName())){
					info.setUrl(parser.nextText());	//获取要升级的APK文件
				}else if ("description".equals(parser.getName())){
					info.setDescription(parser.nextText());	//获取该文件的信息
				}
				break;
			}
			type = parser.next();
		}
		return info;
	}
}
