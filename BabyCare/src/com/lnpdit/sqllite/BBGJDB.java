package com.lnpdit.sqllite;

import android.R.integer;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class BBGJDB extends SQLiteOpenHelper {

	private final static String DATABASE_NAME = "bbgjDB";
	private final static int DATABASE_VERSION = 1;
	private final static String TABLE_NEWS = "news";
	private final static String TABLE_PUSH = "push";
	private final static String TABLE_OLDNEWS = "oldnews";
	private final static String TABLE_QA = "qa";
	private final static String TABLE_COMMUNICATION = "communication";
	private final static String TABLE_REPLY = "reply";
	private final static String TABLE_USERINFO = "userinfo";
	private final static String TABLE_CONTACTINFO = "contactinfo";
	private final static String TABLE_URL = "urlinfo";
	private final static String TABLE_JOURNAL = "journal";
	private final static String TABLE_JOURNAL_INFO = "journalinfo";
	private final static String TABLE_VIDEO_INFO = "videoinfo";
	private final static String TABLE_COLUMNINFO = "columninfo";
	private final static String TABLE_CONTACT_DEPT = "contactdept";
	private final static String TABLE_CONTACT_EMPLOYEES = "contactemployees";

	public static final String TYPE_PRIMARY_KEY = "INTEGER PRIMARY KEY AUTOINCREMENT";
	public static final String TYPE_TEXT = "TEXT";
	public static final String TYPE_DATETIME = "DATETIME";
	public static final String TYPE_INT = "INT";

	public static final String NEWS_ID = "NEWS_ID";
	public static final String NEWS_TITLE = "NEWS_TITLE";
	public static final String NEWS_SOURCE = "NEWS_SOURCE";
	public static final String NEWS_AUTHOR = "NEWS_AUTHOR";
	public static final String NEWS_PICTURE = "NEWS_PICTURE";
	public static final String NEWS_CONTENT = "NEWS_CONTENT";
	public static final String NEWS_CREATETIME = "NEWS_CREATETIME";
	public static final String NEWS_TYPE = "NEWS_TYPE";
	public static final String NEWS_WEBID = "NEWS_WEBID";
	public static final String NEWS_THUMBNAIL = "NEWS_THUMBNAIL";

	public static final String PUSH_ID = "PUSH_ID";
	public static final String PUSH_WEBID = "PUSH_WEBID";
	public static final String PUSH_PIC = "PUSH_PIC";
	public static final String PUSH_REMARK = "PUSH_REMARK";
	public static final String PUSH_TIME = "PUSH_TIME";
	public static final String PUSH_SENDER_NAME = "PUSH_SENDER_NAME";
	public static final String PUSH_SENDER_TEL = "PUSH_SENDER_TEL";
	public static final String PUSH_SENDER_TYPE = "PUSH_SENDER_TYPE";
	public static final String PUSH_AUDIO = "PUSH_AUDIO";
	public static final String PUSH_AUDIO_LENGTH = "PUSH_AUDIO_LENGTH";
	public static final String PUSH_DEV_TYPE = "PUSH_DEV_TYPE";
	public static final String PUSH_RCVER_NAME = "PUSH_RCVER_NAME";
	public static final String PUSH_SENDER_ID = "PUSH_SENDER_ID";

	public static final String JOURNAL_ID = "JOURNAL_ID";
	public static final String JOURNAL_WEBID = "JOURNAL_WEBID";
	public static final String JOURNAL_TITLE = "JOURNAL_TITLE";
	public static final String JOURNAL_PIC = "JOURNAL_PIC";
	public static final String JOURNAL_CONTENT = "JOURNAL_CONTENT";
	public static final String JOURNAL_CRTIME = "JOURNAL_CRTIME";

	public static final String COL_ID = "COL_ID";
	public static final String COL_WEBID = "COL_WEBID";
	public static final String COL_VALUE = "COL_VALUE";
	public static final String COL_TITLE = "COL_TITLE";
	public static final String COL_ORDERS = "COL_ORDERS";
	public static final String COL_PARENTID = "COL_PARENTID";
	public static final String COL_TEMPLATE = "COL_TEMPLATE";
	public static final String COL_REMARK = "COL_REMARK";

	public static final String VIDEO_ID = "VIDEO_ID";
	public static final String VIDEO_WEBID = "VIDEO_WEBID";
	public static final String VIDEO_TITLE = "VIDEO_TITLE";
	public static final String VIDEO_PIC = "VIDEO_PIC";
	public static final String VIDEO_VIDEO = "VIDEO_VIDEO";
	public static final String VIDEO_EXTENSION = "VIDEO_EXTENSION";
	public static final String VIDEO_SIZE = "VIDEO_SIZE";
	public static final String VIDEO_CONTENT = "VIDEO_CONTENT";
	public static final String VIDEO_CRTIME = "VIDEO_CRTIME";

	public static final String JOURNAL_INFO_ID = "JOURNAL_INFO_ID";
	public static final String JOURNAL_INFO_WEBID = "JOURNAL_INFO_WEBID";
	public static final String JOURNAL_INFO_JID = "JOURNAL_INFO_JID";
	public static final String JOURNAL_INFO_PIC = "JOURNAL_INFO_PIC";
	public static final String JOURNAL_INFO_ORDER = "JOURNAL_INFO_ORDER";

	public static final String URL_ID = "URL_ID";
	public static final String URL_WEBID = "URL_WEBID";
	public static final String URL_TITLE = "URL_TITLE";
	public static final String URL_PIC = "URL_PIC";
	public static final String URL_URL = "URL_URL";
	public static final String URL_DETAIL = "URL_DETAIL";
	public static final String URL_CRTIME = "URL_CRTIME";

	public static final String OLD_ID = "OLD_ID";
	public static final String OLD_TITLE = "OLD_TITLE";
	public static final String OLD_SOURCE = "OLD_SOURCE";
	public static final String OLD_AUTHOR = "OLD_AUTHOR";
	public static final String OLD_PICTURE = "OLD_PICTURE";
	public static final String OLD_CONTENT = "OLD_CONTENT";
	public static final String OLD_CREATETIME = "OLD_CREATETIME";
	public static final String OLD_TYPE = "OLD_TYPE";
	public static final String OLD_WEBID = "OLD_WEBID";

	public static final String QA_ID = "QA_ID";
	public static final String QA_QUESTION = "QA_QUESTION";
	public static final String QA_QTIME = "QA_QTIME";
	public static final String QA_ISANSWER = "QA_ISANSWER";
	public static final String QA_NAME = "QA_NAME";
	public static final String QA_ATIME = "QA_ATIME";
	public static final String QA_ACONTENT = "QA_ACONTENT";
	public static final String QA_WEBID = "QA_WEBID";
	public static final String QA_TYPE = "QA_TYPE";

	public static final String COM_ID = "COM_ID";
	public static final String COM_TITLE = "COM_TITLE";
	public static final String COM_CONTENT = "COM_CONTENT";
	public static final String COM_TIME = "COM_TIME";
	public static final String COM_WEBID = "COM_WEBID";
	public static final String COM_RPLNUM = "COM_RPLNUM";

	public static final String RPL_ID = "RPL_ID";
	public static final String RPL_COMID = "RPL_COMID";
	public static final String RPL_NAME = "RPL_NAME";
	public static final String RPL_TIME = "RPL_TIME";
	public static final String RPL_CONTENT = "RPL_CONTENT";
	public static final String RPL_WEBID = "RPL_WEBID";

	public static final String USER_ID = "USER_ID";
	public static final String USER_WEBID = "USER_WENID";
	public static final String USER_NAME = "USER_NAME";
	public static final String USER_IMSI = "USER_IMSI";
	public static final String USER_VERSION = "USER_VERSION";
	public static final String USER_PUSHID = "USER_PUSHID";
	public static final String USER_REFRESH_RATE = "USER_REFRESH_RATE";

	public static final String CONTACT_ID = "CONTACT_ID";
	public static final String CONTACT_WEBID = "CONTACT_WEBID";
	public static final String CONTACT_NAME = "CONTACT_NAME";
	public static final String CONTACT_TEL = "CONTACT_TEL";
	public static final String CONTACT_TYPE = "CONTACT_TYPE";

	public static final String DEPT_ID = "DEPT_ID";
	public static final String DEPT_WEBID = "DEPT_WEBID";
	public static final String DEPT_GRADE = "DEPT_GRADE";
	public static final String DEPT_CLASS = "DEPT_CLASS";
	public static final String DEPT_CRTIME = "DEPT_CRTIME";
	public static final String DEPT_REMARK = "DEPT_REMARK";

	public static final String EMP_ID = "EMP_ID";
	public static final String EMP_WEBID = "EMP_WEBID";
	public static final String EMP_NAME = "EMP_NAME";
	public static final String EMP_DEPTID = "EMP_DEPTID";
	public static final String EMP_MOBILEPHONE = "EMP_MOBILEPHONE";
	public static final String EMP_PHONE = "PHONE";
	public static final String EMP_MAIL = "EMP_MAIL";

	public BBGJDB(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		String create_table_news = "CREATE TABLE" + " " + TABLE_NEWS + "("
				+ NEWS_ID + " " + TYPE_PRIMARY_KEY + "," + NEWS_TITLE + " "
				+ TYPE_TEXT + "," + NEWS_SOURCE + " " + TYPE_TEXT + ","
				+ NEWS_AUTHOR + " " + TYPE_TEXT + "," + NEWS_PICTURE + " "
				+ TYPE_TEXT + "," + NEWS_CONTENT + " " + TYPE_TEXT + ","
				+ NEWS_CREATETIME + " " + TYPE_TEXT + "," + NEWS_TYPE + " "
				+ TYPE_TEXT + "," + NEWS_WEBID + " " + TYPE_TEXT + ","
				+ NEWS_THUMBNAIL + " " + TYPE_TEXT + ")";

		String create_table_oldnews = "CREATE TABLE" + " " + TABLE_OLDNEWS
				+ "(" + OLD_ID + " " + TYPE_PRIMARY_KEY + "," + OLD_TITLE + " "
				+ TYPE_TEXT + "," + OLD_SOURCE + " " + TYPE_TEXT + ","
				+ OLD_AUTHOR + " " + TYPE_TEXT + "," + OLD_PICTURE + " "
				+ TYPE_TEXT + "," + OLD_CONTENT + " " + TYPE_TEXT + ","
				+ OLD_CREATETIME + " " + TYPE_TEXT + "," + OLD_TYPE + " "
				+ TYPE_TEXT + "," + OLD_WEBID + " " + TYPE_TEXT + ")";

		String create_table_qa = "CREATE TABLE" + " " + TABLE_QA + "(" + QA_ID
				+ " " + TYPE_PRIMARY_KEY + "," + QA_QUESTION + " " + TYPE_TEXT
				+ "," + QA_QTIME + " " + TYPE_TEXT + "," + QA_ISANSWER + " "
				+ TYPE_TEXT + "," + QA_NAME + " " + TYPE_TEXT + "," + QA_ATIME
				+ " " + TYPE_TEXT + "," + QA_ACONTENT + " " + TYPE_TEXT + ","
				+ QA_TYPE + " " + TYPE_TEXT + "," + QA_WEBID + " " + TYPE_TEXT
				+ ")";

		String create_table_communication = "CREATE TABLE" + " "
				+ TABLE_COMMUNICATION + "(" + COM_ID + " " + TYPE_PRIMARY_KEY
				+ "," + COM_TITLE + " " + TYPE_TEXT + "," + COM_CONTENT + " "
				+ TYPE_TEXT + "," + COM_TIME + " " + TYPE_TEXT + ","
				+ COM_RPLNUM + " " + TYPE_TEXT + "," + COM_WEBID + " "
				+ TYPE_TEXT + ")";

		String create_table_reply = "CREATE TABLE" + " " + TABLE_REPLY + "("
				+ RPL_ID + " " + TYPE_PRIMARY_KEY + "," + RPL_COMID + " "
				+ TYPE_TEXT + "," + RPL_NAME + " " + TYPE_TEXT + "," + RPL_TIME
				+ " " + TYPE_TEXT + "," + RPL_CONTENT + " " + TYPE_TEXT + ","
				+ RPL_WEBID + " " + TYPE_TEXT + ")";

		String create_table_userinfo = "CREATE TABLE" + " " + TABLE_USERINFO
				+ "(" + USER_ID + " " + TYPE_PRIMARY_KEY + "," + USER_WEBID
				+ " " + TYPE_TEXT + "," + USER_NAME + " " + TYPE_TEXT + ","
				+ USER_IMSI + " " + TYPE_TEXT + "," + USER_VERSION + " "
				+ TYPE_TEXT + "," + USER_PUSHID + " " + TYPE_TEXT + ","
				+ USER_REFRESH_RATE + " " + TYPE_TEXT + ")";

		String create_table_contactinfo = "CREATE TABLE" + " "
				+ TABLE_CONTACTINFO + "(" + CONTACT_ID + " " + TYPE_PRIMARY_KEY
				+ "," + CONTACT_WEBID + " " + TYPE_TEXT + "," + CONTACT_NAME
				+ " " + TYPE_TEXT + "," + CONTACT_TEL + " " + TYPE_TEXT + ","
				+ CONTACT_TYPE + " " + TYPE_TEXT + ")";

		String create_table_contactdept = "CREATE TABLE" + " "
				+ TABLE_CONTACT_DEPT + "(" + DEPT_ID + " " + TYPE_PRIMARY_KEY
				+ "," + DEPT_WEBID + " " + TYPE_TEXT + "," + DEPT_GRADE + " "
				+ TYPE_TEXT + "," + DEPT_CLASS + " " + TYPE_TEXT + ","
				+ DEPT_CRTIME + " " + TYPE_TEXT + "," + DEPT_REMARK + " "
				+ TYPE_TEXT + ")";

		String create_table_contactemp = "CREATE TABLE" + " "
				+ TABLE_CONTACT_EMPLOYEES + "(" + EMP_ID + " "
				+ TYPE_PRIMARY_KEY + "," + EMP_WEBID + " " + TYPE_TEXT + ","
				+ EMP_NAME + " " + TYPE_TEXT + "," + EMP_DEPTID + " "
				+ TYPE_TEXT + "," + EMP_MOBILEPHONE + " " + TYPE_TEXT + ","
				+ EMP_PHONE + " " + TYPE_TEXT + "," + EMP_MAIL + " "
				+ TYPE_TEXT + ")";

		String create_table_push = "CREATE TABLE" + " " + TABLE_PUSH + "("
				+ PUSH_ID + " " + TYPE_PRIMARY_KEY + "," + PUSH_WEBID + " "
				+ TYPE_TEXT + "," + PUSH_REMARK + " " + TYPE_TEXT + ","
				+ PUSH_PIC + " " + TYPE_TEXT + "," + PUSH_TIME + " "
				+ TYPE_TEXT + "," + PUSH_SENDER_NAME + " " + TYPE_TEXT + ","
				+ PUSH_SENDER_TEL + " " + TYPE_TEXT + "," + PUSH_SENDER_TYPE
				+ " " + TYPE_TEXT + "," + PUSH_AUDIO + " " + TYPE_TEXT + ","
				+ PUSH_AUDIO_LENGTH + " " + TYPE_TEXT + "," + PUSH_DEV_TYPE
				+ " " + TYPE_TEXT + "," + PUSH_RCVER_NAME + " " + TYPE_TEXT
				+ "," + PUSH_SENDER_ID + " " + TYPE_TEXT + ")";

		String create_table_url = "CREATE TABLE" + " " + TABLE_URL + "("
				+ URL_ID + " " + TYPE_PRIMARY_KEY + "," + URL_WEBID + " "
				+ TYPE_TEXT + "," + URL_TITLE + " " + TYPE_TEXT + "," + URL_PIC
				+ " " + TYPE_TEXT + "," + URL_URL + " " + TYPE_TEXT + ","
				+ URL_DETAIL + " " + TYPE_TEXT + "," + URL_CRTIME + " "
				+ TYPE_TEXT + ")";

		String create_table_journal = "CREATE TABLE" + " " + TABLE_JOURNAL
				+ "(" + JOURNAL_ID + " " + TYPE_PRIMARY_KEY + ","
				+ JOURNAL_WEBID + " " + TYPE_TEXT + "," + JOURNAL_TITLE + " "
				+ TYPE_TEXT + "," + JOURNAL_PIC + " " + TYPE_TEXT + ","
				+ JOURNAL_CONTENT + " " + TYPE_TEXT + "," + JOURNAL_CRTIME
				+ " " + TYPE_TEXT + ")";

		String create_table_journalinfo = "CREATE TABLE" + " "
				+ TABLE_JOURNAL_INFO + "(" + JOURNAL_INFO_ID + " "
				+ TYPE_PRIMARY_KEY + "," + JOURNAL_INFO_WEBID + " " + TYPE_TEXT
				+ "," + JOURNAL_INFO_JID + " " + TYPE_TEXT + ","
				+ JOURNAL_INFO_PIC + " " + TYPE_TEXT + "," + JOURNAL_INFO_ORDER
				+ " " + TYPE_TEXT + ")";

		String create_table_video = "CREATE TABLE" + " " + TABLE_VIDEO_INFO
				+ "(" + VIDEO_ID + " " + TYPE_PRIMARY_KEY + "," + VIDEO_WEBID
				+ " " + TYPE_TEXT + "," + VIDEO_TITLE + " " + TYPE_TEXT + ","
				+ VIDEO_PIC + " " + TYPE_TEXT + "," + VIDEO_VIDEO + " "
				+ TYPE_TEXT + "," + VIDEO_EXTENSION + " " + TYPE_TEXT + ","
				+ VIDEO_SIZE + " " + TYPE_TEXT + "," + VIDEO_CONTENT + " "
				+ TYPE_TEXT + "," + VIDEO_CRTIME + " " + TYPE_TEXT + ")";

		String create_table_col = "CREATE TABLE" + " " + TABLE_COLUMNINFO + "("
				+ COL_ID + " " + TYPE_PRIMARY_KEY + "," + COL_WEBID + " "
				+ TYPE_TEXT + "," + COL_VALUE + " " + TYPE_TEXT + ","
				+ COL_TITLE + " " + TYPE_TEXT + "," + COL_ORDERS + " "
				+ TYPE_TEXT + "," + COL_PARENTID + " " + TYPE_TEXT + ","
				+ COL_TEMPLATE + " " + TYPE_TEXT + "," + COL_REMARK + " "
				+ TYPE_TEXT + ")";

		db.execSQL(create_table_news);
		db.execSQL(create_table_qa);
		db.execSQL(create_table_communication);
		db.execSQL(create_table_reply);
		db.execSQL(create_table_oldnews);
		db.execSQL(create_table_userinfo);
		db.execSQL(create_table_contactinfo);
		db.execSQL(create_table_push);
		db.execSQL(create_table_url);
		db.execSQL(create_table_journal);
		db.execSQL(create_table_journalinfo);
		db.execSQL(create_table_video);
		db.execSQL(create_table_col);
		db.execSQL(create_table_contactdept);
		db.execSQL(create_table_contactemp);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		String sqlnews = "DROP TABLE IF EXISTS " + TABLE_NEWS;
		String sqlqa = "DROP TABLE IF EXISTS " + TABLE_QA;
		String sqlcommunication = "DROP TABLE IF EXISTS " + TABLE_COMMUNICATION;
		String sqlreply = "DROP TABLE IF EXISTS " + TABLE_REPLY;
		String sqlurl = "DROP TABLE IF EXISTS " + TABLE_URL;
		String sqljournal = "DROP TABLE IF EXISTS " + TABLE_JOURNAL;
		String sqljournalinfo = "DROP TABLE IF EXISTS " + TABLE_JOURNAL_INFO;
		String sqlvedio = "DROP TABLE IF EXISTS " + TABLE_VIDEO_INFO;
		db.execSQL(sqlnews);
		db.execSQL(sqlqa);
		db.execSQL(sqlcommunication);
		db.execSQL(sqlreply);
		db.execSQL(sqlurl);
		db.execSQL(sqljournal);
		db.execSQL(sqljournalinfo);
		db.execSQL(sqlvedio);
		onCreate(db);
	}

	public void createdb(SQLiteDatabase db) {
		String create_table_news = "CREATE TABLE" + " " + TABLE_NEWS + "("
				+ NEWS_ID + " " + TYPE_PRIMARY_KEY + "," + NEWS_TITLE + " "
				+ TYPE_TEXT + "," + NEWS_SOURCE + " " + TYPE_TEXT + ","
				+ NEWS_AUTHOR + " " + TYPE_TEXT + "," + NEWS_PICTURE + " "
				+ TYPE_TEXT + "," + NEWS_CONTENT + " " + TYPE_TEXT + ","
				+ NEWS_CREATETIME + " " + TYPE_TEXT + "," + NEWS_TYPE + " "
				+ TYPE_TEXT + "," + NEWS_WEBID + " " + TYPE_TEXT + ","
				+ NEWS_THUMBNAIL + " " + TYPE_TEXT + ")";

		String create_table_qa = "CREATE TABLE" + " " + TABLE_QA + "(" + QA_ID
				+ " " + TYPE_PRIMARY_KEY + "," + QA_QUESTION + " " + TYPE_TEXT
				+ "," + QA_QTIME + " " + TYPE_TEXT + "," + QA_ISANSWER + " "
				+ TYPE_TEXT + "," + QA_NAME + " " + TYPE_TEXT + "," + QA_ATIME
				+ " " + TYPE_TEXT + "," + QA_ACONTENT + " " + TYPE_TEXT + ","
				+ QA_TYPE + " " + TYPE_TEXT + "," + QA_WEBID + " " + TYPE_TEXT
				+ ")";

		String create_table_communication = "CREATE TABLE" + " "
				+ TABLE_COMMUNICATION + "(" + COM_ID + " " + TYPE_PRIMARY_KEY
				+ "," + COM_TITLE + " " + TYPE_TEXT + "," + COM_CONTENT + " "
				+ TYPE_TEXT + "," + COM_TIME + " " + TYPE_TEXT + ","
				+ COM_RPLNUM + " " + TYPE_TEXT + "," + COM_WEBID + " "
				+ TYPE_TEXT + ")";

		String create_table_reply = "CREATE TABLE" + " " + TABLE_REPLY + "("
				+ RPL_ID + " " + TYPE_PRIMARY_KEY + "," + RPL_COMID + " "
				+ TYPE_TEXT + "," + RPL_NAME + " " + TYPE_TEXT + "," + RPL_TIME
				+ " " + TYPE_TEXT + "," + RPL_CONTENT + " " + TYPE_TEXT + ","
				+ RPL_WEBID + " " + TYPE_TEXT + ")";

		String create_table_url = "CREATE TABLE" + " " + TABLE_URL + "("
				+ URL_ID + " " + TYPE_PRIMARY_KEY + "," + URL_WEBID + " "
				+ TYPE_TEXT + "," + URL_TITLE + " " + TYPE_TEXT + "," + URL_PIC
				+ " " + TYPE_TEXT + "," + URL_URL + " " + TYPE_TEXT + ","
				+ URL_DETAIL + " " + TYPE_TEXT + "," + URL_CRTIME + " "
				+ TYPE_TEXT + ")";

		String create_table_userinfo = "CREATE TABLE" + " " + TABLE_USERINFO
				+ "(" + USER_ID + " " + TYPE_PRIMARY_KEY + "," + USER_WEBID
				+ " " + TYPE_TEXT + "," + USER_NAME + " " + TYPE_TEXT + ","
				+ USER_IMSI + " " + TYPE_TEXT + "," + USER_VERSION + " "
				+ TYPE_TEXT + "," + USER_PUSHID + " " + TYPE_TEXT + ","
				+ USER_REFRESH_RATE + " " + TYPE_TEXT + ")";

		String create_table_push = "CREATE TABLE" + " " + TABLE_PUSH + "("
				+ PUSH_ID + " " + TYPE_PRIMARY_KEY + "," + PUSH_WEBID + " "
				+ TYPE_TEXT + "," + PUSH_REMARK + " " + TYPE_TEXT + ","
				+ PUSH_PIC + " " + TYPE_TEXT + "," + PUSH_TIME + " "
				+ TYPE_TEXT + "," + PUSH_SENDER_NAME + " " + TYPE_TEXT + ","
				+ PUSH_SENDER_TEL + " " + TYPE_TEXT + "," + PUSH_SENDER_TYPE
				+ " " + TYPE_TEXT + "," + PUSH_AUDIO + " " + TYPE_TEXT + ","
				+ PUSH_AUDIO_LENGTH + " " + TYPE_TEXT + "," + PUSH_DEV_TYPE
				+ " " + TYPE_TEXT + "," + PUSH_RCVER_NAME + " " + TYPE_TEXT
				+ "," + PUSH_SENDER_ID + " " + TYPE_TEXT + ")";

		String create_table_contactinfo = "CREATE TABLE" + " "
				+ TABLE_CONTACTINFO + "(" + CONTACT_ID + " " + TYPE_PRIMARY_KEY
				+ "," + CONTACT_WEBID + " " + TYPE_TEXT + "," + CONTACT_NAME
				+ " " + TYPE_TEXT + "," + CONTACT_TEL + " " + TYPE_TEXT + ","
				+ CONTACT_TYPE + " " + TYPE_TEXT + ")";

		String create_table_journal = "CREATE TABLE" + " " + TABLE_JOURNAL
				+ "(" + JOURNAL_ID + " " + TYPE_PRIMARY_KEY + ","
				+ JOURNAL_WEBID + " " + TYPE_TEXT + "," + JOURNAL_TITLE + " "
				+ TYPE_TEXT + "," + JOURNAL_PIC + " " + TYPE_TEXT + ","
				+ JOURNAL_CONTENT + " " + TYPE_TEXT + "," + JOURNAL_CRTIME
				+ " " + TYPE_TEXT + ")";

		String create_table_journalinfo = "CREATE TABLE" + " "
				+ TABLE_JOURNAL_INFO + "(" + JOURNAL_INFO_ID + " "
				+ TYPE_PRIMARY_KEY + "," + JOURNAL_INFO_WEBID + " " + TYPE_TEXT
				+ "," + JOURNAL_INFO_JID + " " + TYPE_TEXT + ","
				+ JOURNAL_INFO_PIC + " " + TYPE_TEXT + "," + JOURNAL_INFO_ORDER
				+ " " + TYPE_TEXT + ")";

		String create_table_video = "CREATE TABLE" + " " + TABLE_VIDEO_INFO
				+ "(" + VIDEO_ID + " " + TYPE_PRIMARY_KEY + "," + VIDEO_WEBID
				+ " " + TYPE_TEXT + "," + VIDEO_TITLE + " " + TYPE_TEXT + ","
				+ VIDEO_PIC + " " + TYPE_TEXT + "," + VIDEO_VIDEO + " "
				+ TYPE_TEXT + "," + VIDEO_EXTENSION + " " + TYPE_TEXT + ","
				+ VIDEO_SIZE + " " + TYPE_TEXT + "," + VIDEO_CONTENT + " "
				+ TYPE_TEXT + "," + VIDEO_CRTIME + " " + TYPE_TEXT + ")";

		db.execSQL(create_table_news);
		db.execSQL(create_table_qa);
		db.execSQL(create_table_communication);
		db.execSQL(create_table_reply);
		db.execSQL(create_table_contactinfo);
		db.execSQL(create_table_url);
		db.execSQL(create_table_journal);
		db.execSQL(create_table_journalinfo);
		db.execSQL(create_table_video);
	}

	public void cleardb() {
		SQLiteDatabase db = this.getReadableDatabase();
		String sqlnews = "DROP TABLE IF EXISTS " + TABLE_NEWS;
		String sqlqa = "DROP TABLE IF EXISTS " + TABLE_QA;
		String sqlcommunication = "DROP TABLE IF EXISTS " + TABLE_COMMUNICATION;
		String sqlreply = "DROP TABLE IF EXISTS " + TABLE_REPLY;
		String sqlcontact = "DROP TABLE IF EXISTS " + TABLE_CONTACTINFO;
		String sqlurl = "DROP TABLE IF EXISTS " + TABLE_URL;
		String sqljournal = "DROP TABLE IF EXISTS " + TABLE_JOURNAL;
		String sqljournalinfo = "DROP TABLE IF EXISTS " + TABLE_JOURNAL_INFO;
		String sqlvideo = "DROP TABLE IF EXISTS " + TABLE_VIDEO_INFO;
		db.execSQL(sqlnews);
		db.execSQL(sqlqa);
		db.execSQL(sqlcommunication);
		db.execSQL(sqlreply);
		db.execSQL(sqlcontact);
		db.execSQL(sqlurl);
		db.execSQL(sqljournal);
		db.execSQL(sqljournalinfo);
		db.execSQL(sqlvideo);
		createdb(db);
	}

	public void cleardbusr() {
		SQLiteDatabase db = this.getReadableDatabase();
		String sqlusr = "DROP TABLE IF EXISTS " + TABLE_USERINFO;
		String create_table_userinfo = "CREATE TABLE" + " " + TABLE_USERINFO
				+ "(" + USER_ID + " " + TYPE_PRIMARY_KEY + "," + USER_WEBID
				+ " " + TYPE_TEXT + "," + USER_NAME + " " + TYPE_TEXT + ","
				+ USER_IMSI + " " + TYPE_TEXT + "," + USER_VERSION + " "
				+ TYPE_TEXT + "," + USER_PUSHID + " " + TYPE_TEXT + ","
				+ USER_REFRESH_RATE + " " + TYPE_TEXT + ")";
		db.execSQL(sqlusr);
		db.execSQL(create_table_userinfo);
	}

	public void clearnews() {
		SQLiteDatabase db = this.getReadableDatabase();
		String sqlnews = "DROP TABLE IF EXISTS " + TABLE_NEWS;
		String create_table_news = "CREATE TABLE" + " " + TABLE_NEWS + "("
				+ NEWS_ID + " " + TYPE_PRIMARY_KEY + "," + NEWS_TITLE + " "
				+ TYPE_TEXT + "," + NEWS_SOURCE + " " + TYPE_TEXT + ","
				+ NEWS_AUTHOR + " " + TYPE_TEXT + "," + NEWS_PICTURE + " "
				+ TYPE_TEXT + "," + NEWS_CONTENT + " " + TYPE_TEXT + ","
				+ NEWS_CREATETIME + " " + TYPE_TEXT + "," + NEWS_TYPE + " "
				+ TYPE_TEXT + "," + NEWS_WEBID + " " + TYPE_TEXT + ","
				+ NEWS_THUMBNAIL + " " + TYPE_TEXT + ")";
		db.execSQL(sqlnews);
		db.execSQL(create_table_news);
	}

	public void clearcol() {
		SQLiteDatabase db = this.getReadableDatabase();
		String sqlcol = "DROP TABLE IF EXISTS " + TABLE_COLUMNINFO;
		String create_table_col = "CREATE TABLE" + " " + TABLE_COLUMNINFO + "("
				+ COL_ID + " " + TYPE_PRIMARY_KEY + "," + COL_WEBID + " "
				+ TYPE_TEXT + "," + COL_VALUE + " " + TYPE_TEXT + ","
				+ COL_TITLE + " " + TYPE_TEXT + "," + COL_ORDERS + " "
				+ TYPE_TEXT + "," + COL_PARENTID + " " + TYPE_TEXT + ","
				+ COL_TEMPLATE + " " + TYPE_TEXT + "," + COL_REMARK + " "
				+ TYPE_TEXT + ")";
		db.execSQL(sqlcol);
		db.execSQL(create_table_col);
	}

	public void clearreply() {
		SQLiteDatabase db = this.getReadableDatabase();
		String sqlreply = "DROP TABLE IF EXISTS " + TABLE_REPLY;
		String create_table_reply = "CREATE TABLE" + " " + TABLE_REPLY + "("
				+ RPL_ID + " " + TYPE_PRIMARY_KEY + "," + RPL_COMID + " "
				+ TYPE_TEXT + "," + RPL_NAME + " " + TYPE_TEXT + "," + RPL_TIME
				+ " " + TYPE_TEXT + "," + RPL_CONTENT + " " + TYPE_TEXT + ","
				+ RPL_WEBID + " " + TYPE_TEXT + ")";
		db.execSQL(sqlreply);
		db.execSQL(create_table_reply);
	}

	public void clearSetContact() {
		SQLiteDatabase db = this.getReadableDatabase();
		String drop_dept = "DROP TABLE IF EXISTS " + TABLE_CONTACT_DEPT;
		String drop_emp = "DROP TABLE IF EXISTS " + TABLE_CONTACT_EMPLOYEES;
		String create_table_contactdept = "CREATE TABLE" + " "
				+ TABLE_CONTACT_DEPT + "(" + DEPT_ID + " " + TYPE_PRIMARY_KEY
				+ "," + DEPT_WEBID + " " + TYPE_TEXT + "," + DEPT_GRADE + " "
				+ TYPE_TEXT + "," + DEPT_CLASS + " " + TYPE_TEXT + ","
				+ DEPT_CRTIME + " " + TYPE_TEXT + "," + DEPT_REMARK + " "
				+ TYPE_TEXT + ")";

		String create_table_contactemp = "CREATE TABLE" + " "
				+ TABLE_CONTACT_EMPLOYEES + "(" + EMP_ID + " "
				+ TYPE_PRIMARY_KEY + "," + EMP_WEBID + " " + TYPE_TEXT + ","
				+ EMP_NAME + " " + TYPE_TEXT + "," + EMP_DEPTID + " "
				+ TYPE_TEXT + "," + EMP_MOBILEPHONE + " " + TYPE_TEXT + ","
				+ EMP_PHONE + " " + TYPE_TEXT + "," + EMP_MAIL + " "
				+ TYPE_TEXT + ")";
		db.execSQL(drop_dept);
		db.execSQL(drop_emp);
		db.execSQL(create_table_contactdept);
		db.execSQL(create_table_contactemp);
	}

	public void clearjournalinfo() {
		SQLiteDatabase db = this.getReadableDatabase();
		String sqljournalinfo = "DROP TABLE IF EXISTS " + TABLE_JOURNAL_INFO;
		String create_table_journalinfo = "CREATE TABLE" + " "
				+ TABLE_JOURNAL_INFO + "(" + JOURNAL_INFO_ID + " "
				+ TYPE_PRIMARY_KEY + "," + JOURNAL_INFO_WEBID + " " + TYPE_TEXT
				+ "," + JOURNAL_INFO_JID + " " + TYPE_TEXT + ","
				+ JOURNAL_INFO_PIC + " " + TYPE_TEXT + "," + JOURNAL_INFO_ORDER
				+ " " + TYPE_TEXT + ")";
		db.execSQL(sqljournalinfo);
		db.execSQL(create_table_journalinfo);
	}

	public void clearjournallistinfo() {
		SQLiteDatabase db = this.getReadableDatabase();
		String sqljournal = "DROP TABLE IF EXISTS " + TABLE_JOURNAL;
		String sqljournalinfo = "DROP TABLE IF EXISTS " + TABLE_JOURNAL_INFO;
		String sqlvideo = "DROP TABLE IF EXISTS " + TABLE_VIDEO_INFO;
		String create_table_journal = "CREATE TABLE" + " " + TABLE_JOURNAL
				+ "(" + JOURNAL_ID + " " + TYPE_PRIMARY_KEY + ","
				+ JOURNAL_WEBID + " " + TYPE_TEXT + "," + JOURNAL_TITLE + " "
				+ TYPE_TEXT + "," + JOURNAL_PIC + " " + TYPE_TEXT + ","
				+ JOURNAL_CONTENT + " " + TYPE_TEXT + "," + JOURNAL_CRTIME
				+ " " + TYPE_TEXT + ")";

		String create_table_journalinfo = "CREATE TABLE" + " "
				+ TABLE_JOURNAL_INFO + "(" + JOURNAL_INFO_ID + " "
				+ TYPE_PRIMARY_KEY + "," + JOURNAL_INFO_WEBID + " " + TYPE_TEXT
				+ "," + JOURNAL_INFO_JID + " " + TYPE_TEXT + ","
				+ JOURNAL_INFO_PIC + " " + TYPE_TEXT + "," + JOURNAL_INFO_ORDER
				+ " " + TYPE_TEXT + ")";

		String create_table_video = "CREATE TABLE" + " " + TABLE_VIDEO_INFO
				+ "(" + VIDEO_ID + " " + TYPE_PRIMARY_KEY + "," + VIDEO_WEBID
				+ " " + TYPE_TEXT + "," + VIDEO_TITLE + " " + TYPE_TEXT + ","
				+ VIDEO_PIC + " " + TYPE_TEXT + "," + VIDEO_VIDEO + " "
				+ TYPE_TEXT + "," + VIDEO_EXTENSION + " " + TYPE_TEXT + ","
				+ VIDEO_SIZE + " " + TYPE_TEXT + "," + VIDEO_CONTENT + " "
				+ TYPE_TEXT + "," + VIDEO_CRTIME + " " + TYPE_TEXT + ")";
		db.execSQL(sqljournal);
		db.execSQL(sqljournalinfo);
		db.execSQL(sqlvideo);
		db.execSQL(create_table_journal);
		db.execSQL(create_table_journalinfo);
		db.execSQL(create_table_video);
	}

	public void clearcontact() {
		SQLiteDatabase db = this.getReadableDatabase();
		String clear_contact_info = "DROP TABLE IF EXISTS " + TABLE_CONTACTINFO;
		String create_table_contactinfo = "CREATE TABLE" + " "
				+ TABLE_CONTACTINFO + "(" + CONTACT_ID + " " + TYPE_PRIMARY_KEY
				+ "," + CONTACT_WEBID + " " + TYPE_TEXT + "," + CONTACT_NAME
				+ " " + TYPE_TEXT + "," + CONTACT_TEL + " " + TYPE_TEXT + ","
				+ CONTACT_TYPE + " " + TYPE_TEXT + ")";
		db.execSQL(clear_contact_info);
		db.execSQL(create_table_contactinfo);
	}

	public Cursor selectnews() {
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db
				.query(TABLE_NEWS, null, null, null, null, null, "NEWS_CREATETIME desc");
		return cursor;
	}

	public Cursor selectNewsByWebid(String webid) {
		SQLiteDatabase db = this.getReadableDatabase();
		String selection = NEWS_WEBID + " =?";
		Cursor cursor = db.query(TABLE_NEWS, null, selection, null, null, null,
				null);
		return cursor;
	}

	public Cursor selectcol() {
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.query(TABLE_COLUMNINFO, null, null, null, null,
				null, null);
		return cursor;
	}

	public Cursor selectvideo() {
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.query(TABLE_VIDEO_INFO, null, null, null, null,
				null, null);
		return cursor;
	}

	public Cursor selectpush() {
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db
				.query(TABLE_PUSH, null, null, null, null, null, null);
		return cursor;
	}

	public Cursor selectjournal() {
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.query(TABLE_JOURNAL, null, null, null, null, null,
				null);
		return cursor;
	}

	public Cursor selectjournalinfo() {
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.query(TABLE_JOURNAL_INFO, null, null, null, null,
				null, null);
		return cursor;
	}

	public Cursor selecturl() {
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.query(TABLE_URL, null, null, null, null, null, null);
		return cursor;
	}

	public Cursor selectoldnews() {
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.query(TABLE_OLDNEWS, null, null, null, null, null,
				null);
		return cursor;
	}

	public Cursor selectuser() {
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.query(TABLE_USERINFO, null, null, null, null, null,
				null);
		return cursor;
	}

	public Cursor selectcontact() {
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.query(TABLE_CONTACTINFO, null, null, null, null,
				null, null);
		return cursor;
	}

	public Cursor selectSetContactDept() {
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.query(TABLE_CONTACT_DEPT, null, null, null, null,
				null, null);
		return cursor;
	}

	public Cursor selectSetContactEmp() {
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.query(TABLE_CONTACT_EMPLOYEES, null, null, null,
				null, null, null);
		return cursor;
	}

	public void ClearOldnewsById(String newsid) {
		SQLiteDatabase db = this.getReadableDatabase();
		String id = newsid;
		// String sql = "DELETE * FROM " + TABLE_OLDNEWS +" WHERE " + OLD_WEBID
		// + " = " + id;
		// db.execSQL(sql);
		String where = OLD_WEBID + " = ?";
		String[] whereValue = { id };
		db.delete(TABLE_OLDNEWS, where, whereValue);
	}

	public void ClearOldPushById(String newsid) {
		SQLiteDatabase db = this.getReadableDatabase();
		String id = newsid;
		String where = PUSH_ID + " = ?";
		String[] whereValue = { id };
		db.delete(TABLE_PUSH, where, whereValue);
	}

	public void clearOldPushSendById(String userId) {
		SQLiteDatabase db = this.getReadableDatabase();
		String id = userId;
		String where = PUSH_WEBID + " = ?";
		String[] whereValue = { id };
		db.delete(TABLE_PUSH, where, whereValue);
	}

	public Cursor selectqa() {
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.query(TABLE_QA, null, null, null, null, null, null);
		return cursor;
	}

	public Cursor selectcommunication() {
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.query(TABLE_COMMUNICATION, null, null, null, null,
				null, null);
		return cursor;
	}

	public Cursor selectreply() {
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.query(TABLE_REPLY, null, null, null, null, null,
				null);
		return cursor;
	}

	public long insertnews(ContentValues cv) {
		SQLiteDatabase db = this.getReadableDatabase();
		ContentValues insertcv = cv;
		long row = db.insert(TABLE_NEWS, null, insertcv);
		return row;
	}

	public long insertcol(ContentValues cv) {
		SQLiteDatabase db = this.getReadableDatabase();
		ContentValues insertcv = cv;
		long row = db.insert(TABLE_COLUMNINFO, null, insertcv);
		return row;
	}

	public long insertvideo(ContentValues cv) {
		SQLiteDatabase db = this.getReadableDatabase();
		ContentValues insertcv = cv;
		long row = db.insert(TABLE_VIDEO_INFO, null, insertcv);
		return row;
	}

	public long insertpush(ContentValues cv) {
		SQLiteDatabase db = this.getReadableDatabase();
		ContentValues insertcv = cv;
		long row = db.insert(TABLE_PUSH, null, insertcv);
		return row;
	}

	public long insertjournal(ContentValues cv) {
		SQLiteDatabase db = this.getReadableDatabase();
		ContentValues insertcv = cv;
		long row = db.insert(TABLE_JOURNAL, null, insertcv);
		return row;
	}

	public long insertjournalinfo(ContentValues cv) {
		SQLiteDatabase db = this.getReadableDatabase();
		ContentValues insertcv = cv;
		long row = db.insert(TABLE_JOURNAL_INFO, null, insertcv);
		return row;
	}

	public long insertoldnews(ContentValues cv) {
		SQLiteDatabase db = this.getReadableDatabase();
		ContentValues insertcv = cv;
		long row = db.insert(TABLE_OLDNEWS, null, insertcv);
		return row;
	}

	public long insertqa(ContentValues cv) {
		SQLiteDatabase db = this.getReadableDatabase();
		ContentValues insertcv = cv;
		long row = db.insert(TABLE_QA, null, insertcv);
		return row;
	}

	public long insertcommunication(ContentValues cv) {
		SQLiteDatabase db = this.getReadableDatabase();
		ContentValues insertcv = cv;
		long row = db.insert(TABLE_COMMUNICATION, null, insertcv);
		return row;
	}

	public long insertreply(ContentValues cv) {
		SQLiteDatabase db = this.getReadableDatabase();
		ContentValues insertcv = cv;
		long row = db.insert(TABLE_REPLY, null, insertcv);
		return row;
	}

	public long insertuser(ContentValues cv) {
		SQLiteDatabase db = this.getReadableDatabase();
		ContentValues insertcv = cv;
		long row = db.insert(TABLE_USERINFO, null, insertcv);
		return row;
	}

	public long insertcontact(ContentValues cv) {
		SQLiteDatabase db = this.getReadableDatabase();
		ContentValues insertcv = cv;
		long row = db.insert(TABLE_CONTACTINFO, null, insertcv);
		return row;
	}

	public long insertSetContactDept(ContentValues cv) {
		SQLiteDatabase db = this.getReadableDatabase();
		ContentValues insertcv = cv;
		long row = db.insert(TABLE_CONTACT_DEPT, null, insertcv);
		return row;
	}

	public long insertSetContactEmp(ContentValues cv) {
		SQLiteDatabase db = this.getReadableDatabase();
		ContentValues insertcv = cv;
		long row = db.insert(TABLE_CONTACT_EMPLOYEES, null, insertcv);
		return row;
	}

	public long inserturl(ContentValues cv) {
		SQLiteDatabase db = this.getReadableDatabase();
		ContentValues insertcv = cv;
		long row = db.insert(TABLE_URL, null, insertcv);
		return row;
	}

	public void updateuser(String id, ContentValues cv) {
		SQLiteDatabase db = this.getWritableDatabase();
		String where = USER_WEBID + " =?";
		String[] whereValue = { id };
		db.update(TABLE_USERINFO, cv, where, whereValue);
	}

	public void updatenews(String id, ContentValues cv) {
		SQLiteDatabase db = this.getWritableDatabase();
		String where = NEWS_WEBID + " =?";
		String[] whereValue = { id };
		db.update(TABLE_NEWS, cv, where, whereValue);
	}

}
