package org.adblockplus.adblockplussbrowser.base.view

import android.view.LayoutInflater
import android.view.View

val View.layoutInflater: LayoutInflater
    get() = LayoutInflater.from(this.context)