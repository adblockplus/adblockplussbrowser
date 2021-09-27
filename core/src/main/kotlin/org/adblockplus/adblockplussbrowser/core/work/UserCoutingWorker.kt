package org.adblockplus.adblockplussbrowser.core.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.*
import org.adblockplus.adblockplussbrowser.core.extensions.currentSettings
import org.adblockplus.adblockplussbrowser.core.usercounter.UserCounter
import org.adblockplus.adblockplussbrowser.settings.data.SettingsRepository
import timber.log.Timber

@HiltWorker
internal class UserCountingWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted params: WorkerParameters,
    private val settingsRepository: SettingsRepository,
    private val userCounter: UserCounter,
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        return@withContext try {
            Timber.d("USER COUNTING JOB, run Attempt: %d", runAttemptCount)

            // Don't let a failing worker run eternally...
            if (hasReachedMaxAttempts()) {
                Timber.d("Max attempts reached...")
                return@withContext Result.failure()
            }

            val acceptableAdsEnabled = settingsRepository.currentSettings().acceptableAdsEnabled
            val acceptableAdsSubscription = settingsRepository.getAcceptableAdsSubscription()
            val result = userCounter.count(acceptableAdsSubscription, acceptableAdsEnabled)

            if (isStopped) return@withContext Result.success()

            if (result.isSuccessful()) {
                Timber.i("User counted")
                Result.success()
            } else {
                Timber.i("User counting failed, retrying")
                delay(DELAY_DEFAULT)
                Result.retry()
            }
        } catch (ex: Exception) {
            Timber.e("USER COUNTING JOB failed")
            Result.failure()
        }
    }

    private fun CoroutineWorker.hasReachedMaxAttempts() = runAttemptCount > RUN_ATTEMPT_MAX_COUNT

    companion object {
        private const val DELAY_DEFAULT = 500L
        private const val RUN_ATTEMPT_MAX_COUNT = 4

        internal const val USER_COUNT_KEY_PERIODIC_WORK = "USER_COUNT_PERIODIC_KEY"
    }
}
