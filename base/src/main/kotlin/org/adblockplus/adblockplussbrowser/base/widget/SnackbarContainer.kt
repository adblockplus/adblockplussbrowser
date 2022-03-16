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

package org.adblockplus.adblockplussbrowser.base.widget

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import androidx.annotation.AttrRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.appcompat.content.res.AppCompatResources
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import org.adblockplus.adblockplussbrowser.base.databinding.SnackbarLayoutBinding
import org.adblockplus.adblockplussbrowser.base.view.layoutInflater

class SnackbarContainer @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    @AttrRes defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val config = SnackbarConfig()
    private var snackbar: Snackbar? = null
    private var binding: SnackbarLayoutBinding? = null

    private val dismissRunnable: Runnable = Runnable {
        if (!config.shown && this.isShown) {
            snackbar?.dismiss()
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        // This is just a container, we don't want do display anything
        setMeasuredDimension(0, 0)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        // We need to wait until we are attached to create the Snackbar, because it needs the view hierarchy to find
        // a suitable parent and work properly with CoordinatorLayout
        snackbar = Snackbar.make(this, "", Snackbar.LENGTH_INDEFINITE).also {
            val root = it.view as ViewGroup
            root.removeAllViews()
            binding = SnackbarLayoutBinding.inflate(layoutInflater, root, true)
            adjustAll()
        }
    }

    private fun adjustAll() {
        adjustText()
        adjustTextDrawableStart()
        adjustDuration()
        adjustActionText()
        adjustActionVisibility()
        adjustActionListener()
        adjustShown()
    }

    val shown: Boolean
        get() = snackbar?.isShown ?: false

    fun show() {
        config.shown = true
        adjustShown()
    }

    fun dismiss(delay: Long = 0L) {
        config.shown = false
        config.dismissDelay = delay
        adjustShown()
    }

    private fun adjustShown() {
        snackbar?.let { snackbar ->
            snackbar.view.removeCallbacks(dismissRunnable)
            if (config.shown != snackbar.isShown) {
                if (config.shown) {
                    snackbar.show()
                } else if (config.dismissDelay > 0L) {
                    snackbar.view.postDelayed(dismissRunnable, config.dismissDelay)
                } else {
                    snackbar.dismiss()
                }
            }
        }
    }

    var text: CharSequence?
        get() = config.text
        set(value) {
            config.text = value
            adjustText()
        }

    fun setText(@StringRes stringRes: Int) {
        text = resources.getText(stringRes)
    }

    private fun adjustText() {
        binding?.snackbarText?.text = config.text
    }

    fun setTextDrawableStart(drawable: Drawable?) {
        config.textDrawableStart = drawable
        adjustTextDrawableStart()
    }

    fun setTextDrawableStart(@DrawableRes drawableRes: Int) {
        setTextDrawableStart(AppCompatResources.getDrawable(context, drawableRes))
    }

    private fun adjustTextDrawableStart() {
        binding?.snackbarText?.setCompoundDrawablesRelativeWithIntrinsicBounds(
            config.textDrawableStart,
            null,
            null,
            null
        )
    }

    @BaseTransientBottomBar.Duration
    var duration: Int
        get() = config.duration
        set(value) {
            snackbar?.duration = value
            adjustDuration()
        }

    private fun adjustDuration() {
        snackbar?.duration = config.duration
    }

    var actionText: CharSequence?
        get() = config.actionText
        set(value) {
            config.actionText = value
            adjustActionText()
        }

    fun setActionText(@StringRes stringRes: Int) {
        actionText = resources.getText(stringRes)
    }

    private fun adjustActionText() {
        binding?.snackbarAction?.text = config.actionText
    }

    fun showAction() {
        config.actionVisibility = VISIBLE
        adjustActionVisibility()
    }

    fun hideAction() {
        config.actionVisibility = GONE
        adjustActionVisibility()
    }

    private fun adjustActionVisibility() {
        binding?.snackbarAction?.visibility = config.actionVisibility
    }

    fun setActionListener(listener: OnClickListener) {
        config.actionListener = listener
        adjustActionListener()
    }

    private fun adjustActionListener() {
        binding?.snackbarAction?.setOnClickListener(config.actionListener)
    }

    companion object {
        const val HIDE_DELAY_DEFAULT = 2000L
        const val HIDE_DELAY_LONG = 5000L
    }
}

private class SnackbarConfig {
    var shown: Boolean = false
    var dismissDelay: Long = 0L
    var text: CharSequence? = null
    var textDrawableStart: Drawable? = null
    var duration: Int = Snackbar.LENGTH_INDEFINITE
    var actionText: CharSequence? = null
    var actionVisibility: Int = View.GONE
    var actionListener: View.OnClickListener? = null
}