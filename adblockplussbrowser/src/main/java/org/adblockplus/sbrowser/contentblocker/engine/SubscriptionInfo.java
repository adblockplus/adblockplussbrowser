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

package org.adblockplus.sbrowser.contentblocker.engine;

import android.support.annotation.NonNull;

/**
 * Class for querying subscription information from the engine.
 */
public class SubscriptionInfo implements Comparable<SubscriptionInfo>
{
  public enum Type
  {
    ADS,
    OTHER,
    CUSTOM,
    INITIALIZING,
    USER_FILTERS,
    USER_EXCEPTIONS,
    ACCEPTABLE_ADS
  }

  private final Type type;
  private final String id;
  private final String url;
  private String title;
  private boolean enabled;
  private long lastUpdate;

  private SubscriptionInfo(final Type type, final String id, final String url, final String title,
                           final boolean enabled, final long lastUpdate)
  {
    this.type = type;
    this.id = id;
    this.url = url;
    this.title = title;
    this.enabled = enabled;
    this.lastUpdate = lastUpdate;
  }

  public Type getType()
  {
    return this.type;
  }

  public String getId()
  {
    return this.id;
  }

  public boolean isEnabled()
  {
    return this.enabled;
  }

  public String getTitle()
  {
    return this.title;
  }

  public String getUrl()
  {
    return this.url;
  }

  public long getLastUpdateTime()
  {
    return this.lastUpdate;
  }

  static SubscriptionInfo create(final Engine engine, final Subscription subscription)
  {
    final DefaultSubscriptionInfo defaultSubscription = engine
        .getDefaultSubscriptionInfo(subscription);
    final String title = subscription.getTitle();
    final String acceptableAdsLink = engine.getPrefsDefault("subscriptions_exceptionsurl");
    final String url = subscription.getURL() != null ? subscription.getURL().toString() : null;

    Type type = null;
    switch (subscription.getType())
    {
      case DOWNLOADABLE:
        if (defaultSubscription != null)
        {
          if (acceptableAdsLink.equals(url))
          {
            type = Type.ACCEPTABLE_ADS;
          }
          else if (Type.ADS.toString().equalsIgnoreCase(defaultSubscription.getType()))
          {
            type = Type.ADS;
          }
          else
          {
            type = Type.OTHER;
          }
        }
        break;
      case USER:
        if (Engine.USER_EXCEPTIONS_TITLE.equals(title))
        {
          type = Type.USER_EXCEPTIONS;
        }
        else if (Engine.USER_FILTERS_TITLE.equals(title))
        {
          type = Type.USER_FILTERS;
        }
        else
        {
          throw new IllegalStateException("Unknown user subscription with title '" + title + "'");
        }
        break;
    }

    if (type == null)
    {
      type = acceptableAdsLink.equals(url) ? Type.ACCEPTABLE_ADS : Type.CUSTOM;
    }

    return new SubscriptionInfo(type,
        subscription.getId(),
        url,
        title,
        subscription.isEnabled(),
        subscription.getLastUpdateTimestamp());
  }

  @Override
  public int compareTo(@NonNull final SubscriptionInfo another)
  {
    return this.getTitle().compareTo(another.getTitle());
  }
}
