package org.adblockplus.adblockplussbrowser.base.res

import android.content.res.Configuration
import org.adblockplus.adblockplussbrowser.base.os.atLeastApi24
import java.util.*

val Configuration.localeCompat: Locale
    get() = if (atLeastApi24) {
        this.locales[0]
    } else {
        @Suppress("DEPRECATION")
        this.locale
    }