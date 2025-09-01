package com.ggetters.app.ui.central.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

    // all events for the active team, coming from Room (offline-first)
    val allEvents: StateFlow<List<Event>> =
        eventsRepo.all()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // events visible in the currently displayed month
    val eventsThisMonth: Flow<List<Event>> = combine(allEvents, currentMonth) { list, ym ->
        list.filter { YearMonth.from(it.startAt.atZone(zone)) == ym }
            .sortedBy { it.startAt }
    }

    private val _selectedDate = MutableStateFlow<LocalDate?>(LocalDate.now())
    val selectedDate: StateFlow<LocalDate?> = _selectedDate

    // events for the selected grid day
    val dayEvents: Flow<List<Event>> = combine(allEvents, selectedDate) { list, date ->
        date?.let { d ->
            list.filter { it.startAt.atZone(zone).toLocalDate() == d }
                .sortedBy { it.startAt }
        } ?: emptyList()
    }

    // “Due soon”: next 14 days, top 10
    val dueSoon: Flow<List<Event>> = allEvents.map { list ->
        val now  = Instant.now()
        val soon = now.plus(14, ChronoUnit.DAYS)
        list.filter { it.startAt.isAfter(now as ChronoLocalDateTime<*>?) && it.startAt.isBefore(soon as ChronoLocalDateTime<*>?) }
            .sortedBy { it.startAt }
            .take(10)
    }

    fun goPrevMonth() { _currentMonth.update { it.minusMonths(1) } }
    fun goNextMonth() { _currentMonth.update { it.plusMonths(1) } }
    fun select(date: LocalDate?) { _selectedDate.value = date }

    /** Push dirty → pull fresh (writes to teams/{teamId}/events/{eventId}) */
    fun refresh() = viewModelScope.launch { eventsRepo.sync() }
}