package com.ggetters.app

import android.app.Application
import android.util.Log
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.ggetters.app.core.sync.SyncScheduler
import com.ggetters.app.core.utils.Clogger
import com.ggetters.app.core.utils.DevClass
import com.ggetters.app.data.local.DatabaseMaintenance
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@HiltAndroidApp
class YourApplication : Application(), Configuration.Provider {

    @Inject lateinit var devClass: DevClass
    @Inject lateinit var databaseMaintenance: DatabaseMaintenance
    @Inject lateinit var workerFactory: HiltWorkerFactory

    // Provide WorkManager's configuration via property (no separate function needed)
    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .setMinimumLoggingLevel(if (BuildConfig.DEBUG) Log.VERBOSE else Log.INFO)
            .build()

    override fun onCreate() {
        super.onCreate()
        Clogger.i("DevClass", "Application started")

        if (BuildConfig.DEBUG) {
            // schedule periodic + one-time kick
            SyncScheduler.schedule(this)
            Clogger.i("DevClass", "SyncScheduler scheduled (DEBUG only)")

            // clean legacy DB (once)
            databaseMaintenance.deleteLegacyDbIfNeeded()
            Clogger.i("DevClass", "Legacy database deleted if it existed")

            // dev bootstrap
            CoroutineScope(Dispatchers.IO).launch { devClass.init() }
            Clogger.i("DevClass", "Dev data seeded (DEBUG only)")
        }
    }
}
