package org.adblockplus.adblockplussbrowser.settings.data.local

import android.content.Context
import com.squareup.moshi.*
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.adblockplus.adblockplussbrowser.base.data.model.Subscription
import org.adblockplus.adblockplussbrowser.settings.R

class SubscriptionsLoader(
    @ApplicationContext private val context: Context,
    private val moshi: Moshi
) {

    val defaultSubscriptions: Flow<Subscription> = flow { }

//    private var defaultSubscriptions: List<Subscription>? = null
//
//    fun loadDefaultSubscriptions(): List<Subscription> {
//        defaultSubscriptions?.let { defaultSubscriptions ->
//            return defaultSubscriptions
//        }
//        val subscriptions = mutableListOf<Subscription>()
//        defaultSubscriptions = subscriptions
//        return subscriptions
//    }

    private suspend fun loadDefaultSubscriptions(): List<Subscription> {
        val result = mutableListOf<Subscription>()
        val type = Types.newParameterizedType(List::class.java, PreloadedSubscription::class.java)
        val adapter = moshi.adapter<List<PreloadedSubscription>>(type)
        val reader = context.resources.openRawResource(R.raw.subscriptions).bufferedReader()
        val data = reader.use { it.readText() }
        val preloadedSubscriptions = adapter.fromJson(data)
        preloadedSubscriptions?.forEach { preloadedSubscription ->
            if (preloadedSubscription.type == "ads") {
                val subscription = Subscription(
                    preloadedSubscription.url,
                    preloadedSubscription.title,
                    preloadedSubscription.languages ?: emptyList()
                )
            }
        }
        return result
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