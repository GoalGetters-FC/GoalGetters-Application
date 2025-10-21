package com.ggetters.app.ui.central.views

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.WindowCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import android.widget.ImageButton
import com.ggetters.app.R
import com.ggetters.app.data.model.User
import com.ggetters.app.data.model.UserRole
import com.ggetters.app.data.model.UserStatus
import com.ggetters.app.ui.central.viewmodels.HomeProfileViewModel
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@AndroidEntryPoint
class PlayerProfileFragment : Fragment() {

    companion object {
        private const val ARG_PLAYER_ID = "player_id" // need to pass the actual id from db
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
    private lateinit var backButton: ImageButton
    private lateinit var playerAvatar: ImageView
    private lateinit var playerName: TextView
    private lateinit var playerAge: TextView

    // Form inputs
    private lateinit var playerNameInput: TextInputEditText
    private lateinit var playerNumberInput: TextInputEditText
    private lateinit var playerEmailInput: TextInputEditText
    private lateinit var playerDateOfBirthInput: TextInputEditText
    private lateinit var playerContactInput: TextInputEditText
    private lateinit var playerStatusDropdown: TextInputEditText
    private lateinit var playerRoleDropdown: TextInputEditText

    // Statistics (placeholders)
    private lateinit var statsGoals: TextView
    private lateinit var statsAssists: TextView
    private lateinit var statsMatches: TextView
    private lateinit var statsYellowCards: TextView
    private lateinit var statsRedCards: TextView
    private lateinit var statsCleanSheets: TextView

    // Action buttons
    private lateinit var btnEditProfile: MaterialButton
    private lateinit var btnCancelEdit: MaterialButton
    private lateinit var btnSaveProfile: MaterialButton
    private lateinit var btnDeleteProfile: MaterialButton

    private val userRole = "coach" // TODO: inject/derive real role

    private fun setupStatusBar() {
        // Hide the system status bar to use our custom header
        requireActivity().window.statusBarColor = android.graphics.Color.parseColor("#161620")
        
        // Set up window insets controller for dark status bar
        val windowInsetsController = WindowCompat.getInsetsController(requireActivity().window, requireActivity().window.decorView)
        windowInsetsController.isAppearanceLightStatusBars = false // Dark status bar icons for dark background
    }

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
        setupStatusBar()
        setupViews(view)
        setupRoleVisibility()
        setupActions()

        // Ensure UI matches editing state immediately
        setEditing(isEditing)

        // If no playerId passed, load the currently logged-in user
        if (playerId.isNullOrBlank()) {
            viewModel.loadCurrentUser()
        } else {
            viewModel.loadPlayer(playerId!!)
        }

        viewModel.player.observe(viewLifecycleOwner, Observer { player ->
            player?.let {
                currentPlayer = it
                displayPlayerInfo(it)
                // (optional) refresh button visibility based on actual role
                // setupRoleVisibility()  // uncomment if you want role-based buttons to re-evaluate
            }
        })
    }


    // 🔙 Added back exactly as requested (formatted + safe)
    private fun setupRoleVisibility() {
        when (userRole) {
            "coach", "assistant" -> {
                btnEditProfile.visibility = View.VISIBLE
                btnDeleteProfile.visibility = View.VISIBLE
            }
            "player" -> {
                btnEditProfile.visibility = View.VISIBLE
                btnDeleteProfile.visibility = View.GONE
            }
            "guardian" -> {
                btnEditProfile.visibility = View.GONE
                btnDeleteProfile.visibility = View.GONE
            }
            else -> {
                btnEditProfile.visibility = View.GONE
                btnDeleteProfile.visibility = View.GONE
            }
        }
    }

    private fun setupViews(view: View) {
        // Header
        backButton = view.findViewById(R.id.backButton)
        playerAvatar = view.findViewById(R.id.playerAvatar)
        playerName   = view.findViewById(R.id.playerName)
        playerAge    = view.findViewById(R.id.playerAge)

        playerNameInput        = view.findViewById(R.id.playerNameInput)
        playerNumberInput      = view.findViewById(R.id.playerNumberInput)
        playerEmailInput       = view.findViewById(R.id.playerEmailInput)
        playerDateOfBirthInput = view.findViewById(R.id.playerDateOfBirthInput)
        playerContactInput     = view.findViewById(R.id.playerContactInput)
        playerStatusDropdown   = view.findViewById(R.id.playerStatusDropdown)
        playerRoleDropdown     = view.findViewById(R.id.playerRoleDropdown)

        statsGoals        = view.findViewById(R.id.statsGoals)
        statsAssists      = view.findViewById(R.id.statsAssists)
        statsMatches      = view.findViewById(R.id.statsMatches)
        statsYellowCards  = view.findViewById(R.id.statsYellowCards)
        statsRedCards     = view.findViewById(R.id.statsRedCards)
        statsCleanSheets  = view.findViewById(R.id.statsCleanSheets)

        btnEditProfile   = view.findViewById(R.id.btnEditProfile)
        btnCancelEdit    = view.findViewById(R.id.btnCancelEdit)
        btnSaveProfile   = view.findViewById(R.id.btnSaveProfile)
        btnDeleteProfile = view.findViewById(R.id.btnDeleteProfile)

        // Setup dropdowns with click listeners for selection dialogs
        setupDropdowns()

        setupDatePicker()
    }

