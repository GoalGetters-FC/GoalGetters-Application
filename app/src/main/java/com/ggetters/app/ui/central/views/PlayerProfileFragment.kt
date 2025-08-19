package com.ggetters.app.ui.central.views

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.ggetters.app.R
import com.ggetters.app.ui.central.models.Player
import com.ggetters.app.ui.central.models.PlayerStats
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import android.widget.AutoCompleteTextView
import android.widget.ArrayAdapter

class PlayerProfileFragment : Fragment() {
    
    companion object {
        private const val ARG_PLAYER_ID = "player_id"
        private const val ARG_START_EDITING = "start_editing"
        
        fun newInstance(playerId: String, startEditing: Boolean = false): PlayerProfileFragment {
            val fragment = PlayerProfileFragment()
            val args = Bundle()
            args.putString(ARG_PLAYER_ID, playerId)
            args.putBoolean(ARG_START_EDITING, startEditing)
            fragment.arguments = args
            return fragment
        }
    }
    
    private var playerId: String? = null
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
    
    // Statistics
    private lateinit var statsGoals: TextView
    private lateinit var statsAssists: TextView
    private lateinit var statsMatches: TextView
    private lateinit var statsYellowCards: TextView
    private lateinit var statsRedCards: TextView
    private lateinit var statsCleanSheets: TextView
    
    // Action buttons
    private lateinit var btnEditProfile: MaterialButton
    private lateinit var btnSendMessage: MaterialButton
    private lateinit var btnViewHistory: MaterialButton
    private lateinit var editActionsRow: View
    private var actionButtonsRow: ViewGroup? = null
    
    // TODO: Backend - Get user role from backend/UserRepository
    private val userRole = "coach"

