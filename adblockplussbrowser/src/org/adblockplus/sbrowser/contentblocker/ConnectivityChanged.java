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

import org.adblockplus.sbrowser.contentblocker.engine.Engine;
import org.adblockplus.sbrowser.contentblocker.engine.EngineService;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class ConnectivityChanged extends BroadcastReceiver implements
    EngineService.OnEngineCreatedCallback
{
  private static final String TAG = ConnectivityChanged.class.getSimpleName();

  private static final String ACTION = "android.net.conn.CONNECTIVITY_CHANGE";

  @Override
  public void onReceive(final Context context, final Intent intent)
  {
    if (intent != null && ACTION.equals(intent.getAction()))
    {
      Log.d(TAG, "Triggering connectivity changed event");
      EngineService.startService(context, this);
    }
  }

  @Override
  public void onEngineCreated(Engine engine, boolean success)
  {
    if (success)
    {
      engine.connectivityChanged();
    }
  }
}
