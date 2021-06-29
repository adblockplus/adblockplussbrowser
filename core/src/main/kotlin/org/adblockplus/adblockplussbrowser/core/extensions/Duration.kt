package org.adblockplus.adblockplussbrowser.core.extensions

import kotlin.time.Duration
import kotlin.time.ExperimentalTime

@ExperimentalTime
internal fun Int.minutes(): Duration =
    Duration.minutes(this)

@ExperimentalTime
internal fun Int.hours(): Duration =
    Duration.hours(this)