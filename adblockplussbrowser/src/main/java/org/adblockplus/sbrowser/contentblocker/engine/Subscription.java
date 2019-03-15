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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.Log;

import org.adblockplus.sbrowser.contentblocker.util.SubscriptionUtils;

import static org.adblockplus.sbrowser.contentblocker.engine.Notification.NOTIFICATION_DOWNLOAD_INTERVAL;

/**
 * Simple subscription representation.
 */
final class Subscription
{
  private static final String TAG = Subscription.class.getSimpleName();
  public static final String KEY_TITLE = "title";
  public static final String KEY_VERSION = "version";
  public static final String KEY_HTTP_ETAG = "_etag";
  public static final String KEY_HTTP_LAST_MODIFIED = "_last_modified";
  public static final String KEY_UPDATE_TIMESTAMP = "_update_timestamp";
  public static final String KEY_TRIED_UPDATE_TIMESTAMP = "_tried_update_timestamp";
  public static final String KEY_DOWNLOAD_COUNT = "_download_count";
  public static final String KEY_ENABLED = "_enabled";
  public static final String KEY_META_HASH = "_meta_hash";

  private static final long MINIMAL_DOWNLOAD_INTERVAL = DateUtils.MINUTE_IN_MILLIS;
  private static final long DOWNLOAD_RETRY_INTERVAL = DateUtils.HOUR_IN_MILLIS;

  /**
   * List of meta keys that are allowed to import from a downloaded
   * subscription.
   */
  private static final String[] ALLOWED_META_KEYS_ARRAY =
      {
          "checksum", KEY_VERSION, KEY_TITLE, "last modified", "expires", "homepage", "licence"
      };
  private static final HashSet<String> ALLOWED_META_KEYS =
      new HashSet<>(Arrays.asList(ALLOWED_META_KEYS_ARRAY));

  private static final Locale LOCALE_EN = Locale.ENGLISH;

  private final long updateInterval = DateUtils.DAY_IN_MILLIS
      + (long) (DateUtils.HOUR_IN_MILLIS * 8. * Math.random());

  private final URL url;
  private final Type type;
  private final HashMap<String, String> meta = new HashMap<>();
  private final HashSet<String> filters = new HashSet<>();

  private boolean metaDataValid = true;
  private boolean filtersValid = true;

  /**
   * Subscription type.
   *
   * @author René Jeschke &lt;rene@adblockplus.org&gt;
   */
  public enum Type
  {
    /**
     * Initiated from an URL, can be automatically downloaded.
     */
    DOWNLOADABLE,
    /**
     * User defined filters or exceptions.
     */
    USER
  }

  /**
   * Creates a subscription. The type gets determined by {@code url} being
   * {@code null} or not.
   *
   * @param url
   * @see Subscription.Type
   */
  private Subscription(final URL url)
  {
    this.url = url;
    this.type = url != null ? Type.DOWNLOADABLE : Type.USER;
  }

  /**
   * Creates a {@code USER} subscription.
   *
   * @see Subscription.Type
   */
  private Subscription()
  {
    this.url = null;
    this.type = Type.USER;
  }

  public boolean isMetaDataValid()
  {
    return this.metaDataValid;
  }

  public boolean isFiltersValid()
  {
    return this.filtersValid;
  }

  static long parseLong(final String number)
  {
    try
    {
      return Long.parseLong(number);
    }
    catch (final NumberFormatException nfe)
    {
      return 0;
    }
  }

  public long getVersion()
  {
    return parseLong(this.getMeta(KEY_VERSION, "0"));
  }

  public long getDownloadCount()
  {
    return parseLong(this.getMeta(KEY_DOWNLOAD_COUNT, "0"));
  }

  public long getLastUpdateTimestamp()
  {
    return parseLong(this.getMeta(KEY_UPDATE_TIMESTAMP, "0"));
  }

  public long getLastTriedUpdateTimestamp()
  {
    return parseLong(this.getMeta(KEY_TRIED_UPDATE_TIMESTAMP, "0"));
  }

