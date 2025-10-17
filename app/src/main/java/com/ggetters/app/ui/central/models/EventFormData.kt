package com.ggetters.app.ui.central.models

import java.time.LocalDate
import java.time.LocalTime

data class EventFormData(
    val title: String,
    val description: String?,
    val location: String?,
    val opponent: String?,
    val date: LocalDate?,
    val start: LocalTime?,
    val end: LocalTime?,
    val meet: LocalTime?
)
