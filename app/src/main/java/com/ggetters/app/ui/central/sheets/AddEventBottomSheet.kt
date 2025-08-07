package com.ggetters.app.ui.central.sheets

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.RadioButton
import android.widget.RadioGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.textfield.TextInputEditText
import com.ggetters.app.R
import com.ggetters.app.ui.central.models.Event
import com.ggetters.app.ui.central.models.EventType
import java.text.SimpleDateFormat
import java.util.*

class AddEventBottomSheet : BottomSheetDialogFragment() {
    
    private var selectedDate: Date? = null
    private var selectedTime: String? = null
    private var onEventCreatedListener: ((Event) -> Unit)? = null
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.bottom_sheet_add_event, container, false)
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.BottomSheetDialogFragment)
        
        // Enable smooth bottom sheet transitions
        setEnterTransition(android.transition.Fade())
        setExitTransition(android.transition.Fade())
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupViews(view)
        setupEventTypeListener(view)
        setupDateTimePickers(view)
        setupActionButtons(view)
    }
    
    private fun setupViews(view: View) {
        // Set default event type
        val practiceRadio = view.findViewById<RadioButton>(R.id.practiceRadio)
        practiceRadio.isChecked = true
    }
    
    private fun setupEventTypeListener(view: View) {
        val eventTypeRadioGroup = view.findViewById<RadioGroup>(R.id.eventTypeRadioGroup)
        val opponentLayout = view.findViewById<View>(R.id.opponentLayout)
        
        eventTypeRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.gameRadio -> opponentLayout.visibility = View.VISIBLE
                else -> opponentLayout.visibility = View.GONE
            }
        }
    }
    
    private fun setupDateTimePickers(view: View) {
        val dateInput = view.findViewById<TextInputEditText>(R.id.eventDateInput)
        val timeInput = view.findViewById<TextInputEditText>(R.id.eventTimeInput)
        
        dateInput.setOnClickListener {
            showDatePicker { date ->
                selectedDate = date
                val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                dateInput.setText(dateFormat.format(date))
            }
        }
        
        timeInput.setOnClickListener {
            showTimePicker { time ->
                selectedTime = time
                timeInput.setText(time)
            }
        }
        
        // Set default date if provided
        selectedDate?.let { date ->
            val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            dateInput.setText(dateFormat.format(date))
        }
    }
    
    private fun setupActionButtons(view: View) {
        val cancelButton = view.findViewById<Button>(R.id.cancelButton)
        val saveButton = view.findViewById<Button>(R.id.saveButton)
        
        cancelButton.setOnClickListener {
            dismiss()
        }
        
        saveButton.setOnClickListener {
            if (validateForm(view)) {
                val event = saveEvent(view)
                onEventCreatedListener?.invoke(event)
                dismiss()
            }
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
    
    private fun validateForm(view: View): Boolean {
        val titleInput = view.findViewById<TextInputEditText>(R.id.eventTitleInput)
        val dateInput = view.findViewById<TextInputEditText>(R.id.eventDateInput)
        val timeInput = view.findViewById<TextInputEditText>(R.id.eventTimeInput)
        val venueInput = view.findViewById<TextInputEditText>(R.id.eventVenueInput)
        val opponentInput = view.findViewById<TextInputEditText>(R.id.eventOpponentInput)
        
        var isValid = true
        
        if (titleInput.text.isNullOrBlank()) {
            titleInput.error = "Title is required"
            isValid = false
        }
        
        if (dateInput.text.isNullOrBlank()) {
            dateInput.error = "Date is required"
            isValid = false
        }
        
        if (timeInput.text.isNullOrBlank()) {
            timeInput.error = "Time is required"
            isValid = false
        }
        
        if (venueInput.text.isNullOrBlank()) {
            venueInput.error = "Venue is required"
            isValid = false
        }
        
        // Check opponent for games
        val eventTypeRadioGroup = view.findViewById<RadioGroup>(R.id.eventTypeRadioGroup)
        if (eventTypeRadioGroup.checkedRadioButtonId == R.id.gameRadio && opponentInput.text.isNullOrBlank()) {
            opponentInput.error = "Opponent is required for games"
            isValid = false
        }
        
        return isValid
    }
    
    private fun saveEvent(view: View): Event {
        val titleInput = view.findViewById<TextInputEditText>(R.id.eventTitleInput)
        val venueInput = view.findViewById<TextInputEditText>(R.id.eventVenueInput)
        val opponentInput = view.findViewById<TextInputEditText>(R.id.eventOpponentInput)
        val descriptionInput = view.findViewById<TextInputEditText>(R.id.eventDescriptionInput)
        val eventTypeRadioGroup = view.findViewById<RadioGroup>(R.id.eventTypeRadioGroup)
        
        val eventType = when (eventTypeRadioGroup.checkedRadioButtonId) {
            R.id.practiceRadio -> EventType.PRACTICE
            R.id.gameRadio -> EventType.MATCH
            R.id.otherRadio -> EventType.OTHER
            else -> EventType.PRACTICE
        }
        
        val event = Event(
            id = UUID.randomUUID().toString(),
            title = titleInput.text.toString(),
            type = eventType,
            date = selectedDate ?: Date(),
            time = selectedTime ?: "",
            venue = venueInput.text.toString(),
            opponent = if (eventType == EventType.MATCH) opponentInput.text.toString() else null,
            description = descriptionInput.text.toString().takeIf { it.isNotBlank() },
            createdBy = "Current User" // TODO: Get from auth
        )
        
        // TODO: Backend - Save event to database
        // Endpoint: POST /api/events
        // Request: { title, type, date, time, venue, opponent?, description?, teamId }
        // Response: { event: Event, success: Boolean }
        // Error handling: { message: String, code: String }
        
        return event
    }
    
    fun setSelectedDate(date: Date) {
        selectedDate = date
    }
    
    fun setOnEventCreatedListener(listener: (Event) -> Unit) {
        onEventCreatedListener = listener
    }
} 