    private var isEditing: Boolean = false
    private var currentPlayer: Player? = null
    
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
    ): View? {
        return inflater.inflate(R.layout.fragment_player_profile, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews(view)
        loadPlayerProfile()
        setupRoleVisibility()
        setupActions()
        // If launched to edit directly
        if (isEditing) setEditing(true)
    }
    
    private fun setupViews(view: View) {
        playerAvatar = view.findViewById(R.id.playerAvatar)
        playerName = view.findViewById(R.id.playerName)
        playerAge = view.findViewById(R.id.playerAge)
        
        // Form inputs
        playerNameInput = view.findViewById(R.id.playerNameInput)
        playerNumberInput = view.findViewById(R.id.playerNumberInput)
        playerEmailInput = view.findViewById(R.id.playerEmailInput)
        playerDateOfBirthInput = view.findViewById(R.id.playerDateOfBirthInput)
        playerContactInput = view.findViewById(R.id.playerContactInput)
        playerStatusDropdown = view.findViewById(R.id.playerStatusDropdown)
        playerRoleDropdown = view.findViewById(R.id.playerRoleDropdown)
        
        // Statistics
        statsGoals = view.findViewById(R.id.statsGoals)
        statsAssists = view.findViewById(R.id.statsAssists)
        statsMatches = view.findViewById(R.id.statsMatches)
        statsYellowCards = view.findViewById(R.id.statsYellowCards)
        statsRedCards = view.findViewById(R.id.statsRedCards)
        statsCleanSheets = view.findViewById(R.id.statsCleanSheets)
        
        // Action buttons
        btnEditProfile = view.findViewById(R.id.btnEditProfile)
        btnSendMessage = view.findViewById(R.id.btnSendMessage)
        btnViewHistory = view.findViewById(R.id.btnViewHistory)
        editActionsRow = view.findViewById(R.id.editActionsRow)
        actionButtonsRow = btnEditProfile.parent as? ViewGroup

        // Setup dropdown adapters
        val statusOptions = listOf("Fulltime Player", "Part-time Player", "Trialist", "Injured")
        val roleOptions = listOf("Goalkeeper", "Defender", "Midfielder", "Forward", "Center Striker")
        val statusAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, statusOptions)
        val roleAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, roleOptions)
        playerStatusDropdown.setAdapter(statusAdapter)
        playerRoleDropdown.setAdapter(roleAdapter)
        
        // Setup date picker for date of birth
        setupDatePicker()
    }
    
    private fun loadPlayerProfile() {
        // TODO: Backend - Fetch player data from backend using playerId
        // val player = playerRepo.getById(playerId)
        
        // Sample data for demo
        val player = Player(
            id = playerId ?: "1",
            firstName = "John",
            lastName = "Doe",
            position = "Forward",
            jerseyNumber = "10",
            avatar = null,
            isActive = true,
            stats = PlayerStats(
                goals = 15,
                assists = 8,
                matches = 25,
                yellowCards = 2,
                redCards = 0,
                cleanSheets = 0
            ),
            email = "john.doe@example.com",
            phone = "+1 234 567 8900",
            dateOfBirth = "1995-03-15",
            joinedDate = "2024-01-15"
        )
        
        currentPlayer = player
        displayPlayerInfo(player)
    }
    
    private fun displayPlayerInfo(player: Player) {
        playerName.text = player.name
        playerAge.text = calculateAge(player.dateOfBirth) + " yo"
        
        // Populate form fields
        playerNameInput.setText(player.name)
        playerNumberInput.setText(player.jerseyNumber)
        playerEmailInput.setText(player.email ?: "")
        playerDateOfBirthInput.setText(player.dateOfBirth ?: "")
        playerContactInput.setText(player.phone ?: "")
        
        // Set dropdown values
        playerStatusDropdown.setText("Fulltime Player")
        playerRoleDropdown.setText(player.position)
        
        // Display statistics
        statsGoals.text = player.stats.goals.toString()
        statsAssists.text = player.stats.assists.toString()
        statsMatches.text = player.stats.matches.toString()
        statsYellowCards.text = player.stats.yellowCards.toString()
        statsRedCards.text = player.stats.redCards.toString()
        statsCleanSheets.text = player.stats.cleanSheets.toString()
        
        // TODO: Load player avatar using Glide/Coil
        // Glide.with(this).load(player.avatar).into(playerAvatar)
    }
    
    private fun calculateAge(dateOfBirth: String?): String {
        if (dateOfBirth.isNullOrEmpty()) return "Unknown"
        
        return try {
            val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
            val birthDate = dateFormat.parse(dateOfBirth)
            if (birthDate != null) {
                val today = java.util.Calendar.getInstance()
                val birthCalendar = java.util.Calendar.getInstance()
                birthCalendar.time = birthDate
                
                var age = today.get(java.util.Calendar.YEAR) - birthCalendar.get(java.util.Calendar.YEAR)
                if (today.get(java.util.Calendar.DAY_OF_YEAR) < birthCalendar.get(java.util.Calendar.DAY_OF_YEAR)) {
                    age--
                }
                age.toString()
            } else {
                "Unknown"
            }
        } catch (e: Exception) {
            "Unknown"
        }
    }
    
    private fun setupRoleVisibility() {
        // Role-based visibility for actions
        when (userRole) {
            "coach", "assistant" -> {
                // Coaches and assistants can edit profiles and send messages
                btnEditProfile.visibility = View.VISIBLE
                btnSendMessage.visibility = View.VISIBLE
                btnViewHistory.visibility = View.VISIBLE
            }
            "player" -> {
                // Players can only view their own profile or send messages
                if (playerId == "current_user_id") { // TODO: Compare with current user ID
                    btnEditProfile.visibility = View.VISIBLE
                } else {
                    btnEditProfile.visibility = View.GONE
                }
                btnSendMessage.visibility = View.VISIBLE
                btnViewHistory.visibility = View.VISIBLE
            }
            "guardian" -> {
                // Guardians can only view their child's profile
                btnEditProfile.visibility = View.GONE
                btnSendMessage.visibility = View.VISIBLE
                btnViewHistory.visibility = View.VISIBLE
            }
            else -> {
                // Default: hide all actions
                btnEditProfile.visibility = View.GONE
                btnSendMessage.visibility = View.GONE
                btnViewHistory.visibility = View.GONE
            }
        }
    }
    
    private fun setupActions() {
        btnEditProfile.setOnClickListener {
            if (isEditing) {
                // Cancel edit mode
                currentPlayer?.let { displayPlayerInfo(it) }
                setEditing(false)
            } else {
                // Enter edit mode
                setEditing(true)
            }
        }
        
        btnSendMessage.setOnClickListener {
            showSendMessageDialog()
        }
        
        btnViewHistory.setOnClickListener {
            showPlayerHistory()
        }

        view?.findViewById<MaterialButton>(R.id.btnCancelEdit)?.setOnClickListener {
            // Revert edits
            currentPlayer?.let { displayPlayerInfo(it) }
            setEditing(false)
        }

        view?.findViewById<MaterialButton>(R.id.btnSaveProfile)?.setOnClickListener {
            if (validateInputs()) {
                val updated = collectPlayerFromInputs()
                // TODO: Persist via repository (e.g., UserRepository.upsert)
                currentPlayer = updated
                displayPlayerInfo(updated)
                setEditing(false)
                Snackbar.make(requireView(), "Profile updated successfully", Snackbar.LENGTH_SHORT).show()
            }
        }

        view?.findViewById<MaterialButton>(R.id.btnDeleteProfile)?.setOnClickListener {
            confirmDelete()
        }
    }
    
    private fun showEditProfileDialog() {
        // TODO: Backend - Show edit profile dialog/screen
        Snackbar.make(requireView(), "Edit profile functionality coming soon", Snackbar.LENGTH_SHORT).show()
    }

    private fun setEditing(enabled: Boolean) {
        isEditing = enabled
        
        // Toggle input states with visual feedback
        val inputFields = listOf(
            playerNameInput, playerNumberInput, playerEmailInput, 
            playerDateOfBirthInput, playerContactInput
        )
        
        inputFields.forEach { field ->
            field.isEnabled = enabled
            field.alpha = if (enabled) 1.0f else 0.6f
        }
        
        // Toggle dropdown states
        val dropdowns = listOf(playerStatusDropdown, playerRoleDropdown)
        dropdowns.forEach { dropdown ->
            dropdown.isEnabled = enabled
            dropdown.isClickable = enabled
            dropdown.isFocusable = enabled
            dropdown.alpha = if (enabled) 1.0f else 0.6f
        }

        // Toggle action rows
        actionButtonsRow?.visibility = if (enabled) View.GONE else View.VISIBLE
        editActionsRow.visibility = if (enabled) View.VISIBLE else View.GONE

        // Update button text and icon
        btnEditProfile.text = if (enabled) "Cancel Edit" else "Edit Profile"
        
        if (enabled) {
            playerNameInput.requestFocus()
            // Show keyboard
            val imm = requireActivity().getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
            imm.showSoftInput(playerNameInput, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT)
        } else {
            // Hide keyboard
            val imm = requireActivity().getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
            imm.hideSoftInputFromWindow(requireView().windowToken, 0)
        }
    }

    private fun validateInputs(): Boolean {
        val name = playerNameInput.text?.toString()?.trim().orEmpty()
        val number = playerNumberInput.text?.toString()?.trim().orEmpty()
        val email = playerEmailInput.text?.toString()?.trim().orEmpty()
        val contact = playerContactInput.text?.toString()?.trim().orEmpty()
        val role = playerRoleDropdown.text?.toString()?.trim().orEmpty()
        val status = playerStatusDropdown.text?.toString()?.trim().orEmpty()
        
        if (name.isEmpty()) {
            playerNameInput.error = "Name is required"
            return false
        }
        
        if (number.isEmpty()) {
            playerNumberInput.error = "Player number is required"
            return false
        }
        
        if (email.isNotEmpty() && !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            playerEmailInput.error = "Invalid email format"
            return false
        }
        
        if (contact.isNotEmpty() && contact.length < 10) {
            playerContactInput.error = "Contact number too short"
            return false
        }
        
        if (role.isEmpty()) {
            playerRoleDropdown.error = "Player role is required"
            return false
        }
        
        if (status.isEmpty()) {
            playerStatusDropdown.error = "Player status is required"
            return false
        }
        
        return true
    }

    private fun collectPlayerFromInputs(): Player {
        val existing = currentPlayer
        val fullName = playerNameInput.text?.toString()?.trim().orEmpty()
        val nameParts = fullName.split(' ', limit = 2)
        val firstName = nameParts.getOrNull(0) ?: ""
        val lastName = nameParts.getOrNull(1) ?: ""

        return Player(
            id = existing?.id ?: (playerId ?: "1"),
            firstName = firstName,
            lastName = lastName,
            position = playerRoleDropdown.text?.toString()?.trim().orEmpty(),
            jerseyNumber = playerNumberInput.text?.toString()?.trim().orEmpty(),
            avatar = existing?.avatar,
            isActive = existing?.isActive ?: true,
            stats = existing?.stats ?: PlayerStats(0,0,0,0,0,0),
            email = playerEmailInput.text?.toString()?.trim(),
            phone = playerContactInput.text?.toString()?.trim(),
            dateOfBirth = playerDateOfBirthInput.text?.toString()?.trim(),
            joinedDate = existing?.joinedDate
        )
    }

    private fun confirmDelete() {
        val name = currentPlayer?.name ?: "this player"
        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Delete Player")
            .setMessage("Are you sure you want to delete $name? This action cannot be undone.")
            .setPositiveButton("Delete") { _, _ ->
                deletePlayer()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deletePlayer() {
        // TODO: Backend - Remove player from repository
        // e.g., playerRepo.delete(currentPlayer.id)
        Snackbar.make(requireView(), "Player deleted", Snackbar.LENGTH_SHORT).show()
        requireActivity().onBackPressedDispatcher.onBackPressed()
    }
    
    private fun showSendMessageDialog() {
        // TODO: Backend - Implement messaging functionality
        Snackbar.make(requireView(), "Messaging functionality coming soon", Snackbar.LENGTH_SHORT).show()
    }
    
    private fun setupDatePicker() {
        playerDateOfBirthInput.setOnClickListener {
            if (isEditing) {
                showDatePicker()
            }
        }
    }
    
    private fun showDatePicker() {
        val calendar = java.util.Calendar.getInstance()
        
        // Try to parse existing date
        val existingDate = playerDateOfBirthInput.text?.toString()
        if (!existingDate.isNullOrEmpty()) {
            try {
                val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                val date = dateFormat.parse(existingDate)
                if (date != null) {
                    calendar.time = date
                }
            } catch (e: Exception) {
                // Use current date if parsing fails
            }
        }
        
        val datePickerDialog = android.app.DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                val selectedDate = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth)
                playerDateOfBirthInput.setText(selectedDate)
            },
            calendar.get(java.util.Calendar.YEAR),
            calendar.get(java.util.Calendar.MONTH),
            calendar.get(java.util.Calendar.DAY_OF_MONTH)
        )
        
        // Set max date to today (no future dates for date of birth)
        datePickerDialog.datePicker.maxDate = System.currentTimeMillis()
        datePickerDialog.show()
    }
    
    private fun showPlayerHistory() {
        // TODO: Backend - Show player performance history
        Snackbar.make(requireView(), "Player history functionality coming soon", Snackbar.LENGTH_SHORT).show()
    }
} 