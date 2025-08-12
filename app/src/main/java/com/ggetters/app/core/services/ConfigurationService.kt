package com.ggetters.app.core.services

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/**
 * Service to interact with the application DataStore.
 */
class ConfigurationService
@Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val TAG = "ConfigurationService"
    }
}