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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import org.adblockplus.adblockplussbrowser.R;
import org.adblockplus.sbrowser.contentblocker.util.ConnectivityUtils;
import org.adblockplus.sbrowser.contentblocker.util.SharedPrefsUtils;
import org.adblockplus.sbrowser.contentblocker.util.SubscriptionUtils;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.PersistableBundle;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.Log;
import android.widget.Toast;

public final class Engine
{
  private static final String TAG = Engine.class.getSimpleName();

  public static final String USER_FILTERS_TITLE = "__filters";
  public static final String USER_EXCEPTIONS_TITLE = "__exceptions";

  public static final String SBROWSER_APP_ID = "com.sec.android.app.sbrowser";
  private static final String ACTION_OPEN_SETTINGS = "com.samsung.android.sbrowser.contentBlocker.ACTION_SETTING";
  private static final String ACTION_UPDATE = "com.samsung.android.sbrowser.contentBlocker.ACTION_UPDATE";

  public static final String SUBSCRIPTIONS_EXCEPTIONSURL = "subscriptions_exceptionsurl";

  // The value below specifies an interval of [x, 2*x[, where x =
  // INITIAL_UPDATE_CHECK_DELAY
  private static final long INITIAL_UPDATE_CHECK_DELAY = 5 * DateUtils.SECOND_IN_MILLIS;
  private static final long UPDATE_CHECK_INTERVAL = 30 * DateUtils.MINUTE_IN_MILLIS;
  private static final long BROADCAST_COMBINATION_DELAY = 2500;

  private static final int NO_FLAG = 0;
  private static final int OLDEST_SAMSUNG_INTERNET_5_VERSIONCODE = 500000000;

  private final ReentrantLock accessLock = new ReentrantLock();
  private DefaultSubscriptions defaultSubscriptions;
  private Subscriptions subscriptions;
  private JSONPrefs jsonPrefs;
  private AppInfo appInfo;
  private final LinkedBlockingQueue<EngineEvent> engineEvents = new LinkedBlockingQueue<>();
  private Thread handlerThread;
  private Downloader downloader;
  private SubscriptionUpdateCallback subscriptionUpdateCallback;
  private final Context context;
  private ComponentName componentName;
  private boolean wasFirstRun = false;
  private long nextUpdateBroadcast = Long.MAX_VALUE;
  private int jobId = 0;

  private Engine(final Context context)
  {
    this.context = context;
    this.componentName = new ComponentName(context, DownloadJobService.class);
  }

  public String getPrefsDefault(final String key)
  {
    return this.jsonPrefs.getDefaults(key);
  }

  DefaultSubscriptionInfo getDefaultSubscriptionInfo(final Subscription sub)
  {
    return this.defaultSubscriptions.getForUrl(sub.getURL());
  }

  void lock()
  {
    this.accessLock.lock();
  }

  void unlock()
  {
    this.accessLock.unlock();
  }

  public static boolean openSBrowserSettings(final Context context)
  {
    final Intent intent = new Intent(ACTION_OPEN_SETTINGS);
    final List<ResolveInfo> list = context.getPackageManager()
        .queryIntentActivities(intent, 0);
    if (list.size() > 0)
    {
      context.startActivity(intent);
    }
    return list.size() > 0;
  }

  public static boolean hasCompatibleSBrowserInstalled(final Context context)
  {
    try
    {
      return context.getPackageManager()
          .queryIntentActivities(new Intent(ACTION_OPEN_SETTINGS), 0).size() > 0;
    }
    catch (final Throwable t)
    {
      return false;
    }
  }

  /**
   * Starting with Samsung Internet 5.0, the way to enable ad blocking has changed. As a result, we
   * need to check for the version of Samsung Internet and apply text changes to the first run slide.
   *
   * @param context
   * @return a boolean that indicates, if the user has Samsung Internet version 5.x
   */
  public static boolean hasSamsungInternetVersion5OrNewer(final Context context)
  {
    try
    {
      PackageInfo packageInfo = context.getPackageManager().getPackageInfo(SBROWSER_APP_ID, NO_FLAG);
      return packageInfo.versionCode >= OLDEST_SAMSUNG_INTERNET_5_VERSIONCODE;
    }
    catch (PackageManager.NameNotFoundException e)
    {
      // Should never happen, as checkAAStatusAndProceed() should not be called if the user
      // has no compatible SBrowser installed. Nevertheless we have to handle the Exception.
      Log.d(TAG, "No compatible Samsung Browser found.", e);
      return false;
    }
  }

