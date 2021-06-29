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
    private val colorSelected = ContextCompat.getColor(context, R.color.abp_foreground_accent)
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