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

package org.adblockplus.adblockplussbrowser.base.os;

import android.util.Size;

import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.shadow.api.Shadow;

/**
 * The size class in Robolectric is not implemented, this provides a basic implementation that is enough to test
 * the extensions implemented in SizeExtensions.kt
 */
// Suppressing the unused warning because all these methods are used via reflection by Robolectric only
@SuppressWarnings("unused")
@Implements(Size.class)
public class SizeShadow extends Shadow {

  private int height;
  private int width;

  @Implementation
  protected void __constructor__(int width, int height) {
    this.width = width;
    this.height = height;
  }

  @Implementation
  public int getWidth() { return width; }

  @Implementation
  public int getHeight() { return height; }
}
