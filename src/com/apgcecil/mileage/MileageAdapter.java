package com.apgcecil.mileage;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

public class MileageAdapter extends CursorAdapter {
	
	public MileageAdapter(Context context, Cursor c) {		
		super(context, c);
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		
		TextView dateView = (TextView)view.findViewById(R.id.dateListItem);
		TextView mpgView = (TextView)view.findViewById(R.id.mpgListItem);
		TextView ppmView = (TextView)view.findViewById(R.id.ppmListItem);
		
		
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
		
		dateView.setText(outDate);
		mpgView.setText(nf.format(mpg) + " mpg");
		ppmView.setText(nf.format(ppm) + " ppm");
		
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		LayoutInflater inflater = LayoutInflater.from(context);
		View v = inflater.inflate(R.layout.listitem, parent, false);
		bindView(v, context, cursor);
		return v;
	}

}
