package com.ggetters.app

import android.app.Application
import android.content.pm.ApplicationInfo
import com.ggetters.app.core.utils.Clogger
import com.ggetters.app.core.utils.DevClass
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.ggetters.app.BuildConfig
@HiltAndroidApp
class YourApplication : Application() {

    @Inject lateinit var devClass: DevClass

    override fun onCreate() {
        super.onCreate()
        Clogger.i("DevClass","Application started")

        // THIS is your debug‐guard
        if (BuildConfig.DEBUG) {
            CoroutineScope(Dispatchers.IO).launch {
                devClass.init()
            }
            Clogger.i("DevClass","Dev data seeded (DEBUG only)")
        }
    }
}
