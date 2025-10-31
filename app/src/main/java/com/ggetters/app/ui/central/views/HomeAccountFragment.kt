package com.ggetters.app.ui.central.views

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.ggetters.app.R
import com.ggetters.app.core.utils.Clogger
import com.ggetters.app.data.model.User
import com.ggetters.app.data.model.UserPosition
import com.ggetters.app.data.model.UserRole
import com.ggetters.app.ui.central.models.AppbarTheme
import com.ggetters.app.ui.central.models.HomeUiConfiguration
import com.ggetters.app.ui.central.viewmodels.HomeViewModel
import com.ggetters.app.ui.central.viewmodels.ProfileViewModel
import com.ggetters.app.ui.shared.components.HomeViewHeaderWidget
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import com.ggetters.app.core.utils.DateUtils

@AndroidEntryPoint
class HomeAccountFragment : Fragment() {
    companion object {
        private const val TAG = "HomeAccountFragment"
    }

    private val activeModel: ProfileViewModel by viewModels()
    private val sharedModel: HomeViewModel by activityViewModels()

    // Personal Information
    private lateinit var firstNameInput: EditText
    private lateinit var lastNameInput: EditText
    private lateinit var emailInput: EditText
    private lateinit var aliasInput: EditText
    private lateinit var dateOfBirthInput: EditText

    // Football Information
    private lateinit var positionInput: EditText
    private lateinit var roleInput: EditText
    private lateinit var statusText: TextView
    private lateinit var cardPosition: MaterialCardView

    // Header elements
    private lateinit var widgetHeader: HomeViewHeaderWidget

    // Team Information
    private lateinit var teamNameText: TextView
    private lateinit var teamCodeText: TextView
    private lateinit var btnShareTeamCode: ImageButton

    // Action Buttons
    private lateinit var editButton: MaterialButton
    private lateinit var saveButton: MaterialButton
    private lateinit var cancelButton: MaterialButton
    private lateinit var deleteAccountButton: MaterialButton

    private var isEditMode = false
    private var originalUser: User? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_home_account, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews(view)
        setupClickListeners()
        observeUserData()
        observeActiveTeam()