  public boolean shouldUpdate(final boolean forced)
  {
    final long now = System.currentTimeMillis();
    final long lastUpdate = this.getLastUpdateTimestamp();
    final long lastTry = this.getLastTriedUpdateTimestamp();

    if (forced)
    {
      return now - Math.max(lastUpdate, lastTry) > MINIMAL_DOWNLOAD_INTERVAL;
    }

    if (SubscriptionUtils.isNotificationSubscription(this.getId()))
    {
      if (lastTry > lastUpdate)
      {
        return now - lastTry > DOWNLOAD_RETRY_INTERVAL;
      }
      else
      {
        return now - lastUpdate > NOTIFICATION_DOWNLOAD_INTERVAL;
      }
    }

    if (lastTry > lastUpdate)
    {
      return now - lastTry > DOWNLOAD_RETRY_INTERVAL;
    }
    else
    {
      return now - lastUpdate > this.updateInterval;
    }
  }

  /**
   * @return the download URL, null for a {@code USER} subscription.
   */
  public URL getURL()
  {
    return this.url;
  }

  /**
   * @return the type of this subscription
   * @see Subscription.Type
   */
  public Type getType()
  {
    return this.type;
  }

  /**
   * Retrieves a meta data entry
   *
   * @param key
   *          gets converted to all lower case
   * @param defaultValue
   * @return the meta data or {@code defaultValue} if not defined
   */
  public String getMeta(final String key, final String defaultValue)
  {
    final String value = this.meta.get(key.toLowerCase(LOCALE_EN));
    return value != null ? value : defaultValue;
  }

  /**
   * Retrieves a meta data entry
   *
   * @param key
   *          gets converted to all lower case
   * @return the meta data or {@code null} if not defined
   */
  public String getMeta(final String key)
  {
    return this.meta.get(key.toLowerCase(LOCALE_EN));
  }

  public String putMeta(final String key, final String value)
  {
    return this.meta.put(key, value);
  }

  public String getTitle()
  {
    final String title = this.getMeta(KEY_TITLE);
    return title != null ? title : this.url.toString();
  }

  /**
   * Creates a {@code DOWNLOADABLE} subscription.
   *
   * @param url
   *          the update URL
   * @param lines
   * @return the subscription
   * @see Subscription.Type
   */
  public static Subscription create(final URL url, final List<String> lines)
  {
    final Subscription sub = new Subscription(url);
    sub.parseLines(lines);
    return sub;
  }

  /**
   * Creates a {@code DOWNLOADABLE} subscription.
   *
   * @param url
   *          the update URL
   * @return the subscription
   * @see Subscription.Type
   */
  public static Subscription create(final URL url)
  {
    return new Subscription(url);
  }

  public static Subscription create(final String urlString) throws IOException
  {
    try
    {
      return new Subscription(new URL(urlString));
    }
    catch (final IOException e)
    {
      Log.d(TAG, "Creation failed for: '" + urlString + "'");
      throw e;
    }
  }

  /**
   * Creates a {@code USER} subscription.
   *
   * @param title
   * @return the subscription
   * @see Subscription.Type
   */
  public static Subscription createUserSubscription(final String title)
  {
    final Subscription sub = new Subscription();
    sub.meta.put(KEY_TITLE, title);
    return sub;
  }

  public boolean isEnabled()
  {
    return "true".equals(this.getMeta(KEY_ENABLED));
  }

  public void setEnabled(boolean enable)
  {
    this.putMeta(KEY_ENABLED, Boolean.toString(enable));
  }

  public void copyFilters(Collection<String> filters)
  {
    if (filters != null)
    {
      filters.addAll(this.filters);
    }
  }

  public void clearFilters()
  {
    this.filters.clear();
  }

  /**
   * @return an internal management ID
   */
  public String getId()
  {
    return getId(this);
  }

  public static String getId(final Subscription subscription)
  {
    switch (subscription.type)
    {
      case DOWNLOADABLE:
        return "url:" + subscription.url.toString();
      case USER:
        return "user:" + subscription.getMeta(KEY_TITLE);
    }
    return "";
  }

