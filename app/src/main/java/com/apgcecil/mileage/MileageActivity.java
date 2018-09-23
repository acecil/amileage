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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentProviderClient;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;
import android.os.Bundle;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.apgcecil.mileage.MileageAdapter.MileageItemData;

@SuppressLint("NewApi")
@SuppressWarnings("deprecation")
public class MileageActivity extends Activity {
	
	private static final int PREFERENCE_CODE = 1;
	public static final String DB_DATE_FORMAT = "yyyy-dd-MM HH:mm:ss";
	private static final String DISTANCE_SAVE = "Distance";
	private static final String VOLUME_SAVE = "Volume";
	private static final String PRICE_SAVE = "Price";
	public static final String DISTANCE_UNITS = "DistanceUnits";
	private static final String VOLUME_UNITS = "VolumeUnits";
	public static final String ECONOMY_UNITS = "EconomyUnits";
	public static final int DISTANCE_UNITS_MILES = 0;
	public static final int DISTANCE_UNITS_KM = 1;
	private static final int VOLUME_UNITS_LITRES = 0;
	private static final int VOLUME_UNITS_GALLONS = 1;
	private static final int VOLUME_UNITS_US_GALLONS = 2;
	public static final int ECONOMY_UNITS_MPG = 0;
	public static final int ECONOMY_UNITS_US_MPG = 1;
	public static final int ECONOMY_UNITS_LP100KM = 2;
	private static final String ECONOMY_LABEL_MPG = "mpg";
	private static final String ECONOMY_LABEL_US_MPG = "mpg";
	private static final String ECONOMY_LABEL_LP100KM = "l/100km";
	private static final String COST_LABEL_PPM = "p/m";
	private static final String COST_LABEL_PPKM = "p/km";

	private TextView distanceEntry = null;
	private TextView volumeEntry = null;
	private TextView priceEntry = null;
	private TextView economyLabel = null;
	private TextView costLabel = null;
	private MileageAdapter adapt = null;
	private ContentProviderClient cpr = null;
	private SharedPreferences prefs = null;
	private MileageCallbacks callbacks = null;

	private Cursor cursor = null;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		/* Create database helper. */
		cpr = getContentResolver().acquireContentProviderClient(MileageProvider.URI);

		/* Get references to all widgets in GUI. */
		Button addButton = findViewById(R.id.addButton);
		Button mergeButton = findViewById(R.id.mergeButton);
		ListView mileageList = findViewById(R.id.mileageList);
		distanceEntry = findViewById(R.id.distanceEntry);
		volumeEntry = findViewById(R.id.volumeEntry);
		priceEntry = findViewById(R.id.priceEntry);
		economyLabel = findViewById(R.id.economyLabel);
		costLabel = findViewById(R.id.costLabel);

		/* Set labels based on preferences. */
		setLabels();

		/* Set adapter for mapping database values to mileage list. */
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		if( Build.VERSION.SDK_INT >= 11 ) {
			adapt = new MileageAdapter(this, prefs);
		} else {
			SQLiteOpenHelper dbHelper = new Database(this);
			SQLiteDatabase db = dbHelper.getWritableDatabase();
			cursor = db.query(Database.TABLE_NAME, null, null, null, null, null,
					Database.KEY_DATE + " DESC");
			adapt = new MileageAdapter(this, cursor, prefs);
		}
		mileageList.setAdapter(adapt);

		/* Save activity so we can reference it in the callbacks. */
		final MileageActivity activity = this;
		
		if( Build.VERSION.SDK_INT >= 11 ) {
			callbacks = new MileageCallbacks(this);
		}
		
		/* Make mileage list clickable. */
		mileageList.setClickable(true);

