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

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.GZIPOutputStream;

import org.adblockplus.adblockplussbrowser.R;
import org.adblockplus.sbrowser.contentblocker.util.SharedPrefsUtils;

import android.content.Context;
import android.text.format.DateUtils;
import android.util.Log;

import javax.net.ssl.HttpsURLConnection;

public class Notification
{
  private static final String TAG = Notification.class.getSimpleName();
  public static final String NOTIFICATION_URL = "https://notification.adblockplus.org/notification.json";
  public static final String NOTIFICATION_DATA_FILE_NAME = "notification.abp";
  public static final String KEY_EXTRA_ID = "_extra_id";
  public static final String KEY_EXTRA_URL = "_extra_url";

  private static final long NOTIFICATION_DOWNLOAD_INTERVAL = DateUtils.DAY_IN_MILLIS;
  private static final long DOWNLOAD_RETRY_INTERVAL = DateUtils.HOUR_IN_MILLIS;

  public static boolean shouldUpdate(final Context context)
  {
    final long now = System.currentTimeMillis();
    final long lastUpdate = SharedPrefsUtils.getLong(context, R.string.key_last_notification_update_timestamp, 0);
    final long lastTry = SharedPrefsUtils.getLong(context, R.string.key_last_tried_notification_update_timestamp, 0);

    if (lastTry > lastUpdate)
    {
      return now - lastTry > DOWNLOAD_RETRY_INTERVAL;
    }
    else
    {
      return now - lastUpdate > NOTIFICATION_DOWNLOAD_INTERVAL;
    }
  }

  public static void update(final Context context, final int responseCode, final String text,
      final File notificationDataFile)
  {
    if (responseCode != HttpsURLConnection.HTTP_OK || text == null)
    {
      SharedPrefsUtils.putLong(context, R.string.key_last_tried_notification_update_timestamp, System.currentTimeMillis());
    }
    else
    {
      SharedPrefsUtils.putLong(context, R.string.key_last_notification_update_timestamp, System.currentTimeMillis());
      persistData(notificationDataFile, text);
    }
  }

  private static void persistData(final File filtersFile, final String text)
  {
    try (final DataOutputStream outputStream = new DataOutputStream(new BufferedOutputStream(
        new GZIPOutputStream(new FileOutputStream(filtersFile)))))
    {
      outputStream.write(text.getBytes());
    }
    catch (IOException e)
    {
      Log.d(TAG, "Failed to write notification data to internal storage.", e);
    }
  }
}
