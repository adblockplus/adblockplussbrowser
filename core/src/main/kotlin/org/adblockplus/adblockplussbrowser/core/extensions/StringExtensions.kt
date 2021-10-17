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

package org.adblockplus.adblockplussbrowser.core.extensions

internal fun String.sanatizeUrl(): String {
    return if (this.startsWith("http://") || this.startsWith("https://")) {
        this
    } else {
        "https://$this"
    }
}

internal fun String.toAllowRule(): String {
    if (this.startsWith("@@||") && this.endsWith("^\$document")) return this
    return "@@||${this}^\$document"
}

internal fun String.toBlockRule(): String {
    if (this.startsWith("||") && this.endsWith("^")) return this
    return "||${this}^"
}