package com.ggetters.app.ui.central.sheets

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ggetters.app.R
import com.ggetters.app.ui.central.adapters.PlayerSelectionAdapter
import com.ggetters.app.ui.central.models.*
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RecordEventBottomSheet : BottomSheetDialogFragment() {

    private var matchId: String = ""
    private var currentMinute: Int = 0
    private var selectedEventType: MatchEventType? = null
    private var selectedPlayer: LineupPlayer? = null
    private var selectedGoalType: GoalType? = null
    private var selectedCardType: CardType? = null
    private var selectedPlayerOut: LineupPlayer? = null
    private var selectedPlayerIn: LineupPlayer? = null

    companion object {
        fun newInstance(matchId: String, currentMinute: Int): RecordEventBottomSheet {
            return RecordEventBottomSheet().apply {
                this.matchId = matchId
                this.currentMinute = currentMinute
            }
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

        setupEventTypeSelection()
        setupPlayerSelection()
        setupActionButtons()
    }

    private fun setupEventTypeSelection() {
        val eventTypeSpinner = view?.findViewById<Spinner>(R.id.eventTypeSpinner)
        val eventTypeOptions = listOf(
            "Select Event Type",
            "Goal",
            "Yellow Card",
            "Red Card",
            "Substitution"
        )

        eventTypeSpinner?.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            eventTypeOptions
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }

        eventTypeSpinner?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedEventType = when (position) {
                    1 -> MatchEventType.GOAL
                    2 -> MatchEventType.YELLOW_CARD
                    3 -> MatchEventType.RED_CARD
                    4 -> MatchEventType.SUBSTITUTION
                    else -> null
                }
                updateUIForEventType()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                selectedEventType = null
            }
        }
    }

    private fun setupPlayerSelection() {
        // TODO: Backend - Load actual players from match lineup
        val samplePlayers = listOf(
            LineupPlayer("1", "John Smith", "GK", 1),
            LineupPlayer("2", "Mike Johnson", "LB", 2),
            LineupPlayer("3", "David Wilson", "CB", 3),
            LineupPlayer("4", "Chris Brown", "CB", 4),
            LineupPlayer("5", "Tom Davis", "RB", 5),
            LineupPlayer("6", "James Miller", "CM", 6),
            LineupPlayer("7", "Robert Garcia", "CM", 7),
            LineupPlayer("8", "Daniel Martinez", "CM", 8),
            LineupPlayer("9", "Kevin Rodriguez", "LW", 9),
            LineupPlayer("10", "Steven Lopez", "ST", 10),
            LineupPlayer("11", "Andrew Gonzalez", "RW", 11)
        )

        val playerRecyclerView = view?.findViewById<RecyclerView>(R.id.playerRecyclerView)
        playerRecyclerView?.layoutManager = LinearLayoutManager(context)
        playerRecyclerView?.adapter = PlayerSelectionAdapter(samplePlayers) { player ->
            selectedPlayer = player
            updateUIForEventType()
        }
    }

    private fun updateUIForEventType() {
        val goalTypeLayout = view?.findViewById<View>(R.id.goalTypeLayout)
        val cardTypeLayout = view?.findViewById<View>(R.id.cardTypeLayout)
        val substitutionLayout = view?.findViewById<View>(R.id.substitutionLayout)
        val recordButton = view?.findViewById<MaterialButton>(R.id.btnRecordEvent)

        // Hide all event-specific layouts
        goalTypeLayout?.visibility = View.GONE
        cardTypeLayout?.visibility = View.GONE
        substitutionLayout?.visibility = View.GONE

        when (selectedEventType) {
            MatchEventType.GOAL -> {
                goalTypeLayout?.visibility = View.VISIBLE
                setupGoalTypeSelection()
            }
            MatchEventType.YELLOW_CARD, MatchEventType.RED_CARD -> {
                cardTypeLayout?.visibility = View.VISIBLE
                setupCardTypeSelection()
            }
            MatchEventType.SUBSTITUTION -> {
                substitutionLayout?.visibility = View.VISIBLE
                setupSubstitutionSelection()
            }
            else -> {
                // No specific layout needed
            }
        }

        // Enable/disable record button based on selection
        recordButton?.isEnabled = selectedEventType != null && selectedPlayer != null
    }

    private fun setupGoalTypeSelection() {
        val goalTypeSpinner = view?.findViewById<Spinner>(R.id.goalTypeSpinner)
        val goalTypeOptions = listOf(
            "Select Goal Type",
            "Open Play",
            "Penalty",
            "Free Kick",
            "Own Goal",
            "Header",
            "Volley"
        )

        goalTypeSpinner?.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            goalTypeOptions
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }

        goalTypeSpinner?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedGoalType = when (position) {
                    1 -> GoalType.OPEN_PLAY
                    2 -> GoalType.PENALTY
                    3 -> GoalType.FREE_KICK
                    4 -> GoalType.OWN_GOAL
                    5 -> GoalType.HEADER
                    6 -> GoalType.VOLLEY
                    else -> null
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                selectedGoalType = null
            }
        }
    }

    private fun setupCardTypeSelection() {
        val cardTypeSpinner = view?.findViewById<Spinner>(R.id.cardTypeSpinner)
        val cardTypeOptions = listOf(
            "Select Card Type",
            "Yellow Card",
            "Red Card",
            "Second Yellow"
        )

        cardTypeSpinner?.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            cardTypeOptions
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }

        cardTypeSpinner?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedCardType = when (position) {
                    1 -> CardType.YELLOW
                    2 -> CardType.RED
                    3 -> CardType.SECOND_YELLOW
                    else -> null
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                selectedCardType = null
            }
        }
    }

    private fun setupSubstitutionSelection() {
        // TODO: Backend - Load substitutes for player in selection
        val substitutes = listOf(
            LineupPlayer("12", "Ryan Hernandez", "GK", 12),
            LineupPlayer("13", "Brandon Torres", "CB", 13),
            LineupPlayer("14", "Nathan Flores", "CM", 14),
            LineupPlayer("15", "Timothy Collins", "ST", 15)
        )

        val playerOutRecyclerView = view?.findViewById<RecyclerView>(R.id.playerOutRecyclerView)
        val playerInRecyclerView = view?.findViewById<RecyclerView>(R.id.playerInRecyclerView)

        playerOutRecyclerView?.layoutManager = LinearLayoutManager(context)
        playerOutRecyclerView?.adapter = PlayerSelectionAdapter(selectedPlayer?.let { listOf(it) } ?: emptyList()) { player ->
            selectedPlayerOut = player
        }

        playerInRecyclerView?.layoutManager = LinearLayoutManager(context)
        playerInRecyclerView?.adapter = PlayerSelectionAdapter(substitutes) { player ->
            selectedPlayerIn = player
        }
    }

    private fun setupActionButtons() {
        view?.findViewById<MaterialButton>(R.id.btnRecordEvent)?.setOnClickListener {
            recordEvent()
        }

        view?.findViewById<MaterialButton>(R.id.btnCancel)?.setOnClickListener {
            dismiss()
        }
    }

    private fun recordEvent() {
        // TODO: Backend - Record event in backend
        val event = when (selectedEventType) {
            MatchEventType.GOAL -> {
                MatchEvent(
                    matchId = matchId,
                    eventType = MatchEventType.GOAL,
                    minute = currentMinute,
                    playerId = selectedPlayer?.playerId,
                    playerName = selectedPlayer?.playerName,
                    details = mapOf("goalType" to (selectedGoalType?.name ?: "OPEN_PLAY")),
                    createdBy = "Coach"
                )
            }
            MatchEventType.YELLOW_CARD -> {
                MatchEvent(
                    matchId = matchId,
                    eventType = MatchEventType.YELLOW_CARD,
                    minute = currentMinute,
                    playerId = selectedPlayer?.playerId,
                    playerName = selectedPlayer?.playerName,
                    details = mapOf("cardType" to (selectedCardType?.name ?: "YELLOW")),
                    createdBy = "Coach"
                )
            }
            MatchEventType.RED_CARD -> {
                MatchEvent(
                    matchId = matchId,
                    eventType = MatchEventType.RED_CARD,
                    minute = currentMinute,
                    playerId = selectedPlayer?.playerId,
                    playerName = selectedPlayer?.playerName,
                    details = mapOf("cardType" to (selectedCardType?.name ?: "RED")),
                    createdBy = "Coach"
                )
            }
            MatchEventType.SUBSTITUTION -> {
                MatchEvent(
                    matchId = matchId,
                    eventType = MatchEventType.SUBSTITUTION,
                    minute = currentMinute,
                    playerId = selectedPlayerOut?.playerId,
                    playerName = selectedPlayerOut?.playerName,
                    details = mapOf(
                        "playerOut" to (selectedPlayerOut?.playerName ?: ""),
                        "playerIn" to (selectedPlayerIn?.playerName ?: "")
                    ),
                    createdBy = "Coach"
                )
            }
            else -> null
        }

        if (event != null) {
            // TODO: Backend - Save event to backend
            Snackbar.make(
                requireView(),
                "Event recorded: ${event.getEventDescription()}",
                Snackbar.LENGTH_SHORT
            ).show()
            dismiss()
        } else {
            Snackbar.make(
                requireView(),
                "Please select all required fields",
                Snackbar.LENGTH_SHORT
            ).show()
        }
    }
} 