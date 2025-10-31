package com.ggetters.app.core.extensions.kotlin

import android.content.Context
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.net.toUri

fun Context.openBrowserTo(
    webViewUrl: String,
    useContext: Context = this
) {
    val intent = CustomTabsIntent.Builder().build()
    intent.launchUrl(useContext, webViewUrl.toUri())
}