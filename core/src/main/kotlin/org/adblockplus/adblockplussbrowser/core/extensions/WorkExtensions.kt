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

import androidx.work.BackoffPolicy
import androidx.work.ListenableWorker
import androidx.work.OneTimeWorkRequest
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkRequest
import java.util.concurrent.TimeUnit
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

@ExperimentalTime
internal fun WorkRequest.Builder<OneTimeWorkRequest.Builder, OneTimeWorkRequest>.setBackoffTime(
    duration: Duration
): WorkRequest.Builder<OneTimeWorkRequest.Builder, OneTimeWorkRequest> {
    this.setBackoffCriteria(BackoffPolicy.EXPONENTIAL, duration.inWholeSeconds, TimeUnit.SECONDS)
    return this
}

