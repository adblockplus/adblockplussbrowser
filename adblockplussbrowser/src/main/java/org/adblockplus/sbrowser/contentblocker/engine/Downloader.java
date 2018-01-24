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
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import android.annotation.SuppressLint;
import android.util.Log;

@SuppressLint("DefaultLocale")
final class Downloader
{
  private static final int MAX_RETRIES = 5;

  private static final String TAG = Downloader.class.getSimpleName();
  private final Engine engine;
  private final ReentrantLock accessLock = new ReentrantLock();
  private Thread downloaderThread;
  private final LinkedBlockingQueue<DownloadJob> downloadJobs = new LinkedBlockingQueue<>();
  private final HashSet<String> enqueuedIds = new HashSet<>();
  private boolean downloaderEnabled = true;

  private Downloader(final Engine engine)
  {
    this.engine = engine;
  }

  void lock()
  {
    this.accessLock.lock();
  }

  void unlock()
  {
    this.accessLock.unlock();
  }

  void connectivityChanged()
  {
    this.lock();
    if (!this.downloaderEnabled)
    {
      Log.d(TAG, "Re-checking download permission");
    }
    this.downloaderEnabled = true;
    this.unlock();
  }

  static void download(final DownloadJob job) throws IOException
  {
    final HttpURLConnection connection = (HttpURLConnection) job.url.openConnection();
    connection.setRequestMethod("GET");
    for (final Entry<String, String> e : job.headers.entrySet())
    {
      connection.addRequestProperty(e.getKey(), e.getValue());
    }
    connection.connect();

    job.responseCode = connection.getResponseCode();
    job.responseHeaders.clear();
    job.responseText = null;

    for (int i = 1;; i++)
    {
      final String key = connection.getHeaderFieldKey(i);
      if (key == null)
      {
        break;
      }
      final String value = connection.getHeaderField(i);
      job.responseHeaders.put(key.toLowerCase(), value);
    }

    final StringBuilder sb = new StringBuilder();
    try (final BufferedReader r = new BufferedReader(new InputStreamReader(
        connection.getInputStream(), StandardCharsets.UTF_8)))
    {
      for (int ch = r.read(); ch != -1; ch = r.read())
      {
        sb.append((char) ch);
      }
      job.responseText = sb.toString();
    }
  }

  public void enqueueDownload(final URL url, final String id, final Map<String, String> headers,
      final boolean allowMetered)
  {
    this.lock();
    try
    {
      if (!this.enqueuedIds.contains(id))
      {
        this.enqueuedIds.add(id);
        this.downloadJobs.add(new DownloadJob(url, id, headers, allowMetered));
      }
    }
    finally
    {
      this.unlock();
    }
  }

  public static Downloader create(final Engine engine)
  {
    final Downloader downloader = new Downloader(engine);

    downloader.downloaderThread = new Thread(new DownloaderHandler(downloader));
    downloader.downloaderThread.setDaemon(true);
    downloader.downloaderThread.start();

    return downloader;
  }

  private static class DownloaderHandler implements Runnable
  {
    private static final String TAG = DownloaderHandler.class.getSimpleName();

    private final Downloader downloader;

    public DownloaderHandler(final Downloader downloader)
    {
      this.downloader = downloader;
    }

    @Override
    public void run()
    {
      Log.d(TAG, "Handler thread started");
      final LinkedBlockingQueue<DownloadJob> reQueue = new LinkedBlockingQueue<>();
      boolean interrupted = false;
      while (!interrupted)
      {
        DownloadJob job = null;
        try
        {
          if (!this.downloader.downloaderEnabled)
          {
            Thread.sleep(30000);
            continue;
          }
          job = this.downloader.downloadJobs.poll(5 * 60, TimeUnit.SECONDS);
          if (job != null)
          {
            if (this.downloader.engine.canUseInternet(job.allowMetered))
            {
              Log.d(TAG, "Downloading '" + job.id + "' using " + job.url);
              download(job);
              Log.d(TAG, "Downloading '" + job.id + "' finished with response code "
                  + job.responseCode);
              this.downloader.lock();
              try
              {
                this.downloader.enqueuedIds.remove(job.id);
              }
              finally
              {
                this.downloader.unlock();
              }

              this.downloader.engine.downloadFinished(job.id, job.responseCode, job.responseText,
                  job.responseHeaders);

              // Check for retries
              if (!reQueue.isEmpty())
              {
                this.downloader.downloadJobs.add(reQueue.poll());
              }
            }
            else
            {
              // we just keep jobs in queue
              Log.d(TAG, "Updates disabled, re-queuing and disabling downloader");
              this.downloader.downloadJobs.add(job);
              this.downloader.lock();
              this.downloader.downloaderEnabled = false;
              this.downloader.unlock();
            }
          }
        }
        catch (final InterruptedException e)
        {
          Log.d(TAG, "Handler interrupted", e);
          interrupted = true;
        }
        catch (final Throwable t)
        {
          Log.e(TAG, "Downloading failed: " + t.getMessage(), t);
          if (job != null)
          {
            if (job.retryCount++ < MAX_RETRIES)
            {
              reQueue.add(job);
            }
            else
            {
              this.downloader.lock();
              try
              {
                this.downloader.enqueuedIds.remove(job.id);
              }
              finally
              {
                this.downloader.unlock();
              }
              this.downloader.engine.downloadFinished(job.id, -1, null, null);
            }
          }
        }
      }
      Log.d(TAG, "Handler thread finished");
    }
  }

  private static class DownloadJob
  {
    private final URL url;
    private final String id;
    private final boolean allowMetered;
    private final HashMap<String, String> headers = new HashMap<>();
    private int retryCount = 0;

    private int responseCode = 0;
    private final HashMap<String, String> responseHeaders = new HashMap<>();
    private String responseText = null;

    public DownloadJob(final URL url, final String id, final Map<String, String> headers, boolean allowMetered)
    {
      this.url = url;
      this.id = id;
      this.allowMetered = allowMetered;
      if (headers != null)
      {
        this.headers.putAll(headers);
      }
    }
  }
}
