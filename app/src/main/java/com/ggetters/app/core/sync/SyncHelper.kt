package com.ggetters.app.core.sync

import com.ggetters.app.core.utils.Clogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncHelper @Inject constructor(
    private val syncManager: SyncManager
) {

    /** Called by UI (pull-to-refresh, manual refresh buttons, etc.) */
    fun refresh(scope: CoroutineScope, onComplete: (() -> Unit)? = null, onError: ((Throwable) -> Unit)? = null) {
        scope.launch(Dispatchers.IO) {
            try {
                Clogger.i("Sync", "Manual refresh start")
                syncManager.syncAll()
                Clogger.i("Sync", "Manual refresh done")
                onComplete?.invoke()
            } catch (e: Exception) {
                Clogger.e("Sync", "Manual refresh failed", e)
                onError?.invoke(e)
            }
        }
    }
}
