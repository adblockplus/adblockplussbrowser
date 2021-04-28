package org.adblockplus.adblockplussbrowser.app

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.runBlocking
import org.adblockplus.adblockplussbrowser.base.data.model.Subscription
import org.adblockplus.adblockplussbrowser.settings.data.SettingsRepository
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject lateinit var repository: SettingsRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<TextView>(R.id.hello_tv).setOnClickListener {
            android.util.Log.i("ABPSI", "button clicked")
            runBlocking {
                repository.setAcceptableAdsEnabled(true)
                repository.setActiveOtherSubscriptions(listOf(
                    Subscription("myurl", "mytitle", listOf("pt"))
                ))
                repository.observeSettings().collect {
                    android.util.Log.i("ABPSI", "settings is ${it}")
                }

//                repository.observeDefaultCustomSubscriptions().collect {
//                    android.util.Log.i("ABPSI", "collecting subscriptions ${it.size}")
//                    it.forEach { subscription ->
//                        android.util.Log.i("ABPSI", "${subscription.title} ${subscription.languages.joinToString()} ${subscription.url}")
//                    }
//                }
            }
        }
    }
}