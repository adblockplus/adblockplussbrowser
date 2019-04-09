/*
 * This file is part of Adblock Plus <https://adblockplus.org/>,
 * Copyright (C) 2006-present eyeo GmbH
 *
 * Adblock Plus is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3 as
 * published by the Free Software Foundation.
 *
 * Adblock Plus is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Adblock Plus.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.adblockplus.sbrowser.contentblocker;

import org.adblockplus.adblockplussbrowser.BuildConfig;
import org.adblockplus.adblockplussbrowser.R;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;

public class Preferences extends PreferenceFragment
{
  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    this.addPreferencesFromResource(R.xml.preferences_main);

    final Preference aboutScreen = findPreference(getString(R.string.key_about));
    final boolean isAbpFlavor = BuildConfig.FLAVOR_product.equals(BuildConfig.FLAVOR_PRODUCT_ABP);
    if (aboutScreen != null && !isAbpFlavor) {
      final PreferenceScreen prefScreen = getPreferenceScreen();
      prefScreen.removePreference(aboutScreen);
    }
  }
}
