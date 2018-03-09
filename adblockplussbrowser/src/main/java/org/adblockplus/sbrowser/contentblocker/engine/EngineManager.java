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

import android.content.Context;
import android.os.AsyncTask;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class EngineManager
{
  private static final EngineManager INSTANCE = new EngineManager();

  private Engine engine;
  private CreateEngineAsyncTask createEngineTask;
  private final List<WeakReference<OnEngineCreatedCallback>> engineCreatedCallbacks = new ArrayList<>();

  private EngineManager()
  {
  }

  public static EngineManager getInstance()
  {
    return INSTANCE;
  }

  public void retrieveEngine(final Context context, final OnEngineCreatedCallback callback)
  {
    synchronized (engineCreatedCallbacks)
    {
      if (callback != null)
      {
        engineCreatedCallbacks.add(new WeakReference<>(callback));
      }
    }
    if (engine != null)
    {
      notifyEngineCreated();
    }
    else if (createEngineTask == null || createEngineTask.isCancelled())
    {
      this.createEngineTask = new CreateEngineAsyncTask();
      this.createEngineTask.execute(context);
    }
  }

  public void removeOnEngineCreatedCallback(final OnEngineCreatedCallback callback)
  {
    if (callback != null)
    {
      synchronized (engineCreatedCallbacks)
      {
        final Iterator<WeakReference<OnEngineCreatedCallback>> iterator = engineCreatedCallbacks.iterator();
        while (iterator.hasNext())
        {
          final OnEngineCreatedCallback cb = iterator.next().get();
          if (callback.equals(cb))
          {
            iterator.remove();
          }
        }
      }
    }
  }

  private void setEngineAndNotify(final Engine engine)
  {
    this.createEngineTask = null;
    this.engine = engine;
    notifyEngineCreated();
  }

  private void notifyEngineCreated()
  {
    synchronized (engineCreatedCallbacks)
    {
      final Iterator<WeakReference<OnEngineCreatedCallback>> iterator = engineCreatedCallbacks.iterator();
      while (iterator.hasNext())
      {
        final OnEngineCreatedCallback callback = iterator.next().get();
        if (callback != null)
        {
          Engine.runOnUiThread(new Runnable()
          {
            @Override
            public void run()
            {
              callback.onEngineCreated(engine);
            }
          });
        }
        iterator.remove();
      }
    }
  }

  public interface OnEngineCreatedCallback
  {
    void onEngineCreated(Engine engine);
  }

  private static final class CreateEngineAsyncTask extends AsyncTask<Context, Void, Engine>
  {
    @Override
    protected Engine doInBackground(final Context... context)
    {
      try
      {
        return Engine.create(context[0].getApplicationContext());
      }
      catch (IOException e)
      {
      }
      return null;
    }

    @Override
    protected void onPostExecute(final Engine engine)
    {
      EngineManager.getInstance().setEngineAndNotify(engine);
    }
  }
}