        sharedModel.useViewConfiguration(
            HomeUiConfiguration(
                appBarColor = AppbarTheme.NIGHT,
                appBarTitle = "",
                appBarShown = true,
            )
        )
    }

    private fun setupViews(view: View) {
        // Header elements
        widgetHeader = view.findViewById(R.id.widget_header)

        // Personal Information
        firstNameInput = view.findViewById(R.id.etFirstName)
        lastNameInput = view.findViewById(R.id.etLastName)
        emailInput = view.findViewById(R.id.etEmail)
        aliasInput = view.findViewById(R.id.etAlias)
        dateOfBirthInput = view.findViewById(R.id.etDateOfBirth)

        // Football Information
        positionInput = view.findViewById(R.id.etPosition)
        roleInput = view.findViewById(R.id.etRole)
        statusText = view.findViewById(R.id.tvStatus)
        cardPosition = view.findViewById<MaterialCardView>(R.id.cardPosition)

        // Team Information
        teamNameText = view.findViewById(R.id.tvTeamName)
        teamCodeText = view.findViewById(R.id.tvTeamCode)
        btnShareTeamCode = view.findViewById<ImageButton>(R.id.btnShareTeamCode)

        // Action Buttons
        editButton = view.findViewById(R.id.btnEdit)
        saveButton = view.findViewById(R.id.btnSave)
        cancelButton = view.findViewById(R.id.btnCancel)
        deleteAccountButton = view.findViewById(R.id.btnDeleteAccount)

        // Initially disable all inputs
        setInputsEnabled(false)
        updateButtonVisibility()

        // Setup role selection click listener
        setupRoleSelection()
        
        // Setup date picker
        setupDatePicker()
        
        // Setup role-based UI
        setupRoleBasedUI()
        
        // Setup team code sharing
        setupTeamCodeSharing()
    }

    private fun setupRoleSelection() {
        roleInput.setOnClickListener {
            if (isEditMode) {
                showRoleSelectionDialog()
            }
        }
    }

    private fun showRoleSelectionDialog() {
        val roles = UserRole.values()
        val roleNames = roles.map {
            it.name.replace("_", " ").lowercase().replaceFirstChar { char -> char.uppercase() }
        }.toTypedArray()

        val currentRoleIndex = roles.indexOfFirst {
            it.name.replace("_", " ").lowercase()
                .replaceFirstChar { char -> char.uppercase() } == roleInput.text.toString()
        }

        AlertDialog.Builder(requireContext(), R.style.Theme_GoalGetters_Dialog)
            .setTitle("Select Role")
            .setSingleChoiceItems(roleNames, currentRoleIndex) { dialog, which ->
                val selectedRole = roles[which]
                roleInput.setText(roleNames[which])
                dialog.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun setupClickListeners() {
        editButton.setOnClickListener { enableEditMode() }
        saveButton.setOnClickListener { saveChanges() }
        cancelButton.setOnClickListener { cancelEdit() }
        deleteAccountButton.setOnClickListener { showDeleteAccountConfirmation() }
    }
    
    private fun setupDatePicker() {
        dateOfBirthInput.setOnClickListener {
            showDatePicker()
        }
    }
    
    private fun showDatePicker() {
        // Try to parse existing date, or use current date as fallback
        val existingDate = dateOfBirthInput.text?.toString()?.let { dateStr ->
            DateUtils.parseDate(dateStr)
        } ?: DateUtils.getCurrentDate()
        
        val (year, month, day) = DateUtils.getDatePickerValues(existingDate)
        
        val dialog = android.app.DatePickerDialog(
            requireContext(),
            { _, y, m, d -> 
                val selectedDate = DateUtils.createDateFromPicker(y, m, d)
                dateOfBirthInput.setText(DateUtils.formatForDisplay(selectedDate))
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
    
    private fun setupRoleBasedUI() {
        // This will be called when user data is loaded
    }
    
    private fun updateUIForRole(user: User) {
        when (user.role) {
            UserRole.COACH -> {
                // Hide player-specific fields for coaches and clear data
                cardPosition.visibility = View.GONE
                positionInput.text = null
            }
            UserRole.FULL_TIME_PLAYER, UserRole.PART_TIME_PLAYER, UserRole.COACH_PLAYER -> {
                // Show player-specific fields for players
                cardPosition.visibility = View.VISIBLE
            }
            else -> {
                // Default: show all fields
                cardPosition.visibility = View.VISIBLE
            }
        }
    }
    
    private fun setupTeamCodeSharing() {
        btnShareTeamCode.setOnClickListener {
            shareTeamCode()
        }
    }

    private fun generateTeamCodeLocal(): String {
        val chars = ("0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ").toCharArray()
        val rnd = java.security.SecureRandom()
        val sb = StringBuilder()
        repeat(6) { sb.append(chars[rnd.nextInt(chars.size)]) }
        return sb.toString()
    }
    
    private fun shareTeamCode() {
        val teamCode = teamCodeText.text.toString()
        if (teamCode.isNotBlank() && teamCode != "Loading...") {
            val shareIntent = Intent().apply {
                action = Intent.ACTION_SEND
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, "Join my team on GoalGetters FC!\n\nTeam Code: $teamCode\n\nDownload the app and use this code to join our team.")
                putExtra(Intent.EXTRA_SUBJECT, "Join my team on GoalGetters FC")
            }
            
            val shareChooser = Intent.createChooser(shareIntent, "Share Team Code")
            startActivity(shareChooser)
        }
    }

    private fun observeUserData() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                activeModel.currentUser.collect { user ->
                    if (user != null) {
                        originalUser = user
                        populateUserData(user)
                        updateUIForRole(user)
                        Clogger.d(TAG, "User data loaded: ${user.fullName()}")
                    } else {
                        Clogger.w(TAG, "No user data found")
                        showPlaceholderData()
                    }
                }
            }
        }
    }

    private fun observeActiveTeam() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                activeModel.activeTeam.collect { team ->
                    if (team != null) {
                        // Update team information section
                        teamNameText.text = team.name
                        val code = team.code?.uppercase()
                        val valid = code?.matches(Regex("^[A-Z0-9]{6}$")) == true
                        teamCodeText.text = if (valid) code else "No code"
                        // If no valid code, trigger persistence generation
                        if (!valid && team.id.isNotBlank()) {
                            activeModel.ensureTeamCode(team.id)
                        }
                        
                        // Show share button if team has a code
                        if (valid) {
                            btnShareTeamCode.visibility = View.VISIBLE
                        } else {
                            btnShareTeamCode.visibility = View.GONE
                            // Clear any sensitive data from share button
                            btnShareTeamCode.contentDescription = null
                        }
                    } else {
                        // Update team information section
                        teamNameText.text = "No active team"
                        teamCodeText.text = ""
                        btnShareTeamCode.visibility = View.GONE
                        btnShareTeamCode.contentDescription = null
                    }
                }
            }
        }
    }

    private fun populateUserData(user: User) {
        // Update header
        widgetHeader.setHeadingText(
            user.fullName().ifBlank { user.alias.ifBlank { "User Account" } })
        widgetHeader.setMessageText(
            user.role.name.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() })

        // Personal Information
        firstNameInput.setText(user.name)
        lastNameInput.setText(user.surname)
        emailInput.setText(user.email ?: "")
        aliasInput.setText(user.alias)

        // Date of Birth
        user.dateOfBirth?.let { dateOfBirth ->
            dateOfBirthInput.setText(DateUtils.formatForDisplay(dateOfBirth))
        } ?: run {
            dateOfBirthInput.setText("")
        }

        // Football Information
        positionInput.setText(user.position?.name ?: "")
        roleInput.setText(
            user.role.name.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() })
        statusText.setText(user.status?.name?.lowercase()?.replaceFirstChar { it.uppercase() }
            ?: "Active")
    }

    private fun showPlaceholderData() {
        widgetHeader.setHeadingText("User Account")
        widgetHeader.setMessageText("Account Settings")

        firstNameInput.setText("")
        lastNameInput.setText("")
        emailInput.setText("")
        aliasInput.setText("")
        dateOfBirthInput.setText("")
        positionInput.setText("")
        roleInput.setText("Player")
        statusText.setText("Active")
        teamNameText.text = "No team"
        teamCodeText.text = ""
    }

    private fun enableEditMode() {
        isEditMode = true
        setInputsEnabled(true)
        updateButtonVisibility()
    }

    private fun cancelEdit() {
        isEditMode = false
        setInputsEnabled(false)
        originalUser?.let { populateUserData(it) }
        updateButtonVisibility()
    }

    private fun saveChanges() {
        val currentUser = originalUser ?: return

        // Validate required fields
        if (firstNameInput.text.toString().trim().isEmpty()) {
            firstNameInput.error = "First name is required"
            return
        }
        if (lastNameInput.text.toString().trim().isEmpty()) {
            lastNameInput.error = "Last name is required"
            return
        }
        if (emailInput.text.toString().trim().isEmpty()) {
            emailInput.error = "Email is required"
            return
        }

        // Parse role from display text
        val roleText = roleInput.text.toString().trim()
        val selectedRole = try {
            UserRole.valueOf(roleText.uppercase().replace(" ", "_"))
        } catch (e: Exception) {
            currentUser.role // Keep current role if parsing fails
        }

        // Create updated user
        val updatedUser = currentUser.copy(
            name = firstNameInput.text.toString().trim(),
            surname = lastNameInput.text.toString().trim(),
            email = emailInput.text.toString().trim(),
            alias = aliasInput.text.toString().trim(),
            role = selectedRole,
            dateOfBirth = dateOfBirthInput.text.toString().trim().takeIf { it.isNotEmpty() }
                ?.let { dateStr ->
                    try {
                        LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                    } catch (e: Exception) {
                        null
                    }
                },
            position = positionInput.text.toString().trim().takeIf { it.isNotEmpty() }
                ?.let { posStr ->
                    try {
                        UserPosition.valueOf(posStr.uppercase().replace(" ", "_"))
                    } catch (e: Exception) {
                        UserPosition.UNKNOWN
                    }
                },
            updatedAt = Instant.now()
        )

        // Save changes
        activeModel.updateUserProfile(updatedUser)

        isEditMode = false
        setInputsEnabled(false)
        updateButtonVisibility()

        Snackbar.make(requireView(), "Account updated successfully", Snackbar.LENGTH_SHORT).show()
    }

    private fun setInputsEnabled(enabled: Boolean) {
        firstNameInput.isEnabled = enabled
        lastNameInput.isEnabled = enabled
        emailInput.isEnabled = enabled
        aliasInput.isEnabled = enabled
        dateOfBirthInput.isEnabled = enabled
        positionInput.isEnabled = enabled
        roleInput.isEnabled = enabled
        roleInput.isClickable = enabled
        roleInput.isFocusable = enabled
    }

    private fun updateButtonVisibility() {
        editButton.visibility = if (isEditMode) View.GONE else View.VISIBLE
        saveButton.visibility = if (isEditMode) View.VISIBLE else View.GONE
        cancelButton.visibility = if (isEditMode) View.VISIBLE else View.GONE
    }

    private fun showDeleteAccountConfirmation() {
        AlertDialog.Builder(requireContext(), R.style.Theme_GoalGetters_Dialog)
            .setTitle("Delete Account")
            .setMessage("Are you sure you want to delete your account? This action cannot be undone.")
            .setPositiveButton("Delete") { _, _ -> deleteAccount() }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteAccount() {
        lifecycleScope.launch {
            try {
                activeModel.deleteUserAccount()
                // Navigate to login screen after account deletion
                val intent = Intent(
                    requireContext(),
                    com.ggetters.app.ui.startup.views.StartActivity::class.java
                )
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                requireActivity().finish()
            } catch (e: Exception) {
                Clogger.e(TAG, "Error deleting account", e)
                Snackbar.make(
                    requireView(),
                    "Error deleting account. Please try again.",
                    Snackbar.LENGTH_LONG
                ).show()
            }
        }
    }
}
