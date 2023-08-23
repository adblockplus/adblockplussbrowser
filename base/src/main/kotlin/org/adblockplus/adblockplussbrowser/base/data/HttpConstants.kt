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

package org.adblockplus.adblockplussbrowser.base.data

object HttpConstants {
    // https://stackoverflow.com/questions/5358109/what-is-the-average-size-of-an-http-request-response-header
    const val HTTP_ERROR_AVERAGE_HEADERS_SIZE = 800
    const val HTTP_ERROR_MAX_BODY_SIZE = 500

    const val HTTP_HEADER_AUTHORIZATION: String = "Authorization"
    const val EYEO_TELEMETRY_ACTIVEPING_AUTH_TOKEN: String = "<REPLACE ME WITH TOKEN>" // TODO: Replace with real token
}
