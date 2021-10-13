/*
 * This file is part of Adblock Plus <https://adblockplus.org/>,
 * Copyright (C) 2006-present eyeo GmbH
 *
 * Adblock Plus is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3 as
 * published by the Free Software Foundation.
 *
 * Adblock Plus is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Adblock Plus.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.adblockplus.adblockplussbrowser.analytics

import android.content.Context
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import timber.log.Timber

class FirebaseAnalyticsProvider(appContext: Context) : AnalyticsProvider {

    private var firebaseAnalytics = FirebaseAnalytics.getInstance(appContext)

    override fun logEvent(analyticsEvent: AnalyticsEvent) {
        val bundle = Bundle()
        val logEvent = analyticsEvent.eventName
        Timber.i(logEvent)
        firebaseAnalytics.logEvent(logEvent, bundle)
    }

    override fun enable() {
        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(true)
        firebaseAnalytics.setAnalyticsCollectionEnabled(true)
    }

    override fun disable() {
        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(false)
        firebaseAnalytics.setAnalyticsCollectionEnabled(false)
    }
}
