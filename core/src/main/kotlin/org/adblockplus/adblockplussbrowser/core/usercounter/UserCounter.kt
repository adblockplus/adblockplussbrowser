package org.adblockplus.adblockplussbrowser.core.usercounter

internal interface UserCounter {
    suspend fun count(): CountUserResult
}

internal sealed class CountUserResult {
    class Success : CountUserResult()
    class Skipped : CountUserResult()
    class Failed : CountUserResult()
}
