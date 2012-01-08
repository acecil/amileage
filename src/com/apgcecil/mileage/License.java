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

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;

public class License {

	public static void show(final Activity activity) {
		final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
		builder.setTitle(R.string.lic_title);
		builder.setCancelable(true);
		builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
			public void onCancel(DialogInterface dialog) {
				;
			}
		});
		builder.setMessage(readFile(activity));
		builder.create().show();
	}

	private static CharSequence readFile(Activity activity) {
		BufferedReader in = null;
		try {
			in = new BufferedReader(new InputStreamReader(
					activity.getAssets().open("LICENSE")));
			String line;
			StringBuilder buffer = new StringBuilder();
			while ((line = in.readLine()) != null) buffer.append(line).append('\n');
			return buffer;
		} catch (IOException e) {
			return "";
		} finally {
			closeStream(in);
		}
	}

	private static void closeStream(Closeable stream) {
		if (stream != null) {
			try {
				stream.close();
			} catch (IOException e) {
				// Ignore
			}
		}
	}

}
