package com.apgcecil.mileage;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

public class MileageProvider extends ContentProvider {
	
	public final static String URI = "com.apgcecil.mileage.MileageProvider";
	public final static Uri CONTENT_URI = Uri.parse("content://" + URI);

	private SQLiteDatabase db = null;

	@Override
	public int delete(Uri uri, String whereClause, String[] whereArgs) {
		return db.delete(Database.TABLE_NAME, whereClause, whereArgs);
	}

	@Override
	public String getType(Uri uri) {
		return "vnd.android.cursor.dir/" + URI;
	}

	@Override
	public Uri insert(Uri uri, ContentValues vals) {
		db.insert(Database.TABLE_NAME, null, vals);
		return uri;
	}

	@Override
	public boolean onCreate() {
		Database dbHelper = new Database(getContext());
		db = dbHelper.getWritableDatabase();
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
			String sortOrder) {
		return db.query(Database.TABLE_NAME, projection, selection, selectionArgs, null, null,
				sortOrder);
	}

	@Override
	public int update(Uri uri, ContentValues vals, String whereClause, String[] whereArgs) {
		return db.update(Database.TABLE_NAME, vals, whereClause, whereArgs);
	}

}
