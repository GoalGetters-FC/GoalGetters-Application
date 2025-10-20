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
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.*

class OtherEventFragment : Fragment() {

    private var date: LocalDate? = null
    private var start: LocalTime? = null
    private var end: LocalTime? = null
    private var meet: LocalTime? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_event_form_event, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupDateTimePickers(view)
    }

    private fun setupDateTimePickers(view: View) {
        val dateInput = view.findViewById<TextInputEditText>(R.id.eventDateInput)
        val startTimeInput = view.findViewById<TextInputEditText>(R.id.eventStartTimeInput)
        val endTimeInput = view.findViewById<TextInputEditText>(R.id.eventEndTimeInput)
        val meetingTimeInput = view.findViewById<TextInputEditText>(R.id.eventMeetingTimeInput)

        // Date picker
        dateInput.setOnClickListener {
            showDatePicker { year, month, dayOfMonth ->
                this.date = LocalDate.of(year, month, dayOfMonth)
                val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.getDefault())
                dateInput.setText(date?.format(formatter))
            }
        }

        // Start time picker
        startTimeInput.setOnClickListener {
            showTimePicker { hour, minute ->
                this.start = LocalTime.of(hour, minute)
                startTimeInput.setText(String.format("%02d:%02d", hour, minute))
            }
        }

        // End time picker
        endTimeInput.setOnClickListener {
            showTimePicker { hour, minute ->
                this.end = LocalTime.of(hour, minute)
                endTimeInput.setText(String.format("%02d:%02d", hour, minute))
            }
        }

        // Meeting time picker
        meetingTimeInput.setOnClickListener {
            showTimePicker { hour, minute ->
                this.meet = LocalTime.of(hour, minute)
                meetingTimeInput.setText(String.format("%02d:%02d", hour, minute))
            }
        }
    }

    private fun showDatePicker(onDateSelected: (Int, Int, Int) -> Unit) {
        val calendar = Calendar.getInstance()
        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                onDateSelected(year, month + 1, dayOfMonth) // month+1 since Calendar.MONTH is 0-based
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        
        // Set min date to today
        datePickerDialog.datePicker.minDate = System.currentTimeMillis()
        datePickerDialog.show()
    }

    private fun showTimePicker(onTimeSelected: (Int, Int) -> Unit) {
        val calendar = Calendar.getInstance()
        val timePickerDialog = TimePickerDialog(
            requireContext(),
            { _, hourOfDay, minute ->
                onTimeSelected(hourOfDay, minute)
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true // 24-hour format
        )
        timePickerDialog.show()
    }

    fun collectFormData(): EventFormData? {
        val title = view?.findViewById<TextInputEditText>(R.id.eventTitleInput)
            ?.text?.toString()?.trim()
        val description = view?.findViewById<TextInputEditText>(R.id.eventDescriptionInput)
            ?.text?.toString()?.trim()
        val location = view?.findViewById<TextInputEditText>(R.id.eventLocationInput)
            ?.text?.toString()?.trim()
        
        // Validate required fields
        if (title.isNullOrBlank()) {
            view?.findViewById<TextInputEditText>(R.id.eventTitleInput)?.error = "Title is required"
            return null
        }
        
        if (date == null) {
            view?.findViewById<TextInputEditText>(R.id.eventDateInput)?.error = "Date is required"
            return null
        }
        
        if (start == null) {
            view?.findViewById<TextInputEditText>(R.id.eventStartTimeInput)?.error = "Start time is required"
            return null
        }
        
        // Validate end time is after start time if provided
        val startTime = start
        val endTime = end
        if (endTime != null && startTime != null && endTime.isBefore(startTime)) {
            view?.findViewById<TextInputEditText>(R.id.eventEndTimeInput)?.error = "End time must be after start time"
            return null
        }
        
        return EventFormData(
            title = title,
            description = description,
            location = location,
            opponent = null,
            date = date,
            start = start,
            end = end,
            meet = meet
        )
    }
}
