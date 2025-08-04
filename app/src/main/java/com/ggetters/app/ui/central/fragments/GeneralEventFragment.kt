package com.ggetters.app.ui.central.fragments

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.ggetters.app.R
import com.google.android.material.textfield.TextInputEditText
import java.text.SimpleDateFormat
import java.util.*

class GeneralEventFragment : Fragment() {

    private var selectedDate: Date? = null
    private var selectedStartTime: String? = null
    private var selectedEndTime: String? = null
    private var selectedMeetingTime: String? = null

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
        setDefaultTimes(view)
    }

    private fun setupDateTimePickers(view: View) {
        val dateInput = view.findViewById<TextInputEditText>(R.id.eventDateInput)
        val startTimeInput = view.findViewById<TextInputEditText>(R.id.eventStartTimeInput)
        val endTimeInput = view.findViewById<TextInputEditText>(R.id.eventEndTimeInput)
        val meetingTimeInput = view.findViewById<TextInputEditText>(R.id.eventMeetingTimeInput)

        // Date picker
        dateInput.setOnClickListener {
            showDatePicker { date ->
                selectedDate = date
                val dateFormat = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())
                dateInput.setText(dateFormat.format(date))
            }
        }

        // Start time picker
        startTimeInput.setOnClickListener {
            showTimePicker { time ->
                selectedStartTime = time
                startTimeInput.setText(time)
            }
        }

        // End time picker
        endTimeInput.setOnClickListener {
            showTimePicker { time ->
                selectedEndTime = time
                endTimeInput.setText(time)
            }
        }

        // Meeting time picker
        meetingTimeInput.setOnClickListener {
            showTimePicker { time ->
                selectedMeetingTime = time
                meetingTimeInput.setText(time)
            }
        }
    }

    private fun setDefaultTimes(view: View) {
        // Set different default times for general events as shown in the image
        val startTimeInput = view.findViewById<TextInputEditText>(R.id.eventStartTimeInput)
        val endTimeInput = view.findViewById<TextInputEditText>(R.id.eventEndTimeInput)
        val meetingTimeInput = view.findViewById<TextInputEditText>(R.id.eventMeetingTimeInput)

        startTimeInput.setText("11:00")
        endTimeInput.setText("11:40")
        meetingTimeInput.setText("12:00")
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
} 