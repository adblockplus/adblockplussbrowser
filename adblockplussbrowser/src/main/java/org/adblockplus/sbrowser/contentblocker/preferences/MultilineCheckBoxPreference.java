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

package org.adblockplus.sbrowser.contentblocker.preferences;

import org.adblockplus.sbrowser.contentblocker.util.PreferenceUtils;

import android.content.Context;
import android.preference.CheckBoxPreference;
import android.util.AttributeSet;
import android.view.View;

/**
 * Represents a Checkbox element in a preference menu.
 * The title of the Checkbox can be larger than the view.
 * In this case, it will be displayed in 2 or more lines.
 * The default behavior of the class CheckBoxPreference
 * doesn't wrap the title.
 */

public class MultilineCheckBoxPreference extends CheckBoxPreference
{
  public MultilineCheckBoxPreference(Context context)
  {
    super(context);
  }

  public MultilineCheckBoxPreference(Context context, AttributeSet attrs)
  {
    super(context, attrs);
  }

  public MultilineCheckBoxPreference(Context context, AttributeSet attrs, int defStyle)
  {
    super(context, attrs, defStyle);
  }

  @Override
  protected void onBindView(View view)
  {
    super.onBindView(view);
    PreferenceUtils.setMultilineTitle(view);
  }
}
