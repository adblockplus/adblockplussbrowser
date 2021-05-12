package org.adblockplus.adblockplussbrowser.base.navigation

import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment

fun AppCompatActivity.navControllerFromFragmentContainerView(@IdRes viewId: Int): NavController {
    val navHostFragment = supportFragmentManager.findFragmentById(viewId) as NavHostFragment
    return navHostFragment.navController
}