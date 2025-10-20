package com.ggetters.app.ui.central.sheets

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.ggetters.app.R
import com.ggetters.app.data.model.Event
import com.ggetters.app.data.model.EventCategory
import java.time.format.DateTimeFormatter
import java.util.Locale

class EventDetailsBottomSheet : BottomSheetDialogFragment() {

    private var event: Event? = null

    companion object {
        private const val ARG_EVENT = "event"

        fun newInstance(event: Event): EventDetailsBottomSheet {
            val fragment = EventDetailsBottomSheet()
            val args = Bundle()
            args.putSerializable(ARG_EVENT, event) // ✅ Serializable works with your model
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setEnterTransition(android.transition.Fade())
        setExitTransition(android.transition.Fade())

        arguments?.let { args ->
            event = args.getSerializable(ARG_EVENT) as? Event
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.bottom_sheet_event_details, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        event?.let { setupEventDetails(view, it) }
    }

    private fun setupEventDetails(view: View, event: Event) {
        val eventCategory = view.findViewById<TextView>(R.id.eventCategory)
        val eventCategoryIcon = view.findViewById<com.google.android.material.card.MaterialCardView>(R.id.eventCategoryIcon)
        val eventName = view.findViewById<TextView>(R.id.eventName)
        val eventStyle = view.findViewById<TextView>(R.id.eventStyle)
        val eventDate = view.findViewById<TextView>(R.id.eventDate)
        val eventTime = view.findViewById<TextView>(R.id.eventTime)
        val eventLocation = view.findViewById<TextView>(R.id.eventLocation)
        val eventDescription = view.findViewById<TextView>(R.id.eventDescription)
        val eventCreatedBy = view.findViewById<TextView>(R.id.eventCreatedBy)
        val editButton = view.findViewById<com.google.android.material.button.MaterialButton>(R.id.editButton)
        val deleteButton = view.findViewById<com.google.android.material.button.MaterialButton>(R.id.deleteButton)
        val descriptionContainer = view.findViewById<View>(R.id.descriptionContainer)

        // Formatters
        val dateFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy", Locale.getDefault())
        val timeFormatter = DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault())

        // --- Bind Data ---
        eventCategory.text = event.category.displayName
        
        // Set category icon background color based on event type
        when (event.category) {
            EventCategory.MATCH -> {
                eventCategoryIcon.setCardBackgroundColor(android.graphics.Color.parseColor("#FF6B35")) // Orange for games
            }
            EventCategory.PRACTICE -> {
                eventCategoryIcon.setCardBackgroundColor(android.graphics.Color.parseColor("#4CAF50")) // Green for practice
            }
            EventCategory.TRAINING -> {
                eventCategoryIcon.setCardBackgroundColor(android.graphics.Color.parseColor("#FF9800")) // Orange for training
            }
            EventCategory.OTHER -> {
                eventCategoryIcon.setCardBackgroundColor(android.graphics.Color.parseColor("#2196F3")) // Blue for other events
            }
        }
        
        eventName.text = event.name
        eventStyle.text = event.style.displayName
        eventDate.text = dateFormatter.format(event.startAt)
        
        // Show time range if end time is available, otherwise just start time
        val timeText = if (event.endAt != null) {
            "${timeFormatter.format(event.startAt)} - ${timeFormatter.format(event.endAt)}"
        } else {
            timeFormatter.format(event.startAt)
        }
        eventTime.text = timeText
        
        eventLocation.text = event.location ?: "No location"
        eventCreatedBy.text = "Created by ${event.creatorId ?: "Unknown"}"

        if (!event.description.isNullOrBlank()) {
            descriptionContainer.visibility = View.VISIBLE
            eventDescription.text = event.description
        } else {
            descriptionContainer.visibility = View.GONE
            eventDescription.text = "" // Clear text to prevent data leakage
        }

        // Edit/Delete buttons (stubbed for now)
        editButton.setOnClickListener {
            // TODO: Open edit event bottom sheet
            dismiss()
        }

        deleteButton.setOnClickListener {
            // TODO: Confirm + delete event
            dismiss()
        }
    }
}