  public void setSubscriptionUpdateCallback(final SubscriptionUpdateCallback subscriptionUpdateCallback)
  {
    this.subscriptionUpdateCallback = subscriptionUpdateCallback;
  }

  public void requestUpdateBroadcast()
  {
    this.nextUpdateBroadcast = System.currentTimeMillis() + BROADCAST_COMBINATION_DELAY;
  }

  private void writeFileAndSendUpdateBroadcast()
  {
    createAndWriteFile();

    runOnUiThread(new Runnable()
    {
      @Override
      public void run()
      {
        final Intent intent = new Intent();
        intent.setAction(ACTION_UPDATE);
        intent.setData(Uri.parse("package:" + Engine.this.context.getPackageName()));
        Engine.this.context.sendBroadcast(intent);
      }
    });
  }

  boolean canUseInternet(final boolean allowMetered)
  {
    // allow a metered connection to update default subscriptions at the first run.
    // See https://issues.adblockplus.org/ticket/5237
    return ConnectivityUtils.canUseInternet(context, allowMetered || wasFirstRun());
  }

  public void forceUpdateSubscriptions(final boolean allowMetered)
  {
    try
    {
      subscriptions.checkForUpdates(true, allowMetered);
      Toast.makeText(context, context.getText(R.string.updating_subscriptions), Toast.LENGTH_LONG).show();
    }
    catch (IOException e)
    {
      Log.e(TAG, "Failed checking for updates", e);
    }
  }

  public List<SubscriptionInfo> getListedSubscriptions()
  {
    return this.subscriptions.getSubscriptions(this);
  }

  public void changeSubscriptionState(final String id, final boolean enabled)
  {
    if (this.subscriptionUpdateCallback != null)
    {
      subscriptionUpdateCallback.subscriptionUpdateRequested(enabled);
    }
    this.engineEvents.add(new ChangeEnabledStateEvent(id, enabled));
  }

  public void subscriptionStateChanged()
  {
    if (this.subscriptionUpdateCallback != null)
    {
      subscriptionUpdateCallback.subscriptionUpdatedApplied();
    }
  }

  public void createAndAddSubscriptionFromUrl(final String url,
      final SubscriptionAddedCallback callback) throws IOException
  {
    final Subscription sub = Subscription.create(url);
    sub.putMeta(Subscription.KEY_TITLE, url);
    sub.setEnabled(true);
    subscriptions.add(sub);
    subscriptions.persistSubscription(sub);
    callback.subscriptionAdded();
  }

  public void removeSubscriptionById(final String subscriptionId)
  {
    subscriptions.remove(subscriptionId);
  }

  void downloadFinished(final String id, final int responseCode, final String response,
      final Map<String, String> headers)
  {
    this.engineEvents.add(new DownloadFinishedEvent(id, responseCode, response, headers));
  }

  private void createAndWriteFile()
  {
    this.lock();
    try
    {
      Log.d(TAG, "Writing filters...");
      final File filterFile = this.subscriptions.createAndWriteFile();
      writeWhitelistedWebsites(this.context, filterFile);

      SharedPrefsUtils.putString(
          this.context, R.string.key_cached_filter_path, filterFile.getAbsolutePath());

      Log.d(TAG, "Cleaning up cache...");
      final File dummyFile = getDummyFilterFile(this.context);
      final File[] cacheDirFiles = getFilterCacheDir(this.context).listFiles();
      if (cacheDirFiles != null)
      {
        for (final File file : cacheDirFiles)
        {
          if (!file.equals(dummyFile) && !file.equals(filterFile))
          {
            Log.d(TAG, "Deleting file:" + file);
            file.delete();
          }
        }
      }
    }
    catch (IOException e)
    {
      Log.e(TAG, "Failed to write filters", e);
    }
    finally
    {
      this.unlock();
    }
  }

  public static void runOnUiThread(final Runnable runnable)
  {
    new Handler(Looper.getMainLooper()).post(runnable);
  }

  public boolean isAcceptableAdsEnabled()
  {
    this.lock();
    try
    {
      return this.subscriptions.isSubscriptionEnabled("url:"
          + this.getPrefsDefault(SUBSCRIPTIONS_EXCEPTIONSURL));
    }
    finally
    {
      this.unlock();
    }
  }

