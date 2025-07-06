package com.ggetters.app.ui.central.sheets

import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.ggetters.app.R
import com.ggetters.app.ui.central.adapters.EventAdapter
import com.ggetters.app.ui.central.models.Event
import com.ggetters.app.ui.central.models.EventType
import java.text.SimpleDateFormat
import java.util.*

class EventListBottomSheet : BottomSheetDialogFragment() {
    
    private var selectedDay: Int = 0
    private var events: List<Event> = emptyList()
    private lateinit var eventAdapter: EventAdapter
    
    companion object {
        private const val ARG_DAY = "day"
        private const val ARG_EVENTS = "events"
        
        fun newInstance(day: Int, events: List<Event>): EventListBottomSheet {
            val fragment = EventListBottomSheet()
            val args = Bundle()
            args.putInt(ARG_DAY, day)
            args.putParcelableArrayList(ARG_EVENTS, ArrayList(events.map { EventParcelable.fromEvent(it) }))
            fragment.arguments = args
            return fragment
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.BottomSheetDialogFragment)
        
        arguments?.let { args ->
            selectedDay = args.getInt(ARG_DAY)
            val eventParcelables = args.getParcelableArrayList<EventParcelable>(ARG_EVENTS) ?: emptyList()
            events = eventParcelables.map { it.toEvent() }
        }
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.bottom_sheet_event_list, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupViews(view)
        setupEventList(view)
    }
    
    private fun setupViews(view: View) {
        val dateText = view.findViewById<TextView>(R.id.dateText)
        val eventCountText = view.findViewById<TextView>(R.id.eventCountText)
        val addEventButton = view.findViewById<Button>(R.id.addEventButton)
        
        // Format the date
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, selectedDay)
        val dateFormat = SimpleDateFormat("EEEE, MMMM d", Locale.getDefault())
        dateText.text = dateFormat.format(calendar.time)
        
        // Set event count
        eventCountText.text = "${events.size} event${if (events.size != 1) "s" else ""}"
        
        addEventButton.setOnClickListener {
            dismiss()
            // TODO: Open add event bottom sheet for this day
        }
    }
    
    private fun setupEventList(view: View) {
        val recyclerView = view.findViewById<RecyclerView>(R.id.eventsRecyclerView)
        eventAdapter = EventAdapter(
            onEventClick = { event ->
                showEventDetails(event)
            },
            onEventLongClick = { event ->
                showEventOptions(event)
            }
        )
        
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = eventAdapter
        eventAdapter.updateEvents(events)
    }
    
    private fun showEventDetails(event: Event) {
        val eventDetailsBottomSheet = EventDetailsBottomSheet.newInstance(event)
        eventDetailsBottomSheet.show(childFragmentManager, "EventDetailsBottomSheet")
    }
    
    private fun showEventOptions(event: Event) {
        // TODO: Show options menu (edit, delete, share)
    }
}

// Parcelable wrapper for Event to pass through Bundle
data class EventParcelable(
    val id: String,
    val title: String,
    val type: String,
    val date: Long,
    val time: String,
    val venue: String,
    val opponent: String?,
    val description: String?,
    val createdBy: String,
    val createdAt: Long
) : Parcelable {
    
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readLong(),
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString(),
        parcel.readString(),
        parcel.readString() ?: "",
        parcel.readLong()
    )
    
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(title)
        parcel.writeString(type)
        parcel.writeLong(date)
        parcel.writeString(time)
        parcel.writeString(venue)
        parcel.writeString(opponent)
        parcel.writeString(description)
        parcel.writeString(createdBy)
        parcel.writeLong(createdAt)
    }
    
    override fun describeContents(): Int {
        return 0
    }
    
    companion object {
        @JvmField
        val CREATOR = object : Parcelable.Creator<EventParcelable> {
            override fun createFromParcel(parcel: Parcel): EventParcelable {
                return EventParcelable(parcel)
            }
            
            override fun newArray(size: Int): Array<EventParcelable?> {
                return arrayOfNulls(size)
            }
        }
        
        fun fromEvent(event: Event): EventParcelable {
            return EventParcelable(
                id = event.id,
                title = event.title,
                type = event.type.name,
                date = event.date.time,
                time = event.time,
                venue = event.venue,
                opponent = event.opponent,
                description = event.description,
                createdBy = event.createdBy,
                createdAt = event.createdAt.time
            )
        }
    }
    
    fun toEvent(): Event {
        return Event(
            id = id,
            title = title,
            type = EventType.valueOf(type),
            date = Date(date),
            time = time,
            venue = venue,
            opponent = opponent,
            description = description,
            createdBy = createdBy,
            createdAt = Date(createdAt)
        )
    }
} 