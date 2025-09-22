// app/src/main/java/com/ggetters/app/ui/extensions/RSVPExtensions.kt
package com.ggetters.app.ui.shared.extensions

import com.ggetters.app.data.model.RSVPStatus
import com.ggetters.app.R

fun RSVPStatus.getDisplayText(): String = when (this) {
    RSVPStatus.AVAILABLE -> "Available"
    RSVPStatus.MAYBE -> "Maybe"
    RSVPStatus.UNAVAILABLE -> "Unavailable"
    RSVPStatus.NOT_RESPONDED -> "No Response"
}

fun RSVPStatus.getIcon(): String = when (this) {
    RSVPStatus.AVAILABLE -> "✅"
    RSVPStatus.MAYBE -> "❓"
    RSVPStatus.UNAVAILABLE -> "❌"
    RSVPStatus.NOT_RESPONDED -> "⭕"
}

fun RSVPStatus.getColorRes(): Int = when (this) {
    RSVPStatus.AVAILABLE -> R.color.success
    RSVPStatus.MAYBE -> R.color.warning
    RSVPStatus.UNAVAILABLE -> R.color.error
    RSVPStatus.NOT_RESPONDED -> R.color.text_tertiary
}