    private fun setupDropdowns() {
        val statusOptions = listOf(UserStatus.ACTIVE.name, UserStatus.INJURY.name)
        val roleOptions = UserRole.values().map { it.name }

        playerStatusDropdown.setOnClickListener {
            if (isEditing) {
                showSelectionDialog("Select Status", statusOptions) { selected ->
                    playerStatusDropdown.setText(selected)
                }
            }
        }

        playerRoleDropdown.setOnClickListener {
            if (isEditing) {
                showSelectionDialog("Select Role", roleOptions) { selected ->
                    playerRoleDropdown.setText(selected)
                }
            }
        }
    }

    private fun showSelectionDialog(title: String, options: List<String>, onSelected: (String) -> Unit) {
        val builder = android.app.AlertDialog.Builder(requireContext())
        builder.setTitle(title)
            .setItems(options.toTypedArray()) { _, which ->
                onSelected(options[which])
            }
            .show()
    }

    private fun setupActions() {
        // Back button - use proper fragment navigation
        backButton.setOnClickListener {
            // Use the fragment's parent fragment manager to pop back stack
            if (parentFragmentManager.backStackEntryCount > 0) {
                parentFragmentManager.popBackStack()
            } else {
                // If no back stack, use the activity's back press dispatcher
                requireActivity().onBackPressedDispatcher.onBackPressed()
            }
        }

        // Edit toggle
        btnEditProfile.setOnClickListener { setEditing(!isEditing) }

        btnCancelEdit.setOnClickListener {
            currentPlayer?.let { displayPlayerInfo(it) }
            setEditing(false)
        }

        btnSaveProfile.setOnClickListener {
            if (validateInputs()) {
                currentPlayer?.let { existing ->
                    val updated = buildUserFromInputs(existing)
                    viewModel.updatePlayer(updated)
                    Snackbar.make(requireView(), "Profile updated", Snackbar.LENGTH_SHORT).show()
                    setEditing(false)
                }
            }
        }

        btnDeleteProfile.setOnClickListener {
            currentPlayer?.let { viewModel.deletePlayer(it) }
            Snackbar.make(requireView(), "Player deleted", Snackbar.LENGTH_SHORT).show()
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

    }

    private fun displayPlayerInfo(player: User) {
        playerName.text = player.fullName()
        playerAge.text  = player.dateOfBirth?.year?.let { "${java.time.LocalDate.now().year - it} yo" } ?: "--"

        playerNameInput.setText(player.fullName())
        playerNumberInput.setText(player.number?.toString() ?: "")
        playerEmailInput.setText(player.email ?: "")
        playerDateOfBirthInput.setText(player.dateOfBirth?.format(DateTimeFormatter.ISO_DATE) ?: "")
        playerContactInput.setText("") // no contact field in User

        playerStatusDropdown.setText(player.status?.name ?: UserStatus.ACTIVE.name)
        playerRoleDropdown.setText(player.role.name)

        // Stats placeholders
        statsGoals.text       = "0"
        statsAssists.text     = "0"
        statsMatches.text     = "0"
        statsYellowCards.text = "0"
        statsRedCards.text    = "0"
        statsCleanSheets.text = "0"
    }

    private fun setEditing(enabled: Boolean) {
        isEditing = enabled

        val inputs = listOf(
            playerNameInput, playerNumberInput, playerEmailInput,
            playerDateOfBirthInput, playerContactInput
        )
        inputs.forEach { field ->
            field.isEnabled = enabled
            field.alpha = if (enabled) 1f else 0.6f
        }

        listOf(playerStatusDropdown, playerRoleDropdown).forEach { dd ->
            dd.isEnabled = enabled
            dd.isClickable = enabled
            dd.isFocusable = enabled
            dd.alpha = if (enabled) 1f else 0.6f
        }

        btnEditProfile.visibility = if (enabled) View.GONE else View.VISIBLE
        btnSaveProfile.visibility = if (enabled) View.VISIBLE else View.GONE
        btnCancelEdit.visibility = if (enabled) View.VISIBLE else View.GONE

        btnEditProfile.text = if (enabled) "Cancel Edit" else "Edit Profile"

        val imm = requireActivity()
            .getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
        if (enabled) {
            playerNameInput.requestFocus()
            imm.showSoftInput(playerNameInput, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT)
        } else {
            imm.hideSoftInputFromWindow(requireView().windowToken, 0)
        }
    }

    private fun validateInputs(): Boolean {
        val name   = playerNameInput.text?.toString()?.trim().orEmpty()
        val number = playerNumberInput.text?.toString()?.trim().orEmpty()
        if (name.isEmpty()) { playerNameInput.error = "Name required"; return false }
        if (number.isEmpty()) { playerNumberInput.error = "Number required"; return false }
        return true
    }

    private fun buildUserFromInputs(existing: User): User {
        val fullName = playerNameInput.text?.toString()?.trim().orEmpty()
        val parts = fullName.split(" ", limit = 2)
        val first = parts.getOrNull(0) ?: ""
        val last  = parts.getOrNull(1) ?: ""

        val role = runCatching {
            UserRole.valueOf(playerRoleDropdown.text.toString().trim())
        }.getOrDefault(existing.role)

        val status = runCatching {
            UserStatus.valueOf(playerStatusDropdown.text.toString().trim().uppercase())
        }.getOrDefault(existing.status ?: UserStatus.ACTIVE)

        val dob = runCatching {
            playerDateOfBirthInput.text?.toString()?.takeIf { it.isNotBlank() }?.let { LocalDate.parse(it) }
        }.getOrNull()

        return existing.copy(
            name = first,
            surname = last,
            number = playerNumberInput.text?.toString()?.toIntOrNull(),
            email = playerEmailInput.text?.toString(),
            role = role,
            status = status,
            dateOfBirth = dob
        )
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

