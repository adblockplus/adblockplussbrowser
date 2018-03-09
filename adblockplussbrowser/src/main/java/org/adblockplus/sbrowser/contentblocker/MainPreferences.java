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

import org.adblockplus.sbrowser.contentblocker.engine.Engine;
import org.adblockplus.sbrowser.contentblocker.engine.EngineManager;
import org.adblockplus.adblockplussbrowser.R;
import org.adblockplus.sbrowser.contentblocker.util.ConnectivityUtils;
import org.adblockplus.sbrowser.contentblocker.util.SharedPrefsUtils;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;

public class MainPreferences extends PreferenceActivity implements
    EngineManager.OnEngineCreatedCallback, Engine.SubscriptionUpdateCallback,
    Preference.OnPreferenceClickListener
{
  private static final String TAG = MainPreferences.class.getSimpleName();
  private Engine engine = null;
  private AlertDialog dialog;
  private int dialogTitleResId;

  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    PreferenceManager.setDefaultValues(this, R.xml.preferences_main, false);

    this.getFragmentManager()
        .beginTransaction()
        .replace(android.R.id.content, new Preferences(), Preferences.class.getSimpleName())
        .commit();
  }

  @Override
  protected void onStart()
  {
    this.dialogTitleResId = R.string.initialization_title;
    this.dialog = ProgressDialog.show(this,
        this.getString(this.dialogTitleResId),
        this.getString(R.string.initialization_message));
    super.onStart();
    SharedPrefsUtils.registerOnSharedPreferenceChangeListener(this, listener);
    EngineManager.getInstance().retrieveEngine(this, this);
  }

  @Override
  protected void onStop()
  {
    super.onStop();
    SharedPrefsUtils.unregisterOnSharedPreferenceChangeListener(this, listener);
    this.dismissDialog();
  }

  @Override
  protected void onDestroy()
  {
    EngineManager.getInstance().removeOnEngineCreatedCallback(this);
    super.onDestroy();
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
    final boolean aaInfoShown = SharedPrefsUtils.getBoolean(this, R.string.key_aa_info_shown, false);
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
              SharedPrefsUtils.putBoolean(MainPreferences.this, R.string.key_aa_info_shown, true);
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
    final boolean applicationActivated = SharedPrefsUtils.getBoolean(
        this, R.string.key_application_activated, false);

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

      final Button btNeutral = this.dialog.getButton(AlertDialog.BUTTON_NEUTRAL);
      final LinearLayout.LayoutParams btNeutralLayoutParams = (LinearLayout.LayoutParams) btNeutral.getLayoutParams();
      btNeutralLayoutParams.width = LinearLayout.LayoutParams.MATCH_PARENT;
      btNeutralLayoutParams.gravity = Gravity.CENTER;
      btNeutral.setLayoutParams(btNeutralLayoutParams);
    }
  }

  @Override
  public void onEngineCreated(Engine engine)
  {
    Log.d(TAG, "onEngineCreated: " + (engine != null));
    this.engine = engine;

    if (engine != null)
    {
      this.engine.setSubscriptionUpdateCallback(this);
    }

    if (this.dialogTitleResId == R.string.initialization_title)
    {
      this.dismissDialog();

      this.checkForCompatibleSBrowserAndProceed();
    }

    final Fragment preferecesFragment = getFragmentManager()
        .findFragmentByTag(Preferences.class.getSimpleName());
    if (preferecesFragment != null)
    {
      final Preference button = ((Preferences) preferecesFragment)
          .findPreference(getString(R.string.key_force_update_subscriptions));
      button.setOnPreferenceClickListener(this);
    }
  }

  @Override
  public void subscriptionUpdateRequested(final boolean enabled)
  {
    this.dialog = ProgressDialog.show(this, null, enabled
        ? getString(R.string.add_subscription_dialog_message)
        : getString(R.string.remove_subscription_dialog_message));
  }

  @Override
  public void subscriptionUpdatedApplied()
  {
    this.dismissDialog();
  }

  @Override
  public boolean onPreferenceClick(Preference preference)
  {
    if (getString(R.string.key_force_update_subscriptions).equals(preference.getKey()))
    {
      if (ConnectivityUtils.hasNonMeteredConnection(this))
      {
        if (engine != null)
        {
          engine.forceUpdateSubscriptions(false);
        }
      }
      else
      {
        boolean meteredInternetAvailable = ConnectivityUtils.canUseInternet(this, true);
        this.dialog = new AlertDialog.Builder(this)
            .setTitle(R.string.update_subscriptions)
            .setMessage(
                meteredInternetAvailable
                ? R.string.metered_connection_warning
                : R.string.check_your_connection)
            .setNegativeButton(android.R.string.cancel, new OnClickListener()
            {
              @Override
              public void onClick(DialogInterface dialog, int which)
              {
                dialog.cancel();
              }
            })
            .create();

        if (meteredInternetAvailable)
        {
          this.dialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(android.R.string.yes),
              new OnClickListener()
              {
                @Override
                public void onClick(DialogInterface dialogInterface, int i)
                {
                  if (engine != null)
                  {
                    engine.forceUpdateSubscriptions(true);
                  }
                }
              });
        }
        this.dialog.show();
      }
      return true;
    }
    return false;
  }

  private final SharedPrefsUtils.OnSharedPreferenceChangeListener listener =
      new SharedPrefsUtils.OnSharedPreferenceChangeListener()
  {
    @Override
    protected void onSharedPreferenceChanged(String key)
    {
      if (getString(R.string.key_automatic_updates).equals(key) && engine != null)
      {
        engine.connectivityChanged();
      }
      else if (getString(R.string.key_acceptable_ads).equals(key) && engine != null)
      {
        final boolean enabled = SharedPrefsUtils.getBoolean
            (MainPreferences.this, R.string.key_acceptable_ads, true);
        final String id = "url:" + engine.getPrefsDefault(Engine.SUBSCRIPTIONS_EXCEPTIONSURL);
        Log.d(TAG, "Acceptable ads " + (enabled ? "enabled" : "disabled"));
        engine.changeSubscriptionState(id, enabled);
      }
      else if (getString(R.string.key_application_activated).equals(key))
      {
        if (dialogTitleResId == R.string.setup_dialog_title)
        {
          dismissDialog();
        }
      }
    }
  };
}