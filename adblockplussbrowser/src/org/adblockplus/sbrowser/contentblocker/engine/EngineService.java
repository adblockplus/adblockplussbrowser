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

import java.util.concurrent.LinkedBlockingQueue;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public final class EngineService extends Service
{
  private static final String TAG = EngineService.class.getSimpleName();

  private volatile Engine engine = null;
  private volatile boolean isInitialized = false;
  private Throwable failureCause = null;
  private static final LinkedBlockingQueue<EngineCreatedCallbackWrapper> ON_CREATED_CALLBACKS =
      new LinkedBlockingQueue<>();

  @Override
  public int onStartCommand(Intent intent, int flags, int startId)
  {
    return Service.START_STICKY;
  }

  /**
   * The callback gets executed on the UI thread.
   *
   * @param context
   * @param callback
   */
  public static void startService(final Context context, final OnEngineCreatedCallback callback)
  {
    startService(context, callback, true);
  }

  /**
   *
   * @param context
   * @param callback
   * @param runOnUiThread
   *          {@code true} if the callback should be executed on the UI thread
   */
  public static void startService(final Context context, final OnEngineCreatedCallback callback,
      final boolean runOnUiThread)
  {
    context.startService(new Intent(context, EngineService.class));
    ON_CREATED_CALLBACKS.offer(new EngineCreatedCallbackWrapper(callback, runOnUiThread));
  }

  @Override
  public void onCreate()
  {
    super.onCreate();
    startDaemonThread(new Initializer(this));
    startDaemonThread(new CreationNotifier(this));
  }

  @Override
  public IBinder onBind(Intent intent)
  {
    return null;
  }

  private static void startDaemonThread(final Runnable runnable)
  {
    final Thread t = new Thread(runnable);
    t.setDaemon(true);
    t.start();
  }

  private static class Initializer implements Runnable
  {
    private final EngineService service;

    public Initializer(final EngineService service)
    {
      this.service = service;
    }

    @Override
    public void run()
    {
      try
      {
        this.service.engine = Engine.create(this.service.getApplicationContext());
      }
      catch (Throwable t)
      {
        Log.e(TAG, "Initialization failed: " + t.getMessage(), t);
        this.service.failureCause = t;
      }
      finally
      {
        this.service.isInitialized = true;
      }
    }
  }

  private static class CreationNotifier implements Runnable
  {
    private static final String TAG = CreationNotifier.class.getSimpleName();
    private final EngineService service;

    public CreationNotifier(final EngineService service)
    {
      this.service = service;
    }

    @Override
    public void run()
    {
      try
      {
        while (!this.service.isInitialized)
        {
          Thread.sleep(250);
        }

        for (;;)
        {
          final EngineCreatedCallbackWrapper wrapper = EngineService.ON_CREATED_CALLBACKS.take();
          if (wrapper != null)
          {
            if (wrapper.runOnUiThread)
            {
              Engine.runOnUiThread(new Runnable()
              {
                private final EngineService service = CreationNotifier.this.service;

                @Override
                public void run()
                {
                  wrapper.callback.onEngineCreated(this.service.engine,
                      this.service.failureCause == null);
                }
              });
            }
            else
            {
              wrapper.callback.onEngineCreated(this.service.engine,
                  this.service.failureCause == null);
            }
          }
        }
      }
      catch (final Throwable t)
      {
        Log.e(TAG, "Notifier died: " + t.getMessage(), t);
      }
    }
  }

  public interface OnEngineCreatedCallback
  {
    void onEngineCreated(Engine engine, boolean success);
  }

  private static class EngineCreatedCallbackWrapper
  {
    final OnEngineCreatedCallback callback;
    final boolean runOnUiThread;

    public EngineCreatedCallbackWrapper(final OnEngineCreatedCallback callback,
        final boolean runOnUiThread)
    {
      this.callback = callback;
      this.runOnUiThread = runOnUiThread;
    }
  }
}