  public DefaultSubscriptionInfo getDefaultSubscriptionInfoForUrl(final String url)
  {
    return this.defaultSubscriptions.getForUrl(url);
  }

  /**
   * If the user starts the app for the first time, we force to update the subscription which was
   * selected as the default, no matter if he has a WIFI connection or not. From the second start
   * we only update when the user has a WIFI connection.
   *
   * @return a boolean that indicated if this is the first start of the app
   */
  private boolean wasFirstRun()
  {
    if (wasFirstRun)
    {
      this.wasFirstRun = false;
      return true;
    }
    else
    {
      return false;
    }
  }

  private void migrateFromPreviousVersion(final Context context)
  {
    try
    {
      final int versionCode = context.getPackageManager().getPackageInfo(context.getPackageName(),
          0).versionCode;

      final int previousVersionCode = SharedPrefsUtils.getInt(
          context, R.string.key_previous_version_code, 0);

      if (versionCode > previousVersionCode)
      {
        if (previousVersionCode > 0)
        {
          migrateDefaultSubscriptions();
        }
        SharedPrefsUtils.putInt(context, R.string.key_previous_version_code, versionCode);
      }
    }
    catch (final Throwable t)
    {
      Log.e(TAG, "Failed on migration, please clear all application data", t);
    }
  }

  private void migrateDefaultSubscriptions() throws IOException
  {
      try (final InputStream subscriptionsXml = context.getResources()
              .openRawResource(R.raw.subscriptions))
      {
          defaultSubscriptions = DefaultSubscriptions.fromStream(subscriptionsXml);
      }
      final List<Subscription> latestSubscriptionsList = createSubscriptions(defaultSubscriptions);

      for (final Subscription sub : latestSubscriptionsList)
      {
          if (!subscriptions.hasSubscription(sub.getId()))
          {
              subscriptions.add(sub);
              subscriptions.persistSubscription(sub);
              Log.d(TAG, "Added subscription: " + sub.getURL());
          }
      }
  }

  static Engine create(final Context context) throws IOException
  {
    final Engine engine = new Engine(context);
    engine.appInfo = AppInfo.create(context);
    Log.d(TAG, "Creating engine, appInfo=" + engine.appInfo.toString());

    try (final InputStream subscriptionsXml = context.getResources()
        .openRawResource(R.raw.subscriptions))
    {
      engine.defaultSubscriptions = DefaultSubscriptions.fromStream(subscriptionsXml);
    }
    Log.d(TAG, "Finished reading 'subscriptions.xml'");

    engine.subscriptions = Subscriptions.initialize(engine, getSubscriptionsDir(context),
        getFilterCacheDir(context));

    try (final InputStream prefsJson = context.getResources().openRawResource(R.raw.prefs))
    {
      engine.jsonPrefs = JSONPrefs.create(prefsJson);
    }

    Log.d(TAG, "Finished reading JSON preferences");

    // Check if this is a fresh start, if so: initialize bundled easylist.
    engine.wasFirstRun = engine.subscriptions.wasUnitialized();
    if (engine.subscriptions.wasUnitialized())
    {
      Log.d(TAG, "Subscription storage was uninitialized, initializing...");

      try (final InputStream easylistTxt = context.getResources().openRawResource(R.raw.easylist))
      {
        final Subscription easylist = engine.subscriptions.add(Subscription
            // Use bundled EasyList as default and update it with locale specific list later
            // see: https://issues.adblockplus.org/ticket/5237
            .create(SubscriptionUtils.chooseDefaultSubscriptionUrl(
                engine.defaultSubscriptions.getSubscriptions()))
            .parseLines(readLines(easylistTxt)));
        easylist.putMeta(Subscription.KEY_UPDATE_TIMESTAMP, "0");
        easylist.setEnabled(true);
      }
      Log.d(TAG, "Added and enabled bundled easylist");

      try (final InputStream exceptionsTxt = context.getResources()
          .openRawResource(R.raw.exceptionrules))
      {
        final Subscription exceptions = engine.subscriptions.add(Subscription
            .create(engine.getPrefsDefault(SUBSCRIPTIONS_EXCEPTIONSURL))
            .parseLines(readLines(exceptionsTxt)));
        exceptions.putMeta(Subscription.KEY_UPDATE_TIMESTAMP, "0");
        exceptions.setEnabled(true);
      }
      Log.d(TAG, "Added and enabled bundled exceptionslist");

      // The Notification should be download regularly.
      // See https://issues.adblockplus.org/ticket/6238
      final Subscription notification = engine.subscriptions.add(Subscription
          .create(Notification.NOTIFICATION_URL));
      notification.setEnabled(true);

      int additional = 0;
      for (final Subscription sub : createSubscriptions(engine.defaultSubscriptions))
      {
        if (!engine.subscriptions.hasSubscription(sub.getId()))
        {
          additional++;
          engine.subscriptions.add(sub);
        }
      }

      Log.d(TAG, "Added " + additional + " additional default/built-in subscriptions");
      engine.subscriptions.persistSubscriptions();
    }

    // Migration data from previous version (if needed)
    engine.migrateFromPreviousVersion(context);
    Log.d(TAG, "Migration done");

    engine.handlerThread = new Thread(new EventHandler(engine));
    engine.handlerThread.setDaemon(true);
    engine.handlerThread.start();

    engine.downloader = Downloader.create(engine);

    final File cachedFilterFile = getCachedFilterFile(context);
    if (cachedFilterFile == null || !cachedFilterFile.exists())
    {
      engine.writeFileAndSendUpdateBroadcast();
    }

    return engine;
  }

