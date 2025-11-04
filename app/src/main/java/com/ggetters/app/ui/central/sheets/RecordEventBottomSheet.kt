package com.ggetters.app.ui.central.sheets

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.ggetters.app.R
import kotlinx.coroutines.launch
import com.ggetters.app.data.model.MatchEvent
import com.ggetters.app.data.model.MatchEventType
import com.ggetters.app.data.model.RSVPStatus
import com.ggetters.app.ui.central.viewmodels.MatchEventViewModel
import com.ggetters.app.ui.shared.extensions.getFullName
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import java.util.UUID

/**
 * Bottom sheet for recording specific match events (goals, cards, substitutions).
 * Provides forms for different event types with appropriate fields.
 */
@AndroidEntryPoint
class RecordEventBottomSheet : BottomSheetDialogFragment() {

    private val viewModel: MatchEventViewModel by viewModels()

    private var matchId: String = ""
    private var eventType: String = ""

    // UI Components
    private lateinit var eventTypeTitle: TextView
    private lateinit var eventTypeDescription: TextView
    private lateinit var minuteInput: EditText
    private lateinit var playerSpinner: Spinner
    private lateinit var goalTypeSpinner: Spinner
    private lateinit var cardTypeSpinner: Spinner
    private lateinit var substituteInSpinner: Spinner
    private lateinit var substituteOutSpinner: Spinner
    private lateinit var notesInput: EditText
    private lateinit var recordButton: Button
    private lateinit var cancelButton: Button

    // Dynamic layouts for different event types
    private lateinit var playerSelectionLayout: LinearLayout
    private lateinit var goalTypeLayout: LinearLayout
    private lateinit var cardTypeLayout: LinearLayout
    private lateinit var substitutionLayout: LinearLayout
    private lateinit var notesLayout: LinearLayout

