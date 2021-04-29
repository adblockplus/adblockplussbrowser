package org.adblockplus.adblockplussbrowser.settings.data.local

import android.content.Context
import com.squareup.moshi.*
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.adblockplus.adblockplussbrowser.base.data.model.Subscription
import org.adblockplus.adblockplussbrowser.settings.R

internal class SubscriptionsLoader(
    @ApplicationContext private val context: Context,
    private val moshi: Moshi
) {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val preloadedSubscriptionsFlow =
        MutableStateFlow<List<PreloadedSubscription>>(emptyList())

    // TODO: We might want to use a string resource here for localization
    private val urlToTitleMap = mapOf(
        "https://easylist-downloads.adblockplus.org/i_dont_care_about_cookies.txt" to "Disable cookies",
        "https://easylist-downloads.adblockplus.org/fanboy-notifications.txt" to "Disable notifications",
        "https://easylist-downloads.adblockplus.org/easyprivacy.txt" to "Disable tracking",
        "https://easylist-downloads.adblockplus.org/fanboy-social.txt" to "Disable social media buttons"
    )

    val defaultAdsSubscriptions: Flow<List<Subscription>> = flow {
        checkIfLoadIsNeeded()
        emitAll(
            preloadedSubscriptionsFlow.map { preloadeSubscriptions ->
                preloadeSubscriptions.filter { it.type == "ads" }.map { it.toSubscription() }
            }
        )
    }

    val defaultOtherSubscriptions: Flow<List<Subscription>> = flow {
        checkIfLoadIsNeeded()
        emitAll(
            preloadedSubscriptionsFlow.map { preloadeSubscriptions ->
                preloadeSubscriptions.filter { it.type != "ads" }
                    .map { it.toSubscription(urlToTitleMap[it.url]) }
            }
        )
    }

    private fun checkIfLoadIsNeeded() {
        if (preloadedSubscriptionsFlow.value.isEmpty()) {
            scope.launch {
                preloadedSubscriptionsFlow.value = loadPreloadedSubscriptions()
            }
        }
    }

    private fun loadPreloadedSubscriptions(): List<PreloadedSubscription> {
        val type = Types.newParameterizedType(List::class.java, PreloadedSubscription::class.java)
        val adapter = moshi.adapter<List<PreloadedSubscription>>(type)
        val reader = context.resources.openRawResource(R.raw.subscriptions).bufferedReader()
        val data = reader.use { it.readText() }
        return adapter.fromJson(data) ?: emptyList()
    }
}

@JsonClass(generateAdapter = true)
internal data class PreloadedSubscription(
    val type: String,
    val languages: List<String>?,
    val title: String,
    val url: String,
    val homepage: String?,
)

private fun PreloadedSubscription.toSubscription(customTitle: String? = null): Subscription =
    Subscription(
        this.url,
        customTitle ?: this.title,
        0L,
        this.languages ?: emptyList()
    )