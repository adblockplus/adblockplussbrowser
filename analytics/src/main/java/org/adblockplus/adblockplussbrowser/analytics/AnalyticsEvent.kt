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

enum class AnalyticsEvent(val eventName: String) {

    /**
     * A new language list has been added.
     */
    LANGUAGE_LIST_ADDED("language_list_added"),

    /**
     * A language filter list has been removed.
     */
    LANGUAGE_LIST_REMOVED("language_list_removed"),

    /**
     * Trackers turned on.
     */
    DISABLE_TRACKING_ON("disable_tracking_on"),

    /**
     * Trackers turned off.
     */
    DISABLE_TRACKING_OFF("disable_tracking_off"),

    /**
     * Social media buttons turned on.
     */
    SOCIAL_MEDIA_BUTTONS_ON("social_media_buttons_on"),

    /**
     * Social media buttons turned off.
     */
    SOCIAL_MEDIA_BUTTONS_OFF("social_media_buttons_off"),

    /**
     * The user adds manually a custom filter list from url.
     */
    CUSTOM_FILTER_LIST_ADDED_FROM_URL("custom_filter_list_added_from_url"),


    /**
     * The user adds manually a custom filter list from file.
     */
    CUSTOM_FILTER_LIST_ADDED_FROM_FILE("custom_filter_list_added_from_file"),

    /**
     * The user removes a custom filter list.
     */
    CUSTOM_FILTER_LIST_REMOVED("custom_filter_list_removed"),

    /**
     * Url added to allowlist.
     */
    URL_ALLOWLIST_ADDED("url_allowlist_added"),

    /**
     * Url removed from allowlist.
     */
    URL_ALLOWLIST_REMOVED("url_allowlist_removed"),

    /**
     * Always selected for automatic updates.
     */
    AUTOMATIC_UPDATES_WIFI("automatic_updates_wifi"),

    /**
     * Wi-fi on for automatic updates.
     */
    AUTOMATIC_UPDATES_ALWAYS("automatic_updates_always"),

    /**
     * Lists updated manually.
     */
    MANUAL_UPDATE("manual_update"),

    /**
     * Acceptable ads turned on.
     */
    AA_ON("aa_on"),

    /**
     * Acceptable ads turned off.
     */
    AA_OFF("aa_off"),

    /**
     * About section visited.
     */
    ABOUT_VISITED("about_visited"),

    /**
     * Open source licenses visited.
     */
    OPEN_SOURCE_LICENSES_VISITED("open_source_licenses_visited"),

    /**
     * Tap on the "Show me examples" link on the AA onboarding step.
     */
    ONBOARDING_AA_SHOW_ME_EXAMPLES("onboarding_aa_show_me_examples"),

    /**
     * Opt-out from sharing events.
     */
    SHARE_EVENTS_OFF("share_events_off"),

    /**
     * Activate sharing events.
     */
    SHARE_EVENTS_ON("share_events_on"),

    /**
     * A filter list has been requested.
     */
    FILTER_LIST_REQUESTED("filter_list_requested"),

    /**
     * Sending Head request failed.
     */
    HEAD_REQUEST_FAILED("sending_head_request_failed"),

    /**
     * Languages card is dismissed with going to Add Languages
     */
    LANGUAGES_CARD_ADD("languages_card_add_additional_language"),

    /**
     * Languages card is dismissed without going to Add Languages
     */
    LANGUAGES_CARD_NO("languages_card_no_thanks"),

    /**
     * Event sent when device device is not supported.
     * There is no play store, galaxy store and any browser.
     */
    DEVICE_NOT_SUPPORTED("device_not_supported"),

    /**
     * The event is sent when the device file manager returns the wrong
     * result when loading custom filters as a result of user canceling this operation
     * or wrong result from file picker.
     */
    DEVICE_FILE_MANAGER_NOT_SUPPORTED_OR_CANCELED("device_file_manager_not_supported_or_canceled"),

    /**
     * The user has chosen to load custom filter list from url.
     */
    LOAD_CUSTOM_FILTER_LIST_FROM_URL("load_custom_filter_list_from_url"),

    /**
     * The user has chosen to load custom filter list from file.
     */
    LOAD_CUSTOM_FILTER_LIST_FROM_FILE("load_custom_filter_list_from_file")
}
