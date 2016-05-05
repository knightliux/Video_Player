package com.moon.android.moonplayer.history;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase.CursorFactory;

public class HistoryDBHelper extends SQLiteOpenHelper {
	
	public static final int VERSION = 1;

	public HistoryDBHelper(Context context, String name, CursorFactory factory,
			int version) {
		super(context, name, factory, version);
	}

	public HistoryDBHelper(Context context) {
		super(context, HistoryConfigs.DATABASE_NAME, null, VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase sqliteDB) {
		String createSQL = "create table if not exists " + HistoryConfigs.TABLE_NAME
				+ "(_id integer primary key autoincrement,subcatid varchar(100),playIndex varchar(100),playPos varchar(100))";

		sqliteDB.execSQL(createSQL);
	}

	@Override
	public void onUpgrade(SQLiteDatabase sqliteDB, int arg1, int arg2) {
		String deleteSQL = "DROP TABLE IF EXISTS " + HistoryConfigs.TABLE_NAME;

		sqliteDB.execSQL(deleteSQL);
		onCreate(sqliteDB);
	}
}
