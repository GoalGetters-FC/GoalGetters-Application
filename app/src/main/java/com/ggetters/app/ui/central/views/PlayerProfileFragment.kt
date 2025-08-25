package com.ggetters.app.ui.central.views

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AutoCompleteTextView
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.ggetters.app.R
import com.ggetters.app.data.model.User
import com.ggetters.app.ui.central.viewmodels.HomeProfileViewModel
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PlayerProfileFragment : Fragment() {

    companion object {
        private const val ARG_PLAYER_ID = "player_id"
        private const val ARG_START_EDITING = "start_editing"

        fun newInstance(playerId: String, startEditing: Boolean = false): PlayerProfileFragment {
            return PlayerProfileFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PLAYER_ID, playerId)
                    putBoolean(ARG_START_EDITING, startEditing)
                }
            }
        }
    }

    private val viewModel: HomeProfileViewModel by viewModels()

    private var playerId: String? = null
    private var isEditing: Boolean = false
    private var currentPlayer: User? = null

    // Header
    private lateinit var playerAvatar: ImageView
    private lateinit var playerName: TextView
    private lateinit var playerAge: TextView

    // Form inputs
    private lateinit var playerNameInput: TextInputEditText
    private lateinit var playerNumberInput: TextInputEditText
    private lateinit var playerEmailInput: TextInputEditText
    private lateinit var playerDateOfBirthInput: TextInputEditText
    private lateinit var playerContactInput: TextInputEditText
    private lateinit var playerStatusDropdown: AutoCompleteTextView
    private lateinit var playerRoleDropdown: AutoCompleteTextView

    // Statistics (placeholders)
    private lateinit var statsGoals: TextView
    private lateinit var statsAssists: TextView
    private lateinit var statsMatches: TextView
    private lateinit var statsYellowCards: TextView
    private lateinit var statsRedCards: TextView
    private lateinit var statsCleanSheets: TextView

    // Action rows & buttons
    private lateinit var actionsRow: ViewGroup
    private lateinit var editActionsRow: View
    private lateinit var btnEditProfile: MaterialButton
    private lateinit var btnSendMessage: MaterialButton
    private lateinit var btnViewHistory: MaterialButton
    private lateinit var btnCancelEdit: MaterialButton
    private lateinit var btnSaveProfile: MaterialButton
    private lateinit var btnDeleteProfile: MaterialButton

    private val userRole = "coach" // TODO: inject/derive current user role

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            playerId = it.getString(ARG_PLAYER_ID)
            isEditing = it.getBoolean(ARG_START_EDITING, false)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_player_profile, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews(view)
        setupRoleVisibility()
        setupActions()

        playerId?.let { viewModel.loadPlayer(it) }

        viewModel.player.observe(viewLifecycleOwner, Observer { player ->
            player?.let {
                currentPlayer = it
                displayPlayerInfo(it)
            }
        })

        if (isEditing) setEditing(true)
    }

    private fun setupViews(view: View) {
        // header
        playerAvatar = view.findViewById(R.id.playerAvatar)
        playerName   = view.findViewById(R.id.playerName)
        playerAge    = view.findViewById(R.id.playerAge)

        // form
        playerNameInput        = view.findViewById(R.id.playerNameInput)
        playerNumberInput      = view.findViewById(R.id.playerNumberInput)
        playerEmailInput       = view.findViewById(R.id.playerEmailInput)
        playerDateOfBirthInput = view.findViewById(R.id.playerDateOfBirthInput)
        playerContactInput     = view.findViewById(R.id.playerContactInput)
        playerStatusDropdown   = view.findViewById(R.id.playerStatusDropdown)
        playerRoleDropdown     = view.findViewById(R.id.playerRoleDropdown)

        // stats
        statsGoals        = view.findViewById(R.id.statsGoals)
        statsAssists      = view.findViewById(R.id.statsAssists)
        statsMatches      = view.findViewById(R.id.statsMatches)
        statsYellowCards  = view.findViewById(R.id.statsYellowCards)
        statsRedCards     = view.findViewById(R.id.statsRedCards)
        statsCleanSheets  = view.findViewById(R.id.statsCleanSheets)

        // actions
        actionsRow     = view.findViewById(R.id.actionsRow)
        editActionsRow = view.findViewById(R.id.editActionsRow)

        btnEditProfile   = view.findViewById(R.id.btnEditProfile)
        btnSendMessage   = view.findViewById(R.id.btnSendMessage)
        btnViewHistory   = view.findViewById(R.id.btnViewHistory)
        btnCancelEdit    = view.findViewById(R.id.btnCancelEdit)
        btnSaveProfile   = view.findViewById(R.id.btnSaveProfile)
        btnDeleteProfile = view.findViewById(R.id.btnDeleteProfile)

        // dropdown adapters
        val statusOptions = listOf("Fulltime Player", "Part-time Player", "Trialist", "Injured")
        val roleOptions   = listOf("Goalkeeper", "Defender", "Midfielder", "Forward", "Center Striker")
        playerStatusDropdown.setAdapter(
            ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, statusOptions)
        )
        playerRoleDropdown.setAdapter(
            ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, roleOptions)
        )

        setupDatePicker()
    }

    private fun displayPlayerInfo(player: User) {
        playerName.text = player.fullName()
        playerAge.text  = player.dateOfBirth?.year?.let { "${java.time.LocalDate.now().year - it} yo" } ?: "--"

        playerNameInput.setText(player.fullName())
        playerNumberInput.setText(player.number?.toString() ?: "")
        playerEmailInput.setText(player.email ?: "")
        playerDateOfBirthInput.setText(player.dateOfBirth?.toString() ?: "")
        playerContactInput.setText("") // no contact in model

        playerStatusDropdown.setText(player.status?.name ?: "Active", false)
        playerRoleDropdown.setText(player.role.name, false)

        statsGoals.text       = "0"
        statsAssists.text     = "0"
        statsMatches.text     = "0"
        statsYellowCards.text = "0"
        statsRedCards.text    = "0"
        statsCleanSheets.text = "0"
    }

    private fun setupRoleVisibility() {
        when (userRole) {
            "coach", "assistant" -> {
                btnEditProfile.visibility = View.VISIBLE
                btnSendMessage.visibility = View.VISIBLE
                btnViewHistory.visibility = View.VISIBLE
            }
            "player" -> {
                btnEditProfile.visibility = View.VISIBLE
                btnSendMessage.visibility = View.VISIBLE
                btnViewHistory.visibility = View.VISIBLE
            }
            "guardian" -> {
                btnEditProfile.visibility = View.GONE
                btnSendMessage.visibility = View.VISIBLE
                btnViewHistory.visibility = View.VISIBLE
            }
            else -> {
                btnEditProfile.visibility = View.GONE
                btnSendMessage.visibility = View.GONE
                btnViewHistory.visibility = View.GONE
            }
        }
    }

    private fun setupActions() {
        btnEditProfile.setOnClickListener {
            if (isEditing) {
                currentPlayer?.let { displayPlayerInfo(it) }
                setEditing(false)
            } else {
                setEditing(true)
            }
        }
        btnSendMessage.setOnClickListener { showSendMessageDialog() }
        btnViewHistory.setOnClickListener { showPlayerHistory() }

        btnCancelEdit.setOnClickListener {
            currentPlayer?.let { displayPlayerInfo(it) }
            setEditing(false)
        }
        btnSaveProfile.setOnClickListener {
            if (validateInputs()) {
                currentPlayer?.let { updated ->
                    viewModel.updatePlayer(updated)
                    Snackbar.make(requireView(), "Profile updated successfully", Snackbar.LENGTH_SHORT).show()
                }
                setEditing(false)
            }
        }
        btnDeleteProfile.setOnClickListener {
            currentPlayer?.let { viewModel.deletePlayer(it) }
            Snackbar.make(requireView(), "Player deleted", Snackbar.LENGTH_SHORT).show()
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun setEditing(enabled: Boolean) {
        isEditing = enabled
        val inputs = listOf(
            playerNameInput, playerNumberInput, playerEmailInput,
            playerDateOfBirthInput, playerContactInput
        )
        inputs.forEach { it.isEnabled = enabled; it.alpha = if (enabled) 1f else 0.6f }
        listOf(playerStatusDropdown, playerRoleDropdown).forEach {
            it.isEnabled = enabled; it.alpha = if (enabled) 1f else 0.6f
        }

        actionsRow.visibility     = if (enabled) View.GONE  else View.VISIBLE
        editActionsRow.visibility = if (enabled) View.VISIBLE else View.GONE
        btnEditProfile.text = if (enabled) "Cancel Edit" else "Edit Profile"
    }

    private fun validateInputs(): Boolean {
        val name   = playerNameInput.text?.toString()?.trim().orEmpty()
        val number = playerNumberInput.text?.toString()?.trim().orEmpty()
        if (name.isEmpty()) { playerNameInput.error = "Name required"; return false }
        if (number.isEmpty()) { playerNumberInput.error = "Number required"; return false }
        return true
    }

    private fun showSendMessageDialog() {
        Snackbar.make(requireView(), "Messaging coming soon", Snackbar.LENGTH_SHORT).show()
    }

    private fun showPlayerHistory() {
        Snackbar.make(requireView(), "History coming soon", Snackbar.LENGTH_SHORT).show()
    }

    private fun setupDatePicker() {
        playerDateOfBirthInput.setOnClickListener {
            if (isEditing) showDatePicker()
        }
    }

    private fun showDatePicker() {
        val cal = java.util.Calendar.getInstance()
        val dialog = android.app.DatePickerDialog(
            requireContext(),
            { _, y, m, d -> playerDateOfBirthInput.setText(String.format("%04d-%02d-%02d", y, m + 1, d)) },
            cal.get(java.util.Calendar.YEAR),
            cal.get(java.util.Calendar.MONTH),
            cal.get(java.util.Calendar.DAY_OF_MONTH)
        )
        dialog.datePicker.maxDate = System.currentTimeMillis()
        dialog.show()
    }
}
