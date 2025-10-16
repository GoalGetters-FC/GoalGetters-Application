package com.ggetters.app.ui.central.fragments

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.ggetters.app.R
import com.ggetters.app.ui.central.models.EventFormData
import com.google.android.material.textfield.TextInputEditText
import java.text.SimpleDateFormat
import java.util.*
import java.time.LocalDate
import java.time.LocalTime

class GameEventFragment : Fragment() {

    private var selectedDate: Date? = null
    private var selectedStartTime: String? = null
    private var selectedEndTime: String? = null
    private var selectedMeetingTime: String? = null

    private var date: LocalDate? = null
    private var start: LocalTime? = null
    private var end: LocalTime? = null
    private var meet: LocalTime? = null


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_event_form_game, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupDateTimePickers(view)
    }


    private fun setupDateTimePickers(view: View) {
        val dateInput = view.findViewById<TextInputEditText>(R.id.gameDateInput)
        val startTimeInput = view.findViewById<TextInputEditText>(R.id.gameStartTimeInput)
        val endTimeInput = view.findViewById<TextInputEditText>(R.id.gameEndTimeInput)
        val meetingTimeInput = view.findViewById<TextInputEditText>(R.id.gameMeetingTimeInput)

        // Date picker
        dateInput.setOnClickListener {
            val calendar = Calendar.getInstance()
            DatePickerDialog(
                requireContext(),
                { _, year, month, dayOfMonth ->
                    date = LocalDate.of(year, month + 1, dayOfMonth) // ✅ set var
                    dateInput.setText(date.toString())
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        // Start time picker
        startTimeInput.setOnClickListener {
            val calendar = Calendar.getInstance()
            TimePickerDialog(
                requireContext(),
                { _, hour, minute ->
                    start = LocalTime.of(hour, minute) // ✅ set var
                    startTimeInput.setText(start.toString())
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
            ).show()
        }

        // End time picker
        endTimeInput.setOnClickListener {
            val calendar = Calendar.getInstance()
            TimePickerDialog(
                requireContext(),
                { _, hour, minute ->
                    end = LocalTime.of(hour, minute) // ✅ set var
                    endTimeInput.setText(end.toString())
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
            ).show()
        }

        // Meeting time picker
        meetingTimeInput.setOnClickListener {
            val calendar = Calendar.getInstance()
            TimePickerDialog(
                requireContext(),
                { _, hour, minute ->
                    meet = LocalTime.of(hour, minute) // ✅ set var
                    meetingTimeInput.setText(meet.toString())
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
            ).show()
        }
    }

    private fun showDatePicker(onDateSelected: (Date) -> Unit) {
        val calendar = Calendar.getInstance()
        selectedDate?.let { calendar.time = it }
        
        DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                calendar.set(year, month, dayOfMonth)
                onDateSelected(calendar.time)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun showTimePicker(onTimeSelected: (String) -> Unit) {
        val calendar = Calendar.getInstance()
        
        TimePickerDialog(
            requireContext(),
            { _, hourOfDay, minute ->
                val time = String.format("%02d:%02d", hourOfDay, minute)
                onTimeSelected(time)
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true
        ).show()
    }

    fun collectFormData(): EventFormData {
        val opponent = view?.findViewById<TextInputEditText>(R.id.gameOpponentInput)
            ?.text?.toString()?.trim()
        
        // Create title from opponent name if provided
        val title = view?.findViewById<TextInputEditText>(R.id.gameTitleInput)
            ?.text?.toString()?.trim()
            ?.ifBlank { 
                opponent?.let { "vs $it" } ?: "Match"
            } ?: "Match"

        return EventFormData(
            title = title,
            description = view?.findViewById<TextInputEditText>(R.id.gameDescriptionInput)
                ?.text?.toString(),
            location = view?.findViewById<TextInputEditText>(R.id.gameLocationInput)
                ?.text?.toString(),
            date = date,
            start = start,
            end = end,
            meet = meet
        )
    }

} 