/*
 * (c) Copyright 2012 by Andrew Gascoyne-Cecil
 * 
 * This file is part of Mileage.
 *
 * Mileage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Mileage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Mileage.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.apgcecil.mileage;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

class Database extends SQLiteOpenHelper {
	public final static String TABLE_NAME = "mileage";
	public final static String KEY_ID = "_id";
	public final static String KEY_DATE = "date";
	public final static String KEY_MILES = "miles";
	public final static String KEY_LITRES = "litres";
	public final static String KEY_PRICE = "price";

	Database(Context context) {
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
