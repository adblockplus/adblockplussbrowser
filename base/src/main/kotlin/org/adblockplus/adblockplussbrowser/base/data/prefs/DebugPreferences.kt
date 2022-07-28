package org.adblockplus.adblockplussbrowser.base.data.prefs

import kotlinx.coroutines.flow.Flow

interface DebugPreferences {

    val shouldAddTestPages: Flow<Boolean>

    fun addTestPagesCompleted()
}