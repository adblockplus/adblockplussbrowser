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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public final class DefaultSubscriptionInfo implements Comparable<DefaultSubscriptionInfo>
{
  private final static String KEY_TITLE = "title";
  private final static String KEY_URL = "url";
  private final static String KEY_AUTHOR = "author";
  private final static String KEY_PREFIXES = "prefixes";
  private final static String KEY_SPECIALIZATION = "specialization";
  private final static String KEY_HOMEPAGE = "homepage";
  private final static String KEY_TYPE = "type";

  final HashMap<String, String> attributes = new HashMap<>();

  private String getValue(final String key)
  {
    final String value = this.attributes.get(key);
    return value != null ? value : "";
  }

  public String getTitle()
  {
    return this.getValue(KEY_TITLE);
  }

  public String getUrl()
  {
    return this.getValue(KEY_URL);
  }

  public String getHomepage()
  {
    return this.getValue(KEY_HOMEPAGE);
  }

  public String getAuthor()
  {
    return this.getValue(KEY_AUTHOR);
  }

  public String getPrefixes()
  {
    return this.getValue(KEY_PREFIXES);
  }

  public String getSpecialization()
  {
    return this.getValue(KEY_SPECIALIZATION);
  }

  public String getType()
  {
    return this.getValue(KEY_TYPE);
  }

  @Override
  public int compareTo(@NonNull final DefaultSubscriptionInfo o)
  {
    final int cmp = this.getTitle().compareTo(o.getTitle());
    if (cmp != 0)
    {
      return cmp;
    }
    return this.getUrl().compareTo(o.getUrl());
  }

  @Override
  public String toString()
  {
    return this.getTitle() + ": " + this.getUrl();
  }
}
