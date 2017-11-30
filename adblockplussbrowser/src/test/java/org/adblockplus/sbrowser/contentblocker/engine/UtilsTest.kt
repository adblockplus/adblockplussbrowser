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

package org.adblockplus.sbrowser.contentblocker.engine

import android.content.Context
import android.content.res.Resources
import org.adblockplus.adblockplussbrowser.R
import org.adblockplus.sbrowser.contentblocker.util.SharedPrefsUtils
import org.adblockplus.sbrowser.contentblocker.util.SubscriptionUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import java.util.Locale

@RunWith(RobolectricTestRunner::class)
class UtilsTest
{
    private lateinit var mockedDefaultSubs: DefaultSubscriptions
    private lateinit var context: Context
    private val EASYLIST_GERMANY_COMPLETE_URL =
            "https://easylist-downloads.adblockplus.org/easylistgermany+easylist.txt"

    @Before
    fun setup()
    {
        context = RuntimeEnvironment.application
        mockedDefaultSubs = mock(DefaultSubscriptions::class.java)
        RuntimeEnvironment.application.resources
                .openRawResource(R.raw.subscriptions).use{ subscriptionsXml -> mockedDefaultSubs =
                DefaultSubscriptions.fromStream(subscriptionsXml) }
    }

    @Test
    fun subscriptionUtilsChooseDefaultSubscriptionForEmptyList()
    {
        assertEquals(SubscriptionUtils.chooseDefaultSubscriptionUrl(
                emptyList<DefaultSubscriptionInfo>()), Engine.EASYLIST_URL)
    }

    @Test
    fun subscriptionUtilsDefaultSubscriptionForChinaIsNotEasylist()
    {
        Resources.getSystem().configuration.setLocale(Locale.CHINA)
        assertNotEquals(SubscriptionUtils.chooseDefaultSubscriptionUrl(
                mockedDefaultSubs?.adsSubscriptions), Engine.EASYLIST_URL)
    }

    @Test
    fun subscriptionUtilsChooseDefaultSubscriptionUrlForGerman()
    {
        Resources.getSystem().configuration.setLocale(Locale.GERMANY)
        assertEquals(SubscriptionUtils.chooseDefaultSubscriptionUrl(
                mockedDefaultSubs?.adsSubscriptions), EASYLIST_GERMANY_COMPLETE_URL)
    }

    @Test
    fun subscriptionUtilsChooseDefaultSubscriptionForUnsupportedLanguage()
    {
        Resources.getSystem().configuration.setLocale(Locale.forLanguageTag("ab-xy"))
        assertEquals(SubscriptionUtils.chooseDefaultSubscriptionUrl(
                mockedDefaultSubs?.adsSubscriptions), Engine.EASYLIST_URL)
    }

    @Test
    fun sharedPrefsUtilsPutAndGetSameBoolean()
    {
        val testBoolean = true
        SharedPrefsUtils.putBoolean(context, R.string.key_aa_info_shown, testBoolean)
        val equalBoolean = SharedPrefsUtils.getBoolean(context, R.string.key_aa_info_shown,
                !testBoolean)
        assertEquals(testBoolean, equalBoolean)
    }

    @Test
    fun sharedPrefsUtilsPutAndGetDifferentBoolean()
    {
        val testBoolean = true
        SharedPrefsUtils.putBoolean(context, R.string.key_aa_info_shown, testBoolean)
        val unequalBoolean = SharedPrefsUtils.getBoolean(context, R.string.key_acceptable_ads,
                !testBoolean)
        assertNotEquals(testBoolean, unequalBoolean)
    }

    @Test
    fun sharedPrefsUtilsPutAndGetSameInteger()
    {
        val testInteger = 5
        SharedPrefsUtils.putInt(context, R.string.key_whitelisted_websites, testInteger)
        val equalInteger = SharedPrefsUtils.getInt(context, R.string.key_whitelisted_websites,
                testInteger + 1)
        assertEquals(testInteger, equalInteger)
    }

    @Test
    fun sharedPrefsUtilsPutAndGetDifferentInteger()
    {
        val testInteger = 5
        SharedPrefsUtils.putInt(context, R.string.key_whitelisted_websites, testInteger)
        val unequalInteger = SharedPrefsUtils.getInt(context,
                R.string.key_application_activated, testInteger + 1)
        assertNotEquals(testInteger, unequalInteger)
    }

    @Test
    fun sharedPrefsUtilsPutAndGetSameString()
    {
        val testString = "Hello World"
        SharedPrefsUtils.putString(context, R.string.key_automatic_updates, testString)
        val equalString = SharedPrefsUtils.getString(context, R.string.key_automatic_updates,
                testString + "!")
        assertEquals(testString, equalString)
    }

    @Test
    fun sharedPrefsUtilsPutAndGetDifferentString()
    {
        val testString = "Hello World"
        SharedPrefsUtils.putString(context, R.string.key_automatic_updates, testString)
        val unequalString = SharedPrefsUtils.getString(context,
                R.string.key_cached_filter_path, testString + "!")
        assertNotEquals(testString, unequalString)
    }

    @Test
    fun sharedPrefsUtilsPutAndGetSameStringSet()
    {
        val testStringSet = setOf("Hello", "World")
        SharedPrefsUtils.putStringSet(context, R.string.key_previous_version_code, testStringSet)
        val equalStringSet = SharedPrefsUtils.getStringSet(context,
                R.string.key_previous_version_code, setOf("Hello", "World", "!"))
        assertEquals(testStringSet, equalStringSet)
    }

    @Test
    fun sharedPrefsUtilsPutAndGetDifferentStringSet()
    {
        val testStringSet = setOf("Hello", "World")
        SharedPrefsUtils.putStringSet(context, R.string.key_previous_version_code, testStringSet)
        val unequalStringSet = SharedPrefsUtils.getStringSet(context,
                R.string.key_aa_info_shown, setOf("Hello", "World", "!"))
        assertNotEquals(testStringSet, unequalStringSet)
    }
}