  private static String byteArrayToHexString(final byte[] array)
  {
    final StringBuilder sb = new StringBuilder(array.length * 2);
    for (final byte b : array)
    {
      final int value = b & 255;
      if (value < 16)
      {
        sb.append('0');
      }
      sb.append(Integer.toHexString(value));
    }
    return sb.toString();
  }

  private static String createMetaDataHash(final HashMap<String, String> meta) throws IOException
  {
    final ArrayList<String> keyValues = new ArrayList<>();
    for (final Entry<String, String> e : meta.entrySet())
    {
      if (!KEY_META_HASH.equals(e.getKey()))
      {
        keyValues.add(e.getKey() + ":" + e.getValue());
      }
    }
    return createFilterHash(keyValues);
  }

  private static String createFilterHash(List<String> filters) throws IOException
  {
    try
    {
      final MessageDigest md5 = MessageDigest.getInstance("MD5");
      Collections.sort(filters);
      for (final String filter : filters)
      {
        md5.update(filter.getBytes(StandardCharsets.UTF_8));
      }
      return byteArrayToHexString(md5.digest());
    }
    catch (final NoSuchAlgorithmException e)
    {
      throw new IOException("MD5 is unavailable: " + e.getMessage(), e);
    }
  }

  public void serializeMetaData(final File metaFile) throws IOException
  {
    this.putMeta(KEY_META_HASH, createMetaDataHash(this.meta));
    try (final DataOutputStream metaOut = new DataOutputStream(new BufferedOutputStream(
        new GZIPOutputStream(new FileOutputStream(metaFile)))))
    {
      metaOut.writeUTF(this.url != null ? this.url.toString() : "");
      metaOut.writeInt(this.meta.size());
      for (final Entry<String, String> e : this.meta.entrySet())
      {
        metaOut.writeUTF(e.getKey());
        metaOut.writeUTF(e.getValue());
      }
    }
  }

  public void serializeFilters(final File filtersFile) throws IOException
  {
    try (final DataOutputStream filtersOut = new DataOutputStream(new BufferedOutputStream(
        new GZIPOutputStream(new FileOutputStream(filtersFile)))))
    {
      filtersOut.writeInt(this.filters.size());
      filtersOut.writeUTF(createFilterHash(new ArrayList<>(this.filters)));
      for (final String s : this.filters)
      {
        final byte[] b = s.getBytes(StandardCharsets.UTF_8);
        filtersOut.writeInt(b.length);
        filtersOut.write(b);
      }
    }
  }

  public void serializeSubscription(final File metaFile, final File filtersFile) throws IOException
  {
    this.serializeMetaData(metaFile);
    this.serializeFilters(filtersFile);
  }

  public static Subscription deserializeSubscription(final File metaFile)
  {
    Subscription sub = null;
    try (final DataInputStream in = new DataInputStream(new BufferedInputStream(new GZIPInputStream(
        new FileInputStream(metaFile)))))
    {
      final String urlString = in.readUTF();
      sub = new Subscription(!TextUtils.isEmpty(urlString) ? new URL(urlString) : null);
      sub.metaDataValid = false;
      final int numMetaEntries = in.readInt();
      for (int i = 0; i < numMetaEntries; i++)
      {
        final String key = in.readUTF();
        final String value = in.readUTF();
        sub.meta.put(key, value);
      }
      sub.metaDataValid = createMetaDataHash(sub.meta).equals(sub.getMeta(KEY_META_HASH));
    }
    catch (Throwable t)
    {
      // We catch Throwable here in order to return whatever we could retrieve from the meta file
    }
    return sub;
  }

