package com.ggetters.app.ui.central.fragments

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.ggetters.app.R
import com.ggetters.app.core.utils.Clogger
import com.ggetters.app.ui.central.models.EventFormData
import com.google.android.material.textfield.TextInputEditText
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalTime
import java.util.*

class PracticeEventFragment : Fragment() {

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
        return inflater.inflate(R.layout.fragment_event_form_practice, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupDateTimePickers(view)
    }

    private fun setupDateTimePickers(view: View) {
        val dateInput = view.findViewById<TextInputEditText>(R.id.practiceDateInput)
        val startTimeInput = view.findViewById<TextInputEditText>(R.id.practiceStartTimeInput)
        val endTimeInput = view.findViewById<TextInputEditText>(R.id.practiceEndTimeInput)
        val meetingTimeInput = view.findViewById<TextInputEditText>(R.id.practiceMeetingTimeInput)

        // Date picker
        dateInput.setOnClickListener {
            showDatePicker { selectedDate ->
                this.selectedDate = selectedDate
                this.date = LocalDate.of(
                    selectedDate.year + 1900, // Date.getYear() returns year - 1900
                    selectedDate.month + 1,   // Date.getMonth() returns 0-11
                    selectedDate.date
                )
                val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                dateInput.setText(formatter.format(selectedDate))
            }
        }

        // Start time picker
        startTimeInput.setOnClickListener {
            showTimePicker { hour, minute ->
                this.selectedStartTime = String.format("%02d:%02d", hour, minute)
                this.start = LocalTime.of(hour, minute)
                startTimeInput.setText(selectedStartTime)
            }
        }

        // End time picker
        endTimeInput.setOnClickListener {
            showTimePicker { hour, minute ->
                this.selectedEndTime = String.format("%02d:%02d", hour, minute)
                this.end = LocalTime.of(hour, minute)
                endTimeInput.setText(selectedEndTime)
            }
        }

        // Meeting time picker
        meetingTimeInput.setOnClickListener {
            showTimePicker { hour, minute ->
                this.selectedMeetingTime = String.format("%02d:%02d", hour, minute)
                this.meet = LocalTime.of(hour, minute)
                meetingTimeInput.setText(selectedMeetingTime)
            }
        }
    }

    private fun showDatePicker(onDateSelected: (Date) -> Unit) {
        val calendar = Calendar.getInstance()
        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                val selectedDate = Calendar.getInstance().apply {
                    set(year, month, dayOfMonth)
                }.time
                onDateSelected(selectedDate)
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
        Clogger.i("PracticeEventFragment", "collectFormData() called")
        
        val title = view?.findViewById<TextInputEditText>(R.id.practiceTitleInput)
            ?.text?.toString()?.trim()
        val description = view?.findViewById<TextInputEditText>(R.id.practiceDescriptionInput)
            ?.text?.toString()?.trim()
        val location = view?.findViewById<TextInputEditText>(R.id.practiceLocationInput)
            ?.text?.toString()?.trim()
            
        Clogger.i("PracticeEventFragment", "Collected: title='$title', description='$description', location='$location'")
        Clogger.i("PracticeEventFragment", "Date/Time: date=$date, start=$start, end=$end, meet=$meet")
        
        // Validate required fields
        if (title.isNullOrBlank()) {
            Clogger.e("PracticeEventFragment", "❌ Title is blank")
            view?.findViewById<TextInputEditText>(R.id.practiceTitleInput)?.error = "Title is required"
            return null
        }
        
        if (date == null) {
            Clogger.e("PracticeEventFragment", "❌ Date is null")
            view?.findViewById<TextInputEditText>(R.id.practiceDateInput)?.error = "Date is required"
            return null
        }
        
        if (start == null) {
            Clogger.e("PracticeEventFragment", "❌ Start time is null")
            view?.findViewById<TextInputEditText>(R.id.practiceStartTimeInput)?.error = "Start time is required"
            return null
        }
        
        // Validate end time is after start time if provided
        val startTime = start
        val endTime = end
        if (endTime != null && startTime != null && endTime.isBefore(startTime)) {
            view?.findViewById<TextInputEditText>(R.id.practiceEndTimeInput)?.error = "End time must be after start time"
            return null
        }
        
        Clogger.i("PracticeEventFragment", "✅ All validation passed, creating EventFormData")
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