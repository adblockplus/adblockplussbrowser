package org.adblockplus.adblockplussbrowser.base.os

import android.os.Build

val atLeastApi24: Boolean
    get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N