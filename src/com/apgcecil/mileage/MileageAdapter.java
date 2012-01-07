package com.apgcecil.mileage;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

public class MileageAdapter extends CursorAdapter {
	
	private final SharedPreferences preferences;
	
	public MileageAdapter(Context context, Cursor c, SharedPreferences pref) {		
		super(context, c);
		preferences = pref;
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		
		TextView dateView = (TextView)view.findViewById(R.id.dateListItem);
		TextView economyView = (TextView)view.findViewById(R.id.economyListItem);
		TextView costView = (TextView)view.findViewById(R.id.costListItem);
		
		
		String date = cursor.getString(cursor.getColumnIndex(Database.KEY_DATE));
		double miles = cursor.getDouble(cursor.getColumnIndex(Database.KEY_MILES));
		double litres = cursor.getDouble(cursor.getColumnIndex(Database.KEY_LITRES));
		double price = cursor.getDouble(cursor.getColumnIndex(Database.KEY_PRICE));
		
		String outDate = null;
		SimpleDateFormat curFormater = new SimpleDateFormat("yyyy-dd-MM HH:mm:ss");
		Date dateObj;
		try {
			dateObj = curFormater.parse(date);
			SimpleDateFormat postFormater = new SimpleDateFormat("dd MMM yyyy");
			outDate = postFormater.format(dateObj);
		} catch (ParseException e) {
			/* Just use first 10 chars of date string. */
			outDate = date.substring(0, 10);
		} 
		double mpg = miles / (litres / 4.54609188);
		double ppm = price * litres / miles;
		NumberFormat nf = NumberFormat.getInstance();
		nf.setMaximumFractionDigits(3);
		nf.setMinimumFractionDigits(3);
		
		/* Get settings from settings objects. */
		String units = preferences.getString("Units", "0");
		if( units.equals("0") ) {
			economyView.setText(nf.format(mpg) + " mpg");
			costView.setText(nf.format(ppm) + " ppm");			
		} else if( units.equals("1") ) {
			double mpusg = mpg / 1.20095042;
			economyView.setText(nf.format(mpusg) + " mpg");
			costView.setText(nf.format(ppm) + " ppm");
		} else if( units.equals("2") ) {
			double lp100km = 100 * 4.54609188 / 1.609344 / mpg;
			economyView.setText(nf.format(lp100km) + " l/100km");
			double ppkm = ppm / 1.609344;
			costView.setText(nf.format(ppkm) + " ppkm");			
		}
		
		dateView.setText(outDate);
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		LayoutInflater inflater = LayoutInflater.from(context);
		View v = inflater.inflate(R.layout.listitem, parent, false);
		bindView(v, context, cursor);
		return v;
	}

}
