package com.majestic_dev.gct

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.util.TypedValue
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.*
import com.majestic_dev.gct.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {

    companion object {

        private const val URL = "https://google.com"

    }

    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    private var session: CustomTabsSession? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        warmUp()

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
            .build()
            .launchUrl(this, Uri.parse(URL))
    }

    private fun warmUp() {
        CustomTabsClient.bindCustomTabsService(
            this,
            getPackageNameToUse(this),
            object : CustomTabsServiceConnection() {

                override fun onServiceDisconnected(p0: ComponentName?) {
                    Log.d("qwe", "")
                }

                override fun onCustomTabsServiceConnected(
                    name: ComponentName,
                    client: CustomTabsClient
                ) {
                    Log.d("qwe", "1")

                    client.warmup(0L)

                    session = client.newSession(null)
                    session?.mayLaunchUrl(Uri.parse(URL), null, null)
                }

            })
    }

    fun getPackageNameToUse(context: Context): String? {
        val STABLE_PACKAGE = "com.android.chrome"
        val BETA_PACKAGE = "com.chrome.beta"
        val DEV_PACKAGE = "com.chrome.dev"
        val LOCAL_PACKAGE = "com.google.android.apps.chrome"
        val EXTRA_CUSTOM_TABS_KEEP_ALIVE = "android.support.customtabs.extra.KEEP_ALIVE"
        val ACTION_CUSTOM_TABS_CONNECTION = "android.support.customtabs.action.CustomTabsService"

        val pm: PackageManager = context.packageManager
        // Get default VIEW intent handler.
        val activityIntent = Intent(Intent.ACTION_VIEW, Uri.parse(URL))
        val defaultViewHandlerInfo = pm.resolveActivity(activityIntent, 0)
        var defaultViewHandlerPackageName: String? = null
        if (defaultViewHandlerInfo != null) {
            defaultViewHandlerPackageName = defaultViewHandlerInfo.activityInfo.packageName
        }

        // Get all apps that can handle VIEW intents.
        val resolvedActivityList = pm.queryIntentActivities(activityIntent, 0)
        val packagesSupportingCustomTabs: MutableList<String> = ArrayList()
        for (info in resolvedActivityList) {
            val serviceIntent = Intent()
            serviceIntent.action = ACTION_CUSTOM_TABS_CONNECTION
            serviceIntent.setPackage(info.activityInfo.packageName)
            if (pm.resolveService(serviceIntent, 0) != null) {
                packagesSupportingCustomTabs.add(info.activityInfo.packageName)
            }
        }

        // Now packagesSupportingCustomTabs contains all apps that can handle both VIEW intents
        // and service calls.
        if (packagesSupportingCustomTabs.size == 1) {
            return packagesSupportingCustomTabs[0]
        } else if (!TextUtils.isEmpty(defaultViewHandlerPackageName)
            && !hasSpecializedHandlerIntents(context, activityIntent)
            && packagesSupportingCustomTabs.contains(defaultViewHandlerPackageName)
        ) {
            return defaultViewHandlerPackageName
        } else if (packagesSupportingCustomTabs.contains(STABLE_PACKAGE)) {
            return STABLE_PACKAGE
        } else if (packagesSupportingCustomTabs.contains(BETA_PACKAGE)) {
            return BETA_PACKAGE
        } else if (packagesSupportingCustomTabs.contains(DEV_PACKAGE)) {
            return DEV_PACKAGE
        } else if (packagesSupportingCustomTabs.contains(LOCAL_PACKAGE)) {
            return LOCAL_PACKAGE
        } else {
            return null
        }
    }

    private fun hasSpecializedHandlerIntents(context: Context, intent: Intent): Boolean {
        try {
            val pm = context.packageManager
            val handlers = pm.queryIntentActivities(
                intent,
                PackageManager.GET_RESOLVED_FILTER
            )
            if (handlers.size == 0) {
                return false
            }
            for (resolveInfo in handlers) {
                val filter = resolveInfo.filter ?: continue
                if (filter.countDataAuthorities() == 0 || filter.countDataPaths() == 0) continue
                if (resolveInfo.activityInfo == null) continue
                return true
            }
        } catch (e: RuntimeException) {
            Log.e("qwe", "Runtime exception while getting specialized handlers")
        }
        return false
    }


}