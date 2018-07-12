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
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.PersistableBundle;
import android.util.Log;

import javax.net.ssl.HttpsURLConnection;

/**
 * JobService that handles download jobs
 */
public class DownloadJobService extends JobService implements EngineManager.OnEngineCreatedCallback
{
  private static final String TAG = DownloadJobService.class.getSimpleName();
  private DownloadJobAsyncTask downloadJobAsyncTask = null;
  private Engine engine = null;

  @Override
  public void onCreate()
  {
    super.onCreate();
    Log.i(TAG, "DownloadJobService created.");
    EngineManager.getInstance().retrieveEngine(this, this);
  }

  @Override
  public int onStartCommand(final Intent intent, final int flags, final int startId)
  {
    return START_NOT_STICKY;
  }

  @Override
  public boolean onStartJob(final JobParameters params)
  {
    Log.i(TAG, "Job with id " + params.getJobId() + " started.");
    this.downloadJobAsyncTask = new DownloadJobAsyncTask()
    {
      @Override
      protected void onPostExecute(final DownloadJob job)
      {
        Log.i(DownloadJobService.TAG, "Job with id " + params.getJobId() + " finished.");
        jobFinished(params, false);
        if (engine != null && job != null)
        {
          engine.downloadFinished(job.id, job.responseCode, job.responseText, job.headers);
        }
      }
    };
    try
    {
      this.downloadJobAsyncTask.execute(createDownloadJobFromExtras(params.getExtras()));
    }
    catch (final MalformedURLException e)
    {
      Log.e(TAG, "Malformed URL, cannot create download.", e);
      return false;
    }
    return true;
  }

  @Override
  public boolean onStopJob(JobParameters params)
  {
    if (downloadJobAsyncTask != null)
    {
      downloadJobAsyncTask.cancel(true);
    }
    return true;
  }

  @Override
  public void onDestroy()
  {
    EngineManager.getInstance().removeOnEngineCreatedCallback(this);
    Log.i(TAG, "DownloadJobService destroyed.");
    super.onDestroy();
  }

  private DownloadJob createDownloadJobFromExtras(final PersistableBundle extras) throws MalformedURLException
  {
    return new DownloadJob(
        new URL(extras.getString(Notification.KEY_EXTRA_URL)),
        extras.getString(Notification.KEY_EXTRA_ID),
        null);
  }

  @Override
  public void onEngineCreated(final Engine engine)
  {
    this.engine = engine;
  }

  private static class DownloadJobAsyncTask extends AsyncTask<DownloadJob, Void, DownloadJob>
  {
    public static final String TAG = DownloadJobAsyncTask.class.getSimpleName();

    @Override
    protected DownloadJob doInBackground(final DownloadJob... downloadJob)
    {
      final DownloadJob job = downloadJob[0];
      try
      {
        return download(job);
      }
      catch (Exception e)
      {
        Log.e(TAG, "Error at download: ", e);
        return null;
      }
    }

    private DownloadJob download(final DownloadJob job) throws IOException
    {
      final HttpsURLConnection connection = (HttpsURLConnection) job.url.openConnection();
      connection.setRequestMethod("GET");
      for (final Map.Entry<String, String> e : job.headers.entrySet())
      {
        connection.addRequestProperty(e.getKey(), e.getValue());
      }
      connection.connect();
      job.responseCode = connection.getResponseCode();
      job.responseHeaders.clear();
      job.responseText = null;

      for (int i = 1; ; i++)
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
      return job;
    }
  }

  private static class DownloadJob
  {
    private final URL url;
    private final String id;
    private final HashMap<String, String> headers = new HashMap<>();

    private int responseCode;
    private final HashMap<String, String> responseHeaders = new HashMap<>();
    private String responseText;

    DownloadJob(final URL url, final String id, final Map<String, String> headers)
    {
      this.url = url;
      this.id = id;
      if (headers != null)
      {
        this.headers.putAll(headers);
      }
    }
  }
}