  public static String readFileAsString(InputStream instream) throws IOException
  {
    final StringBuilder sb = new StringBuilder();
    try (final BufferedReader r = new BufferedReader(new InputStreamReader(
        instream, StandardCharsets.UTF_8)))
    {
      for (int ch = r.read(); ch != -1; ch = r.read())
      {
        sb.append((char) ch);
      }
    }
    return sb.toString();
  }

  public static List<String> readLines(InputStream instream) throws IOException
  {
    final ArrayList<String> list = new ArrayList<>();
    try (final BufferedReader r = new BufferedReader(new InputStreamReader(
        instream, StandardCharsets.UTF_8)))
    {
      for (String line = r.readLine(); line != null; line = r.readLine())
      {
        list.add(line);
      }
    }
    return list;
  }

  public static File getOrCreateCachedFilterFile(Context context) throws IOException
  {
    final File cachedFilterFile = getCachedFilterFile(context);
    if (cachedFilterFile != null && cachedFilterFile.exists())
    {
      Log.d(TAG, "Cached filter file found: " + cachedFilterFile);
      return cachedFilterFile;
    }

    Log.d(TAG, "Cached filter file not found. Using dummy filter file");
    final File dummyFilterFile = getDummyFilterFile(context);
    if (!dummyFilterFile.exists())
    {
      Log.d(TAG, "Creating dummy filter file...");
      dummyFilterFile.getParentFile().mkdirs();
      try (final BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
          new FileOutputStream(dummyFilterFile), StandardCharsets.UTF_8)))
      {
        writeFilterHeaders(writer);
      }
    }
    return dummyFilterFile;
  }

  public static void writeFilterHeaders(Writer writer) throws IOException
  {
    writer.write("[Adblock Plus 2.0]\n");
    writer.write("! This file was automatically created.\n");
  }

  private static void writeWhitelistedWebsites(Context context, File filterFile) throws IOException
  {
    Log.d(TAG, "Writing whitelisted websites...");
    final Set<String> whitelistedWebsites = new TreeSet<>();
    whitelistedWebsites.addAll(SharedPrefsUtils.getStringSet(
        context, R.string.key_whitelisted_websites, Collections.<String>emptySet()));

    try (final BufferedWriter w = new BufferedWriter( new OutputStreamWriter(
        new FileOutputStream(filterFile, true), StandardCharsets.UTF_8)))
    {
      for (final String url : whitelistedWebsites)
      {
        try
        {
          final URI uri = new URI(url);
          final String host = uri.getHost() != null ? uri.getHost() : uri.getPath();
          w.write("@@||" + host + "^$document");
          w.write('\n');
        }
        catch (URISyntaxException e)
        {
          Log.w(TAG, "Failed to parse whitelisted website: " + url);
        }
      }
    }
  }

  private static File getCachedFilterFile(Context context)
  {
    final String cachedFilterPath = SharedPrefsUtils.getString(
        context, R.string.key_cached_filter_path, null);

    if (cachedFilterPath != null)
    {
      return new File(cachedFilterPath);
    }

    return null;
  }

  private static File getDummyFilterFile(Context context)
  {
    return new File(getFilterCacheDir(context), "dummy.txt");
  }

  private static File getFilterCacheDir(Context context)
  {
    return new File(context.getCacheDir(), "subscriptions");
  }

  private static File getSubscriptionsDir(Context context)
  {
    return new File(context.getFilesDir(), "subscriptions");
  }

  private static List<Subscription> createSubscriptions(
          final DefaultSubscriptions defaultSubscriptions) throws IOException
  {
    final ArrayList<Subscription> subs = new ArrayList<>();
    for (final DefaultSubscriptionInfo info : defaultSubscriptions.getSubscriptions())
    {
      if (!info.getUrl().isEmpty())
      {
        final Subscription sub = Subscription.create(info.getUrl());
        sub.putMeta(Subscription.KEY_TITLE, info.getTitle());
        subs.add(sub);
      }
    }
    return subs;
  }

  URL createDownloadURL(final Subscription sub) throws IOException
  {
    final StringBuilder sb = new StringBuilder();

    sb.append(sub.getURL());
    if (sub.getURL().getQuery() != null)
    {
      sb.append('&');
    }
    else
    {
      sb.append('?');
    }

    sb.append("addonName=");
    sb.append(URLEncoder.encode(this.appInfo.addonName, StandardCharsets.UTF_8.name()));
    sb.append("&addonVersion=");
    sb.append(URLEncoder.encode(this.appInfo.addonVersion, StandardCharsets.UTF_8.name()));
    sb.append("&application=");
    sb.append(URLEncoder.encode(this.appInfo.application, StandardCharsets.UTF_8.name()));
    sb.append("&applicationVersion=");
    sb.append(URLEncoder.encode(this.appInfo.applicationVersion, StandardCharsets.UTF_8.name()));
    sb.append("&platform=");
    sb.append(URLEncoder.encode(this.appInfo.platform, StandardCharsets.UTF_8.name()));
    sb.append("&platformVersion=");
    sb.append(URLEncoder.encode(this.appInfo.platformVersion, StandardCharsets.UTF_8.name()));
    sb.append("&lastVersion=");
    sb.append(sub.getVersion());
    sb.append("&downloadCount=");
    final long downloadCount = sub.getDownloadCount();
    if (downloadCount < 5)
    {
      sb.append(downloadCount);
    }
    else
    {
      sb.append("4%2B"); // "4+" URL encoded
    }

    return new URL(sb.toString());
  }

  public boolean isAcceptableAdsUrl(final SubscriptionInfo subscriptionInfo)
  {
    return getPrefsDefault(SUBSCRIPTIONS_EXCEPTIONSURL).equals(subscriptionInfo.getUrl());
  }

  private static class EventHandler implements Runnable
  {
    private static final String TAG = EventHandler.class.getSimpleName();
    private final Engine engine;

    public EventHandler(final Engine engine)
    {
      this.engine = engine;
    }

    @Override
    public void run()
    {
      Log.d(TAG, "Handler thread started");
      boolean interrupted = false;
      long nextUpdateCheck = System.currentTimeMillis()
          + (long) ((1 + Math.random()) * INITIAL_UPDATE_CHECK_DELAY);
      while (!interrupted)
      {
        try
        {
          final EngineEvent event = this.engine.engineEvents.poll(100, TimeUnit.MILLISECONDS);
          engine.lock();
          try
          {
            if (event != null)
            {
              switch (event.getType())
              {
                case CHANGE_ENABLED_STATE:
                {
                  final ChangeEnabledStateEvent cese = (ChangeEnabledStateEvent) event;
                  Log.d(TAG, "Changing " + cese.id + " to enabled: " + cese.enabled);
                  engine.subscriptions.changeSubscriptionState(cese.id, cese.enabled);
                  break;
                }
                case DOWNLOAD_FINISHED:
                {
                  final DownloadFinishedEvent dfe = (DownloadFinishedEvent) event;
                  Log.d(TAG, "Download finished for '" + dfe.id + "' with response code "
                      + dfe.responseCode);
                  this.engine.subscriptions.updateSubscription(dfe.id, dfe.responseCode,
                        dfe.response, dfe.headers);
                  break;
                }
                default:
                  Log.d(TAG, "Unhandled type: " + event.getType());
                  break;
              }
            }

            final long currentTime = System.currentTimeMillis();
            if (currentTime > nextUpdateCheck)
            {
              nextUpdateCheck = currentTime + UPDATE_CHECK_INTERVAL;

              this.engine.subscriptions.checkForUpdates(false, false);
            }

            if (currentTime > this.engine.nextUpdateBroadcast)
            {
              this.engine.nextUpdateBroadcast = Long.MAX_VALUE;
              Log.d(TAG, "Sending update broadcast");
              this.engine.writeFileAndSendUpdateBroadcast();
            }
          }
          finally
          {
            engine.unlock();
          }
        }
        catch (final InterruptedException e)
        {
          Log.d(TAG, "Handler interrupted", e);
          interrupted = true;
        }
        catch (final Throwable t)
        {
          Log.e(TAG, "Event processing failed: " + t.getMessage(), t);
        }
      }
      Log.d(TAG, "Handler thread finished");
    }
  }

  private static class EngineEvent
  {
    public enum EngineEventType
    {
      CHANGE_ENABLED_STATE,
      FORCE_DOWNLOAD,
      DOWNLOAD_FINISHED
    }

    private final EngineEventType type;

    EngineEvent(final EngineEventType type)
    {
      this.type = type;
    }

    public EngineEventType getType()
    {
      return this.type;
    }
  }

  private static class ChangeEnabledStateEvent extends EngineEvent
  {
    private final String id;
    private final boolean enabled;

    public ChangeEnabledStateEvent(final String id, final boolean enabled)
    {
      super(EngineEvent.EngineEventType.CHANGE_ENABLED_STATE);
      this.id = id;
      this.enabled = enabled;
    }
  }

  private static class DownloadFinishedEvent extends EngineEvent
  {
    private final String id;
    private final int responseCode;
    private final String response;
    private final HashMap<String, String> headers = new HashMap<>();

    public DownloadFinishedEvent(final String id,
        final int responseCode,
        final String response,
        final Map<String, String> headers)
    {
      super(EngineEvent.EngineEventType.DOWNLOAD_FINISHED);
      this.id = id;
      this.responseCode = responseCode;
      this.response = response;
      if (headers != null)
      {
        this.headers.putAll(headers);
      }
    }
  }

  public void enqueueDownload(final Subscription sub, final boolean forced,
      final boolean allowMetered) throws IOException
  {

    if (sub.getURL() != null && sub.shouldUpdate(forced))
    {
      // For now we want to use JobScheduler only for the Notification download.
      // See https://issues.adblockplus.org/ticket/6238.
      if (SubscriptionUtils.isNotificationSubscription(sub.getId()))
      {
        scheduleJob(this.createDownloadURL(sub), sub.getId(), allowMetered);
        return;
      }

      final HashMap<String, String> headers = new HashMap<>();
      if (sub.isMetaDataValid() && sub.isFiltersValid())
      {
        final String lastModified = sub.getMeta(Subscription.KEY_HTTP_LAST_MODIFIED);
        if (!TextUtils.isEmpty(lastModified))
        {
          headers.put("If-Modified-Since", lastModified);
        }
        final String etag = sub.getMeta(Subscription.KEY_HTTP_ETAG);
        if (!TextUtils.isEmpty(etag))
        {
          headers.put("If-None-Match", etag);
        }
      }
      this.downloader.enqueueDownload(this.createDownloadURL(sub), sub.getId(), headers, allowMetered);
    }
  }

  private void scheduleJob(final URL url, final String id, final boolean allowMetered)
  {
    final JobInfo.Builder builder = new JobInfo.Builder(jobId++, componentName);
    builder.setRequiredNetworkType(allowMetered
            ? JobInfo.NETWORK_TYPE_UNMETERED
            : JobInfo.NETWORK_TYPE_ANY);

    final PersistableBundle extras = new PersistableBundle();
    extras.putString(Notification.KEY_EXTRA_ID, id);
    extras.putString(Notification.KEY_EXTRA_URL, url.toString());
    builder.setExtras(extras);

    final JobScheduler scheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
    if (scheduler != null)
    {
      scheduler.schedule(builder.build());
    }
  }

  public void connectivityChanged()
  {
    this.downloader.connectivityChanged();
  }

  public interface SubscriptionUpdateCallback
  {
    void subscriptionUpdateRequested(boolean enabled);
    void subscriptionUpdatedApplied();
  }

  public interface SubscriptionAddedCallback
  {
    void subscriptionAdded();
  }
}
