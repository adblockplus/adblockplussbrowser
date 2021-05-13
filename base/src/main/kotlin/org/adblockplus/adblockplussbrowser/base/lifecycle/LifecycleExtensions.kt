package org.adblockplus.adblockplussbrowser.base.lifecycle

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

fun <T> LiveData<T>.asMutable() =  this as MutableLiveData