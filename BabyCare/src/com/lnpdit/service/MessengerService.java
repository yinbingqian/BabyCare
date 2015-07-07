package com.lnpdit.service;

import com.lnpdit.babycare.MainActivity;

public class MessengerService {

	/*
	 * 浜ゆ祦妯″潡,鐐瑰嚮鑱旂郴浜哄垪琛ㄤ腑ITEM骞挎挱鑱旂郴浜篒D涓庡锟�
	 */
	public static final String CONTACT_CHOOSE_CONTACT = "lnpditnews.broadcast.com.contact.choose";
	/*
	 * 璁剧疆甯冨眬鍙鍙傛暟
	 */
	public static final int VISIBILITY_TRUE = 1;
	public static final int VISIBILITY_FALSE = 8;
	/*
	 * 缃戠粶璁块棶瓒呮椂闄愬埗
	 */
	public static final int LOADING_TIME = 60000;
	/*
	 * Webservice璺緞
	 */
	public static final String NAMESPACE = "MobileNewspaper";
	// public static final String IP = "http://211.137.9.129:8001";
	public static final String URL = MainActivity.IP + "/phoneinvoke.asmx?wsdl";
	public static final String URL_WITHOUT_WSDL = MainActivity.IP
			+ "/phoneinvoke.asmx";
	public static final String PIC_FILE = MainActivity.IP + "/manage/pic/";
	public static final String PIC_JOURNAL = MainActivity.IP
			+ "/manage/magpic/";
	public static final String PIC_PUSH = MainActivity.IP + "/upload/";
	public static final String URL_SERVER = MainActivity.IP
			+ "/apksource/version.xml";
	public static final String VIDEO_PATH = MainActivity.IP
			+ "/manage/videofile/";
	public static final String AUDIO_PATH = MainActivity.IP + "/audio/";
	public static final String COL_PATH = MainActivity.IP + "/columns.xml";
	/*
	 * Webservice鏂规硶
	 */
	public static final String METHOD_GETDEPTLIST = "GetDeptList";
	public static final String METHOD_GETDEPTLISTBYID = "GetDeptListById";
	public static final String METHOD_GETADDRESSBOOKDISPLAY = "GetAddressBookDisplay";
	public static final String METHOD_GETADDRESSBOOKLIST = "GetAddressBookList";
	public static final String METHOD_USERINFOLOGINBYSIM = "UserInfoLoginBySim";
	public static final String METHOD_GETNEWSLIST = "GetNewsList";
	public static final String METHOD_QUESTIONADD = "QuestionAdd";
	public static final String METHOD_GETQUESTION = "GetQuestion";
	public static final String METHOD_GETANSWER = "GetAnswer";
	public static final String METHOD_NEWSTOUSERADD = "NewsToUserAdd";
	public static final String METHOD_GETCOMMUNICATION = "GetCommunication";
	public static final String METHOD_GETCOMUNREPLY = "GetCommunReply";
	public static final String METHOD_EDITIONCODE = "EditionCode";
	public static final String METHOD_COMMUNREPLYADD = "CommunReplyAdd";
	public static final String METHOD_COMMUNICATIONADD = "CommunicationAdd";
	public static final String METHOD_USERLOGADD = "UserLogAdd";
	public static final String METHOD_GETPUSHMESSAGE = "GetPushMessage";
	public static final String METHOD_GETNEWSPAGESIZE = "GetNewsPageSize";
	public static final String METHOD_USERINFOLOGIN = "UserInfoLogin";
	public static final String METHOD_GETUSERINFOBYCLASS = "GetUserInfoByClass";
	public static final String METHOD_INTERACTIONSUBMIT = "InteractionSubmit";
	public static final String METHOD_GETINTERACTIONMESSAGE = "GetInteractionMessage";
	public static final String METHOD_GETWEBURL = "GetWebUrl";
	public static final String METHOD_GETMAGAZINEINFO = "GetMagazineInfo";
	public static final String METHOD_GETMAGAZINEPICINFO = "GetMagazinePicInfo";
	public static final String METHOD_GETVIDEOINFO = "GetVideoInfo";
	public static final String METHOD_GETCOLUMNS = "GetColumns";
	public static final String METHOD_GETCOMMUNREPLYBYUSER = "GetCommunReplyByUser";
	public static final String METHOD_COMMUNREPLYREMOVE = "CommunReplyRemove";
	public static final String METHOD_GETLATESTNEWS = "GetLatestNews";
	public static final String METHOD_GETNEWSCONTENT = "GetNewsContent";

	public static final String WEEK = "week";

}