		/* Button callbacks. */
		addButton.setOnClickListener(v -> {
            /* Add items to database. */
            try {
                double distance = Double.parseDouble(distanceEntry
                        .getText().toString());
                double volume = Double.parseDouble(volumeEntry.getText()
                        .toString());
                double price = Double.parseDouble(priceEntry.getText()
                        .toString());

                /* Convert distance and volume if required. */
                SharedPreferences prefs = PreferenceManager
                        .getDefaultSharedPreferences(activity);
                int distanceUnits = Integer.parseInt(prefs.getString(
                        DISTANCE_UNITS,
                        Integer.toString(DISTANCE_UNITS_MILES)));
                switch (distanceUnits) {
                case DISTANCE_UNITS_MILES:
                    /* Nothing to do. */
                    break;
                case DISTANCE_UNITS_KM:
                    distance *= 0.621371192;
                    break;
                }
                int volumeUnits = Integer.parseInt(prefs.getString(
                        VOLUME_UNITS, Integer.toString(VOLUME_UNITS_LITRES)));
                switch (volumeUnits) {
                case VOLUME_UNITS_LITRES:
                    /* Nothing to do. */
                    break;
                case VOLUME_UNITS_GALLONS:
                    volume *= 4.54609188;
                    break;
                case VOLUME_UNITS_US_GALLONS:
                    volume *= 3.78541178;
                    break;
                }

                SimpleDateFormat sdate = new SimpleDateFormat(
                        DB_DATE_FORMAT, Locale.getDefault());
                ContentValues vals = new ContentValues();
                vals.put(Database.KEY_DATE, sdate.format(new Date()));
                vals.put(Database.KEY_MILES, distance);
                vals.put(Database.KEY_LITRES, volume);
                vals.put(Database.KEY_PRICE, price);
                try {
                    cpr.insert(MileageProvider.CONTENT_URI, vals);
                    if( Build.VERSION.SDK_INT >= 11 ) {
                        getLoaderManager().restartLoader(0, null, callbacks);
                    } else {
                        adapt.getCursor().requery();
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                }

                /* Clear text boxes. */
                distanceEntry.setText("");
                volumeEntry.setText("");
                priceEntry.setText("");

            } catch (NumberFormatException n) {
                Toast toast = Toast.makeText(getApplicationContext(), "Invalid value", Toast.LENGTH_LONG);
                toast.show();
            }
        });

		mergeButton.setOnClickListener(v -> {
            try {
            /* Merge item with top item in database. */
            double miles = Double.parseDouble(distanceEntry.getText()
                    .toString());
            double litres = Double.parseDouble(volumeEntry.getText()
                    .toString());
            double price = Double.parseDouble(priceEntry.getText()
                    .toString());

            if( !adapt.getCursor().moveToFirst() ) {
                Toast toast = Toast.makeText(getApplicationContext(), "No items to merge with", Toast.LENGTH_LONG);
                toast.show();
                return;
            }

            double nMiles = adapt.getCursor().getDouble(adapt.getCursor()
                    .getColumnIndex(Database.KEY_MILES));
            double nLitres = adapt.getCursor().getDouble(adapt.getCursor()
                    .getColumnIndex(Database.KEY_LITRES));
            double nPrice = adapt.getCursor().getDouble(adapt.getCursor()
                    .getColumnIndex(Database.KEY_PRICE));

            price = (price * litres + nPrice * nLitres)
                    / (litres + nLitres);
            miles += nMiles;
            litres += nLitres;

            SimpleDateFormat sdate = new SimpleDateFormat(DB_DATE_FORMAT, Locale.getDefault());
            ContentValues vals = new ContentValues();
            vals.put(Database.KEY_DATE, sdate.format(new Date()));
            vals.put(Database.KEY_MILES, miles);
            vals.put(Database.KEY_LITRES, litres);
            vals.put(Database.KEY_PRICE, price);
            try {
                cpr.update(MileageProvider.CONTENT_URI,
                        vals,
                        Database.KEY_ID
                                + "="
                                + adapt.getCursor().getInt(adapt.getCursor()
                                        .getColumnIndex(Database.KEY_ID)), null);
                if( Build.VERSION.SDK_INT >= 11 ) {
                    getLoaderManager().restartLoader(0, null, callbacks);
                } else {
                    adapt.getCursor().requery();
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }

            /* Clear text boxes. */
            distanceEntry.setText("");
            volumeEntry.setText("");
            priceEntry.setText("");

            } catch (NumberFormatException n) {
                Toast toast = Toast.makeText(getApplicationContext(), "Invalid value", Toast.LENGTH_LONG);
                toast.show();
            }
        });
		
		if( Build.VERSION.SDK_INT >= 11 ) {
			getLoaderManager().initLoader(0, null, callbacks);
		} else {
			adapt.getCursor().requery();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.mainmenu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem menuItem) {
		switch (menuItem.getItemId()) {
		case R.id.aboutItem:
			onClickAboutMenuItem();
			return true;
		case R.id.deleteHistoryItem:
			onClickDeleteHistoryMenuItem();
			return true;
		case R.id.preferencesItem:
			onClickPreferencesMenuItem();
			return true;
		default:
			return false;
		}
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		savedInstanceState.putString(DISTANCE_SAVE, distanceEntry.getText()
				.toString());
		savedInstanceState.putString(VOLUME_SAVE, volumeEntry.getText()
				.toString());
		savedInstanceState.putString(PRICE_SAVE, priceEntry.getText()
				.toString());
		super.onSaveInstanceState(savedInstanceState);
	}

	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		distanceEntry.setText(savedInstanceState.getString(DISTANCE_SAVE));
		volumeEntry.setText(savedInstanceState.getString(DISTANCE_SAVE));
		priceEntry.setText(savedInstanceState.getString(PRICE_SAVE));
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		cursor.close();
	}

	public void listItemClickHandler(View v) {
		Log.v("ClickHandler", "hit");
		LinearLayout ll = (LinearLayout) v.getParent();

		/* Get cursor from MileageAdapter. */
		MileageItemData m = adapt.getItemData(ll);

		/* Use data from cursor to fill items. */
		double distance = m.miles;
		int distanceUnits = Integer.parseInt(prefs.getString(DISTANCE_UNITS,
				Integer.toString(DISTANCE_UNITS_MILES)));
		switch (distanceUnits) {
		case DISTANCE_UNITS_MILES:
			/* Nothing to do. */
			break;
		case DISTANCE_UNITS_KM:
			distance *= 1.609344;
			break;
		}
		double volume = m.litres;
		int volumeUnits = Integer.parseInt(prefs.getString(VOLUME_UNITS,
				Integer.toString(VOLUME_UNITS_LITRES)));
		switch (volumeUnits) {
		case VOLUME_UNITS_LITRES:
			/* Nothing to do. */
			break;
		case VOLUME_UNITS_GALLONS:
			volume *= 4.54609188;
			break;
		case VOLUME_UNITS_US_GALLONS:
			volume *= 3.78541178;
			break;
		}
		distanceEntry.setText(String.format(Locale.US, "%f", distance));
		volumeEntry.setText(String.format(Locale.US, "%f", volume));
		priceEntry.setText(String.format(Locale.US, "%f", m.price));
	}

	private void onClickAboutMenuItem() {
		String versionString;
		try {
			versionString = getPackageManager().getPackageInfo(
					getPackageName(), 0).versionName;
		} catch (NameNotFoundException e) {
			/* Just set version string as unknown. */
			versionString = "Unknown";
		}
		final Activity activity = this;
		AlertDialog.Builder b = new AlertDialog.Builder(this);
		b.setTitle("About Mileage")
				.setMessage(
						"(c) 2014 - 2018 Andrew Gascoyne-Cecil\n<gascoyne@gmail.com>\nVersion "
								+ versionString)
				.setNeutralButton("License", (dialog, which) -> License.show(activity));
		AlertDialog d = b.create();
		d.show();
	}

	private void onClickDeleteHistoryMenuItem() {
		AlertDialog.Builder b = new AlertDialog.Builder(this);
		b.setTitle("Delete History")
				.setMessage(
						"This will erase all previous records.\nAre you sure?")
				.setPositiveButton("Yes", (dialog, which) -> {
                    try {
                        cpr.delete(MileageProvider.CONTENT_URI, null, null);
                        if( Build.VERSION.SDK_INT >= 11 ) {
                            getLoaderManager().restartLoader(0, null, callbacks);
                        } else {
                            adapt.getCursor().requery();
                        }
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    dialog.cancel();
                }).setNegativeButton("No", (dialog, which) -> dialog.cancel());
		AlertDialog d = b.create();
		d.show();
	}

	private void onClickPreferencesMenuItem() {
		/* Launch Preference activity. */
		Intent i = new Intent(MileageActivity.this, MileagePrefsActivity.class);
		startActivityForResult(i, PREFERENCE_CODE);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case PREFERENCE_CODE:
			setLabels();
			adapt.notifyDataSetChanged();
			break;
		}
	}

	private void setLabels() {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);

		/* Get label for distance. */
		int distanceUnits = Integer.parseInt(prefs.getString(DISTANCE_UNITS,
				Integer.toString(DISTANCE_UNITS_MILES)));
		String[] distanceLabels = getResources().getStringArray(
				R.array.distanceUnitsEntries);
		distanceEntry.setHint(distanceLabels[distanceUnits]);
		switch (distanceUnits) {
		case DISTANCE_UNITS_MILES:
			costLabel.setText(COST_LABEL_PPM);
			break;
		case DISTANCE_UNITS_KM:
			costLabel.setText(COST_LABEL_PPKM);
			break;
		}

		/* Get label for volume. */
		int volumeUnits = Integer.parseInt(prefs.getString(VOLUME_UNITS,
				Integer.toString(VOLUME_UNITS_LITRES)));
		String[] volumeLabels = getResources().getStringArray(
				R.array.volumeUnitsEntries);
		volumeEntry.setHint(volumeLabels[volumeUnits]);

		int economyUnits = Integer.parseInt(prefs.getString(ECONOMY_UNITS,
				Integer.toString(ECONOMY_UNITS_MPG)));
		switch (economyUnits) {
		case ECONOMY_UNITS_MPG:
			economyLabel.setText(ECONOMY_LABEL_MPG);
			break;
		case ECONOMY_UNITS_US_MPG:
			economyLabel.setText(ECONOMY_LABEL_US_MPG);
			break;
		case ECONOMY_UNITS_LP100KM:
			economyLabel.setText(ECONOMY_LABEL_LP100KM);
			break;
		}
	}

	private class MileageCallbacks
		implements LoaderManager.LoaderCallbacks<Cursor> {
		
		private final Activity activity;
		
		MileageCallbacks(Activity activity) {
			this.activity = activity;
		}
		
		public Loader<Cursor> onCreateLoader(int id, Bundle args) {
			return new CursorLoader(activity, MileageProvider.CONTENT_URI, null, null, null, null);
		}
	
		public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
			adapt.swapCursor(cursor);
		}
	
		public void onLoaderReset(Loader<Cursor> loader) {
			adapt.swapCursor(null);
		}
	}
}