  public void deserializeFilters(final File filtersFile)
  {
    this.clearFilters();
    this.filtersValid = false;
    try (final DataInputStream in = new DataInputStream(new BufferedInputStream(new GZIPInputStream(
        new FileInputStream(filtersFile)))))
    {
      final int numFilters = in.readInt();
      final String filtersHash = in.readUTF();
      for (int i = 0; i < numFilters; i++)
      {
        final int length = in.readInt();
        final byte[] b = new byte[length];
        in.readFully(b);
        this.filters.add(new String(b, StandardCharsets.UTF_8));
      }
      this.filtersValid = createFilterHash(new ArrayList<>(this.filters)).equals(
          filtersHash);
      Log.d(TAG, "Filters valid: " + this.filtersValid);
    }
    catch (Throwable t)
    {
      // We catch Throwable here in order to load whatever we could retrieve from the filters file
    }
  }

  /**
   * Adds the given string, which should be a single filter to this
   * subscription.
   *
   * @param input
   */
  public Subscription parseLine(String input)
  {
    final String line = input.trim();
    if (!line.isEmpty())
    {
      if (line.startsWith("!"))
      {
        // Meta data
        final int colon = line.indexOf(':');
        if (colon > 2)
        {
          final String key = line.substring(1, colon).trim().toLowerCase(LOCALE_EN);
          final String value = line.substring(colon + 1).trim();
          if (!key.isEmpty() && !value.isEmpty() && ALLOWED_META_KEYS.contains(key))
          {
            this.meta.put(key, value);
          }
        }
      }
      else if (line.startsWith("["))
      {
        // currently ignored
      }
      else
      {
        this.filters.add(line);
      }
    }
    return this;
  }

  public Subscription parseLines(final List<String> lines)
  {
    for (String line : lines)
    {
      this.parseLine(line);
    }
    return this;
  }

  public Subscription parseText(final String string)
  {
    try (final BufferedReader r = new BufferedReader(new StringReader(string)))
    {
      for (String line = r.readLine(); line != null; line = r.readLine())
      {
        this.parseLine(line);
      }
    }
    catch (final IOException e)
    {
      // ignored ... we're reading from a String
    }
    return this;
  }

  boolean updateSubscription(final int responseCode, final String text,
      final Map<String, String> httpHeaders, final File metaFile, final File filtersFile)
      throws IOException
  {
    boolean filtersChanged = false;
    if (responseCode == 304)
    {
      // Not changed, update update timestamp only
      this.meta.put(KEY_UPDATE_TIMESTAMP, Long.toString(System.currentTimeMillis()));
    }
    else
    {
      if (responseCode != 200 || text == null)
      {
        // We tried, but we failed
        this.meta.put(KEY_TRIED_UPDATE_TIMESTAMP, Long.toString(System.currentTimeMillis()));
      }
      else
      {
        if (SubscriptionUtils.isNotificationSubscription(getId()))
        {
          this.meta.put(KEY_UPDATE_TIMESTAMP, Long.toString(System.currentTimeMillis()));
          this.meta.put(KEY_DOWNLOAD_COUNT, Long.toString(this.getDownloadCount() + 1));
          this.meta.put(KEY_VERSION, Notification.getNotificationVersion(text));
          Notification.persistNotificationData(filtersFile, text);
          this.serializeMetaData(metaFile);
          return false;
        }
        // Update succeeded, update filters
        filtersChanged = true;
        this.meta.put(KEY_UPDATE_TIMESTAMP, Long.toString(System.currentTimeMillis()));
        if (httpHeaders != null)
        {
          final String etag = httpHeaders.get("etag");
          final String lastModified = httpHeaders.get("last-modified");

          if (etag != null)
          {
            this.meta.put(KEY_HTTP_ETAG, etag);
          }
          else
          {
            this.meta.remove(KEY_HTTP_ETAG);
          }

          if (lastModified != null)
          {
            this.meta.put(KEY_HTTP_LAST_MODIFIED, lastModified);
          }
          else
          {
            this.meta.remove(KEY_HTTP_LAST_MODIFIED);
          }
          this.meta.put(KEY_DOWNLOAD_COUNT, Long.toString(this.getDownloadCount() + 1));

          this.clearFilters();
          this.parseText(text);
        }
      }
    }

    this.serializeMetaData(metaFile);
    if (filtersChanged)
    {
      this.serializeFilters(filtersFile);
      this.clearFilters();
    }

    return filtersChanged;
  }
}
