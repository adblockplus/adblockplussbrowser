/*
 * This file is part of Adblock Plus <https://adblockplus.org/>,
 * Copyright (C) 2006-2016 Eyeo GmbH
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.adblockplus.sbrowser.contentblocker.engine.Engine;
import org.adblockplus.sbrowser.contentblocker.engine.EngineService;
import org.adblockplus.adblockplussbrowser.R;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.text.Html;
import android.util.Log;

public class MainPreferences extends PreferenceActivity implements
    EngineService.OnEngineCreatedCallback, SharedPreferences.OnSharedPreferenceChangeListener
{
  private static final String TAG = MainPreferences.class.getSimpleName();
  private static final String SBROWSER_APP_ID = "com.sec.android.app.sbrowser";
  private ProgressDialog progressDialog = null;
  private Engine engine = null;
  private AlertDialog setupDialog = null;

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

  private void checkForCompatibleSBrowserAndProceed()
  {
    if (!Engine.hasCompatibleSBrowserInstalled(this.getApplicationContext()))
    {
      final AlertDialog d = new AlertDialog.Builder(this)
          .setCancelable(false)
          .setTitle(R.string.sbrowser_dialog_title)
          .setMessage(Html.fromHtml(this.readTextFile(R.raw.sbrowser_dialog)))
          .setNeutralButton(R.string.sbrowser_dialog_button, new OnClickListener()
          {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
              try
              {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id="
                    + SBROWSER_APP_ID)));
              }
              catch (final Throwable t)
              {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri
                    .parse("https://play.google.com/store/apps/details?id=" + SBROWSER_APP_ID)));
              }
            }
          }).create();
      d.show();
    }
    else
    {
      this.checkAAStatusAndProceed();
    }
  }

  private void checkAAStatusAndProceed()
  {
    final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
    final String keyAaInfoShown = this.getString(R.string.key_aa_info_shown);
    final boolean aaInfoShown = prefs.getBoolean(keyAaInfoShown, false);
    if (!aaInfoShown)
    {
      final AlertDialog d = new AlertDialog.Builder(this)
          .setCancelable(false)
          .setTitle(R.string.aa_dialog_title)
          .setMessage(Html.fromHtml(this.readTextFile(R.raw.aa_dialog)))
          .setNeutralButton(R.string.aa_dialog_button, new OnClickListener()
          {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
              prefs.edit()
                  .putBoolean(keyAaInfoShown, true)
                  .commit();
              MainPreferences.this.checkSetupStatus();
            }
          }).create();
      d.show();
    }
    else
    {
      this.checkSetupStatus();
    }
  }

  private void checkSetupStatus()
  {
    final boolean applicationActivated = PreferenceManager.getDefaultSharedPreferences(this)
        .getBoolean(this.getString(R.string.key_application_activated), false);

    if (!applicationActivated)
    {
      Log.d(TAG, "Showing setup dialog");
      this.setupDialog = new AlertDialog.Builder(this)
          .setCancelable(false)
          .setTitle(R.string.setup_dialog_title)
          .setMessage(Html.fromHtml(this.readTextFile(R.raw.setup_dialog)))
          .setNeutralButton(R.string.setup_dialog_button, new OnClickListener()
          {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
              Engine.openSBrowserSettings(MainPreferences.this);
            }
          })
          .create();
      this.setupDialog.show();
    }
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

      this.checkForCompatibleSBrowserAndProceed();
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
    else if (this.getString(R.string.key_application_activated).equals(key))
    {
      if (this.setupDialog != null)
      {
        this.setupDialog.dismiss();
        this.setupDialog = null;
      }
    }
  }

  private String readTextFile(int id)
  {
    try
    {
      final BufferedReader r = new BufferedReader(new InputStreamReader(this.getResources()
          .openRawResource(id), "UTF-8"));
      try
      {
        final StringBuilder sb = new StringBuilder();
        for (String line = r.readLine(); line != null; line = r.readLine())
        {
          sb.append(line);
          sb.append('\n');
        }
        return sb.toString();
      }
      finally
      {
        r.close();
      }
    }
    catch (IOException e)
    {
      Log.e(TAG, "Resource reading failed for: " + id, e);
      return "...";
    }
  }
}
