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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.adblockplus.sbrowser.contentblocker.engine.Subscription.Type;

import android.util.Log;

/**
 * This class holds all listed subscriptions and manages the subscription
 * aggregation cache folder.
 */
final class Subscriptions
{
  private static final String TAG = Subscriptions.class.getSimpleName();
  private static final String[] USER_SUBSCRIPTIONS =
  { Engine.USER_FILTERS_TITLE, Engine.USER_EXCEPTIONS_TITLE };
  // Filters that begin with '|$' , '||$' , '@@|$' or '@@||$'
  // See https://issues.adblockplus.org/ticket/4772
  private static final String UNSUPPORTED_FILTERS_REGEX =  "^(\\|\\$|\\|\\|\\$|@@\\|\\$|@@\\|\\|\\$).*";
  private final HashMap<String, Subscription> subscriptions = new HashMap<String, Subscription>();

  private final Engine engine;
  private final File subscriptionFolder;
  private final File cacheFolder;
  private final boolean wasUnitialized;

  private Subscriptions(final Engine engine, final File appFolder, final File cacheFolder)
  {
    this.engine = engine;
    this.subscriptionFolder = appFolder;
    this.wasUnitialized = !this.subscriptionFolder.exists();
    this.cacheFolder = cacheFolder;
  }

  public boolean wasUnitialized()
  {
    return this.wasUnitialized;
  }

  public File createAndWriteFile() throws IOException
  {
    for (;;)
    {
      final File file = new File(this.cacheFolder, String.format("tmp-%d.txt",
          (int) (Math.random() * 1e8)));
      if (!file.exists())
      {
        Log.d(TAG, "Writing filters to " + file);
        this.writeFile(file);
        return file;
      }
    }
  }

  List<SubscriptionInfo> getSubscriptions(final Engine engine)
  {
    final ArrayList<SubscriptionInfo> subs = new ArrayList<SubscriptionInfo>();
    for (final Subscription sub : this.subscriptions.values())
    {
      subs.add(SubscriptionInfo.create(engine, sub));
    }
    return subs;
  }

  void getSubscriptions(final List<Subscription> list)
  {
    list.addAll(this.subscriptions.values());
  }

  public boolean hasSubscription(final String id)
  {
    return this.subscriptions.containsKey(id);
  }

  public boolean isSubscriptionEnabled(final String id)
  {
    final Subscription sub = this.subscriptions.get(id);
    return sub != null && sub.isEnabled();
  }

  public boolean changeSubscriptionState(final String id, final boolean enabled) throws IOException
  {
    final Subscription sub = this.subscriptions.get(id);
    if (sub != null)
    {
      if (enabled != sub.isEnabled())
      {
        sub.setEnabled(enabled);
        sub.serializeMetaData(this.getMetaFile(sub));
        if (enabled)
        {
          this.engine.enqueueDownload(sub, true);
        }

        this.engine.requestUpdateBroadcast();
        return true;
      }
    }
    return false;
  }

  File getFiltersFile(final Subscription sub)
  {
    final File filtersFile;
    if (sub.getType() == Type.USER)
    {
      filtersFile = new File(this.subscriptionFolder, "user_" + sub.getTitle() + ".sub");
    }
    else
    {
      filtersFile = new File(this.subscriptionFolder, "url_"
          + new File(sub.getURL().getPath()).getName() + ".sub");
    }
    return filtersFile;
  }

  File getMetaFile(final Subscription sub)
  {
    return new File(getFiltersFile(sub).getAbsolutePath() + ".meta");
  }

  void persistSubscription(final Subscription sub) throws IOException
  {
    sub.serializeSubscription(this.getMetaFile(sub), this.getFiltersFile(sub));
  }

  void persistSubscriptions() throws IOException
  {
    for (final Subscription sub : this.subscriptions.values())
    {
      this.persistSubscription(sub);
    }
  }

  /**
   * This method combines all currently listed and enabled subscriptions into
   * one text file.
   *
   * @param output
   * @throws IOException
   */
  private void writeFile(final File output) throws IOException
  {
    final HashSet<String> filters = new HashSet<String>();
    for (final Subscription s : this.subscriptions.values())
    {
      if (s.isEnabled())
      {
        Log.d(TAG, "Adding filters for '" + s.getId() + "'");
        s.clearFilters();
        s.deserializeFilters(this.getFiltersFile(s));
        s.getFilters(filters);
        s.clearFilters();
      }
    }

    final BufferedWriter w = new BufferedWriter(
        new OutputStreamWriter(new FileOutputStream(output), "UTF-8"));
    try
    {
      Log.d(TAG, "Writing " + filters.size() + " filters");
      Engine.writeFilterHeaders(w);
      for (final String filter : filters)
      {
        // This is a temporary fix to not write filters that might crash Samsung Internet
        // See https://issues.adblockplus.org/ticket/4772
        if (!filter.matches(UNSUPPORTED_FILTERS_REGEX))
        {
          w.write(filter);
          w.write('\n');
        }
        else
        {
          Log.d(TAG, "Ignoring unsupported filter: " + filter);
        }
      }
    }
    finally
    {
      w.close();
    }
  }

  public Subscription add(final Subscription sub)
  {
    final String id = sub.getId();
    if (!this.subscriptions.containsKey(id))
    {
      this.subscriptions.put(id, sub);
      return sub;
    }
    return this.subscriptions.get(id);
  }

  public static Subscriptions initialize(final Engine engine, final File appFolder,
      final File cacheFolder)
      throws IOException
  {
    final Subscriptions subs = new Subscriptions(engine, appFolder, cacheFolder);

    subs.subscriptionFolder.mkdirs();
    subs.cacheFolder.mkdirs();

    final File[] files = subs.subscriptionFolder.listFiles();
    for (File f : files)
    {
      if (f.getName().endsWith(".sub"))
      {
        final File metaFile = new File(f.getAbsolutePath() + ".meta");
        if (metaFile.exists())
        {
          final Subscription sub = Subscription.deserializeSubscription(metaFile);
          subs.subscriptions.put(sub.getId(), sub);
        }
      }
    }

    subs.createUserSubscriptions();

    return subs;
  }

  /**
   * Adds default user subscriptions if not exist.
   */
  private void createUserSubscriptions()
  {
    for (String title : USER_SUBSCRIPTIONS)
    {
      final Subscription userSub = Subscription.createUserSubscription(title);
      if (!this.subscriptions.containsKey(userSub.getId()))
      {
        this.subscriptions.put(userSub.getId(), userSub);
      }
    }
  }

  public void checkForUpdates() throws IOException
  {
    for (Subscription sub : this.subscriptions.values())
    {
      if (sub.isEnabled())
      {
        this.engine.enqueueDownload(sub, false);
      }
    }
  }

  public void updateSubscription(final String id, final int responseCode, final String text,
      final Map<String, String> httpHeaders)
      throws IOException
  {
    final Subscription sub = this.subscriptions.get(id);
    if (sub != null)
    {
      if (sub.updateSubscription(responseCode, text, httpHeaders, this.getMetaFile(sub),
          this.getFiltersFile(sub)))
      {
        this.engine.requestUpdateBroadcast();
      }
    }
  }
}
