package org.adblockplus.adblockplussbrowser.preferences.ui

import android.graphics.*
import androidx.core.content.ContextCompat
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
            clearCanvas(canvas, itemView.right + dX, itemView.top.toFloat(), itemView.right.toFloat(),
                itemView.bottom.toFloat())
            super.onChildDraw(canvas, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            return
        }

        val deleteIcon = ContextCompat.getDrawable(recyclerView.context, R.drawable.ic_baseline_delete_24)
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

    private fun clearCanvas(canvas: Canvas?, left: Float, top: Float, right: Float, bottom: Float) {
        val clearPaint = Paint().apply { xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR) }
        canvas?.drawRect(left, top, right, bottom, clearPaint)
    }
}