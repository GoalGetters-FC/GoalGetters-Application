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
        when (eventType.id) {
            "goal" -> showGoalEvent()
            "substitution" -> showSubstitutionEvent()
            "yellow_card" -> showCardEvent("Yellow Card")
            "red_card" -> showCardEvent("Red Card")
            "injury" -> showInjuryEvent()
            "other" -> showOtherEvent()
        }
        dismiss()
    }

    private fun showGoalEvent() {
        // TODO: Implement goal event tracking
        showSnackbar("Goal tracking coming soon")
    }

    private fun showSubstitutionEvent() {
        // TODO: Implement substitution tracking
        showSnackbar("Substitution tracking coming soon")
    }

    private fun showCardEvent(cardType: String) {
        // TODO: Implement card event tracking
        showSnackbar("$cardType tracking coming soon")
    }

    private fun showInjuryEvent() {
        // TODO: Implement injury tracking
        showSnackbar("Injury tracking coming soon")
    }

    private fun showOtherEvent() {
        // TODO: Implement other event tracking
        showSnackbar("Other event tracking coming soon")
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
