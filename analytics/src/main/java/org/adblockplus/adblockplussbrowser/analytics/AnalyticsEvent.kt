package org.adblockplus.adblockplussbrowser.analytics

enum class AnalyticsEvent(val eventName: String) {

    /**
     * This event is used to create an audience based on AA status. On every app launch,
     * this event will be triggered if AA is enabled.
     */
    AUDIENCE_AA_ENABLED("audience_aa_enabled"),

    /**
     * This event is used to create an audience based on AA status. On every app launch,
     * this event will be triggered if AA is disabled.
     */
    AUDIENCE_AA_DISABLED("audience_aa_disabled"),

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
     * The user adds manually a custom filter list.
     */
    CUSTOM_FILTER_LIST_ADDED("custom_filter_list_added"),

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
     * Privacy policy visited.
     */
    PRIVACY_POLICY_VISITED("privacy_policy_visited"),

    /**
     * Terms of use visited.
     */
    TERMS_OF_USE_VISITED("terms_of_use_visited"),

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
     * Parsing Head response data failed.
     */
    HEAD_RESPONSE_DATA_PARSING_FAILED("parsing_head_response_data_failed"),

    /**
     * Sending Head request failed.
     */
    HEAD_REQUEST_FAILED("sending_head_request_failed")
}
