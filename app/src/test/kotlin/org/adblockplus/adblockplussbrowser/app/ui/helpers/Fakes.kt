package org.adblockplus.adblockplussbrowser.app.ui.helpers

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import org.adblockplus.adblockplussbrowser.analytics.AnalyticsEvent
import org.adblockplus.adblockplussbrowser.analytics.AnalyticsProvider
import org.adblockplus.adblockplussbrowser.analytics.AnalyticsUserProperty
import org.adblockplus.adblockplussbrowser.base.data.model.CustomSubscriptionType
import org.adblockplus.adblockplussbrowser.base.data.model.Subscription
import org.adblockplus.adblockplussbrowser.base.data.prefs.AppPreferences
import org.adblockplus.adblockplussbrowser.settings.data.SettingsRepository
import org.adblockplus.adblockplussbrowser.settings.data.model.Settings
import org.adblockplus.adblockplussbrowser.settings.data.model.UpdateConfig

class Fakes {

    internal open class CustomFakeAppPreferences(
        private val customOnBoardingCompleted: Boolean = true,
        private val customLastFilterListRequest: Long = 0L
    ): AppPreferences {
        override val referrerAlreadyChecked: Boolean
            get() = TODO("Not yet implemented")

        override fun referrerChecked() {
            TODO("Not yet implemented")
        }

        override val onboardingCompleted: Flow<Boolean>
            get() = flowOf(customOnBoardingCompleted)

        override suspend fun completeOnboarding() {}

        override val lastFilterListRequest: Flow<Long>
            get() = flowOf(customLastFilterListRequest)

        override suspend fun isAdblockEnabled(): Flow<Boolean> {
            return super.isAdblockEnabled()
        }

        override suspend fun updateLastFilterRequest(lastFilterListRequest: Long) {
            TODO("Not yet implemented")
        }

        override val shouldAddTestPages: Flow<Boolean>
            get() = TODO("Not yet implemented")

        override fun initialTestPagesConfigurationCompleted() {
            TODO("Not yet implemented")
        }
    }

    internal class FakeAnalyticsProvider : AnalyticsProvider {

        var event : AnalyticsEvent? = null
        var exception : Exception? = null
        var error : String? = null
        var userPropertyName : AnalyticsUserProperty? = null
        var userPropertyValue : String? = null

        override fun logEvent(analyticsEvent: AnalyticsEvent) {
            this.event = analyticsEvent
        }

        override fun logException(exception: Exception) {
            this.exception = exception
        }

        override fun logError(error: String) {
            this.error = error
        }

        override fun setUserProperty(
            analyticsProperty: AnalyticsUserProperty,
            analyticsPropertyValue: String
        ) {
            userPropertyName = analyticsProperty
            userPropertyValue = analyticsPropertyValue
        }

        override fun enable() {}

        override fun disable() {}
    }

    internal class FakeSettingsRepository(private val serverUrl: String) : SettingsRepository {
        var acceptableAdsStatus: Boolean = true

        override val settings: Flow<Settings>
            get() = flow {
                emit(
                    Settings(
                        true,
                        acceptableAdsStatus,
                        UpdateConfig.ALWAYS,
                        listOf(""),
                        listOf(""),
                        listOf(
                            Subscription("$serverUrl/easylist.txt", "", 0L, CustomSubscriptionType.FROM_URL),
                            Subscription("$serverUrl/exceptionrules.txt", "", 0L, CustomSubscriptionType.FROM_URL)
                        ),
                        listOf(),
                        analyticsEnabled = true,
                        languagesOnboardingCompleted = true
                    )
                )
            }

        override suspend fun getEasylistSubscription(): Subscription {
            return Subscription("$serverUrl/easylist.txt", "", 0L, CustomSubscriptionType.FROM_URL)
        }

        override suspend fun getAcceptableAdsSubscription(): Subscription {
            return Subscription("$serverUrl/exceptionrules.txt", "", 0L, CustomSubscriptionType.FROM_URL)
        }

        override suspend fun getTestPagesSubscription(): Subscription {
            return Subscription("$serverUrl/exceptionrules.txt", "", 0L, CustomSubscriptionType.FROM_URL)
        }

        override suspend fun getDefaultPrimarySubscriptions(): List<Subscription> {
            return listOf(
                Subscription("$serverUrl/easylist.txt", "", 0L, CustomSubscriptionType.FROM_URL),
                Subscription("$serverUrl/exceptionrules.txt", "", 0L, CustomSubscriptionType.FROM_URL)
            )
        }

        override suspend fun getDefaultOtherSubscriptions(): List<Subscription> {
            TODO("Not yet implemented")
        }

        override suspend fun setAdblockEnabled(enabled: Boolean) {}

        override suspend fun setAcceptableAdsEnabled(enabled: Boolean) {}

        override suspend fun setUpdateConfig(updateConfig: UpdateConfig) {}

        override suspend fun addAllowedDomain(domain: String) {}

        override suspend fun removeAllowedDomain(domain: String) {}

        override suspend fun setAllowedDomains(domains: List<String>) {}

        override suspend fun addBlockedDomain(domain: String) {}

        override suspend fun removeBlockedDomain(domain: String) {}

        override suspend fun setBlockedDomains(domains: List<String>) {}

        override suspend fun addActivePrimarySubscription(subscription: Subscription) {}

        override suspend fun removeActivePrimarySubscription(subscription: Subscription) {}

        override suspend fun setActivePrimarySubscriptions(subscriptions: List<Subscription>) {}

        override suspend fun addActiveOtherSubscription(subscription: Subscription) {}

        override suspend fun removeActiveOtherSubscription(subscription: Subscription) {}

        override suspend fun setActiveOtherSubscriptions(subscriptions: List<Subscription>) {}

        override suspend fun updatePrimarySubscriptionLastUpdate(url: String, lastUpdate: Long) {}

        override suspend fun updateOtherSubscriptionLastUpdate(url: String, lastUpdate: Long) {}

        override suspend fun updatePrimarySubscriptionsLastUpdate(subscriptions: List<Subscription>) {}

        override suspend fun updateOtherSubscriptionsLastUpdate(subscriptions: List<Subscription>) {}

        override suspend fun setAnalyticsEnabled(enabled: Boolean) {}

        override suspend fun getAdditionalTrackingSubscription(): Subscription {
            TODO("Not yet implemented")
        }

        override suspend fun getSocialMediaTrackingSubscription(): Subscription {
            TODO("Not yet implemented")
        }

        override suspend fun markLanguagesOnboardingCompleted() {}
        override suspend fun checkLanguagesOnboardingCompleted() {
            TODO("Not yet implemented")
        }
    }

}