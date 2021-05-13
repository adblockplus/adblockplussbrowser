package org.adblockplus.adblockplussbrowser.base.os

import android.os.Build

val atLeastApi22: Boolean
    get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1

val atLeastApi23: Boolean
    get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M

val atLeastApi24: Boolean
    get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N

val atLeastApi25: Boolean
    get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1

val atLeastApi26: Boolean
    get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O

val atLeastApi27: Boolean
    get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1

val atLeastApi28: Boolean
    get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.P

val atLeastApi29: Boolean
    get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q

val atLeastApi30: Boolean
    get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.R
