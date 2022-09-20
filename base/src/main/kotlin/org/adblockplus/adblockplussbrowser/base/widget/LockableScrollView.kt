package org.adblockplus.adblockplussbrowser.base.widget

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.ScrollView

class LockableScrollView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : ScrollView(context, attrs, defStyle) {

    var scrollable = true

    override fun onTouchEvent(ev: MotionEvent?): Boolean {
        return when(ev?.action) {
            MotionEvent.ACTION_DOWN -> scrollable && super.onTouchEvent(ev)
            else -> super.onTouchEvent(ev)
        }
    }

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        return !scrollable || super.onInterceptTouchEvent(ev)
    }
}