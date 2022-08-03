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

package org.adblockplus.adblockplussbrowser.preferences.ui

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import org.adblockplus.adblockplussbrowser.preferences.R

abstract class SwipeToDeleteCallback : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {

    override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        return false
    }

    override fun onChildDraw(canvas: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder,
                             dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean
    ) {
        val itemView = viewHolder.itemView
        val itemHeight = itemView.bottom - itemView.top
        val isCanceled = dX == 0f && !isCurrentlyActive

        if (isCanceled) {
            canvas.clear(itemView.right + dX, itemView.top.toFloat(), itemView.right.toFloat(),
                itemView.bottom.toFloat())
            super.onChildDraw(canvas, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            return
        }

        val deleteIcon = AppCompatResources.getDrawable(recyclerView.context, R.drawable.ic_baseline_delete_24)
        val iconWidth = deleteIcon!!.intrinsicWidth
        val iconHeight = deleteIcon.intrinsicHeight
        val iconMargin = (itemHeight - iconHeight) / 2
        val iconTop = itemView.top + (itemHeight - iconHeight) / 2
        val iconBottom = iconTop + iconHeight
        val iconLeft = itemView.right - iconMargin - iconWidth
        val iconRight = itemView.right - iconMargin

        deleteIcon.apply {
            setBounds(iconLeft, iconTop, iconRight, iconBottom)
            setTint(Color.RED)
            draw(canvas)
        }

        super.onChildDraw(canvas, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
    }

    private fun Canvas.clear(left: Float, top: Float, right: Float, bottom: Float) {
        val clearPaint = Paint().apply { xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR) }
        this.drawRect(left, top, right, bottom, clearPaint)
    }
}

