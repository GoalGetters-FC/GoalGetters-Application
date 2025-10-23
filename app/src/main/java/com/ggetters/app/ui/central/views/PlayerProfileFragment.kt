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
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import android.widget.ImageButton
import com.ggetters.app.R
import com.ggetters.app.data.model.User
import com.ggetters.app.data.model.UserRole
import com.ggetters.app.data.model.UserStatus
import com.ggetters.app.ui.central.viewmodels.HomeProfileViewModel
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import com.ggetters.app.core.utils.DateUtils
import com.ggetters.app.core.validation.UserValidationUtils
import com.ggetters.app.core.services.StatisticsService

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
    private lateinit var statisticsButton: ImageButton
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
    private lateinit var cardPlayerNumber: MaterialCardView

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
        
        // Use the same keyboard handling as the account tab
        requireActivity().window.setSoftInputMode(android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
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
        statisticsButton = view.findViewById(R.id.statisticsButton)
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
        cardPlayerNumber       = view.findViewById<MaterialCardView>(R.id.cardPlayerNumber)

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
        
        // Setup focus listeners to scroll to focused fields
        setupFocusListeners()
    }

    private fun setupFocusListeners() {
        // Simple focus handling like the account tab
        val inputFields = listOf(
            playerNameInput,
            playerNumberInput,
            playerEmailInput,
            playerDateOfBirthInput,
            playerContactInput,
            playerStatusDropdown,
            playerRoleDropdown
        )
        
        inputFields.forEach { field ->
            field.setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    // Simple scroll to field - let the system handle keyboard management
                    field.post {
                        field.requestFocus()
                    }
                }
            }
        }
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
                    lifecycleScope.launch {
                        try {
                            viewModel.updatePlayer(updated)
                            setEditing(false)
                            Snackbar.make(requireView(), "Player updated successfully", Snackbar.LENGTH_SHORT).show()
                        } catch (e: Exception) {
                            Snackbar.make(requireView(), "Failed to update player: ${e.message}", Snackbar.LENGTH_LONG).show()
                        }
                    }
                }
            }
        }

        btnDeleteProfile.setOnClickListener {
            showDeletePlayerConfirmation()
        }

        // Statistics button
        statisticsButton.setOnClickListener {
            currentPlayer?.let { player ->
                navigateToStatistics(player.id)
            }
        }

    }

    private fun displayPlayerInfo(player: User) {
        playerName.text = player.fullName()
        playerAge.text  = player.dateOfBirth?.year?.let { "${java.time.LocalDate.now().year - it} yo" } ?: "--"

        playerNameInput.setText(player.fullName())
        playerNumberInput.setText(player.number?.toString() ?: "")
        playerEmailInput.setText(player.email ?: "")
        playerDateOfBirthInput.setText(player.dateOfBirth?.let { DateUtils.formatForDisplay(it) } ?: "")
        playerContactInput.setText("") // no contact field in User

        playerStatusDropdown.setText(player.status?.name ?: UserStatus.ACTIVE.name)
        playerRoleDropdown.setText(player.role.name)
        
        // Update UI based on role
        updateUIForRole(player)

        // Load real-time statistics
        loadPlayerStatistics(player)
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

        // Simple keyboard handling like the account tab
        if (enabled) {
            playerNameInput.requestFocus()
        } else {
            val imm = requireActivity()
                .getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
            imm.hideSoftInputFromWindow(requireView().windowToken, 0)
        }
    }

    private fun validateInputs(): Boolean {
        val fullName = playerNameInput.text?.toString()?.trim().orEmpty()
        val parts = fullName.split(" ", limit = 2)
        val firstName = parts.getOrNull(0) ?: ""
        val lastName = parts.getOrNull(1) ?: ""
        val email = playerEmailInput.text?.toString()?.trim()
        val number = playerNumberInput.text?.toString()?.trim()
        val dateOfBirth = playerDateOfBirthInput.text?.toString()?.trim()
        val phone = playerContactInput.text?.toString()?.trim()
        val position = "" // Not implemented in current UI
        val role = playerRoleDropdown.text?.toString()?.trim() ?: "FULL_TIME_PLAYER"
        val status = playerStatusDropdown.text?.toString()?.trim() ?: "ACTIVE"
        
        val validation = UserValidationUtils.validateUserData(
            firstName, lastName, email, number, dateOfBirth, phone, position, role, status
        )
        
        if (!validation.isValid) {
            // Clear previous errors
            clearFieldErrors()
            
            // Show validation errors
            val errorMessage = UserValidationUtils.getErrorMessage(validation.errors)
            Snackbar.make(requireView(), errorMessage, Snackbar.LENGTH_LONG).show()
            
            // Set specific field errors
            if (firstName.isBlank()) playerNameInput.error = "Name required"
            if (email.isNullOrBlank()) playerEmailInput.error = "Email required"
            if (!number.isNullOrBlank() && !UserValidationUtils.isValidPlayerNumber(number)) {
                playerNumberInput.error = "Number must be 1-99"
            }
            if (!dateOfBirth.isNullOrBlank() && !UserValidationUtils.isValidDateOfBirth(dateOfBirth)) {
                playerDateOfBirthInput.error = "Invalid date"
            }
            
            return false
        }
        
        return true
    }
    
    private fun clearFieldErrors() {
        playerNameInput.error = null
        playerEmailInput.error = null
        playerNumberInput.error = null
        playerDateOfBirthInput.error = null
        playerContactInput.error = null
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
            playerDateOfBirthInput.text?.toString()?.takeIf { it.isNotBlank() }?.let { DateUtils.parseDate(it) }
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
            showDatePicker()
        }
    }

    private fun showDatePicker() {
        // Try to parse existing date, or use current date as fallback
        val existingDate = playerDateOfBirthInput.text?.toString()?.let { dateStr ->
            DateUtils.parseDate(dateStr)
        } ?: DateUtils.getCurrentDate()
        
        val (year, month, day) = DateUtils.getDatePickerValues(existingDate)
        
        val dialog = android.app.DatePickerDialog(
            requireContext(),
            { _, y, m, d -> 
                val selectedDate = DateUtils.createDateFromPicker(y, m, d)
                playerDateOfBirthInput.setText(DateUtils.formatForDisplay(selectedDate))
            },
            year,
            month,
            day
        )
        
        // Set reasonable date range (5 to 100 years old)
        val calendar = java.util.Calendar.getInstance()
        val currentYear = calendar.get(java.util.Calendar.YEAR)
        val minYear = currentYear - 100
        val maxYear = currentYear - 5
        
        dialog.datePicker.minDate = java.util.Calendar.getInstance().apply {
            set(minYear, 0, 1)
        }.timeInMillis
        
        dialog.datePicker.maxDate = java.util.Calendar.getInstance().apply {
            set(maxYear, 11, 31)
        }.timeInMillis
        
        dialog.show()
    }

    private fun showDeletePlayerConfirmation() {
        currentPlayer?.let { player ->
            android.app.AlertDialog.Builder(requireContext())
                .setTitle("Delete Player")
                .setMessage("Are you sure you want to delete ${player.fullName()}? This action cannot be undone and will remove all associated data including attendance records and statistics.")
                .setPositiveButton("Delete") { _, _ ->
                    deletePlayer(player)
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    private fun deletePlayer(player: User) {
        lifecycleScope.launch {
            try {
                viewModel.deletePlayer(player)
                Snackbar.make(requireView(), "Player deleted successfully", Snackbar.LENGTH_SHORT).show()
                // Navigate back to previous fragment
                if (parentFragmentManager.backStackEntryCount > 0) {
                    parentFragmentManager.popBackStack()
                } else {
                    parentFragmentManager.beginTransaction().remove(this@PlayerProfileFragment).commit()
                }
            } catch (e: Exception) {
                Snackbar.make(requireView(), "Failed to delete player: ${e.message}", Snackbar.LENGTH_LONG).show()
            }
        }
    }
    
    private fun updateUIForRole(player: User) {
        when (player.role) {
            UserRole.COACH -> {
                // Hide player-specific fields for coaches
                cardPlayerNumber.visibility = View.GONE
            }
            UserRole.FULL_TIME_PLAYER, UserRole.PART_TIME_PLAYER, UserRole.COACH_PLAYER -> {
                // Show player-specific fields for players
                cardPlayerNumber.visibility = View.VISIBLE
            }
            else -> {
                // Default: show all fields
                cardPlayerNumber.visibility = View.VISIBLE
            }
        }
    }

    private fun navigateToStatistics(playerId: String) {
        val statisticsFragment = StatisticsFragment.newInstance(playerId)
        parentFragmentManager.beginTransaction()
            .replace(android.R.id.content, statisticsFragment)
            .addToBackStack("statistics")
            .commit()
    }

    private fun loadPlayerStatistics(player: User) {
        // For now, we'll use mock statistics
        // In a real implementation, this would use dependency injection to get StatisticsService
        statsGoals.text = "0"
        statsAssists.text = "0"
        statsMatches.text = "0"
        statsYellowCards.text = "0"
        statsRedCards.text = "0"
        statsCleanSheets.text = "0"
    }
}

