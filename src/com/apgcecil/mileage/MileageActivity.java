package com.apgcecil.mileage;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

public class MileageActivity extends Activity {
	public static final String DB_DATE_FORMAT = "yyyy-dd-MM HH:mm:ss";
	private TextView milesEntry = null;
	private TextView litresEntry = null;
	private TextView priceEntry = null;
	private Button addButton = null;
	private Button mergeButton = null;
	private ListView mileageList = null;
	private Database dbHelper = null;
	private Cursor cursor = null;
	private MileageAdapter adapt = null;
	private SQLiteDatabase db = null;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		/* Create database helper. */
		dbHelper = new Database(this);
		db = dbHelper.getWritableDatabase();
		cursor = db.query(Database.TABLE_NAME, null, null, null, null, null,
				Database.KEY_DATE + " DESC");

		addButton = (Button) findViewById(R.id.addButton);
		mergeButton = (Button) findViewById(R.id.mergeButton);
		mileageList = (ListView) findViewById(R.id.mileageList);
		milesEntry = (TextView) findViewById(R.id.milesEntry);
		litresEntry = (TextView) findViewById(R.id.litresEntry);
		priceEntry = (TextView) findViewById(R.id.priceEntry);

		adapt = new MileageAdapter(this, cursor);
		mileageList.setAdapter(adapt);

		addButton.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				/* Add items to database. */
				double miles = Double.parseDouble(milesEntry.getText()
						.toString());
				double litres = Double.parseDouble(litresEntry.getText()
						.toString());
				double price = Double.parseDouble(priceEntry.getText()
						.toString());

				SimpleDateFormat sdate = new SimpleDateFormat(DB_DATE_FORMAT);
				ContentValues vals = new ContentValues();
				vals.put(Database.KEY_DATE, sdate.format(new Date()));
				vals.put(Database.KEY_MILES, miles);
				vals.put(Database.KEY_LITRES, litres);
				vals.put(Database.KEY_PRICE, price);
				db.insert(Database.TABLE_NAME, null, vals);
				cursor.requery();

				/* Select item in list. */
			}
		});

		mergeButton.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				/* Merge item with top item in database. */
				double miles = Double.parseDouble(milesEntry.getText()
						.toString());
				double litres = Double.parseDouble(litresEntry.getText()
						.toString());
				double price = Double.parseDouble(priceEntry.getText()
						.toString());

				cursor.moveToFirst();
				double nMiles = cursor.getDouble(cursor
						.getColumnIndex(Database.KEY_MILES));
				double nLitres = cursor.getDouble(cursor
						.getColumnIndex(Database.KEY_LITRES));
				double nPrice = cursor.getDouble(cursor
						.getColumnIndex(Database.KEY_PRICE));

				price = (price * litres + nPrice * nLitres)
						/ (litres + nLitres);
				miles += nMiles;
				litres += nLitres;

				SimpleDateFormat sdate = new SimpleDateFormat(DB_DATE_FORMAT);
				ContentValues vals = new ContentValues();
				vals.put(Database.KEY_DATE, sdate.format(new Date()));
				vals.put(Database.KEY_MILES, miles);
				vals.put(Database.KEY_LITRES, litres);
				vals.put(Database.KEY_PRICE, price);
				db.update(
						Database.TABLE_NAME,
						vals,
						Database.KEY_ID
								+ "="
								+ cursor.getInt(cursor
										.getColumnIndex(Database.KEY_ID)), null);

				/* Select item in list. */
				cursor.requery();
			}
		});

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		menu.add("Delete History");
		menu.add("About");
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem menuItem) {
		String menuTitle = menuItem.getTitle().toString();
		if (menuTitle.equals("About")) {

			String versionString = "Unknown";
			try {
				versionString = getPackageManager().getPackageInfo(
						getPackageName(), 0).versionName;
			} catch (NameNotFoundException e) {
				/* Just set version string as unknown. */
				versionString = "Unknown";
			}
			AlertDialog.Builder b = new AlertDialog.Builder(this);
			b.setTitle("About Mileage")
					.setMessage(
							"© 2011 Andrew Gascoyne-Cecil\n<gascoyne@gmail.com>\nVersion "
									+ versionString)
					.setPositiveButton("Close",
							new AlertDialog.OnClickListener() {

								public void onClick(DialogInterface dialog,
										int which) {
									dialog.cancel();
								}
							});
			AlertDialog d = b.create();
			d.show();

			return true;
		} else if (menuTitle.equals("Delete History")) {

			AlertDialog.Builder b = new AlertDialog.Builder(this);
			b.setTitle("Delete History")
					.setMessage(
							"This will erase all previous records.\nAre you sure?")
					.setPositiveButton("Yes",
							new AlertDialog.OnClickListener() {

								public void onClick(DialogInterface dialog,
										int which) {
									db.delete(Database.TABLE_NAME, null, null);
									cursor.requery();
									dialog.cancel();
								}
							})
					.setNegativeButton("No", new AlertDialog.OnClickListener() {

						public void onClick(DialogInterface dialog, int which) {
							dialog.cancel();
						}
					});
			AlertDialog d = b.create();
			d.show();

			return true;
		}

		return false;
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		savedInstanceState.putString("Miles", milesEntry.getText().toString());
		savedInstanceState
				.putString("Litres", litresEntry.getText().toString());
		savedInstanceState.putString("Price", priceEntry.getText().toString());
		super.onSaveInstanceState(savedInstanceState);
	}

	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		milesEntry.setText(savedInstanceState.getString("Miles"));
		litresEntry.setText(savedInstanceState.getString("Litres"));
		priceEntry.setText(savedInstanceState.getString("Price"));
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		cursor.close();
		db.close();
	}
}