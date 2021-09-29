package org.adblockplus.adblockplussbrowser.core.usercounter;

import kotlinx.coroutines.coroutineScope
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import org.adblockplus.adblockplussbrowser.base.data.model.Subscription
import org.adblockplus.adblockplussbrowser.core.AppInfo
import org.adblockplus.adblockplussbrowser.core.data.CoreRepository
import org.adblockplus.adblockplussbrowser.core.extensions.currentData
import org.adblockplus.adblockplussbrowser.core.extensions.sanatizeUrl
import org.adblockplus.adblockplussbrowser.core.retryIO
import ru.gildor.coroutines.okhttp.await
import timber.log.Timber
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import kotlin.time.ExperimentalTime


@ExperimentalTime
internal class OkHttpUserCounter(
    private val okHttpClient: OkHttpClient,
    private val repository: CoreRepository,
    private val appInfo: AppInfo
) : UserCounter {

    override suspend fun count(subscription: Subscription,
                               acceptableAdsEnabled: Boolean)
    : CountUserResult = coroutineScope {
        try {
            val lastVersion = repository.currentData().lastVersion
            Timber.d("User count lastVersion saved `%d`", lastVersion)

            val lastVersionFormat = SimpleDateFormat("yyyyMMddHHmm", Locale.ENGLISH)
            if (lastVersion != 0L && wasSentToday(lastVersion, lastVersionFormat)) {
                Timber.d("User count request skipped because it has been already sent today")
                return@coroutineScope CountUserResult.Success()
            }

            val url = createUrl(subscription, acceptableAdsEnabled, lastVersion.toString())
            val request = Request.Builder().url(url).head().build()
            val response = retryIO(description = subscription.title) {
                okHttpClient.newCall(request).await()
            }

            val result = when (response.code) {
                200 -> {
                    lastVersionFormat.timeZone = TimeZone.getTimeZone("GMT")
                    val timestamp = try {
                        // Expected date format: "Thu, 23 Sep 2021 17:31:01 GMT"
                        val parser = SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.ENGLISH)
                        lastVersionFormat.format(parser.parse(response.headers["Date"]))
                    } catch (ex: ParseException) {
                        Timber.e("Parsing 'Date' from header failed, using client GMT time")
                        Timber.e(ex)
                        lastVersionFormat.format(Calendar.getInstance().time)
                    }
                    repository.updateLastVersion(timestamp.toLong())
                    Timber.d("User count response date: %s (%s)", response.headers["Date"],
                        timestamp)
                    CountUserResult.Success()
                }
                else -> {
                    CountUserResult.Failed()
                }
            }
            response.close()
            result
        } catch (ex: Exception) {
            Timber.e("User count request failed")
            Timber.e(ex)
            CountUserResult.Failed()
        }
    }

    private fun wasSentToday(lastVersion: Long, lastVersionFormat: SimpleDateFormat): Boolean {
        val dayFormat = SimpleDateFormat("yyyyMMdd", Locale.ENGLISH)
        val lastVersionDayString = dayFormat.format(lastVersionFormat.parse(lastVersion.toString()))
        dayFormat.timeZone = TimeZone.getTimeZone("GMT")
        val todayString = dayFormat.format(Calendar.getInstance().time)
        Timber.d("wasSentToday compares `%s` to `%s`", lastVersionDayString, todayString)
        return lastVersionDayString.equals(todayString)
    }

    private fun createUrl(subscription: Subscription,
                          acceptableAdsEnabled: Boolean,
                          lastVersion: String
    ): HttpUrl {
        return subscription.url.sanatizeUrl().toHttpUrl().newBuilder().apply {
            addQueryParameter("addonName", appInfo.addonName)
            addQueryParameter("addonVersion", appInfo.addonVersion)
            addQueryParameter("application", appInfo.application)
            addQueryParameter("applicationVersion", appInfo.applicationVersion)
            addQueryParameter("platform", appInfo.platform)
            addQueryParameter("platformVersion", appInfo.platformVersion)
            addQueryParameter("disabled", (!acceptableAdsEnabled).toString())
            addQueryParameter("lastVersion", lastVersion)
        }.build()
    }
}
