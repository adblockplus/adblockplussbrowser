package org.adblockplus.adblockplussbrowser.core.extensions

internal fun String.sanatizeUrl(): String {
    return if (this.startsWith("http://") || this.startsWith("https://")) {
        this
    } else {
        "https://$this"
    }
}

internal fun String.toAllowRule(): String {
    if (this.startsWith("@@||") && this.endsWith("^\$document")) return this
    return "@@||${this}^\$document"
}

internal fun String.toBlockRule(): String {
    if (this.startsWith("||") && this.endsWith("^")) return this
    return "||${this}^"
}