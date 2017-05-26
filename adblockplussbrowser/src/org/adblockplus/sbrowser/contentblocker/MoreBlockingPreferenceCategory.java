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

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.adblockplus.sbrowser.contentblocker.engine.DefaultSubscriptionInfo;
import org.adblockplus.sbrowser.contentblocker.engine.Engine;
import org.adblockplus.sbrowser.contentblocker.engine.EngineService;
import org.adblockplus.sbrowser.contentblocker.engine.SubscriptionInfo;
import org.adblockplus.adblockplussbrowser.R;

import android.content.Context;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.Preference.OnPreferenceChangeListener;
import android.text.format.DateUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

public class MoreBlockingPreferenceCategory extends PreferenceCategory implements
    EngineService.OnEngineCreatedCallback, OnPreferenceChangeListener
{
  private Engine engine = null;
  private static final int[] WHITELISTED_LIST_TITLES =
  {
      R.string.subscription_disable_tracking,
      R.string.subscription_disable_malware,
      R.string.subscription_disable_anti_adblock,
      R.string.subscription_disable_social_media
  };

  private static final String[] WHITELISTED_LIST_URLS =
  {
      "https://easylist-downloads.adblockplus.org/easyprivacy.txt",
      "https://easylist-downloads.adblockplus.org/malwaredomains_full.txt",
      "https://easylist-downloads.adblockplus.org/antiadblockfilters.txt",
      "https://easylist-downloads.adblockplus.org/fanboy-social.txt"
  };

  private static final HashMap<String, Integer> URL_TO_RES_ID_MAP = new HashMap<>();

  static
  {
    for (int i = 0; i < WHITELISTED_LIST_TITLES.length; i++)
    {
      URL_TO_RES_ID_MAP.put(WHITELISTED_LIST_URLS[i], WHITELISTED_LIST_TITLES[i]);
    }
  }

  public MoreBlockingPreferenceCategory(final Context context)
  {
    super(context);
  }

  public MoreBlockingPreferenceCategory(final Context context, final AttributeSet attrs)
  {
    super(context, attrs);
  }

  @Override
  protected View onCreateView(final ViewGroup parent)
  {
    return super.onCreateView(parent);
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
    final String aaLink = engine.getPrefsDefault(Engine.SUBSCRIPTIONS_EXCEPTIONSURL);

    if (success)
    {
      final List<SubscriptionInfo> subs = engine.getListedSubscriptions();
      Collections.sort(subs);
      this.removeAll();

      for (final SubscriptionInfo sub : subs)
      {
        final DefaultSubscriptionInfo info = engine.getDefaultSubscriptionInfoForUrl(sub.getUrl());

        Integer resInt = URL_TO_RES_ID_MAP.get(sub.getUrl());
        if (!(aaLink.equals(sub.getUrl()) || sub.getTitle().startsWith("__"))
            && resInt != null
            && (info == null || info.getPrefixes().isEmpty() || sub.getType() != SubscriptionInfo.Type.ADS))
        {

          final CheckBoxPreference cbp = new CheckBoxPreference(this.getContext());

          if (sub.isEnabled())
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

          cbp.setTitle(this.getContext().getString(resInt.intValue()));
          cbp.setChecked(sub.isEnabled());
          cbp.setPersistent(false);
          cbp.setKey(sub.getId());
          cbp.setOnPreferenceChangeListener(this);
          this.addPreference(cbp);
        }
      }
    }
  }

  @Override
  public boolean onPreferenceChange(final Preference preference, final Object newValue)
  {
    final String id = preference.getKey();
    final boolean enabled = ((Boolean) newValue).booleanValue();

    this.engine.changeSubscriptionState(id, enabled);

    return true;
  }
}
