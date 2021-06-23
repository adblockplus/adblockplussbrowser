package org.adblockplus.adblockplussbrowser.onboarding.ui

import androidx.annotation.LayoutRes
import androidx.annotation.StringRes

internal sealed class PageInfo {

    data class Default(
        @StringRes val title1StringRes: Int,
        @StringRes val title2StringRes: Int,
        @StringRes val title3StringRes: Int,
        @LayoutRes val contentLayoutRes: Int
    ) : PageInfo()
}