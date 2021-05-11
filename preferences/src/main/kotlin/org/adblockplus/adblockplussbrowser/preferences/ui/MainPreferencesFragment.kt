package org.adblockplus.adblockplussbrowser.preferences.ui

import android.os.Bundle
import android.util.TypedValue
import dagger.hilt.android.AndroidEntryPoint
import org.adblockplus.adblockplussbrowser.base.databinding.DataBindingFragment
import org.adblockplus.adblockplussbrowser.preferences.R
import org.adblockplus.adblockplussbrowser.preferences.databinding.FragmentMainPreferencesBinding

@AndroidEntryPoint
class MainPreferencesFragment :
    DataBindingFragment<FragmentMainPreferencesBinding>(R.layout.fragment_main_preferences) {

//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        val tv = TypedValue()
//        requireActivity().theme.resolveAttribute(R.attr.preferenceTheme, tv, true)
//        var theme = tv.resourceId
//        if (theme == 0) {
//            // Fallback to default theme.
//            theme = R.style.PreferenceThemeOverlay
//        }
//        requireActivity().theme.applyStyle(theme, false)
//    }

    override fun onBindView(binding: FragmentMainPreferencesBinding) {

    }
}