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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ggetters.app.R
import com.ggetters.app.ui.central.adapters.PlayerAdapter
import com.ggetters.app.ui.central.models.Player
import com.ggetters.app.ui.central.viewmodels.HomePlayersViewModel
import com.ggetters.app.ui.central.viewmodels.HomeViewModel
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomePlayersFragment : Fragment() {

    private val activeModel: HomePlayersViewModel by viewModels()
    private val sharedModel: HomeViewModel by activityViewModels()

    private lateinit var playersRecyclerView: RecyclerView
    private lateinit var emptyStateText: View
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
        loadSampleData()
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
            showAddPlayerDialog()
        }
    }

    private fun setupRecyclerView() {
        playerAdapter = PlayerAdapter(
            onPlayerClick = { player -> navigateToPlayerProfile(player) },
            onPlayerLongPress = { player -> showPlayerActionsDialog(player) }
        )
        playersRecyclerView.layoutManager = LinearLayoutManager(context)
        playersRecyclerView.adapter = playerAdapter
    }

    // ————————————————————————————————————
    // Actions
    // ————————————————————————————————————
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
    // UI helpers
    // ————————————————————————————————————
    private fun updateEmptyState(isEmpty: Boolean) {
        emptyStateText.visibility = if (isEmpty) View.VISIBLE else View.GONE
        playersRecyclerView.visibility = if (isEmpty) View.GONE else View.VISIBLE
    }

    private fun loadSampleData() {
        // Set team info
        teamNameText.text = "Goal Getters FC"
        teamSportText.text = "Football (Soccer)"
        
        // Load sample players
        allPlayers = listOf(
            Player(
                id = "1",
                firstName = "Fortune",
                lastName = "Martinez",
                position = "Striker",
                jerseyNumber = "10",
                email = "fortune@example.com",
                dateOfBirth = "2008-03-15"
            ),
            Player(
                id = "2",
                firstName = "Alex",
                lastName = "Johnson",
                position = "Midfielder",
                jerseyNumber = "7",
                email = "alex@example.com",
                dateOfBirth = "2008-07-22"
            ),
            Player(
                id = "3",
                firstName = "Sarah",
                lastName = "Williams",
                position = "Defender",
                jerseyNumber = "4",
                email = "sarah@example.com",
                dateOfBirth = "2008-11-08"
            ),
            Player(
                id = "4",
                firstName = "Mike",
                lastName = "Chen",
                position = "Goalkeeper",
                jerseyNumber = "1",
                email = "mike@example.com",
                dateOfBirth = "2008-01-30"
            ),
            Player(
                id = "5",
                firstName = "Emma",
                lastName = "Davis",
                position = "Forward",
                jerseyNumber = "9",
                email = "emma@example.com",
                dateOfBirth = "2008-05-12"
            ),
            Player(
                id = "6",
                firstName = "Tom",
                lastName = "Wilson",
                position = "Midfielder",
                jerseyNumber = "6",
                email = "tom@example.com",
                dateOfBirth = "2008-09-18"
            )
        )
        
        applyFilter()
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

            // TODO: Backend - Save player to repository
            // For now, just show success message
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