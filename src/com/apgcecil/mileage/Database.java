package com.apgcecil.mileage;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class Database extends SQLiteOpenHelper {
	public final static String TABLE_NAME = "mileage";
	public final static String KEY_ID = "_id";
	public final static String KEY_DATE = "date";
	public final static String KEY_MILES = "miles";
	public final static String KEY_LITRES = "litres";
	public final static String KEY_PRICE = "price";

	public Database(Context context) {
		super(context, "db", null, 1);
		
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		/* Create the table. */
		db.execSQL("CREATE TABLE "+TABLE_NAME+
				" ("+KEY_ID+" INTEGER PRIMARY KEY AUTOINCREMENT, "+
				KEY_DATE+" DATETIME, "+
				KEY_MILES+" REAL, "+
				KEY_LITRES+" REAL, "+
				KEY_PRICE+" REAL);");

	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// Nothing to do at present

	}

}
