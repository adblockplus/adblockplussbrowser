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

import org.adblockplus.adblockplussbrowser.R;

import android.content.Context;
import android.preference.Preference;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

public class ImprintPreference extends Preference
{
  public ImprintPreference(Context context, AttributeSet attrs, int defStyleAttr)
  {
    super(context, attrs, defStyleAttr);
  }

  public ImprintPreference(Context context, AttributeSet attrs)
  {
    super(context, attrs);
    setLayoutResource(R.layout.imprint_screen);
    setSelectable(false);
  }

  public ImprintPreference(Context context)
  {
    super(context);
  }

  @Override
  protected void onBindView(View view)
  {
    super.onBindView(view);
    final TextView tvImprint = (TextView) view.findViewById(R.id.tv_imprint);
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N)
    {
      tvImprint.setText(Html.fromHtml(getContext().getString(R.string.imprint_text), Html.FROM_HTML_MODE_LEGACY));
    }
    else
    {
      tvImprint.setText(Html.fromHtml(getContext().getString(R.string.imprint_text)));
    }
    tvImprint.setMovementMethod(LinkMovementMethod.getInstance());
  }
}
