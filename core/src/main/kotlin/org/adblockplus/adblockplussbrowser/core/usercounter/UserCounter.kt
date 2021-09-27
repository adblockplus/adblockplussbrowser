package org.adblockplus.adblockplussbrowser.core.usercounter

import org.adblockplus.adblockplussbrowser.base.data.model.Subscription

internal interface UserCounter {
    suspend fun count(subscription: Subscription, acceptableAdsEnabled: Boolean): CountUserResult
}

internal sealed class CountUserResult {
    class Success : CountUserResult()
    class Failed : CountUserResult()

    fun isSuccessful(): Boolean {
        return when(this) {
            is Success -> true
            is Failed -> false
        }
    }
}
