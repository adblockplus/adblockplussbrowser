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

import org.adblockplus.adblockplussbrowser.R;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class ConnectivityUtils
{
  public static boolean hasNonMeteredConnection(final Context context)
  {
    final ConnectivityManager connManager = (ConnectivityManager) context
        .getSystemService(Context.CONNECTIVITY_SERVICE);
    final NetworkInfo current = connManager.getActiveNetworkInfo();

    if (current != null && current.isConnectedOrConnecting())
    {
      switch (current.getType())
      {
        case ConnectivityManager.TYPE_BLUETOOTH:
        case ConnectivityManager.TYPE_ETHERNET:
        case ConnectivityManager.TYPE_WIFI:
        case ConnectivityManager.TYPE_WIMAX:
          return true;
        default:
          return false;
      }
    }
    return false;
  }

  public static boolean canUseInternet(final Context context, final boolean acceptMetered)
  {
    final ConnectivityManager connManager = (ConnectivityManager) context
        .getSystemService(Context.CONNECTIVITY_SERVICE);
    final NetworkInfo current = connManager.getActiveNetworkInfo();
    if (current == null)
    {
      return false;
    }

    if (current.isConnectedOrConnecting() && acceptMetered)
    {
      return true;
    }

    final boolean wifiOnly = "1".equals(SharedPrefsUtils.getString(
        context, R.string.key_automatic_updates , "1"));

    if (wifiOnly)
    {
      return hasNonMeteredConnection(context);
    }
    return current.isConnectedOrConnecting();
  }
}
