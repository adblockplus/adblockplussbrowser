package org.adblockplus.adblockplussbrowser.preferences.ui.allowlist

import org.adblockplus.adblockplussbrowser.preferences.ui.GroupItemLayout
import java.io.Serializable

internal data class AllowlistItem(val domain: String, val layout: GroupItemLayout) : Serializable