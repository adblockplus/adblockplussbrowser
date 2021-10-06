package org.adblockplus.adblockplussbrowser.core.usercounter

internal interface UserCounter {
    suspend fun count(): CountUserResult
    fun wasUserCountedToday(): Boolean
}

internal sealed class CountUserResult {
    class Success : CountUserResult()
    class Failed : CountUserResult()
}
