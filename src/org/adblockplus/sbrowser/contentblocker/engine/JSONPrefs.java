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

package org.adblockplus.sbrowser.contentblocker.engine;

import java.io.IOException;
import java.io.InputStream;

import org.json.JSONException;
import org.json.JSONObject;

final class JSONPrefs
{
  private final JSONObject jsonObject;

  private JSONPrefs(JSONObject object)
  {
    this.jsonObject = object;
  }

  public String getDefaults(final String key)
  {
    try
    {
      final JSONObject defaults = this.jsonObject.getJSONObject("defaults");
      return defaults != null && defaults.has(key) ? defaults.getString(key) : null;
    }
    catch (final JSONException e)
    {
      return null;
    }
  }

  public static JSONPrefs create(final InputStream instream) throws IOException
  {
    try
    {
      return new JSONPrefs(new JSONObject(Engine.readFileAsString(instream)));
    }
    catch (JSONException e)
    {
      throw new IOException("Failed to parse JSON", e);
    }
  }
}
