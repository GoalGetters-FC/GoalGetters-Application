package com.ggetters.app.ui.central.sheets

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ggetters.app.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.snackbar.Snackbar

class MatchEventsBottomSheet : BottomSheetDialogFragment() {

    companion object {
        fun newInstance(): MatchEventsBottomSheet {
            return MatchEventsBottomSheet()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.bottom_sheet_match_events, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerView(view)
    }

    private fun setupRecyclerView(view: View) {
        val recyclerView = view.findViewById<RecyclerView>(R.id.eventsRecyclerView)
        val adapter = EventTypesAdapter { eventType ->
            handleEventSelection(eventType)
        }
        
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter
        
        // Load event types
        adapter.updateEvents(getEventTypes())
    }

    private fun getEventTypes(): List<MatchEventType> {
        return listOf(
            MatchEventType("goal", "⚽", "Goal", "Record a goal scored"),
            MatchEventType("substitution", "🔄", "Substitution", "Make a player substitution"),
            MatchEventType("yellow_card", "🟨", "Yellow Card", "Issue a yellow card"),
            MatchEventType("red_card", "🟥", "Red Card", "Issue a red card"),
            MatchEventType("injury", "🦵", "Injury", "Record a player injury"),
            MatchEventType("other", "📝", "Other Event", "Record other match events")
        )
    }

    private fun handleEventSelection(eventType: MatchEventType) {
        dismiss()
        
        // Get match ID from arguments or parent fragment
        val matchId = getMatchIdFromParent()
        if (matchId.isBlank()) {
            showSnackbar("Unable to determine match ID")
            return
        }
        
        // Open the appropriate event recording sheet
        when (eventType.id) {
            "goal" -> showRecordEventSheet(matchId, "goal")
            "substitution" -> showRecordEventSheet(matchId, "substitution")
            "yellow_card" -> showRecordEventSheet(matchId, "yellow_card")
            "red_card" -> showRecordEventSheet(matchId, "red_card")
            "injury" -> showRecordEventSheet(matchId, "injury")
            "other" -> showRecordEventSheet(matchId, "other")
        }
    }

    private fun getMatchIdFromParent(): String {
        // Try to get match ID from this bottom sheet's arguments
        return arguments?.getString("event_id") ?: ""
    }

    private fun showRecordEventSheet(matchId: String, eventType: String) {
        val recordSheet = RecordEventBottomSheet.newInstance(matchId, eventType)
        recordSheet.show(parentFragmentManager, "RecordEventBottomSheet")
    }

    private fun showSnackbar(message: String) {
        activity?.findViewById<View>(android.R.id.content)?.let { view ->
            Snackbar.make(view, message, Snackbar.LENGTH_SHORT).show()
        }
    }

    data class MatchEventType(
        val id: String,
        val emoji: String,
        val title: String,
        val description: String
    )

    inner class EventTypesAdapter(
        private val onEventClick: (MatchEventType) -> Unit
    ) : RecyclerView.Adapter<EventTypesAdapter.EventViewHolder>() {

        private var events = listOf<MatchEventType>()

        fun updateEvents(newEvents: List<MatchEventType>) {
            events = newEvents
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_event_type, parent, false)
            return EventViewHolder(view)
        }

        override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
            holder.bind(events[position])
        }

        override fun getItemCount(): Int = events.size

        inner class EventViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val eventIcon: TextView = itemView.findViewById(R.id.eventIcon)
            private val eventTitle: TextView = itemView.findViewById(R.id.eventTitle)
            private val eventDescription: TextView = itemView.findViewById(R.id.eventDescription)
            private val eventArrow: ImageView = itemView.findViewById(R.id.eventArrow)

            fun bind(event: MatchEventType) {
                eventIcon.text = event.emoji
                eventTitle.text = event.title
                eventDescription.text = event.description
                
                eventArrow.setImageResource(R.drawable.ic_unicons_arrow_right_24)
                
                itemView.setOnClickListener {
                    onEventClick(event)
                }
            }
        }
    }
}
