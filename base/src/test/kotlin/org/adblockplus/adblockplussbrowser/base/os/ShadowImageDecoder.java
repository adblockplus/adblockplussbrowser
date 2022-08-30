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

import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.graphics.ImageDecoder.ImageInfo;
import android.graphics.ImageDecoder.Source;
import android.net.Uri;
import android.util.Size;

import androidx.annotation.NonNull;

import org.mockito.MockSettings;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

import java.io.IOException;

import static android.graphics.Bitmap.createScaledBitmap;
import static android.graphics.BitmapFactory.decodeStream;
import static org.mockito.AdditionalAnswers.delegatesTo;
import static org.mockito.Mockito.*;

/**
 * A custom implementation of an ShadowImageDecoder to implement the createSource and decodeBitmap methods via
 * Mockito.
 */
// We suppress unused warnings because this class methods are only used via reflection by Robolectric
@SuppressWarnings("unused")
@Implements(ImageDecoder.class)
public class ShadowImageDecoder {

  @Implementation
  protected static Source createSource(@NonNull ContentResolver cr, @NonNull Uri uri) {
    // As a Source is an opaque type and we need to be able to extract the uri and the content resolver from it, we
    // make the mocked instance to implement and extra interface for getting them.
    final MockSettings settings = withSettings().extraInterfaces(MockSource.class);
    final Source src = mock(Source.class, settings);
    final MockSource mockSource = (MockSource) src;
    when(mockSource.getUri()).thenReturn(uri);
    when(mockSource.getContentResolver()).thenReturn(cr);
    return src;
  }

  @Implementation
  protected static Bitmap decodeBitmap(@NonNull Source src, @NonNull ImageDecoder.OnHeaderDecodedListener listener) throws IOException {
    final MockSource mockSource = (MockSource) src;
    final Uri uri = mockSource.getUri();
    final ContentResolver cr = mockSource.getContentResolver();
    final Bitmap bitmap = decodeStream(cr.openInputStream(uri));

    final ImageInfo info = mock(ImageInfo.class);
    when(info.getSize()).thenReturn(new Size(bitmap.getWidth(), bitmap.getHeight()));

    final ImageDecoderDelegate delegate = new ImageDecoderDelegate();
    // Verifying if the mocked instance setTarget size is called is not enough, so we delegate the method
    final ImageDecoder decoder = mock(ImageDecoder.class, delegatesTo(delegate));
    listener.onHeaderDecoded(decoder, info, src);

    if (delegate.targetWidth > 0 && delegate.targetHeight >0) {
      final Bitmap scaledBitmap = createScaledBitmap(bitmap, delegate.targetWidth, delegate.targetHeight, true);
      bitmap.recycle();
      return scaledBitmap;
    }
    return bitmap;
  }


  // A delegate for some of the ImageDecoder instance methods
  private static class ImageDecoderDelegate {
    public int targetWidth = -1;
    public int targetHeight = -1;

    public void setTargetSize(int targetWidth, int targetHeight) {
      this.targetWidth = targetWidth;
      this.targetHeight = targetHeight;
    }
  }

  public interface MockSource {
    @NonNull Uri getUri();
    @NonNull ContentResolver getContentResolver();
  }
}
