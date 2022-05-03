package org.adblockplus.adblockplussbrowser.core.helpers

class WorkerParameters(
    val tags: MutableList<String> = mutableListOf(),
    var runAttemptCount: Int = 1
)