package org.adblockplus.adblockplussbrowser.base.kotlin

val <T> T.exhaustive: T
    get() = this

fun <K, V> Map<K, V>.asMutable() = this as MutableMap

fun <T> List<T>.asMutable() = this as MutableList

fun <T> Set<T>.asMutable() = this as MutableSet