package com.moon.android.moonplayer.history;

import com.moon.android.moonplayer.util.Logger;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

public class HistoryProvider extends ContentProvider {
	
	private Logger logger = Logger.getLogger();
	private static final UriMatcher MATCHER = new UriMatcher(
			UriMatcher.NO_MATCH);
	public static final int NOTE_ONE = 1;
	public static final int NOTE_MANY = 2;
	private HistoryDBHelper mDBHelper;
	public static final String AUTHORITY = "com.moon.android.moonplayer.history.historyprovider";
	static {
		MATCHER.addURI(AUTHORITY, "history_table", NOTE_MANY);
		MATCHER.addURI(AUTHORITY, "history_table/#", NOTE_ONE);
	}

	/**
	 * @param context
	 * @param appid
	 *            应用ID
	 * @param subcatId
	 *            电视剧某个节目的ID
	 * @param playIndex
	 *            播放到第几集
	 * @param playPos
	 *            播放到多少秒
	 */
	public static void saveHistory(Context context, String appid,
			String subcatId, String playIndex, String playPos) {
		
	}

	@Override
	public String getType(Uri uri) {
		switch (MATCHER.match(uri)) {
		case NOTE_MANY:
			return "vnd.android.cursor.dir/" + HistoryConfigs.TABLE_NAME;
		case NOTE_ONE:
			return "vnd.android.cursor.item/" + HistoryConfigs.TABLE_NAME;
		default:
			throw new IllegalArgumentException("Unkwon Uri:" + uri.toString());
		}
	}

	@Override
	public boolean onCreate() {
		mDBHelper = new HistoryDBHelper(getContext());
		return false;
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		SQLiteDatabase db = mDBHelper.getWritableDatabase();
        int count = 0;
        switch (MATCHER.match(uri)) {
        case NOTE_MANY:
                count = db.delete(HistoryConfigs.TABLE_NAME, selection, selectionArgs);
                return count;
        case NOTE_ONE:
                long id = ContentUris.parseId(uri);
                String where = "_id=" + id;
                if (selection != null && !"".equals(selection)) {
                        where = selection + " and " + where;
                }
                count = db.delete(HistoryConfigs.TABLE_NAME, where, selectionArgs);
                return count;
        default:
                throw new IllegalArgumentException("Unkwon Uri:" + uri.toString());
        }
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		 SQLiteDatabase db = mDBHelper.getWritableDatabase();
         Uri insertUri = null;
         switch (MATCHER.match(uri)) {
         case NOTE_MANY:
                 long rowid = db.insert(HistoryConfigs.TABLE_NAME, "name", values);
                 insertUri = ContentUris.withAppendedId(uri, rowid);
                 break;
         default:
                 throw new IllegalArgumentException("Unkwon Uri:" + uri.toString());
         }
         return insertUri;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		logger.i("into query");
		SQLiteDatabase db = mDBHelper.getWritableDatabase();
		switch (MATCHER.match(uri)) {
		case NOTE_MANY:
			// 查询所有的数据
			return db.query(HistoryConfigs.TABLE_NAME, projection, selection, selectionArgs,
					null, null, sortOrder);
		case NOTE_ONE:
			// 查询某个ID的数据
			// 通过ContentUris这个工具类解释出ID
			long id = ContentUris.parseId(uri);
			String where = " subcatid =" + id;
			if (!"".equals(selection) && selection != null) {
				where = selection + " and " + where;
			}
			return db.query(HistoryConfigs.TABLE_NAME, projection, where, selectionArgs, null,
					null, sortOrder);
		default:
			throw new IllegalArgumentException("unknow uri" + uri.toString());
		}
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		SQLiteDatabase db = mDBHelper.getWritableDatabase();
		int count = 0;
		switch (MATCHER.match(uri)) {
		case NOTE_MANY:
		count = db.update(HistoryConfigs.TABLE_NAME, values, selection, selectionArgs);
		break;
		case NOTE_ONE:
		// 通过ContentUri工具类得到ID
		long id = ContentUris.parseId(uri);
		String where = "_id=" + id;
		if (selection != null && !"".equals(selection)) {
		where = selection + " and " + where;
		}
		count = db.update(HistoryConfigs.TABLE_NAME, values, where, selectionArgs);
		break;
		default:
		throw new IllegalArgumentException("Unkwon Uri:" + uri.toString());
		}
		return count;
	}

}
