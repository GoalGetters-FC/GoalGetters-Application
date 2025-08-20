package com.ggetters.app.core.extensions

import android.app.Activity
import android.content.Intent
import com.ggetters.app.R

/**
 * Centralized helpers for smooth activity transitions with consistent animations.
 * Provides directional transitions based on navigation flow.
 */

/**
 * Navigate to an activity with forward slide animation (new activity slides in from right)
 */
fun Activity.navigateToActivity(intent: Intent, finishCurrent: Boolean = false) {
    startActivity(intent)
    if (finishCurrent) {
        finish()
    }
    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
}

/**
 * Navigate back to previous activity with backward slide animation (current slides out to right)
 */
fun Activity.navigateBack() {
    finish()
    overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
}

/**
 * Navigate to an activity with fade transition (for modal-like screens)
 */
fun Activity.navigateWithFade(intent: Intent, finishCurrent: Boolean = false) {
    startActivity(intent)
    if (finishCurrent) {
        finish()
    }
    overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
}

/**
 * Navigate to an activity with scale transition (for detail screens)
 */
fun Activity.navigateWithScale(intent: Intent, finishCurrent: Boolean = false) {
    startActivity(intent)
    if (finishCurrent) {
        finish()
    }
    overridePendingTransition(R.anim.scale_in, R.anim.scale_out)
}

/**
 * Navigate to an activity with slide up transition (for bottom sheet-like screens)
 */
fun Activity.navigateWithSlideUp(intent: Intent, finishCurrent: Boolean = false) {
    startActivity(intent)
    if (finishCurrent) {
        finish()
    }
    overridePendingTransition(R.anim.slide_up, R.anim.fade_out)
}

/**
 * Navigate to an activity with slide down transition (for top sheet-like screens)
 */
fun Activity.navigateWithSlideDown(intent: Intent, finishCurrent: Boolean = false) {
    startActivity(intent)
    if (finishCurrent) {
        finish()
    }
    overridePendingTransition(R.anim.slide_down, R.anim.fade_out)
}
