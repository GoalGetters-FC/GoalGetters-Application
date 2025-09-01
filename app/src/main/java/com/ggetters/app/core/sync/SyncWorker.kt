package com.ggetters.app.core.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.ggetters.app.core.utils.Clogger
import com.ggetters.app.data.repository.team.TeamRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val syncManager: SyncManager // ✅ use SyncManager to sync everything
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result = try {
        Clogger.i("Sync", "SyncWorker start")
        syncManager.syncAll()
        Clogger.i("Sync", "SyncWorker done")
        Result.success()
    } catch (e: Exception) {
        Clogger.e("Sync", "SyncWorker failed", e)
        Result.retry()
    }
}
