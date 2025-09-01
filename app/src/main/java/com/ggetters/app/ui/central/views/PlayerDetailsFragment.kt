// app/src/main/java/com/ggetters/app/ui/central/views/PlayerDetailsFragment.kt
package com.ggetters.app.ui.central.views

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.ggetters.app.R
import com.ggetters.app.data.model.User
import com.ggetters.app.ui.central.viewmodels.PlayerDetailsViewModel
import com.google.android.material.button.MaterialButton
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.Period

@AndroidEntryPoint
class PlayerDetailsFragment : Fragment() {

    companion object {
        private const val ARG_PLAYER_ID = "player_id"
        fun newInstance(playerId: String?): PlayerDetailsFragment =
            PlayerDetailsFragment().apply { arguments = bundleOf(ARG_PLAYER_ID to playerId) }
    }

    private val vm: PlayerDetailsViewModel by viewModels()

    // Header views
    private lateinit var playerName: TextView
    private lateinit var playerPosition: TextView
    private lateinit var playerNumber: TextView
    private lateinit var playerAge: TextView
    private lateinit var playerHeight: TextView
    private lateinit var playerWeight: TextView

    // Stats views
    private lateinit var gamesPlayed: TextView
    private lateinit var goalsScored: TextView
    private lateinit var assists: TextView
    private lateinit var yellowCards: TextView
    private lateinit var redCards: TextView
    private lateinit var emptyStatsLabel: View

    // Actions
    private lateinit var editBtn: MaterialButton
    private lateinit var statsBtn: MaterialButton

    private var playerIdArg: String? = null
    private val dash by lazy { getString(R.string.dash) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        playerIdArg = arguments?.getString(ARG_PLAYER_ID)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_player_details, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindViews(view)
        renderEmptyState()

        // Kick off load if we have an id
        playerIdArg?.let { if (!it.isNullOrBlank()) vm.loadPlayer(it) }

        // Observe StateFlow<User?>
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.player.collect { render(it) }
            }
        }

        // Hook these up to your navigation later
        editBtn.setOnClickListener { /* open edit */ }
        statsBtn.setOnClickListener { /* open stats */ }
    }

    private fun bindViews(v: View) {
        playerName     = v.findViewById(R.id.playerName)
        playerPosition = v.findViewById(R.id.playerPosition)
        playerNumber   = v.findViewById(R.id.playerNumber)
        playerAge      = v.findViewById(R.id.playerAge)
        playerHeight   = v.findViewById(R.id.playerHeight)
        playerWeight   = v.findViewById(R.id.playerWeight)

        gamesPlayed    = v.findViewById(R.id.gamesPlayed)
        goalsScored    = v.findViewById(R.id.goalsScored)
        assists        = v.findViewById(R.id.assists)
        yellowCards    = v.findViewById(R.id.yellowCards)
        redCards       = v.findViewById(R.id.redCards)
        emptyStatsLabel= v.findViewById(R.id.emptyStatsLabel)

        editBtn        = v.findViewById(R.id.editPlayerButton)
        statsBtn       = v.findViewById(R.id.playerStatsButton)
    }

    private fun renderEmptyState() {
        playerName.text     = getString(R.string.no_player_selected)
        playerPosition.text = dash
        playerNumber.text   = dash
        playerAge.text      = dash
        playerHeight.text   = dash
        playerWeight.text   = dash
        setStats(null)
        setButtonsEnabled(false)
    }

    private fun render(u: User?) {
        if (u == null) {
            renderEmptyState()
            return
        }
        setButtonsEnabled(true)

        // Name
        val fullName = listOf(u.name?.trim().orEmpty(), u.surname?.trim().orEmpty())
            .filter { it.isNotBlank() }
            .joinToString(" ")
            .ifBlank { getString(R.string.no_player_selected) }
        playerName.text = fullName

        // Header fields
        playerPosition.text = u.position?.name ?: dash
        playerNumber.text   = u.number?.let { "#$it" } ?: dash
        playerAge.text      = u.dateOfBirth?.let { ageYears(it) } ?: dash
        playerHeight.text   = u.healthHeight?.let { "${it.toInt()} cm" } ?: dash
        playerWeight.text   = u.healthWeight?.let { "${it.toInt()} kg" } ?: dash

        // Stats: keep empty until Attendance/Performance wired
        setStats(null)
    }

    private fun ageYears(dob: LocalDate): String =
        Period.between(dob, LocalDate.now()).years.toString()

    private fun setButtonsEnabled(enabled: Boolean) {
        editBtn.isEnabled = enabled
        statsBtn.isEnabled = enabled
    }

    private fun setStats(@Suppress("UNUSED_PARAMETER") stats: Any?) {
        // Show dashes + empty-state label until real aggregates exist
        gamesPlayed.text = dash
        goalsScored.text = dash
        assists.text     = dash
        yellowCards.text = dash
        redCards.text    = dash
        emptyStatsLabel.visibility = View.VISIBLE
    }
}
