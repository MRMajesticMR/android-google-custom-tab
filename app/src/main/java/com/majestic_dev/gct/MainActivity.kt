package com.majestic_dev.gct

import android.content.ComponentName
import android.net.Uri
import android.os.Bundle
import android.util.TypedValue
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.*
import com.majestic_dev.gct.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {

    companion object {

        private const val URL = "https://all-quotes.ru/account/auth"

    }

    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    private var session: CustomTabsSession? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.bGoogle.setOnClickListener { openGoogleCustomTab() }
    }

    private fun openGoogleCustomTab() {
        val typedValue = TypedValue()

        theme.resolveAttribute(R.attr.colorPrimary, typedValue, true)

        CustomTabsIntent.Builder(session)
            .setDefaultColorSchemeParams(
                CustomTabColorSchemeParams.Builder()
                    .setToolbarColor(typedValue.data)
                    .build()
            )
            .setStartAnimations(this, android.R.anim.fade_in, android.R.anim.fade_out)
            .setExitAnimations(this, android.R.anim.fade_in, android.R.anim.fade_out)
            .build()
            .launchUrl(this, Uri.parse(URL))
    }

    private fun warmUp() {
        CustomTabsClient.bindCustomTabsService(this, packageName, object: CustomTabsServiceConnection() {

            override fun onServiceDisconnected(p0: ComponentName?) {

            }

            override fun onCustomTabsServiceConnected(name: ComponentName, client: CustomTabsClient) {
                client.warmup(0L)

                session = client.newSession(null)
                session?.mayLaunchUrl(Uri.parse(URL), null, null)
            }

        })
    }

}