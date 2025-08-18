package com.ggetters.app.ui.central.views

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.AutoCompleteTextView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ggetters.app.R
import com.ggetters.app.data.model.User
import com.ggetters.app.data.model.UserRole
import com.ggetters.app.ui.central.adapters.PlayerAdapter
import com.ggetters.app.ui.central.models.Player
import com.ggetters.app.ui.central.viewmodels.HomePlayersViewModel
import com.ggetters.app.ui.central.viewmodels.HomeViewModel
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HomePlayersFragment : Fragment() {

    private val activeModel: HomePlayersViewModel by viewModels()
    private val sharedModel: HomeViewModel by activityViewModels()

    private lateinit var playersRecyclerView: RecyclerView
    private lateinit var emptyStateText: TextView
    private lateinit var teamNameText: TextView
    private lateinit var teamSportText: TextView
    private lateinit var addPlayerFab: FloatingActionButton
    private lateinit var playerAdapter: PlayerAdapter

    private var allPlayers: List<Player> = emptyList()
    private var filteredPlayers: List<Player> = emptyList()
    private var selectedFilter: String = "All"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_players, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews(view)
        setHasOptionsMenu(true)
        setupRecyclerView()
        observeViewModel()
    }

    // ————————————————————————————————————
    // Observers
    // ————————————————————————————————————
    /** Observe active team for header + enable/disable FAB, and observe roster from Room/Firestore. */
    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {

                // 1) Team header + trigger sync when active team changes
                launch {
                    activeModel.activeTeam.collect { team ->
                        if (team == null) {
                            teamNameText.text = getString(R.string.no_active_team)
                            teamSportText.text = ""
                            addPlayerFab.isEnabled = false
                            updateEmptyState(isEmpty = true)
                        } else {
                            teamNameText.text = team.name
                            teamSportText.text = "Football (Soccer)" // TODO: derive from team.sport if/when available
                            addPlayerFab.isEnabled = true
                            activeModel.refresh() // pull/push for this team
                        }
                    }
                }

                // 2) Real players list (User -> Player UI model)
                launch {
                    activeModel.players.collect { users ->
                        allPlayers = users.map { it.toUiPlayer() }
                        applyFilter() // re-apply current filter whenever data changes
                    }
                }
            }
        }
    }

    // ————————————————————————————————————
    // Menu (filter)
    // ————————————————————————————————————
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_players_toolbar, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean =
        when (item.itemId) {
            R.id.action_filter -> {
                showFilterDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }

    private fun showFilterDialog() {
        val filters = arrayOf("All", "Players", "Coaches", "Goalkeepers", "New members")
        val checkedItem = filters.indexOf(selectedFilter).coerceAtLeast(0)
        AlertDialog.Builder(requireContext())
            .setTitle("Filter Players")
            .setSingleChoiceItems(filters, checkedItem) { _, which ->
                selectedFilter = filters[which]
            }
            .setPositiveButton("Apply") { dialog, _ ->
                applyFilter()
                dialog.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun applyFilter() {
        filteredPlayers = when (selectedFilter) {
            "All" -> allPlayers
            "Players" -> allPlayers.filter { it.position != "Coach" && it.position != "Assistant" }
            "Coaches" -> allPlayers.filter { it.position == "Coach" }
            "Goalkeepers" -> allPlayers.filter { it.position.equals("Goalkeeper", true) }
            "New members" -> allPlayers.filter { it.position.equals("New", true) }
            else -> allPlayers
        }
        playerAdapter.updatePlayers(filteredPlayers)
        updateEmptyState(filteredPlayers.isEmpty())
    }

    // ————————————————————————————————————
    // Setup
    // ————————————————————————————————————
    private fun setupViews(view: View) {
        teamNameText = view.findViewById(R.id.teamNameText)
        teamSportText = view.findViewById(R.id.teamSportText)
        playersRecyclerView = view.findViewById(R.id.playersRecyclerView)
        emptyStateText = view.findViewById(R.id.emptyStateText)
        addPlayerFab = view.findViewById(R.id.addPlayerFab)

        addPlayerFab.setOnClickListener {
            val t = activeModel.activeTeam.value
            if (t == null) {
                Snackbar.make(requireView(), "Select an active team first", Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            showAddPlayerDialogForTeam(t.id) // pass active team id
        }
    }

    private fun setupRecyclerView() {
        playerAdapter = PlayerAdapter(
            onPlayerClick = { player -> navigateToPlayerProfile(player) }
        )
        playersRecyclerView.layoutManager = LinearLayoutManager(context)
        playersRecyclerView.adapter = playerAdapter
    }

    // ————————————————————————————————————
    // Actions
    // ————————————————————————————————————
    /** Small shim: pass teamId into your add-player flow. */
    private fun showAddPlayerDialogForTeam(teamId: String) {
        // TODO: When saving: call a ViewModel method that writes to repo with teamId, then activeModel.refresh()
        showAddPlayerDialog()
    }

    private fun navigateToPlayerProfile(player: Player) {
        // TODO: Analytics if needed
        val playerProfileFragment = PlayerProfileFragment.newInstance(player.id)
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, playerProfileFragment)
            .addToBackStack("players_to_player_profile")
            .commit()
    }

    private fun showPlayerActionsDialog(player: Player) {
        val actions = arrayOf("View Profile", "Edit Role", "Remove from Team", "Send Message")
        AlertDialog.Builder(requireContext())
            .setTitle(player.name)
            .setItems(actions) { _, which ->
                when (which) {
                    0 -> navigateToPlayerProfile(player)
                    1 -> showEditRoleDialog(player)
                    2 -> showRemovePlayerConfirmation(player)
                    3 -> showSendMessageDialog(player)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showGuardianActionsDialog(player: Player) {
        val actions = arrayOf("Approve Attendance")
        AlertDialog.Builder(requireContext())
            .setTitle(player.name)
            .setItems(actions) { _, which ->
                if (which == 0) approveAttendance(player)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showEditRoleDialog(player: Player) {
        val roles = arrayOf("Player", "Assistant", "Coach")
        val currentRoleIndex = roles.indexOf(player.position).coerceAtLeast(0)

        AlertDialog.Builder(requireContext())
            .setTitle("Edit Role for ${player.name}")
            .setSingleChoiceItems(roles, currentRoleIndex) { dialog, which ->
                val newRole = roles[which]
                // TODO: Backend - Update role in repository (permissions: only COACH can change others)
                // playerRepo.updatePlayerRole(player.id, newRole)
                Snackbar.make(requireView(), "Role updated to $newRole", Snackbar.LENGTH_SHORT).show()
                dialog.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showRemovePlayerConfirmation(player: Player) {
        AlertDialog.Builder(requireContext())
            .setTitle("Remove Player")
            .setMessage("Are you sure you want to remove ${player.name} from the team?")
            .setPositiveButton("Remove") { _, _ ->
                // TODO: Backend - Remove from team via repository (enforce coach-only)
                // playerRepo.removeFromTeam(player.id)
                Snackbar.make(requireView(), "${player.name} removed from team", Snackbar.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showSendMessageDialog(player: Player) {
        // TODO: Backend - Implement messaging or open chat screen
        Snackbar.make(requireView(), "Messaging functionality coming soon", Snackbar.LENGTH_SHORT).show()
    }

    private fun approveAttendance(player: Player) {
        // TODO: Backend - Implement attendance approval workflow in repo
        Snackbar.make(requireView(), "Attendance approved for ${player.name}", Snackbar.LENGTH_SHORT).show()
    }

    // ————————————————————————————————————
    // Mapping + UI helpers
    // ————————————————————————————————————
    /** Map domain User to UI Player model used by PlayerAdapter. */
    private fun User.toUiPlayer(): Player {
        val posLabel = position?.name?.replace('_', ' ')
            ?: if (role == UserRole.COACH) "Coach" else "Player"
        return Player(
            id = id,
            firstName = name,
            lastName = surname,
            position = posLabel,
            jerseyNumber = number?.toString().orEmpty(),
            email = email.orEmpty(),
            dateOfBirth = dateOfBirth?.toString().orEmpty()
        )
    }

    private fun updateEmptyState(isEmpty: Boolean) {
        emptyStateText.visibility = if (isEmpty) View.VISIBLE else View.GONE
        playersRecyclerView.visibility = if (isEmpty) View.GONE else View.VISIBLE
    }

    // ————————————————————————————————————
    // Add Player Dialog (UI only; wire save to repo)
    // ————————————————————————————————————
    private fun showAddPlayerDialog() {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_add_player, null)
        val dialog = AlertDialog.Builder(requireContext(), R.style.Theme_GoalGetters_Dialog)
            .setView(dialogView)
            .create()

        // Position dropdown
        val positionInput = dialogView.findViewById<AutoCompleteTextView>(R.id.playerPositionInput)
        val positions = arrayOf(
            "Striker", "Forward", "Midfielder", "Defender",
            "Goalkeeper", "Winger", "Center Back", "Full Back"
        )
        val positionAdapter = android.widget.ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            positions
        )
        positionInput.setAdapter(positionAdapter)

        // Buttons
        dialogView.findViewById<MaterialButton>(R.id.cancelButton).setOnClickListener {
            dialog.dismiss()
        }

        dialogView.findViewById<MaterialButton>(R.id.addPlayerButton).setOnClickListener {
            // Gather form inputs
            val firstName =
                dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.playerFirstNameInput).text.toString().trim()
            val lastName =
                dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.playerLastNameInput).text.toString().trim()
            val email =
                dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.playerEmailInput).text.toString().trim()
            val position =
                dialogView.findViewById<AutoCompleteTextView>(R.id.playerPositionInput).text.toString().trim()
            val jerseyNumber =
                dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.playerJerseyNumberInput).text.toString().trim()
            val dateOfBirth =
                dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.playerDateOfBirthInput).text.toString().trim()

            // Basic validation
            if (firstName.isBlank()) {
                dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.playerFirstNameInput).error =
                    "First name is required"
                return@setOnClickListener
            }
            if (lastName.isBlank()) {
                dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.playerLastNameInput).error =
                    "Last name is required"
                return@setOnClickListener
            }
            if (email.isBlank()) {
                dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.playerEmailInput).error =
                    "Email is required"
                return@setOnClickListener
            }
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.playerEmailInput).error =
                    "Please enter a valid email"
                return@setOnClickListener
            }
            if (position.isBlank()) {
                dialogView.findViewById<AutoCompleteTextView>(R.id.playerPositionInput).error =
                    "Position is required"
                return@setOnClickListener
            }

            val jerseyNum = jerseyNumber.toIntOrNull()

            // Kick off the save
            activeModel.addPlayer(
                firstName = firstName,
                lastName = lastName,
                email = email,
                positionLabel = position,
                jerseyNumber = jerseyNum,
                dateOfBirthIso = dateOfBirth.ifBlank { null }
            )

            // UX: immediate feedback; Room/Flow will reflect once sync completes
            Snackbar.make(
                requireView(),
                "Player ${firstName} ${lastName} added",
                Snackbar.LENGTH_LONG
            ).show()

            dialog.dismiss()
        }

        dialog.show()
    }
}
