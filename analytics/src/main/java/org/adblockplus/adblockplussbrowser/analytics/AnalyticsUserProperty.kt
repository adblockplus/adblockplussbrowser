package org.adblockplus.adblockplussbrowser.analytics

enum class AnalyticsUserProperty(val propertyName: String) {
    /**
     * This user property is used to create an audience based on AA status. On every request of filter list
     * current status of AA is reported.
     */
    AA_STATUS("aa_status")
}
