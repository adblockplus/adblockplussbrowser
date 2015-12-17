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

import java.util.Collections;
import java.util.List;

import org.adblockplus.sbrowser.contentblocker.engine.Engine;
import org.adblockplus.sbrowser.contentblocker.engine.EngineService;
import org.adblockplus.sbrowser.contentblocker.engine.SubscriptionInfo;

import com.example.filterapp1.R;

import android.content.Context;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.Preference.OnPreferenceChangeListener;
import android.text.format.DateUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

public class ListedSubscriptionsPreferenceCategory extends PreferenceCategory implements
    EngineService.OnEngineCreatedCallback, OnPreferenceChangeListener
{
  private Engine engine = null;
  private boolean isEnabledView = false;

  public ListedSubscriptionsPreferenceCategory(Context context)
  {
    super(context);
  }

  public ListedSubscriptionsPreferenceCategory(Context context, AttributeSet attrs)
  {
    super(context, attrs);
  }

  @Override
  protected View onCreateView(ViewGroup parent)
  {
    return super.onCreateView(parent);
  }

  @Override
  protected void onAttachedToActivity()
  {
    EngineService.startService(this.getContext(), this);
    super.onAttachedToActivity();
  }

  @Override
  public void onEngineCreated(Engine engine, boolean success)
  {
    this.engine = engine;
    this.isEnabledView = this.getTitleRes() == R.string.enabled_subscriptions;

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
            case OTHER:
            case CUSTOM:
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
              cbp.setChecked(sub.isEnabled());
              cbp.setPersistent(false);
              cbp.setKey(sub.getId());
              cbp.setOnPreferenceChangeListener(this);
              this.addPreference(cbp);
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
    final boolean enabled = ((Boolean) newValue).booleanValue();

    this.engine.changeSubscriptionState(id, enabled);

    return true;
  }
}
