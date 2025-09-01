package com.ggetters.app.ui.central.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ggetters.app.core.utils.Clogger
import com.ggetters.app.data.model.Event
import com.ggetters.app.data.repository.event.EventRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.chrono.ChronoLocalDateTime
import java.time.temporal.ChronoUnit
import javax.inject.Inject

@HiltViewModel
class HomeCalendarViewModel @Inject constructor(
    private val eventsRepo: EventRepository,
) : ViewModel() {

    private val zone = ZoneId.systemDefault()
    private val _currentMonth = MutableStateFlow(YearMonth.now())
    val currentMonth: StateFlow<YearMonth> = _currentMonth

    // all events for the active team
    val allEvents: StateFlow<List<Event>> =
        eventsRepo.all()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init {
        viewModelScope.launch {
            allEvents.collect { list ->
                Clogger.i("HomeCalendarVM", "allEvents updated: ${list.size} events")
            }
        }
    }

    val eventsThisMonth: Flow<List<Event>> = combine(allEvents, currentMonth) { list, ym ->
        val filtered = list.filter { YearMonth.from(it.startAt.atZone(zone)) == ym }
        Clogger.i("HomeCalendarVM", "eventsThisMonth for $ym: ${filtered.size} events")
        filtered.sortedBy { it.startAt }
    }

    private val _selectedDate = MutableStateFlow<LocalDate?>(LocalDate.now())
    val selectedDate: StateFlow<LocalDate?> = _selectedDate

    val dayEvents: Flow<List<Event>> = combine(allEvents, selectedDate) { list, date ->
        date?.let { d ->
            val filtered = list.filter { it.startAt.atZone(zone).toLocalDate() == d }
            Clogger.i("HomeCalendarVM", "dayEvents for $d: ${filtered.size} events")
            filtered.sortedBy { it.startAt }
        } ?: emptyList()
    }

    fun refresh() = viewModelScope.launch {
        Clogger.i("HomeCalendarVM", "refresh() called → syncing events")
        eventsRepo.sync()
    }

    fun goPrevMonth() { _currentMonth.update { it.minusMonths(1) } }
    fun goNextMonth() { _currentMonth.update { it.plusMonths(1) } }
    fun select(date: LocalDate?) { _selectedDate.value = date }
}
