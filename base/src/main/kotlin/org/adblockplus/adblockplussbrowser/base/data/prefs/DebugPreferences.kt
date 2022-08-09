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

package org.adblockplus.adblockplussbrowser.base.data.prefs

import kotlinx.coroutines.flow.Flow

/**
 *  This interface contains the configuration preferences in debug mode
 */
interface DebugPreferences {

    /**
     * This value will be true by default on the first start, so that
     * if the app is in debug mode, the test pages filters are added.
     */
    val shouldAddTestPages: Flow<Boolean>

    /**
     * The point of this method is to indicate that test pages should
     * no longer be added by default when the filters are updated.
     */
    fun initialTestPagesConfigurationCompleted()
}
