// data/local/DatabaseMaintenance.kt
package com.ggetters.app.data.local

import android.content.Context
import androidx.annotation.VisibleForTesting
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DatabaseMaintenance @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs by lazy {
        context.getSharedPreferences("db_maintenance", Context.MODE_PRIVATE)
    }

    @VisibleForTesting
    fun deleteLegacyDbIfNeeded() {
        if (prefs.getBoolean("legacy_db_deleted", false)) return
        context.deleteDatabase("ggetters.db")
        context.deleteDatabase("ggetters.sqlite")
        prefs.edit().putBoolean("legacy_db_deleted", true).apply()
    }
}
