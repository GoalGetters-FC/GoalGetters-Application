package com.ggetters.app

import android.app.Application
import com.ggetters.app.core.utils.Clogger
import com.ggetters.app.core.utils.DevClass
import com.ggetters.app.data.local.DatabaseMaintenance
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@HiltAndroidApp
class YourApplication : Application() {

    @Inject lateinit var devClass: DevClass
    @Inject lateinit var databaseMaintenance: DatabaseMaintenance

    override fun onCreate() {
        super.onCreate()
        Clogger.i("DevClass", "Application started")

        if (BuildConfig.DEBUG) {
            databaseMaintenance.deleteLegacyDbIfNeeded()
            Clogger.i("DevClass", "Legacy database deleted if it existed")

            CoroutineScope(Dispatchers.IO).launch {
                devClass.init()
            }
            Clogger.i("DevClass", "Dev data seeded (DEBUG only)")
        }
    }
}
