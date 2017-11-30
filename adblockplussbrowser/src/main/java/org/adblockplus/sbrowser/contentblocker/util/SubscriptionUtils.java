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

package org.adblockplus.sbrowser.contentblocker.util;

import java.util.List;
import java.util.Locale;

import org.adblockplus.sbrowser.contentblocker.engine.DefaultSubscriptionInfo;
import org.adblockplus.sbrowser.contentblocker.engine.Engine;

import android.content.res.Resources;

public class SubscriptionUtils
{

  private static final String INDONESIAN_OLD = "in";
  private static final String INDONESIAN_NEW = "id";
  private static final String HEBREW_OLD = "iw";
  private static final String HEBREW_NEW = "he";
  private static final String YIDDISH_OLD = "ji";
  private static final String YIDDISH_NEW = "yi";

  /**
   * @param defaultSubscriptions
   * @return The URL of the filter list which fits best for the device language.
   * If no match was found, return EASYLIST as default.
   */
  public static String chooseDefaultSubscriptionUrl(List<DefaultSubscriptionInfo> defaultSubscriptions)
  {
    for (final DefaultSubscriptionInfo info : defaultSubscriptions)
    {
      if (info != null && info.getPrefixes().contains(getDeviceLanguageCode()) && info.isComplete())
      {
        return info.getUrl();
      }
    }
    return Engine.EASYLIST_URL;
  }

  @SuppressWarnings("deprecation")
  private static String getDeviceLanguageCode()
  {
    Locale locale;
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N)
    {
      locale = Resources.getSystem().getConfiguration().getLocales().get(0);
    }
    else
    {
      locale = Resources.getSystem().getConfiguration().locale;
    }

    return convertOldISOCodes(locale.getLanguage());
  }

  /*
   * ISO 639 is not a stable standard; some of the language codes it defines
   * (specifically "iw", "ji", and "in") have changed. Android returns the old codes
   * ("iw", "ji", and "in") but in subscriptions.xml we use the new codes ("he", "yi", and "id").
   * To match the device language to a subscription, we need to convert the old codes.
   */
  private static String convertOldISOCodes(String language)
  {
    switch (language)
    {
      case HEBREW_OLD:
        return HEBREW_NEW;
      case YIDDISH_OLD:
        return YIDDISH_NEW;
      case INDONESIAN_OLD:
        return INDONESIAN_NEW;
      default:
        return language;
    }
  }
}
