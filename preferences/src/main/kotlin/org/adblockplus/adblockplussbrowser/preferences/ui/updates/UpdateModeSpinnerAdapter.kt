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

package org.adblockplus.adblockplussbrowser.preferences.ui.updates

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.annotation.ArrayRes
import androidx.annotation.IdRes
import androidx.annotation.LayoutRes
import androidx.core.content.ContextCompat
import org.adblockplus.adblockplussbrowser.preferences.R

internal class UpdateModeSpinnerAdapter(
    context: Context,
    strings: List<CharSequence>,
    @LayoutRes layoutResID: Int,
    @IdRes textViewResourceId: Int
) : ArrayAdapter<CharSequence>(context, layoutResID, textViewResourceId, strings) {

    var selectedPosition: Int = 0
    private val colorSelected = ContextCompat.getColor(context, R.color.foreground_accent)
    private val colorNormal = ContextCompat.getColor(context, R.color.abp_foreground)

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = super.getDropDownView(position, convertView, parent) as TextView

        if (position == selectedPosition) {
            view.setTextColor(colorSelected)
        } else {
            view.setTextColor(colorNormal)
        }

        return view
    }

    companion object {
        internal fun createFromResource(
            context: Context,
            @ArrayRes arrayId: Int,
            @LayoutRes layoutResId: Int,
            @LayoutRes dropDownRes: Int,
            @IdRes textViewResourceId: Int
        ): UpdateModeSpinnerAdapter {
            val strings = context.resources.getTextArray(arrayId)
            return UpdateModeSpinnerAdapter(context, strings.toList(), layoutResId,
                textViewResourceId).also { adapter ->
                adapter.setDropDownViewResource(dropDownRes)
            }
        }
    }
}