package com.ggetters.app.ui.central.sheets

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ggetters.app.R
import com.ggetters.app.data.model.CardType
import com.ggetters.app.data.model.GoalType
import com.ggetters.app.data.model.MatchEvent
import com.ggetters.app.data.model.MatchEventType
import com.ggetters.app.data.model.RosterPlayer
import com.ggetters.app.ui.central.adapters.PlayerSelectionAdapter
import com.ggetters.app.ui.shared.extensions.getEventDescription
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import java.util.UUID

@AndroidEntryPoint
class RecordEventBottomSheet : BottomSheetDialogFragment() {

    private var matchId: String = ""
    private var currentMinute: Int = 0

    // Selections
    private var selectedEventType: MatchEventType? = null
    private var selectedPlayer: RosterPlayer? = null
    private var selectedGoalType: GoalType? = null
    private var selectedCardType: CardType? = null
    private var selectedPlayerOut: RosterPlayer? = null
    private var selectedPlayerIn: RosterPlayer? = null

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
    ): View = inflater.inflate(R.layout.bottom_sheet_record_event, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupEventTypeSelection()
        setupPlayerSelection()
        setupActionButtons()
    }

    private fun setupEventTypeSelection() {
        val spinner = view?.findViewById<Spinner>(R.id.eventTypeSpinner) ?: return
        val options = listOf("Select Event Type") + MatchEventType.values().map { it.name.replace("_", " ") }

        spinner.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            options
        ).apply { setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, pos: Int, id: Long) {
                selectedEventType = if (pos > 0) MatchEventType.values()[pos - 1] else null
                updateUIForEventType()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
                selectedEventType = null
            }
        }
    }

    private fun setupPlayerSelection() {
        // TODO: Replace with actual roster from repository
        val samplePlayers = listOf(
            RosterPlayer("1", "John Smith", 1, "GK", status = com.ggetters.app.data.model.RSVPStatus.AVAILABLE),
            RosterPlayer("2", "Mike Johnson", 2, "LB", status = com.ggetters.app.data.model.RSVPStatus.AVAILABLE),
            RosterPlayer("3", "David Wilson", 3, "CB", status = com.ggetters.app.data.model.RSVPStatus.AVAILABLE)
        )

        val recycler = view?.findViewById<RecyclerView>(R.id.playerRecyclerView) ?: return
        recycler.layoutManager = LinearLayoutManager(context)
        recycler.adapter = PlayerSelectionAdapter(samplePlayers) { player ->
            selectedPlayer = player
            updateUIForEventType()
        }
    }

    private fun updateUIForEventType() {
        val goalLayout = view?.findViewById<View>(R.id.goalTypeLayout)
        val cardLayout = view?.findViewById<View>(R.id.cardTypeLayout)
        val subsLayout = view?.findViewById<View>(R.id.substitutionLayout)
        val recordButton = view?.findViewById<MaterialButton>(R.id.btnRecordEvent)

        goalLayout?.visibility = View.GONE
        cardLayout?.visibility = View.GONE
        subsLayout?.visibility = View.GONE

        when (selectedEventType) {
            MatchEventType.GOAL -> {
                goalLayout?.visibility = View.VISIBLE
                setupGoalTypeSelection()
            }
            MatchEventType.YELLOW_CARD, MatchEventType.RED_CARD -> {
                cardLayout?.visibility = View.VISIBLE
                setupCardTypeSelection()
            }
            MatchEventType.SUBSTITUTION -> {
                subsLayout?.visibility = View.VISIBLE
                setupSubstitutionSelection()
            }
            else -> Unit
        }

        recordButton?.isEnabled = selectedEventType != null && selectedPlayer != null
    }

    private fun setupGoalTypeSelection() {
        val spinner = view?.findViewById<Spinner>(R.id.goalTypeSpinner) ?: return
        val options = listOf("Select Goal Type") + GoalType.values().map { it.name.replace("_", " ") }

        spinner.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            options
        ).apply { setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, pos: Int, id: Long) {
                selectedGoalType = if (pos > 0) GoalType.values()[pos - 1] else null
            }
            override fun onNothingSelected(parent: AdapterView<*>?) { selectedGoalType = null }
        }
    }

    private fun setupCardTypeSelection() {
        val spinner = view?.findViewById<Spinner>(R.id.cardTypeSpinner) ?: return
        val options = listOf("Select Card Type") + CardType.values().map { it.name.replace("_", " ") }

        spinner.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            options
        ).apply { setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, pos: Int, id: Long) {
                selectedCardType = if (pos > 0) CardType.values()[pos - 1] else null
            }
            override fun onNothingSelected(parent: AdapterView<*>?) { selectedCardType = null }
        }
    }

    private fun setupSubstitutionSelection() {
        // TODO: Replace with actual substitutes
        val substitutes = listOf(
            RosterPlayer("12", "Ryan Hernandez", 12, "GK", status = com.ggetters.app.data.model.RSVPStatus.AVAILABLE),
            RosterPlayer("13", "Brandon Torres", 13, "CB", status = com.ggetters.app.data.model.RSVPStatus.AVAILABLE)
        )

        val outRv = view?.findViewById<RecyclerView>(R.id.playerOutRecyclerView)
        val inRv = view?.findViewById<RecyclerView>(R.id.playerInRecyclerView)

        outRv?.layoutManager = LinearLayoutManager(context)
        outRv?.adapter = PlayerSelectionAdapter(selectedPlayer?.let { listOf(it) } ?: emptyList()) {
            selectedPlayerOut = it
        }

        inRv?.layoutManager = LinearLayoutManager(context)
        inRv?.adapter = PlayerSelectionAdapter(substitutes) {
            selectedPlayerIn = it
        }
    }

    private fun setupActionButtons() {
        view?.findViewById<MaterialButton>(R.id.btnRecordEvent)?.setOnClickListener { recordEvent() }
        view?.findViewById<MaterialButton>(R.id.btnCancel)?.setOnClickListener { dismiss() }
    }

    private fun recordEvent() {
        val event = when (selectedEventType) {
            MatchEventType.GOAL -> MatchEvent(
                matchId = matchId,
                eventType = MatchEventType.GOAL,
                minute = currentMinute,
                playerId = selectedPlayer?.playerId,
                playerName = selectedPlayer?.playerName,
                details = mapOf("goalType" to (selectedGoalType?.name ?: GoalType.OPEN_PLAY.name)),
                createdBy = "Coach"
            )
            MatchEventType.YELLOW_CARD -> MatchEvent(
                matchId = matchId,
                eventType = MatchEventType.YELLOW_CARD,
                minute = currentMinute,
                playerId = selectedPlayer?.playerId,
                playerName = selectedPlayer?.playerName,
                details = mapOf("cardType" to (selectedCardType?.name ?: CardType.YELLOW.name)),
                createdBy = "Coach"
            )
            MatchEventType.RED_CARD -> MatchEvent(
                matchId = matchId,
                eventType = MatchEventType.RED_CARD,
                minute = currentMinute,
                playerId = selectedPlayer?.playerId,
                playerName = selectedPlayer?.playerName,
                details = mapOf("cardType" to (selectedCardType?.name ?: CardType.RED.name)),
                createdBy = "Coach"
            )
            MatchEventType.SUBSTITUTION -> MatchEvent(
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
            else -> null
        }

        if (event != null) {
            // TODO: Save event to repository (Room + Firestore)
            Snackbar.make(requireView(), "Event recorded: ${event.getEventDescription()}", Snackbar.LENGTH_SHORT).show()
            dismiss()
        } else {
            Snackbar.make(requireView(), "Please select all required fields", Snackbar.LENGTH_SHORT).show()
        }
    }
}
