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

package org.adblockplus.adblockplussbrowser.base.view

import android.view.LayoutInflater
import android.view.View
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber

val View.layoutInflater: LayoutInflater
    get() = LayoutInflater.from(this.context)

private const val DEBOUNCE_TIME = 1000L

fun View.setDebounceOnClickListener(onClickListener: View.OnClickListener, lifecycleOwner: LifecycleOwner) {
    var debounceJob: Job? = null
    val clickWithDebounce: (view: View) -> Unit = {
        if (debounceJob == null) {
            debounceJob = lifecycleOwner.lifecycleScope.launch {
                onClickListener.onClick(it)
                delay(DEBOUNCE_TIME)
                debounceJob = null
            }
        } else {
            Timber.d("Skipping onClick event")
        }
    }
    setOnClickListener(clickWithDebounce)
}
