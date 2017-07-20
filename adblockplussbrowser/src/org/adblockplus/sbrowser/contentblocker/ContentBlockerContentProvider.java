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
import org.adblockplus.sbrowser.contentblocker.util.SharedPrefsUtils;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.support.annotation.NonNull;
import android.util.Log;

public class ContentBlockerContentProvider extends ContentProvider
{
  private static final String TAG = ContentBlockerContentProvider.class.getSimpleName();

  @Override
  public Bundle call(@NonNull String method, String arg, Bundle extras)
  {
    // As of SBC interface v1.4 we return `null` here to signal that we do not
    // use encryption
    return null;
  }

  private void setApplicationActivated()
  {
    final boolean applicationActivated = SharedPrefsUtils.getBoolean(
        this.getContext(), R.string.key_application_activated, false);

    if (!applicationActivated)
    {
      SharedPrefsUtils.putBoolean(this.getContext(), R.string.key_application_activated, true);
    }
  }

  @Override
  public ParcelFileDescriptor openFile(@NonNull final Uri uri, @NonNull final String mode)
      throws FileNotFoundException
  {
    try
    {
      this.setApplicationActivated();
      Log.d(TAG, "Writing filters...");
      final File filterFile = Engine.getOrCreateCachedFilterFile(getContext());
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
    Log.i(TAG, "onCreate() called");
    getContext().startService(new Intent(getContext(), EngineService.class));
    Log.i(TAG, "Requested service startup");
    return true;
  }

  @Override
  public Cursor query(@NonNull final Uri uri, final String[] projection, final String selection,
      final String[] selectionArgs, final String sortOrder)
  {
    return null;
  }

  @Override
  public String getType(@NonNull final Uri uri)
  {
    return null;
  }

  @Override
  public Uri insert(@NonNull final Uri uri, final ContentValues values)
  {
    return null;
  }

  @Override
  public int delete(@NonNull final Uri uri, final String selection, final String[] selectionArgs)
  {
    return 0;
  }

  @Override
  public int update(@NonNull final Uri uri, final ContentValues values, final String selection,
      final String[] selectionArgs)
  {
    return 0;
  }
}
