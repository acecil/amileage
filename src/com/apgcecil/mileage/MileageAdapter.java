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

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MileageAdapter extends CursorAdapter {
	
	public class MileageItemData {
		public double miles;
		public double litres;
		public double price;
	}
	
	private static final String DISPLAY_DATE_FORMAT = "dd MMM yy";	
	private final SharedPreferences prefs;
	private final LayoutInflater inflater;
	private final HashMap<LinearLayout, MileageItemData> adapterData = new HashMap<LinearLayout, MileageItemData>();
	
	@SuppressLint("NewApi")
	public MileageAdapter(Context context, SharedPreferences prefs) {
		super(context, null, 0);
		this.prefs = prefs;
		inflater = LayoutInflater.from(context);
	}
	
	@SuppressWarnings("deprecation")
	public MileageAdapter(Context context, Cursor c, SharedPreferences prefs) {
		super(context, c);
		this.prefs = prefs;
		inflater = LayoutInflater.from(context);
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
				
		TextView dateView = (TextView) view.findViewById(R.id.dateLabel);
		TextView economyView = (TextView) view.findViewById(R.id.economyLabel);
		TextView costView = (TextView) view.findViewById(R.id.costLabel);
		
		final int dateColumn = cursor.getColumnIndex(Database.KEY_DATE);
		final int milesColumn = cursor.getColumnIndex(Database.KEY_MILES);
		final int litresColumn = cursor.getColumnIndex(Database.KEY_LITRES);
		final int priceColumn = cursor.getColumnIndex(Database.KEY_PRICE);
		
		String date = cursor.getString(dateColumn);
		double miles = cursor.getDouble(milesColumn);
		double litres = cursor.getDouble(litresColumn);
		double price = cursor.getDouble(priceColumn);
		
		String outDate = null;
		SimpleDateFormat curFormater = new SimpleDateFormat(MileageActivity.DB_DATE_FORMAT, Locale.getDefault());
		Date dateObj;
		try {
			dateObj = curFormater.parse(date);
			SimpleDateFormat postFormater = new SimpleDateFormat(DISPLAY_DATE_FORMAT, Locale.getDefault());
			outDate = postFormater.format(dateObj);
		} catch (ParseException e) {
			/* Just use first 10 chars of date string. */
			outDate = date.substring(0, 10);
		} 
		double mpg = (litres > 0) ? miles / (litres / 4.54609188) : 0;
		double ppm = (miles > 0) ? price * litres / miles : 0;
		NumberFormat nf = NumberFormat.getInstance();
		nf.setMaximumFractionDigits(3);
		nf.setMinimumFractionDigits(3);
		
		/* Get settings from settings objects. */
		double economy = mpg;
		double cost = ppm;
		int economyUnits = Integer.parseInt(prefs.getString(MileageActivity.ECONOMY_UNITS, Integer.toString(MileageActivity.ECONOMY_UNITS_MPG)));
		switch(economyUnits) {
		case MileageActivity.ECONOMY_UNITS_MPG:
			economy = mpg;
			break;
		case MileageActivity.ECONOMY_UNITS_US_MPG:
			economy = mpg * 0.83267384;
			break;
		case MileageActivity.ECONOMY_UNITS_LP100KM:
			economy = (mpg > 0) ? 100 * 4.54609188 / 1.609344 / mpg : 0;
			break;
		}
		int distanceUnits = Integer.parseInt(prefs.getString(MileageActivity.DISTANCE_UNITS, Integer.toString(MileageActivity.DISTANCE_UNITS_MILES)));
		switch(distanceUnits) {
		case MileageActivity.DISTANCE_UNITS_MILES:
			cost = ppm;
			break;
		case MileageActivity.DISTANCE_UNITS_KM:
			cost = ppm / 1.609344;
			break;
		}
		economyView.setText(nf.format(economy));
		costView.setText(nf.format(cost));		
		dateView.setText(outDate);
		
		/* Add or replace in map. */
		MileageItemData itemData = new MileageItemData();
		itemData.miles = miles;
		itemData.litres = litres;
		itemData.price = price;
		adapterData.put((LinearLayout)view, itemData);
		
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		return inflater.inflate(R.layout.listitem, parent, false);
	}
	
	public MileageItemData getItemData(LinearLayout ll) {
		return adapterData.get(ll);
	}
}
