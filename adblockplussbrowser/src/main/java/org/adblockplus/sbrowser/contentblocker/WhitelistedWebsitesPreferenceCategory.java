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

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.preference.DialogPreference;
import android.preference.PreferenceCategory;
import android.text.Html;
import android.util.AttributeSet;
import android.view.View;

import org.adblockplus.adblockplussbrowser.R;
import org.adblockplus.sbrowser.contentblocker.engine.Engine;
import org.adblockplus.sbrowser.contentblocker.engine.EngineService;
import org.adblockplus.sbrowser.contentblocker.util.SharedPrefsUtils;

import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

public class WhitelistedWebsitesPreferenceCategory extends PreferenceCategory
{
  private final Set<String> whitelistedWebsites = new TreeSet<>();
  private Engine engine;

  public WhitelistedWebsitesPreferenceCategory(Context context, AttributeSet attrs)
  {
    super(context, attrs);
    // This is required to remove the title TextView of the PreferenceCategory
    this.setLayoutResource(R.layout.empty_view);
  }

  @Override
  protected void onAttachedToActivity()
  {
    super.onAttachedToActivity();
    EngineService.startService(this.getContext().getApplicationContext(),
        new EngineService.OnEngineCreatedCallback()
        {
          @Override
          public void onEngineCreated(Engine engine, boolean success)
          {
            if (!success)
            {
              return;
            }
            WhitelistedWebsitesPreferenceCategory.this.engine = engine;
            WhitelistedWebsitesPreferenceCategory.this.initEntries();
          }
        });
  }

  private void initEntries()
  {
    final Set<String> whitelistedWebsites = SharedPrefsUtils.getStringSet(
        this.getContext(), R.string.key_whitelisted_websites, Collections.<String>emptySet());

    this.whitelistedWebsites.clear();
    this.whitelistedWebsites.addAll(whitelistedWebsites);
    this.refreshEntries();
  }

  private void refreshEntries()
  {
    this.removeAll();
    for (final String url : this.whitelistedWebsites)
    {
      final WhitelistedWebsitePreference whitelistedWebsitePreference =
          new WhitelistedWebsitePreference(this.getContext(), url, new DialogInterface.OnClickListener()
          {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
              removeWhitelistedWebsite(url);
            }
          });
      this.addPreference(whitelistedWebsitePreference);
    }

    final InputValidatorDialogPreference urlPreference = new InputValidatorDialogPreference(this.getContext());
    urlPreference.setValidationType(InputValidatorDialogPreference.ValidationType.DOMAIN);
    urlPreference.setTitle(R.string.whitelisted_websites_add_button);
    urlPreference.setDialogTitle(R.string.whitelist_website_dialog_title);
    urlPreference.setDialogMessage(R.string.whitelist_website_dialog_message);
    urlPreference.getEditText().setHint(R.string.whitelist_website_dialog_hint);
    urlPreference.setOnInputReadyListener(new InputValidatorDialogPreference.OnInputReadyListener()
    {
      @Override
      public void onInputReady(String input)
      {
        WhitelistedWebsitesPreferenceCategory.this.whitelistWebsite(input);
      }
    });
    this.addPreference(urlPreference);
  }

  private void whitelistWebsite(String url)
  {
    this.whitelistedWebsites.add(url);
    this.storeWhitelistedWebsites();
  }

  private void removeWhitelistedWebsite(String url)
  {
    this.whitelistedWebsites.remove(url);
    this.storeWhitelistedWebsites();
  }

  private void storeWhitelistedWebsites() {
    SharedPrefsUtils.putStringSet(
        this.getContext(), R.string.key_whitelisted_websites, this.whitelistedWebsites);
    this.refreshEntries();
    this.engine.requestUpdateBroadcast();
  }

  private static class WhitelistedWebsitePreference extends DialogPreference
  {
    private final DialogInterface.OnClickListener onDeleteClickListener;

    WhitelistedWebsitePreference(Context context, String url,
        DialogInterface.OnClickListener onDeleteClickListener)
    {
      super(context);
      this.onDeleteClickListener = onDeleteClickListener;
      final String message = context.getString(R.string.whitelist_remove_dialog_message, url);
      setWidgetLayoutResource(R.layout.whitelisted_website_delete_widget);
      setTitle(url);
      setDialogTitle(R.string.whitelist_remove_dialog_title);
      setDialogMessage(Html.fromHtml(message));
      setNegativeButtonText(android.R.string.cancel);
    }

    @Override
    protected void onBindView(View view)
    {
      super.onBindView(view);
      final View deleteButton = view.findViewById(R.id.whitelisted_website_delete_button);
      deleteButton.setOnClickListener(new View.OnClickListener()
      {
        @Override
        public void onClick(View v)
        {
          if (getDialog() == null || !getDialog().isShowing())
          {
            showDialog(null);
          }
        }
      });
    }

    @Override
    protected void onClick()
    {
      // Overriding the default behaviour of showing a dialog here
      // We just want to show a dialog when the delete button is clicked
    }

    @Override
    protected void onPrepareDialogBuilder(AlertDialog.Builder builder)
    {
      builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener()
      {
        @Override
        public void onClick(DialogInterface dialog, int which)
        {
          if (WhitelistedWebsitePreference.this.onDeleteClickListener != null)
          {
            WhitelistedWebsitePreference.this.onDeleteClickListener.onClick(dialog, which);
          }
        }
      });
    }
  }
}
