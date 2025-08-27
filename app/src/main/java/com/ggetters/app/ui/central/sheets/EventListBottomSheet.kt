package com.ggetters.app.ui.central.sheets

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.ggetters.app.R
import com.ggetters.app.data.model.Event
import com.ggetters.app.ui.central.adapters.EventAdapter
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class EventListBottomSheet : BottomSheetDialogFragment() {

    private var selectedDay: Int = 0
    private var events: List<Event> = emptyList()
    private lateinit var eventAdapter: EventAdapter

    companion object {
        private const val ARG_DAY = "day"
        private const val ARG_EVENTS = "events"

        /** Create new instance with selected day + events */
        fun newInstance(day: Int, events: List<Event>): EventListBottomSheet {
            val fragment = EventListBottomSheet()
            val args = Bundle()
            args.putInt(ARG_DAY, day)
            args.putSerializable(ARG_EVENTS, ArrayList(events)) // Event implements Serializable
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.BottomSheetDialogFragment)

        arguments?.let { args ->
            selectedDay = args.getInt(ARG_DAY)
            @Suppress("UNCHECKED_CAST")
            events = args.getSerializable(ARG_EVENTS) as? List<Event> ?: emptyList()
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

    // --- Setup top bar views (date + count + add button)

    private fun setupViews(view: View) {
        val dateText = view.findViewById<TextView>(R.id.dateText)
        val eventCountText = view.findViewById<TextView>(R.id.eventCountText)
        val addEventButton = view.findViewById<Button>(R.id.addEventButton)

        // Format the selected date
        val calendar = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, selectedDay)
        }
        val dateFormat = SimpleDateFormat("EEEE, MMMM d", Locale.getDefault())
        dateText.text = dateFormat.format(calendar.time)

        // Set event count
        eventCountText.text = "${events.size} event${if (events.size != 1) "s" else ""}"

        addEventButton.setOnClickListener {
            dismiss()
            // TODO: Open add event bottom sheet for this day
        }
    }

    // --- Setup list of events

    private fun setupEventList(view: View) {
        val recyclerView = view.findViewById<RecyclerView>(R.id.eventsRecyclerView)
        eventAdapter = EventAdapter(
            onClick = { event -> showEventDetails(event) },
            onLongClick = { event -> showEventOptions(event) }
        )

        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = eventAdapter
        eventAdapter.update(events)
    }

    // --- Show event details bottom sheet

    private fun showEventDetails(event: Event) {
        val eventDetailsBottomSheet = EventDetailsBottomSheet.newInstance(event)
        eventDetailsBottomSheet.show(childFragmentManager, "EventDetailsBottomSheet")
    }

    // --- Placeholder for long-press options (edit, delete, share)

    private fun showEventOptions(event: Event) {
        // TODO: Show options menu (edit, delete, share)
    }
}
