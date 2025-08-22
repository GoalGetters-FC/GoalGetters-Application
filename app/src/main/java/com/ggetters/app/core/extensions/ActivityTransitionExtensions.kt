package com.ggetters.app.core.extensions

import android.app.Activity
import android.content.Intent
import com.ggetters.app.R

/** Centralized helpers for smooth activity transitions with consistent animations. */
fun Activity.navigateToActivity(intent: Intent, finishCurrent: Boolean = false, clearTask: Boolean = false) {
    startActivity(intent)
    if (clearTask) {
        finishAffinity()
    } else if (finishCurrent) {
        finish()
    }
    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
}

fun Activity.navigateBack() {
    finish()
    overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
}

fun Activity.navigateWithFade(intent: Intent, finishCurrent: Boolean = false, clearTask: Boolean = false) {
    startActivity(intent)
    if (clearTask) {
        finishAffinity()
    } else if (finishCurrent) {
        finish()
    }
    overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
}


