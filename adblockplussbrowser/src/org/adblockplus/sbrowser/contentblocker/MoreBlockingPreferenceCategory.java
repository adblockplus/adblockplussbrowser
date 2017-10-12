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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.adblockplus.sbrowser.contentblocker.engine.DefaultSubscriptionInfo;
import org.adblockplus.sbrowser.contentblocker.engine.Engine;
import org.adblockplus.sbrowser.contentblocker.engine.EngineService;
import org.adblockplus.sbrowser.contentblocker.engine.SubscriptionInfo;
import org.adblockplus.adblockplussbrowser.R;
import org.adblockplus.sbrowser.contentblocker.preferences.MultilineCheckBoxPreference;

import android.content.Context;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.Preference.OnPreferenceChangeListener;
import android.text.format.DateUtils;
import android.util.AttributeSet;
import android.util.Log;

public class MoreBlockingPreferenceCategory extends PreferenceCategory implements
    EngineService.OnEngineCreatedCallback, OnPreferenceChangeListener, Engine.SubscriptionAddedCallback
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

  private static final Map<String, Integer> URL_TO_RES_ID_MAP = new HashMap<>();

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
  protected void onAttachedToActivity()
  {
    EngineService.startService(this.getContext().getApplicationContext(), this);
    super.onAttachedToActivity();
  }

  @Override
  public void onEngineCreated(final Engine engine, final boolean success)
  {
    this.engine = engine;

    if (success)
    {
      refreshEntries();
    }
  }

  private void refreshEntries()
  {
    final List<SubscriptionInfo> subs = getMoreBlockingPreferenceSubscriptions();
    sortSubscriptionsByRelevance(subs);
    this.removeAll();

    for (final SubscriptionInfo sub : subs)
    {
      Integer resInt = URL_TO_RES_ID_MAP.get(sub.getUrl());
      final MultilineCheckBoxPreference cbp = new MultilineCheckBoxPreference(this.getContext());

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
      else
      {
        if (sub.getType() == SubscriptionInfo.Type.CUSTOM)
        {
          engine.removeSubscriptionById(sub.getId());
          continue;
        }
      }

      cbp.setTitle(resInt == null ? sub.getTitle() : getContext().getString(resInt));
      cbp.setChecked(sub.isEnabled());
      cbp.setPersistent(false);
      cbp.setKey(sub.getId());
      cbp.setOnPreferenceChangeListener(this);
      this.addPreference(cbp);
    }

    final InputValidatorDialogPreference urlPreference = new InputValidatorDialogPreference(this.getContext());
    urlPreference.setValidationType(InputValidatorDialogPreference.ValidationType.URL);
    urlPreference.setTitle(R.string.add_another_list);
    urlPreference.setDialogTitle(R.string.add_another_list);
    urlPreference.getEditText().setHint(R.string.add_another_list_url_hint);
    urlPreference.setOnInputReadyListener(new InputValidatorDialogPreference.OnInputReadyListener()
    {
      @Override
      public void onInputReady(String input)
      {
        if (!input.toLowerCase().startsWith("http://") && !input.toLowerCase().startsWith("https://"))
        {
          input =  "http://" + input;
        }

        try
        {
          engine.createAndAddSubscriptionFromUrl(input, MoreBlockingPreferenceCategory.this);
        }
        catch (IOException e)
        {
          Log.e(getClass().getSimpleName(), "Unable to add subscription from url", e);
        }
      }
    });
    this.addPreference(urlPreference);
  }

  private void sortSubscriptionsByRelevance(final List<SubscriptionInfo> moreBlockingPreferenceSubscriptions)
  {
    Collections.sort(moreBlockingPreferenceSubscriptions, new Comparator<SubscriptionInfo>()
    {
      @Override
      public int compare(SubscriptionInfo o1, SubscriptionInfo o2)
      {
        if (URL_TO_RES_ID_MAP.containsKey(o1.getUrl()) && URL_TO_RES_ID_MAP.containsKey(o2.getUrl()))
        {
          return o1.getTitle().compareTo(o2.getTitle());
        }

        if (URL_TO_RES_ID_MAP.containsKey(o1.getUrl()) && !URL_TO_RES_ID_MAP.containsKey(o2.getUrl()))
        {
          return -1;
        }

        if (!URL_TO_RES_ID_MAP.containsKey(o1.getUrl()) && URL_TO_RES_ID_MAP.containsKey(o2.getUrl()))
        {
          return 1;
        }

        return 0;
      }
    });
  }

  private List<SubscriptionInfo> getMoreBlockingPreferenceSubscriptions()
  {
    List<SubscriptionInfo> moreBlockingPreferenceSubscriptions = new ArrayList<>(5);
    for (SubscriptionInfo sub : engine.getListedSubscriptions())
    {
      final DefaultSubscriptionInfo info = engine.getDefaultSubscriptionInfoForUrl(sub.getUrl());
      Integer resInt = URL_TO_RES_ID_MAP.get(sub.getUrl());

      if (sub.getType() == SubscriptionInfo.Type.CUSTOM)
      {
        moreBlockingPreferenceSubscriptions.add(sub);
        continue;
      }

      if (info != null && !info.isComplete() && sub.isEnabled())
      {
        moreBlockingPreferenceSubscriptions.add(sub);
        continue;
      }

      if ((!(engine.isAcceptableAdsUrl(sub)) || sub.getTitle().startsWith("__"))
          && resInt != null
          && (info == null || info.getPrefixes().isEmpty() || sub.getType() != SubscriptionInfo.Type.ADS))
      {
        moreBlockingPreferenceSubscriptions.add(sub);
        continue;
      }
    }

    return moreBlockingPreferenceSubscriptions;
  }

  @Override
  public boolean onPreferenceChange(final Preference preference, final Object newValue)
  {
    final String id = preference.getKey();
    final boolean enabled = (Boolean) newValue;

    this.engine.changeSubscriptionState(id, enabled);

    return true;
  }

  @Override
  public void subscriptionAdded()
  {
    refreshEntries();
  }
}
