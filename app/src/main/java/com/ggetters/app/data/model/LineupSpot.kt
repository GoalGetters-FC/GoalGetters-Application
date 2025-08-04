package com.ggetters.app.data.model

data class LineupSpot(
    val userId: String,           // reference to Player
    val number: Int,              // jersey number
    val position: String,         // e.g. "CB", "ST", "GK"
    val role: SpotRole            // STARTER, BENCH, RESERVE
)
