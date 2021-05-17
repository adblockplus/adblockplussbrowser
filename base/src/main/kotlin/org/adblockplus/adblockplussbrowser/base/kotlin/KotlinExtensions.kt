package org.adblockplus.adblockplussbrowser.base.kotlin

val <T> T.exhaustive: T
    get() = this

fun <T> List<T>.asMutable() = this as MutableList