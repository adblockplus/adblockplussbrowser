package org.adblockplus.adblockplussbrowser.core.extensions

import kotlinx.coroutines.flow.single
import kotlinx.coroutines.flow.take
import org.adblockplus.adblockplussbrowser.core.data.CoreRepository
import org.adblockplus.adblockplussbrowser.core.data.model.CoreData
import org.adblockplus.adblockplussbrowser.core.data.model.SavedState
import org.adblockplus.adblockplussbrowser.settings.data.SettingsRepository
import org.adblockplus.adblockplussbrowser.settings.data.model.Settings

internal suspend fun SettingsRepository.currentSettings(): Settings =
    this.settings.take(1).single()

internal suspend fun CoreRepository.currentData(): CoreData =
    this.data.take(1).single()

internal suspend fun CoreRepository.currentSavedState(): SavedState =
    currentData().lastState