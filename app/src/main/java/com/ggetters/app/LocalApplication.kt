package com.ggetters.app

import android.app.Application
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.ggetters.app.core.services.GlobalAuthenticationListener
import com.ggetters.app.core.sync.SyncScheduler
import com.ggetters.app.core.utils.Clogger
import com.ggetters.app.core.utils.DevClass
import com.ggetters.app.data.local.DatabaseMaintenance
import com.google.firebase.FirebaseApp
import com.google.firebase.perf.FirebasePerformance
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class LocalApplication : Application(), Configuration.Provider {
    companion object {
        const val TAG = "LocalApplication"
    }


// --- Variables


    @Inject
    lateinit var devClass: DevClass


    @Inject
    lateinit var databaseMaintenance: DatabaseMaintenance


    @Inject
    lateinit var hiltDiWorkerFactory: HiltWorkerFactory


    @Inject
    lateinit var authenticationListener: GlobalAuthenticationListener


    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(hiltDiWorkerFactory)
            .setMinimumLoggingLevel(if (BuildConfig.DEBUG) Log.VERBOSE else Log.INFO)
            .build()


// --- Lifecycle


    override fun onCreate() {
        super.onCreate()
        Clogger.i(
            tag = TAG, message = """
            |
            |    
            |       ____                   _    ____          _     _                        
            |      / ___|   ___     __ _  | |  / ___|   ___  | |_  | |_    ___   _ __   ___  
            |     | |  _   / _ \   / _` | | | | |  _   / _ \ | __| | __|  / _ \ | '__| / __| 
            |     | |_| | | (_) | | (_| | | | | |_| | |  __/ | |_  | |_  |  __/ | |    \__ \ 
            |      \____|  \___/   \__,_| |_|  \____|  \___|  \__|  \__|  \___| |_|    |___/
            |                                                 Brought to you by BankBoosta's
            |
            |
            """.trimIndent()
        )

        // Initialization

        disableSystemThemeHooks()

        initFirebase()
        initAuthHook()
        initDebugApp()
    }


// --- Internals


    private fun disableSystemThemeHooks() =
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)


    private fun initFirebase() {
        FirebaseApp.initializeApp(this)
        FirebasePerformance.getInstance().isPerformanceCollectionEnabled = !BuildConfig.DEBUG
        Clogger.i(
            TAG, "Firebase performance monitoring: ${!BuildConfig.DEBUG}"
        )
    }


    private fun initAuthHook() = authenticationListener.listen()


    private fun initDebugApp() {
        if (BuildConfig.DEBUG) {
            SyncScheduler.schedule(this)
            databaseMaintenance.deleteLegacyDbIfNeeded()
            CoroutineScope(Dispatchers.IO).launch {
                devClass.init()
            }

            Clogger.w(
                TAG, "Seeded development data sets for DEBUG build"
            )
        }
    }
}
