package com.ggetters.app.ui.central.sheets

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.RadioGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.textfield.TextInputEditText
import com.ggetters.app.R
import com.ggetters.app.data.model.Event
import com.ggetters.app.data.model.EventCategory
import com.ggetters.app.data.model.EventStyle
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*

class AddEventBottomSheet : BottomSheetDialogFragment() {

    private var selectedDate: Date? = null
    private var selectedTime: String? = null
    private var onEventCreatedListener: ((Event) -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.bottom_sheet_add_event, container, false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.BottomSheetDialogFragment)
        setEnterTransition(android.transition.Fade())
        setExitTransition(android.transition.Fade())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupDateTimePickers(view)
        setupEventTypeListener(view)
        setupActionButtons(view)
    }

    private fun setupDateTimePickers(view: View) {
        val dateInput = view.findViewById<TextInputEditText>(R.id.eventDateInput)
        val timeInput = view.findViewById<TextInputEditText>(R.id.eventTimeInput)

        dateInput.setOnClickListener {
            showDatePicker { date ->
                selectedDate = date
                dateInput.setText(SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(date))
            }
        }

        timeInput.setOnClickListener {
            showTimePicker { time ->
                selectedTime = time
                timeInput.setText(time)
            }
        }

        selectedDate?.let { date ->
            dateInput.setText(SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(date))
        }
    }

    private fun setupEventTypeListener(view: View) {
        val eventTypeRadioGroup = view.findViewById<RadioGroup>(R.id.eventTypeRadioGroup)
        val opponentLayout = view.findViewById<LinearLayout>(R.id.opponentLayout)
        
        eventTypeRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.gameRadio -> {
                    opponentLayout.visibility = View.VISIBLE
                }
                else -> {
                    opponentLayout.visibility = View.GONE
                }
            }
        }
    }

    private fun setupActionButtons(view: View) {
        val cancelButton = view.findViewById<Button>(R.id.cancelButton)
        val saveButton = view.findViewById<Button>(R.id.saveButton)

        cancelButton.setOnClickListener { dismiss() }

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
                onTimeSelected(String.format("%02d:%02d", hourOfDay, minute))
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
        val eventTypeRadioGroup = view.findViewById<RadioGroup>(R.id.eventTypeRadioGroup)

        var isValid = true
        
        // Clear previous errors
        titleInput.error = null
        dateInput.error = null
        timeInput.error = null
        venueInput.error = null
        opponentInput.error = null
        
        // Validate required fields
        if (dateInput.text.isNullOrBlank()) { dateInput.error = "Date required"; isValid = false }
        if (timeInput.text.isNullOrBlank()) { timeInput.error = "Time required"; isValid = false }
        if (venueInput.text.isNullOrBlank()) { venueInput.error = "Venue required"; isValid = false }
        
        // For games, validate opponent field
        val isGame = eventTypeRadioGroup.checkedRadioButtonId == R.id.gameRadio
        if (isGame && opponentInput.text.isNullOrBlank()) {
            opponentInput.error = "Opponent required for games"
            isValid = false
        }
        
        return isValid
    }

    private fun saveEvent(view: View): Event {
        val titleInput = view.findViewById<TextInputEditText>(R.id.eventTitleInput)
        val venueInput = view.findViewById<TextInputEditText>(R.id.eventVenueInput)
        val descriptionInput = view.findViewById<TextInputEditText>(R.id.eventDescriptionInput)
        val opponentInput = view.findViewById<TextInputEditText>(R.id.eventOpponentInput)
        val eventTypeRadioGroup = view.findViewById<RadioGroup>(R.id.eventTypeRadioGroup)

        val category = when (eventTypeRadioGroup.checkedRadioButtonId) {
            R.id.practiceRadio -> EventCategory.PRACTICE
            R.id.gameRadio -> EventCategory.MATCH
            R.id.otherRadio -> EventCategory.OTHER
            else -> EventCategory.PRACTICE
        }

        // Create event title based on type and opponent
        val eventTitle = when (category) {
            EventCategory.MATCH -> {
                val opponent = opponentInput.text.toString().trim()
                if (opponent.isNotEmpty()) {
                    "vs $opponent"
                } else {
                    titleInput.text.toString().ifBlank { "Match" }
                }
            }
            else -> titleInput.text.toString().ifBlank { when (category) {
                EventCategory.PRACTICE -> "Practice"
                EventCategory.OTHER -> "Event"
                else -> "Event"
            }}
        }

        val combinedDateTime = LocalDateTime.ofInstant(
            (selectedDate ?: Date()).toInstant(),
            ZoneId.systemDefault()
        ).withHour(
            selectedTime?.substringBefore(":")?.toIntOrNull() ?: 12
        ).withMinute(
            selectedTime?.substringAfter(":")?.toIntOrNull() ?: 0
        )

        return Event(
            id = UUID.randomUUID().toString(),
            createdAt = Date().toInstant(),
            updatedAt = Date().toInstant(),
            stainedAt = null,
            teamId = "teamId_placeholder", // TODO: inject from active team
            creatorId = "userId_placeholder", // TODO: inject from auth
            name = eventTitle,
            description = descriptionInput.text?.toString(),
            category = category,
            style = EventStyle.STANDARD,
            startAt = combinedDateTime,
            endAt = null,
            location = venueInput.text.toString()
        )
    }

    fun setSelectedDate(date: Date) { selectedDate = date }
    fun setOnEventCreatedListener(listener: (Event) -> Unit) { onEventCreatedListener = listener }
}
