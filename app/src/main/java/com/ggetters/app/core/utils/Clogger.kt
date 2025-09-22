package com.ggetters.app.core.utils

import android.util.Log
import com.google.firebase.crashlytics.FirebaseCrashlytics

/**
 * Logger to synchronise reports to Logcat and Crashlytics.
 * 
 * **Note:** If crashlytics fails to initialize, this logger will still function
 * as expected when writing to Logcat. This utility is safe to use in sensitive 
 * environments where robust and reliable logging is required.
 * 
 * @see d
 * @see i
 * @see w
 * @see e
 */
object Clogger {
    private const val TAG = "Clogger"
    
    
    // --- Startup


    /**
     * The instance of [FirebaseCrashlytics] to communicate with the server.
     */
    private var crashlytics: FirebaseCrashlytics? = null
    init {
        try {
            val disable = System.getProperty("DISABLE_CLOGGER") == "true"
            if (!disable) {
                crashlytics = FirebaseCrashlytics.getInstance()
            }
        } catch (e: Exception) {
            // In unit tests, Crashlytics may not be available
            try { Log.e(TAG, "Failed to initialize crashlytics logger", e) } catch (_: Throwable) {}
        }
    }
    
    
    // --- Logging

    
    /**
     * Log an information message.
     * 
     * @see Log.i
     */
    fun i(tag: String, message: String) {
        try { Log.i(tag, message) } catch (_: Throwable) {}
        crashlytics?.log("[I][${tag}]: $message")
    }

    
    /**
     * Log a warning message.
     * 
     * @see Log.w
     */
    fun w(tag: String, message: String) {
        try { Log.w(tag, message) } catch (_: Throwable) {}
        crashlytics?.log("[W][${tag}]: $message")
    }

    
    /**
     * Log a debug message.
     * 
     * @see Log.d
     */
    fun d(tag: String, message: String) {
        try { Log.d(tag, message) } catch (_: Throwable) {}
        crashlytics?.log("[D][${tag}]: $message")
    }

    
    /**
     * Log an error message.
     *
     * **Note:** This function will record a non-fatal exception in Crashlytics
     * if a throwable is provided as a parameter. Otherwise, it will be treated
     * as any other error log.
     * 
     * @see Log.e
     */
    fun e(tag: String, message: String, throwable: Throwable? = null) {
        try {
            if (throwable != null) Log.e(tag, message, throwable) else Log.e(tag, message)
        } catch (_: Throwable) {}
        crashlytics?.log("[E][${tag}]: $message")
        if (throwable != null) {
            crashlytics?.recordException(throwable)
        }
    }
}

