package com.ggetters.app.ui.central.sheets

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.ggetters.app.R
import com.ggetters.app.ui.central.models.Event
import com.ggetters.app.ui.central.models.EventType
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
        setStyle(STYLE_NORMAL, R.style.BottomSheetDialogFragment)

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
        val eventName = view.findViewById<TextView>(R.id.eventName)
        val eventDate = view.findViewById<TextView>(R.id.eventDate)
        val eventTime = view.findViewById<TextView>(R.id.eventTime)
        val eventLocation = view.findViewById<TextView>(R.id.eventLocation)
        val eventDescription = view.findViewById<TextView>(R.id.eventDescription)
        val eventCreatedBy = view.findViewById<TextView>(R.id.eventCreatedBy)
        val editButton = view.findViewById<Button>(R.id.editButton)
        val deleteButton = view.findViewById<Button>(R.id.deleteButton)

        // Formatters
        val dateFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy", Locale.getDefault())
        val timeFormatter = DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault())

        // --- Bind Data ---
        eventCategory.text = event.type.name
        eventName.text = event.title
        eventDate.text = event.getFormattedDate()
        eventTime.text = event.time
        eventLocation.text = event.venue ?: "No location"
        eventCreatedBy.text = "Created by ${event.createdBy ?: "Unknown"}"

        if (!event.description.isNullOrBlank()) {
            eventDescription.visibility = View.VISIBLE
            eventDescription.text = event.description
        } else {
            eventDescription.visibility = View.GONE
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
