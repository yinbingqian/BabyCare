package com.lnpdit.sqllite;

import android.R.integer;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class ToDoDB extends SQLiteOpenHelper {

	private final static String DATABASE_NAME = "MonitorDB";
	private final static int DATABASE_VERSION = 1;
	private final static String TABLE_FAVOR = "favor";
	private final static String TABLE_GROUP = "clientgroup";
	private final static String TABLE_MSG = "commessage";
	private final static String TABLE_NEWS = "news";

	public static final String TYPE_PRIMARY_KEY = "INTEGER PRIMARY KEY AUTOINCREMENT";
	public static final String TYPE_TEXT = "TEXT";
	public static final String TYPE_DATETIME = "DATETIME";
	public static final String TYPE_INT = "INT";

	public static final String DATA_ID = "DATA_ID";
	public static final String DATA_WEBID = "DATA_WEBID";
	public static final String DATA_USERID = "DATA_USERID";
	public static final String DATA_DEVID = "DATA_DEVID";
	public static final String DATA_CHNO = "DATA_CHNO";

	public static final String GROUP_ID = "GROUP_ID";
	public static final String GROUP_GID = "GROUP_GID";
	public static final String GROUP_NAME = "GROUP_NAME";

	public static final String MSG_ID = "MSG_ID";
	public static final String MSG_WEBID = "MSG_WEBID";
	public static final String MSG_PIC = "MSG_PIC";
	public static final String MSG_REMARK = "MSG_REMARK";
	public static final String MSG_TIME = "MSG_TIME";

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

	public ToDoDB(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		try {
			String create_table_favor = "CREATE TABLE" + " " + TABLE_FAVOR
					+ "(" + DATA_ID + " " + TYPE_PRIMARY_KEY + "," + DATA_WEBID
					+ " " + TYPE_TEXT + "," + DATA_USERID + " " + TYPE_TEXT
					+ "," + DATA_DEVID + " " + TYPE_TEXT + "," + DATA_CHNO
					+ " " + TYPE_TEXT + ")";

			String create_table_group = "CREATE TABLE" + " " + TABLE_GROUP
					+ "(" + GROUP_ID + " " + TYPE_PRIMARY_KEY + "," + GROUP_GID
					+ " " + TYPE_TEXT + "," + GROUP_NAME + " " + TYPE_TEXT
					+ ")";

			String create_table_communication = "CREATE TABLE" + " "
					+ TABLE_MSG + "(" + MSG_ID + " " + TYPE_PRIMARY_KEY + ","
					+ MSG_WEBID + " " + TYPE_TEXT + "," + MSG_PIC + " "
					+ TYPE_TEXT + "," + MSG_REMARK + " " + TYPE_TEXT + ","
					+ MSG_TIME + " " + TYPE_TEXT + ")";

			String create_table_news = "CREATE TABLE" + " " + TABLE_NEWS + "("
					+ NEWS_ID + " " + TYPE_PRIMARY_KEY + "," + NEWS_TITLE + " "
					+ TYPE_TEXT + "," + NEWS_SOURCE + " " + TYPE_TEXT + ","
					+ NEWS_AUTHOR + " " + TYPE_TEXT + "," + NEWS_PICTURE + " "
					+ TYPE_TEXT + "," + NEWS_CONTENT + " " + TYPE_TEXT + ","
					+ NEWS_CREATETIME + " " + TYPE_TEXT + "," + NEWS_TYPE + " "
					+ TYPE_TEXT + "," + NEWS_WEBID + " " + TYPE_TEXT + ","
					+ NEWS_THUMBNAIL + " " + TYPE_TEXT + ")";

			db.execSQL(create_table_favor);
			db.execSQL(create_table_group);
			db.execSQL(create_table_communication);
			db.execSQL(create_table_news);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		String sqlfavor = "DROP TABLE IF EXISTS " + TABLE_FAVOR;
		String sqlgroup = "DROP TABLE IF EXISTS " + TABLE_GROUP;
		String sqlcom = "DROP TABLE IF EXISTS " + TABLE_MSG;
		String sqlnews = "DROP TABLE IF EXISTS " + TABLE_NEWS;
		db.execSQL(sqlfavor);
		db.execSQL(sqlgroup);
		db.execSQL(sqlcom);
		db.execSQL(sqlnews);
		onCreate(db);
	}

	public void createdb(SQLiteDatabase db) {
		// String create_table_favor = "CREATE TABLE" + " " + TABLE_FAVOR + "("
		// + DATA_ID + " " + TYPE_PRIMARY_KEY + "," + DATA_WEBID + " "
		// + TYPE_TEXT + "," + DATA_USERID + " " + TYPE_TEXT + ","
		// + DATA_DEVID + " " + TYPE_TEXT + "," + DATA_CHNO + " "
		// + TYPE_TEXT + ")";
		//
		// String create_table_group = "CREATE TABLE" + " " + TABLE_GROUP + "("
		// + GROUP_ID + " " + TYPE_PRIMARY_KEY + "," + GROUP_GID + " "
		// + TYPE_TEXT + "," + GROUP_NAME + " " + TYPE_TEXT + ")";
		//
		// String create_table_communication = "CREATE TABLE" + " " + TABLE_MSG
		// + "(" + MSG_ID + " " + TYPE_PRIMARY_KEY + "," + MSG_WEBID + " "
		// + TYPE_TEXT + "," + MSG_PIC + " " + TYPE_TEXT + ","
		// + MSG_REMARK + " " + TYPE_TEXT + "," + MSG_TIME + " "
		// + TYPE_TEXT + ")";

		String create_table_news = "CREATE TABLE" + " " + TABLE_NEWS + "("
				+ NEWS_ID + " " + TYPE_PRIMARY_KEY + "," + NEWS_TITLE + " "
				+ TYPE_TEXT + "," + NEWS_SOURCE + " " + TYPE_TEXT + ","
				+ NEWS_AUTHOR + " " + TYPE_TEXT + "," + NEWS_PICTURE + " "
				+ TYPE_TEXT + "," + NEWS_CONTENT + " " + TYPE_TEXT + ","
				+ NEWS_CREATETIME + " " + TYPE_TEXT + "," + NEWS_TYPE + " "
				+ TYPE_TEXT + "," + NEWS_WEBID + " " + TYPE_TEXT + ","
				+ NEWS_THUMBNAIL + " " + TYPE_TEXT + ")";

		// db.execSQL(create_table_favor);
		// db.execSQL(create_table_group);
		// db.execSQL(create_table_communication);
		db.execSQL(create_table_news);
	}

	public void cleardb() {
		SQLiteDatabase db = this.getReadableDatabase();
		// String sqlfavor = "DROP TABLE IF EXISTS " + TABLE_FAVOR;
		// String sqlgroup = "DROP TABLE IF EXISTS " + TABLE_GROUP;
		// String sqlcom = "DROP TABLE IF EXISTS " + TABLE_MSG;
		String sqlnews = "DROP TABLE IF EXISTS " + TABLE_NEWS;
		// db.execSQL(sqlfavor);
		// db.execSQL(sqlgroup);
		// db.execSQL(sqlcom);
		db.execSQL(sqlnews);
		createdb(db);
	}

	public Cursor select_favor() {
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.query(TABLE_FAVOR, null, null, null, null, null,
				null);
		return cursor;
	}

	public long insert_favor(ContentValues cv) {
		SQLiteDatabase db = this.getReadableDatabase();
		ContentValues insertcv = cv;
		long row = db.insert(TABLE_FAVOR, null, insertcv);
		return row;
	}

	public void DeleteFavorById(String userId, String devId, String chNo) {
		SQLiteDatabase db = this.getReadableDatabase();
		String where = DATA_USERID + " = ? and " + DATA_DEVID + " = ? and "
				+ DATA_CHNO + "= ? ";
		String[] whereValue = { userId, devId, chNo };
		db.delete(TABLE_FAVOR, where, whereValue);
	}

	public void clearfavor() {
		SQLiteDatabase db = this.getReadableDatabase();
		String clear_favor_info = "DROP TABLE IF EXISTS " + TABLE_FAVOR;

		String create_table_favor = "CREATE TABLE" + " " + TABLE_FAVOR + "("
				+ DATA_ID + " " + TYPE_PRIMARY_KEY + "," + DATA_WEBID + " "
				+ TYPE_TEXT + "," + DATA_USERID + " " + TYPE_TEXT + ","
				+ DATA_DEVID + " " + TYPE_TEXT + "," + DATA_CHNO + " "
				+ TYPE_TEXT + ")";

		db.execSQL(clear_favor_info);

		db.execSQL(create_table_favor);
	}

	public void updateuser(String id, ContentValues cv) {
		SQLiteDatabase db = this.getWritableDatabase();
		String where = DATA_DEVID + " =?";
		String[] whereValue = { id };
		db.update(TABLE_FAVOR, cv, where, whereValue);
	}

	public Cursor selectgroup() {
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.query(TABLE_GROUP, null, null, null, null, null,
				null);
		return cursor;
	}

	public long insertgroup(ContentValues cv) {
		SQLiteDatabase db = this.getReadableDatabase();
		ContentValues insertcv = cv;
		long row = db.insert(TABLE_GROUP, null, insertcv);
		return row;
	}

	public void cleargroup() {
		SQLiteDatabase db = this.getReadableDatabase();
		String clear_group_info = "DROP TABLE IF EXISTS " + TABLE_GROUP;

		String create_table_groupinfo = "CREATE TABLE" + " " + TABLE_GROUP
				+ "(" + GROUP_ID + " " + TYPE_PRIMARY_KEY + "," + GROUP_GID
				+ " " + TYPE_TEXT + "," + GROUP_NAME + " " + TYPE_TEXT + ")";

		db.execSQL(clear_group_info);

		db.execSQL(create_table_groupinfo);
	}

	public Cursor selectmsg() {
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.query(TABLE_MSG, null, null, null, null, null, null);
		return cursor;
	}

	public long insertmsg(ContentValues cv) {
		SQLiteDatabase db = this.getReadableDatabase();
		ContentValues insertcv = cv;
		long row = db.insert(TABLE_MSG, null, insertcv);
		return row;
	}

	public void cleargmsg() {
		SQLiteDatabase db = this.getReadableDatabase();
		String clear_msg_info = "DROP TABLE IF EXISTS " + TABLE_MSG;

		String create_table_communication = "CREATE TABLE" + " " + TABLE_MSG
				+ "(" + MSG_ID + " " + TYPE_PRIMARY_KEY + "," + MSG_WEBID + " "
				+ TYPE_TEXT + "," + MSG_PIC + " " + TYPE_TEXT + ","
				+ MSG_REMARK + " " + TYPE_TEXT + "," + MSG_TIME + " "
				+ TYPE_TEXT + ")";

		db.execSQL(clear_msg_info);

		db.execSQL(create_table_communication);
	}

	public void DeleteMsgById(String newsid) {
		SQLiteDatabase db = this.getReadableDatabase();
		String id = newsid;
		String where = MSG_WEBID + " = ?";
		String[] whereValue = { id };
		db.delete(TABLE_MSG, where, whereValue);
	}

	public Cursor selectnews() {
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db
				.query(TABLE_NEWS, null, null, null, null, null, null);
		return cursor;
	}

	public long insertnews(ContentValues cv) {
		SQLiteDatabase db = this.getReadableDatabase();
		ContentValues insertcv = cv;
		long row = db.insert(TABLE_NEWS, null, insertcv);
		return row;
	}

}
