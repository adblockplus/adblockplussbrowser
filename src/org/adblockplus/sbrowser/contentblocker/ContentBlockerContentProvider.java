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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.adblockplus.adblockplussbrowser.R;
import org.adblockplus.sbrowser.contentblocker.engine.Engine;
import org.adblockplus.sbrowser.contentblocker.engine.EngineService;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.preference.PreferenceManager;
import android.util.Log;

public class ContentBlockerContentProvider extends ContentProvider implements
    EngineService.OnEngineCreatedCallback
{
  private static final String TAG = ContentBlockerContentProvider.class.getSimpleName();
  private Engine engine = null;

  @Override
  public Bundle call(String method, String arg, Bundle extras)
  {
    // As of SBC interface v1.4 we return `null` here to signal that we do not
    // use encryption
    return null;
  }

  private static boolean getBooleanPref(final SharedPreferences prefs, final String key,
      final boolean defValue)
  {
    try
    {
      return prefs.getBoolean(key, defValue);
    }
    catch (final Throwable t)
    {
      return defValue;
    }
  }

  private void setApplicationActivated()
  {
    final SharedPreferences prefs = PreferenceManager
        .getDefaultSharedPreferences(this.getContext().getApplicationContext());
    final String key = this.getContext().getString(R.string.key_application_activated);
    final boolean applicationActived = getBooleanPref(prefs, key, false);
    if (!applicationActived)
    {
      prefs.edit()
          .putBoolean(key, true)
          .commit();
    }
  }

  @Override
  public ParcelFileDescriptor openFile(final Uri uri, final String mode)
      throws FileNotFoundException
  {
    if (this.engine == null)
    {
      Log.e(TAG, "Engine not initialized");
      throw new FileNotFoundException("Engine not yet initialized");
    }

    try
    {
      this.setApplicationActivated();
      Log.d(TAG, "Writing filters...");
      final File filterFile = this.engine.createAndWriteFile();
      Log.d(TAG, "Delivering filters...");
      return ParcelFileDescriptor.open(filterFile, ParcelFileDescriptor.MODE_READ_ONLY);
    }
    catch (IOException e)
    {
      Log.e(TAG, "File creation failed: " + e.getMessage(), e);
      return null;
    }
  }

  @Override
  public boolean onCreate()
  {
    EngineService.startService(this.getContext().getApplicationContext(), this);
    return true;
  }

  @Override
  public Cursor query(final Uri uri, final String[] projection, final String selection,
      final String[] selectionArgs, final String sortOrder)
  {
    return null;
  }

  @Override
  public String getType(final Uri uri)
  {
    return null;
  }

  @Override
  public Uri insert(final Uri uri, final ContentValues values)
  {
    return null;
  }

  @Override
  public int delete(final Uri uri, final String selection, final String[] selectionArgs)
  {
    return 0;
  }

  @Override
  public int update(final Uri uri, final ContentValues values, final String selection,
      final String[] selectionArgs)
  {
    return 0;
  }

  @Override
  public void onEngineCreated(Engine engine, boolean success)
  {
    if (success)
    {
      this.engine = engine;
    }
  }
}
