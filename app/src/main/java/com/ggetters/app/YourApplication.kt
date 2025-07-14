package com.ggetters.app

import android.app.Application
import com.ggetters.app.core.utils.Clogger
import com.ggetters.app.core.utils.DevClass
import dagger.hilt.android.HiltAndroidApp
import jakarta.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import android.content.pm.ApplicationInfo

@HiltAndroidApp
class YourApplication : Application() {
    @Inject lateinit var devClass: DevClass

    override fun onCreate() {
        super.onCreate()

        val isDebuggable = (applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
        if (isDebuggable) {
            CoroutineScope(Dispatchers.IO).launch { devClass.init() }
            Clogger.i("YourApplication", "Seeded dev data (DEBUG only)")
        }
    }
}
