package com.ggetters.app.ui.calendar

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.ggetters.app.R
import com.ggetters.app.ui.models.Event
import com.ggetters.app.ui.models.EventType
import java.text.SimpleDateFormat
import java.util.*

class EventDetailsBottomSheet : BottomSheetDialogFragment() {
    
    private var event: Event? = null
    
    companion object {
        private const val ARG_EVENT = "event"
        
        fun newInstance(event: Event): EventDetailsBottomSheet {
            val fragment = EventDetailsBottomSheet()
            val args = Bundle()
            args.putParcelable(ARG_EVENT, EventParcelable.fromEvent(event))
            fragment.arguments = args
            return fragment
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.BottomSheetDialogFragment)
        
        arguments?.let { args ->
            val eventParcelable = args.getParcelable<EventParcelable>(ARG_EVENT)
            event = eventParcelable?.toEvent()
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
        val eventTypeIcon = view.findViewById<TextView>(R.id.eventTypeIcon)
        val eventTitle = view.findViewById<TextView>(R.id.eventTitle)
        val eventDate = view.findViewById<TextView>(R.id.eventDate)
        val eventTime = view.findViewById<TextView>(R.id.eventTime)
        val eventVenue = view.findViewById<TextView>(R.id.eventVenue)
        val eventOpponent = view.findViewById<TextView>(R.id.eventOpponent)
        val eventDescription = view.findViewById<TextView>(R.id.eventDescription)
        val eventCreatedBy = view.findViewById<TextView>(R.id.eventCreatedBy)
        val editButton = view.findViewById<Button>(R.id.editButton)
        val deleteButton = view.findViewById<Button>(R.id.deleteButton)
        
        // Set event details
        eventTypeIcon.text = event.type.icon
        eventTypeIcon.setTextColor(android.graphics.Color.parseColor(event.type.color))
        eventTitle.text = event.title
        eventDate.text = event.getFormattedDate()
        eventTime.text = event.time
        eventVenue.text = event.venue
        eventCreatedBy.text = "Created by ${event.createdBy}"
        
        // Show opponent for games
        if (event.type == EventType.GAME && !event.opponent.isNullOrBlank()) {
            eventOpponent.visibility = View.VISIBLE
            eventOpponent.text = "vs ${event.opponent}"
        } else {
            eventOpponent.visibility = View.GONE
        }
        
        // Show description if available
        if (!event.description.isNullOrBlank()) {
            eventDescription.visibility = View.VISIBLE
            eventDescription.text = event.description
        } else {
            eventDescription.visibility = View.GONE
        }
        
        // Set button listeners
        editButton.setOnClickListener {
            // TODO: Open edit event bottom sheet
            dismiss()
        }
        
        deleteButton.setOnClickListener {
            // TODO: Show delete confirmation dialog
            dismiss()
        }
    }
} 