    companion object {
        fun newInstance(matchId: String, eventType: String): RecordEventBottomSheet {
            return RecordEventBottomSheet().apply {
                arguments = Bundle().apply {
                    putString("match_id", matchId)
                    putString("event_type", eventType)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            matchId = it.getString("match_id", "")
            eventType = it.getString("event_type", "")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.bottom_sheet_record_event, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        initViews(view)
        setupEventType()
        setupPlayerSpinner()
        observeViewModel()
    }

    private fun initViews(view: View) {
        eventTypeTitle = view.findViewById(R.id.eventTypeTitle)
        eventTypeDescription = view.findViewById(R.id.eventTypeDescription)
        minuteInput = view.findViewById(R.id.minuteInput)
        playerSpinner = view.findViewById(R.id.playerSpinner)
        goalTypeSpinner = view.findViewById(R.id.goalTypeSpinner)
        cardTypeSpinner = view.findViewById(R.id.cardTypeSpinner)
        substituteInSpinner = view.findViewById(R.id.substituteInSpinner)
        substituteOutSpinner = view.findViewById(R.id.substituteOutSpinner)
        notesInput = view.findViewById(R.id.notesInput)
        recordButton = view.findViewById(R.id.recordButton)
        cancelButton = view.findViewById(R.id.cancelButton)

        // Dynamic layouts
        playerSelectionLayout = view.findViewById(R.id.playerSelectionLayout)
        goalTypeLayout = view.findViewById(R.id.goalTypeLayout)
        cardTypeLayout = view.findViewById(R.id.cardTypeLayout)
        substitutionLayout = view.findViewById(R.id.substitutionLayout)
        notesLayout = view.findViewById(R.id.notesLayout)

        recordButton.setOnClickListener { recordEvent() }
        cancelButton.setOnClickListener { dismiss() }
    }

    private fun setupEventType() {
        val (title, description) = getEventTypeInfo(eventType)
        eventTypeTitle.text = title
        eventTypeDescription.text = description

        // Show/hide appropriate layouts based on event type
        when (eventType) {
            "goal" -> {
                playerSelectionLayout.visibility = View.VISIBLE
                goalTypeLayout.visibility = View.VISIBLE
                // Clear hidden inputs to avoid hidden-data leakage
                (view?.findViewById<android.widget.Spinner>(R.id.cardTypeSpinner))?.setSelection(0)
                view?.findViewById<android.widget.EditText>(R.id.notesInput)?.setText("")
                cardTypeLayout.visibility = View.GONE
                (view?.findViewById<android.widget.Spinner>(R.id.substituteInSpinner))?.setSelection(0)
                (view?.findViewById<android.widget.Spinner>(R.id.substituteOutSpinner))?.setSelection(0)
                view?.findViewById<android.widget.EditText>(R.id.notesInput)?.setText("")
                substitutionLayout.visibility = View.GONE
                notesLayout.visibility = View.VISIBLE
                setupGoalTypeSpinner()
            }
            "yellow_card", "red_card" -> {
                playerSelectionLayout.visibility = View.VISIBLE
                goalTypeLayout.visibility = View.GONE
                cardTypeLayout.visibility = View.VISIBLE
                (view?.findViewById<android.widget.Spinner>(R.id.substituteInSpinner))?.setSelection(0)
                (view?.findViewById<android.widget.Spinner>(R.id.substituteOutSpinner))?.setSelection(0)
                substitutionLayout.visibility = View.GONE
                notesLayout.visibility = View.VISIBLE
                setupCardTypeSpinner()
            }
            "substitution" -> {
                playerSelectionLayout.visibility = View.GONE
                goalTypeLayout.visibility = View.GONE
                (view?.findViewById<android.widget.Spinner>(R.id.cardTypeSpinner))?.setSelection(0)
                view?.findViewById<android.widget.EditText>(R.id.notesInput)?.setText("")
                cardTypeLayout.visibility = View.GONE
                substitutionLayout.visibility = View.VISIBLE
                notesLayout.visibility = View.VISIBLE
            }
            "injury", "other" -> {
                playerSelectionLayout.visibility = View.VISIBLE
                goalTypeLayout.visibility = View.GONE
                (view?.findViewById<android.widget.Spinner>(R.id.cardTypeSpinner))?.setSelection(0)
                cardTypeLayout.visibility = View.GONE
                (view?.findViewById<android.widget.Spinner>(R.id.substituteInSpinner))?.setSelection(0)
                (view?.findViewById<android.widget.Spinner>(R.id.substituteOutSpinner))?.setSelection(0)
                view?.findViewById<android.widget.EditText>(R.id.notesInput)?.setText("")
                substitutionLayout.visibility = View.GONE
                notesLayout.visibility = View.VISIBLE
            }
        }
    }

    private fun setupPlayerSpinner() {
        // Load available players for this match
        viewModel.loadAvailablePlayers(matchId)
    }

    private fun observeViewModel() {
        // Observe StateFlow using lifecycleScope
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.availablePlayers.collect { players ->
                setupPlayerSpinnerData(players)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.eventRecorded.collect { success ->
                if (success) {
                    showSuccessMessage()
                    dismiss()
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.error.collect { error ->
                if (!error.isNullOrBlank()) {
                    showErrorMessage(error)
                }
            }
        }
    }

    private fun setupPlayerSpinnerData(players: List<com.ggetters.app.data.model.User>) {
        val playerNames = players.map { "${it.name} ${it.surname}" }
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, playerNames)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        playerSpinner.adapter = adapter
        substituteInSpinner.adapter = adapter
        substituteOutSpinner.adapter = adapter
    }

    private fun recordEvent() {
        val minute = minuteInput.text.toString().toIntOrNull()
        if (minute == null || minute < 0 || minute > 120) {
            showErrorMessage("Please enter a valid minute (0-120)")
            return
        }

        // Additional validation for substitutions
        if (eventType == "substitution") {
            val players = viewModel.availablePlayers.value
            val inIdx = substituteInSpinner.selectedItemPosition
            val outIdx = substituteOutSpinner.selectedItemPosition
            if (players == null || inIdx !in players.indices || outIdx !in players.indices) {
                showErrorMessage("Select both players for substitution")
                return
            }
            if (inIdx == outIdx) {
                showErrorMessage("Sub-in and sub-out must be different players")
                return
            }
        }

        val selectedPlayerId = when {
            playerSelectionLayout.visibility == View.VISIBLE -> {
                val selectedPlayer = playerSpinner.selectedItemPosition
                if (selectedPlayer >= 0) {
                    viewModel.availablePlayers.value?.get(selectedPlayer)?.id
                } else null
            }
            else -> null
        }

        val event = createMatchEvent(selectedPlayerId, minute)
        
        viewModel.recordEvent(event)
    }

    private fun createMatchEvent(playerId: String?, minute: Int): MatchEvent {
        val eventTypeEnum = when (eventType) {
            "goal" -> MatchEventType.GOAL
            "yellow_card" -> MatchEventType.YELLOW_CARD
            "red_card" -> MatchEventType.RED_CARD
            "substitution" -> MatchEventType.SUBSTITUTION
            "injury" -> MatchEventType.INJURY
            "other" -> MatchEventType.OTHER
            else -> MatchEventType.SCORE_UPDATE
        }

        val details = mutableMapOf<String, Any>()
        var playerNameOverride: String? = null
        
        when (eventType) {
            "goal" -> {
                val goalType = goalTypeSpinner.selectedItem?.toString()
                details["goalType"] = goalType ?: "Open Play"
                
                // Check if it's an opponent goal
                if (goalType == "Opponent Goal") {
                    details["isOpponentGoal"] = true
                    playerNameOverride = "Opponent"
                }
            }
            "yellow_card", "red_card" -> {
                cardTypeSpinner.selectedItem?.toString()?.let { details["cardType"] = it }
            }
            "substitution" -> {
                val subInIndex = substituteInSpinner.selectedItemPosition
                val subOutIndex = substituteOutSpinner.selectedItemPosition
                val players = viewModel.availablePlayers.value
                if (players != null && subInIndex in players.indices && subOutIndex in players.indices && subInIndex != subOutIndex) {
                    val subInPlayer = players[subInIndex]
                    val subOutPlayer = players[subOutIndex]

                    // Store both IDs and names for proper display
                    details["substituteIn"] = subInPlayer.id
                    details["substituteOut"] = subOutPlayer.id
                    details["playerIn"] = subInPlayer.fullName()
                    details["playerOut"] = subOutPlayer.fullName()
                }
            }
        }
        
        notesInput.text.toString().takeIf { it.isNotBlank() }?.let { 
            details["notes"] = it 
        }

        return MatchEvent(
            id = UUID.randomUUID().toString(),
            matchId = matchId,
            eventType = eventTypeEnum,
            minute = minute,
            playerId = if (playerNameOverride == null) playerId else null,
            playerName = playerNameOverride ?: viewModel.availablePlayers.value?.find { it.id == playerId }?.let { 
                "${it.name} ${it.surname}" 
            },
            teamId = null, // TODO: Get from current team
            teamName = null, // TODO: Get from current team
            details = details,
            createdBy = "current_user" // TODO: Get from auth
        )
    }

    private fun getEventTypeInfo(eventType: String): Pair<String, String> {
        return when (eventType) {
            "goal" -> "⚽ Record Goal" to "Record a goal scored during the match"
            "yellow_card" -> "🟨 Yellow Card" to "Issue a yellow card to a player"
            "red_card" -> "🟥 Red Card" to "Issue a red card to a player"
            "substitution" -> "🔄 Substitution" to "Make a player substitution"
            "injury" -> "🦵 Injury" to "Record a player injury"
            "other" -> "📝 Other Event" to "Record other match events"
            else -> "📝 Record Event" to "Record a match event"
        }
    }

    private fun showSuccessMessage() {
        Snackbar.make(requireView(), "Event recorded successfully!", Snackbar.LENGTH_SHORT).show()
    }

    private fun showErrorMessage(message: String) {
        Snackbar.make(requireView(), message, Snackbar.LENGTH_LONG).show()
    }
    
    private fun setupGoalTypeSpinner() {
        val goalTypes = listOf("Open Play", "Penalty", "Free Kick", "Header", "Own Goal", "Opponent Goal")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, goalTypes)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        goalTypeSpinner.adapter = adapter
        
        // Hide player selection when "Opponent Goal" is selected
        goalTypeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedGoalType = goalTypes[position]
                if (selectedGoalType == "Opponent Goal") {
                    playerSelectionLayout.visibility = View.GONE
                    // Clear player selection data when hiding
                    playerSpinner.setSelection(0)
                } else {
                    playerSelectionLayout.visibility = View.VISIBLE
                }
            }
            
            override fun onNothingSelected(parent: AdapterView<*>?) {
                playerSelectionLayout.visibility = View.VISIBLE
            }
        }
    }
    
    private fun setupCardTypeSpinner() {
        val cardTypes = listOf("Foul", "Unsporting Behavior", "Dissent", "Time Wasting", "Other")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, cardTypes)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        cardTypeSpinner.adapter = adapter
    }
}
