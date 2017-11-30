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

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import org.adblockplus.sbrowser.contentblocker.engine.DefaultSubscriptionInfo;
import org.adblockplus.sbrowser.contentblocker.engine.Engine;
import org.adblockplus.sbrowser.contentblocker.engine.EngineService;
import org.adblockplus.sbrowser.contentblocker.engine.SubscriptionInfo;
import org.adblockplus.sbrowser.contentblocker.preferences.MultilinePreferenceCategory;
import org.adblockplus.adblockplussbrowser.R;

import android.annotation.SuppressLint;
import android.content.Context;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.text.format.DateUtils;
import android.util.AttributeSet;

@SuppressLint("DefaultLocale")
public class ListedSubscriptionsPreferenceCategory extends MultilinePreferenceCategory implements
    EngineService.OnEngineCreatedCallback, OnPreferenceChangeListener
{
  private Engine engine = null;
  private boolean isEnabledView = false;

  private static final String[] LANGUAGE_TRANSLATIONS =
  {
      "id", "Bahasa Indonesia",
      "he", "עברית"
  };

  private static final HashMap<String, String> LANGUAGE_TRANSLATION_MAP = new HashMap<>();

  static
  {
    for (int i = 0; i < LANGUAGE_TRANSLATIONS.length; i += 2)
    {
      LANGUAGE_TRANSLATION_MAP.put(LANGUAGE_TRANSLATIONS[i], LANGUAGE_TRANSLATIONS[i + 1]);
    }
  }

  public ListedSubscriptionsPreferenceCategory(final Context context)
  {
    super(context);
  }

  public ListedSubscriptionsPreferenceCategory(final Context context, final AttributeSet attrs)
  {
    super(context, attrs);
  }

  @Override
  protected void onAttachedToActivity()
  {
    EngineService.startService(this.getContext().getApplicationContext(), this);
    super.onAttachedToActivity();
  }

  @Override
  public void onEngineCreated(final Engine engine, final boolean success)
  {
    this.engine = engine;
    this.isEnabledView = this.getTitleRes() == R.string.enabled_subscriptions;

    final HashMap<String, Locale> localeMap = new HashMap<>();
    for (final Locale l : Locale.getAvailableLocales())
    {
      final String lang = l.getLanguage();
      if (!lang.isEmpty())
      {
        localeMap.put(lang.toLowerCase(), l);
      }
    }

    if (success)
    {
      final List<SubscriptionInfo> subs = engine.getListedSubscriptions();
      Collections.sort(subs);
      this.removeAll();

      for (final SubscriptionInfo sub : subs)
      {
        if (sub.isEnabled() == this.isEnabledView)
        {
          switch (sub.getType())
          {
            case ADS:
              final DefaultSubscriptionInfo info = engine.getDefaultSubscriptionInfoForUrl(
                  sub.getUrl());
              if (info != null && !info.getPrefixes().isEmpty() && info.isComplete())
              {
                final CheckBoxPreference cbp = new CheckBoxPreference(this.getContext());
                if (this.isEnabledView)
                {
                  final StringBuilder sb = new StringBuilder();
                  sb.append(this.getContext().getString(R.string.last_update));
                  sb.append(' ');
                  final long timestamp = sub.getLastUpdateTime();
                  if (timestamp > 0)
                  {
                    sb.append(DateUtils.formatDateTime(this.getContext(), timestamp,
                        DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_TIME));
                  }
                  else
                  {
                    sb.append(this.getContext().getString(R.string.last_update_never));
                  }
                  cbp.setSummary(sb.toString());
                }

                cbp.setTitle(sub.getTitle());
                final String[] prefixes = info.getPrefixes().split(",");
                final StringBuilder sb = new StringBuilder();
                for (String p : prefixes)
                {
                  final Locale loc = localeMap.get(p.trim().toLowerCase());
                  if (loc != null)
                  {
                    if (sb.length() > 0)
                    {
                      sb.append(", ");
                    }
                    sb.append(loc.getDisplayLanguage(loc));
                  }
                  else
                  {
                    final String name = LANGUAGE_TRANSLATION_MAP.get(p.trim().toLowerCase());
                    {
                      if (name != null)
                      {
                        if (sb.length() > 0)
                        {
                          sb.append(", ");
                        }
                        sb.append(name);
                      }
                    }
                  }
                }

                if (sb.length() > 0)
                {
                  cbp.setTitle(sb.toString());
                }

                cbp.setChecked(sub.isEnabled());
                cbp.setPersistent(false);
                cbp.setKey(sub.getId());
                cbp.setOnPreferenceChangeListener(this);
                this.addPreference(cbp);
              }
              break;
            default:
              break;
          }
        }
      }
    }
  }

  @Override
  public boolean onPreferenceChange(final Preference preference, final Object newValue)
  {
    final String id = preference.getKey();
    final boolean enabled = (Boolean) newValue;

    this.engine.changeSubscriptionState(id, enabled);

    return true;
  }
}
