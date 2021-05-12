package org.adblockplus.adblockplussbrowser.app

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.navigation.ui.setupActionBarWithNavController
import dagger.hilt.android.AndroidEntryPoint
import org.adblockplus.adblockplussbrowser.app.databinding.ActivityMainBinding
import org.adblockplus.adblockplussbrowser.base.navigation.navControllerFromFragmentContainerView

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding =
            DataBindingUtil.setContentView<ActivityMainBinding>(this, R.layout.activity_main)
        setSupportActionBar(binding.toolbar)

        val navController = navControllerFromFragmentContainerView(R.id.nav_host_fragment)
        setupActionBarWithNavController(navController)
    }
}