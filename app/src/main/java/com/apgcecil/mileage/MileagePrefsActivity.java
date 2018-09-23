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

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

@SuppressLint("NewApi")
public class MileagePrefsActivity extends PreferenceActivity {
	
	@SuppressWarnings("deprecation")
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);

		if( Build.VERSION.SDK_INT >= 11 ) {
			getFragmentManager().beginTransaction().replace(android.R.id.content, new PrefsFragment()).commit();
		} else {
			addPreferencesFromResource(R.xml.preferences);
		}
	}
	
	public static class PrefsFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            PreferenceManager.setDefaultValues(getActivity(),
                    R.xml.preferences, false);

            addPreferencesFromResource(R.xml.preferences);
        }
    }


}
