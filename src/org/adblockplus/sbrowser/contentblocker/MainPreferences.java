/*
 * This file is part of Adblock Plus <https://adblockplus.org/>,
 * Copyright (C) 2006-2015 Eyeo GmbH
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

import org.adblockplus.sbrowser.contentblocker.engine.Engine;
import org.adblockplus.sbrowser.contentblocker.engine.EngineService;

import com.example.filterapp1.R;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.Log;

public class MainPreferences extends PreferenceActivity implements
    EngineService.OnEngineCreatedCallback, SharedPreferences.OnSharedPreferenceChangeListener
{
  private static final String TAG = MainPreferences.class.getSimpleName();
  private ProgressDialog progressDialog = null;
  private Engine engine = null;

  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    PreferenceManager.setDefaultValues(this, R.layout.preferences_main, false);

    this.getFragmentManager()
        .beginTransaction()
        .replace(android.R.id.content, new Preferences())
        .commit();

    PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext())
        .registerOnSharedPreferenceChangeListener(this);
  }

  @Override
  protected void onStart()
  {
    this.progressDialog = ProgressDialog.show(this,
        this.getString(R.string.initialization_title),
        this.getString(R.string.initialization_message));
    super.onStart();
    EngineService.startService(this, this);
  }

  @Override
  protected void onStop()
  {
    super.onStop();
  }

  @Override
  public void onEngineCreated(Engine engine, boolean success)
  {
    Log.d(TAG, "onEngineCreated: " + success);
    this.engine = success ? engine : null;
    if (this.progressDialog != null)
    {
      this.progressDialog.dismiss();
      this.progressDialog = null;
    }
  }

  @Override
  public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
  {
    if (this.getString(R.string.key_automatic_updates).equals(key) && this.engine != null)
    {
      this.engine.connectivityChanged();
    }
    else if (this.getString(R.string.key_acceptable_ads).equals(key))
    {
      boolean enabled = sharedPreferences.getBoolean(key, true);
      final String id = "url:" + this.engine.getPrefsDefault(Engine.SUBSCRIPTIONS_EXCEPTIONSURL);
      Log.d(TAG, "Acceptable ads " + (enabled ? "enabled" : "disabled"));
      this.engine.changeSubscriptionState(id, enabled);
    }
  }
}
