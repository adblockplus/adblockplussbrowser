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

import org.adblockplus.sbrowser.contentblocker.engine.Engine;
import org.adblockplus.sbrowser.contentblocker.engine.EngineService;
import org.adblockplus.adblockplussbrowser.R;

import android.app.AlertDialog;
import android.app.Dialog;
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
  private Engine engine = null;
  private Dialog dialog;
  private int dialogTitleResId;

  private SharedPreferences getSharedPreferences()
  {
    return PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
  }

  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    PreferenceManager.setDefaultValues(this, R.xml.preferences_main, false);

    this.getFragmentManager()
        .beginTransaction()
        .replace(android.R.id.content, new Preferences())
        .commit();

    // This try/catch block is a workaround for a preference mismatch
    // issue. We check for a type mismatch in one particular key and,
    // if there's a mismatch, clean sweep the preferences.
    // See: https://issues.adblockplus.org/ticket/3931
    try
    {
      this.getSharedPreferences().getBoolean(
          this.getString(R.string.key_application_activated),
          false);
    }
    catch(final Throwable t)
    {
      this.getSharedPreferences()
          .edit()
          .clear()
          .commit();
    }
  }

  @Override
  protected void onStart()
  {
    this.dialogTitleResId = R.string.initialization_title;
    this.dialog = ProgressDialog.show(this,
        this.getString(this.dialogTitleResId),
        this.getString(R.string.initialization_message));
    super.onStart();
    this.getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    EngineService.startService(this.getApplicationContext(), this);
  }

  @Override
  protected void onStop()
  {
    super.onStop();
    this.getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    this.dismissDialog();
  }

  private void dismissDialog()
  {
    if (this.dialog != null)
    {
      this.dialogTitleResId = 0;
      this.dialog.dismiss();
      this.dialog = null;
    }
  }

  private void checkForCompatibleSBrowserAndProceed()
  {
    if (!Engine.hasCompatibleSBrowserInstalled(this.getApplicationContext()))
    {
      this.dialogTitleResId = R.string.sbrowser_dialog_title;
      this.dialog = new AlertDialog.Builder(this)
          .setCancelable(false)
          .setTitle(this.dialogTitleResId)
          .setMessage(Html.fromHtml(getString(R.string.sbrowser_dialog_message)))
          .setNeutralButton(R.string.sbrowser_dialog_button, new OnClickListener()
          {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
              try
              {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id="
                    + Engine.SBROWSER_APP_ID)));
              }
              catch (final Throwable t)
              {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri
                    .parse("https://play.google.com/store/apps/details?id=" + Engine.SBROWSER_APP_ID)));
              }
            }
          }).create();
      this.dialog.show();
    }
    else
    {
      this.checkAAStatusAndProceed();
    }
  }

  private void checkAAStatusAndProceed()
  {
    final SharedPreferences prefs = this.getSharedPreferences();
    final String keyAaInfoShown = this.getString(R.string.key_aa_info_shown);
    final boolean aaInfoShown = prefs.getBoolean(keyAaInfoShown, false);
    if (!aaInfoShown)
    {
      this.dialogTitleResId = R.string.aa_dialog_title;
      this.dialog = new AlertDialog.Builder(this)
          .setCancelable(false)
          .setTitle(this.dialogTitleResId)
          .setMessage(Html.fromHtml(getString(R.string.aa_dialog_message)))
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
      this.dialog.show();
    }
    else
    {
      this.checkSetupStatus();
    }
  }

  private void checkSetupStatus()
  {
    final boolean applicationActivated = this.getSharedPreferences()
        .getBoolean(this.getString(R.string.key_application_activated), false);

    if (!applicationActivated)
    {
      Log.d(TAG, "Showing setup dialog");
      this.dialogTitleResId = R.string.setup_dialog_title;
      this.dialog = new AlertDialog.Builder(this)
          .setCancelable(false)
          .setTitle(this.dialogTitleResId)
          .setMessage(Html.fromHtml(getString(Engine.hasSamsungInternetVersion5OrNewer(MainPreferences.this) ?
                  R.string.setup_dialog_message_sbrowser_5 : R.string.setup_dialog_message_sbrowser_4)))
          .setNeutralButton(R.string.setup_dialog_button, new OnClickListener()
          {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
              Engine.openSBrowserSettings(MainPreferences.this);
            }
          })
          .create();
      this.dialog.show();
    }
  }

  @Override
  public void onEngineCreated(Engine engine, boolean success)
  {
    Log.d(TAG, "onEngineCreated: " + success);
    this.engine = success ? engine : null;
    if (this.dialogTitleResId == R.string.initialization_title)
    {
      this.dismissDialog();

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
      if (this.dialogTitleResId == R.string.setup_dialog_title)
      {
        this.dismissDialog();
      }
    }
  }
}