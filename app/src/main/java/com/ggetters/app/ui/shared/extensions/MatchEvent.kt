// app/src/main/java/com/ggetters/app/ui/shared/extensions/MatchEventExtensions.kt
package com.ggetters.app.ui.shared.extensions

import com.ggetters.app.data.model.MatchEvent
import com.ggetters.app.data.model.MatchEventType

fun MatchEvent.getFormattedTime(): String = "${minute}'"

fun MatchEvent.getEventDescription(): String = when (eventType) {
    MatchEventType.GOAL -> "Goal by ${playerName ?: "Unknown"}"
    MatchEventType.YELLOW_CARD -> "Yellow card for ${playerName ?: "Unknown"}"
    MatchEventType.RED_CARD -> "Red card for ${playerName ?: "Unknown"}"
    MatchEventType.SUBSTITUTION -> {
        val out = details["playerOut"] as? String ?: "Unknown"
        val inP = details["playerIn"] as? String ?: "Unknown"
        "$inP ↔ $out"
    }
    MatchEventType.MATCH_START -> "Match started"
    MatchEventType.MATCH_END -> "Match ended"
    MatchEventType.HALF_TIME -> "Half time"
    MatchEventType.SCORE_UPDATE -> {
        val home = details["homeScore"] as? Int ?: 0
        val away = details["awayScore"] as? Int ?: 0
        "Score updated: $home - $away"
    }
}
