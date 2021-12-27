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

package org.adblockplus.adblockplussbrowser.analytics

enum class AnalyticsUserProperty(val propertyName: String) {
    /**
     * User property used for an audience based on AA status.
     */
    IS_AA_ENABLED("is_aa_enabled"),

    /**
     * User property used for tracking install referrer.
     */
    INSTALL_REFERRER("install_referrer"),

    /**
     * User property used for tracking HTTP error from download request.
     */
    DOWNLOAD_HTTP_ERROR("download_http_error"),

    /**
     * User property used for tracking HTTP error from user counting request.
     */
    USER_COUNTING_HTTP_ERROR("user_counting_http_error")
}
