package org.adblockplus.adblockplussbrowser.base.samsung

import android.content.Context
import android.content.Intent
import androidx.work.Worker
import androidx.work.WorkerParameters
import org.adblockplus.adblockplussbrowser.base.samsung.constants.SamsungInternetConstants

class OpenSISettingsWorker(context: Context, params: WorkerParameters) : Worker(context, params) {
    override fun doWork(): Result {
        val intent = Intent(SamsungInternetConstants.SBROWSER_ACTION_OPEN_SETTINGS)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        applicationContext.startActivity(intent)
        return Result.success()
    }
}
