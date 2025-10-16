package com.ggetters.app.ui.central.sheets

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.launch
import com.ggetters.app.R
import com.ggetters.app.data.model.User
import com.ggetters.app.data.model.UserPosition
import com.ggetters.app.data.model.UserRole
import com.ggetters.app.data.model.UserStatus
import com.ggetters.app.databinding.BottomSheetAccountEditBinding
import com.ggetters.app.ui.central.viewmodels.ProfileViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@AndroidEntryPoint
class AccountEditBottomSheet : BottomSheetDialogFragment() {
    
    companion object {
        private const val ARG_USER_ID = "user_id"
        
        fun newInstance(userId: String, onUserUpdated: (User) -> Unit): AccountEditBottomSheet {
            return AccountEditBottomSheet().apply {
                arguments = Bundle().apply {
                    putString(ARG_USER_ID, userId)
                }
                this.onUserUpdated = onUserUpdated
            }
        }
    }
    
    private var _binding: BottomSheetAccountEditBinding? = null
    private val binding get() = _binding!!
    
    private val profileViewModel: ProfileViewModel by viewModels()
    private var onUserUpdated: ((User) -> Unit)? = null
    private var currentUser: User? = null
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetAccountEditBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        val userId = arguments?.getString(ARG_USER_ID)
        if (userId != null) {
            // Load user from ViewModel
            viewLifecycleOwner.lifecycleScope.launch {
                viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                    profileViewModel.currentUser.collect { user ->
                        if (user != null && user.id == userId) {
                            currentUser = user
                            populateFields(user)
                            setupClickListeners()
                        }
                    }
                }
            }
        }
    }
    
    private fun populateFields(user: User) {
        binding.apply {
            etFirstName.setText(user.name)
            etLastName.setText(user.surname)
            etEmail.setText(user.email ?: "")
            etAlias.setText(user.alias)
            etPosition.setText(user.position?.name ?: "")
            
            // Set date of birth if available
            user.dateOfBirth?.let { dateOfBirth ->
                val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                etDateOfBirth.setText(dateOfBirth.format(formatter).toString())
            }
            
            // Set role
            when (user.role) {
                UserRole.FULL_TIME_PLAYER -> rbFullTimePlayer.isChecked = true
                UserRole.PART_TIME_PLAYER -> rbPartTimePlayer.isChecked = true
                UserRole.COACH -> rbCoach.isChecked = true
                UserRole.COACH_PLAYER -> rbCoach.isChecked = true
                UserRole.OTHER -> rbFullTimePlayer.isChecked = true
            }
            
            // Set status
            when (user.status) {
                UserStatus.ACTIVE -> rbActive.isChecked = true
                UserStatus.INJURY -> rbInactive.isChecked = true
                null -> rbActive.isChecked = true
            }
        }
    }
    
    private fun setupClickListeners() {
        binding.apply {
            btnSave.setOnClickListener {
                saveUserChanges()
            }
            
            btnCancel.setOnClickListener {
                dismiss()
            }
            
            btnDeleteAccount.setOnClickListener {
                showDeleteAccountConfirmation()
            }
        }
    }
    
    private fun saveUserChanges() {
        val user = currentUser ?: return
        
        binding.apply {
            val updatedUser = user.copy(
                name = etFirstName.text.toString().trim(),
                surname = etLastName.text.toString().trim(),
                email = etEmail.text.toString().trim(),
                alias = etAlias.text.toString().trim(),
                position = etPosition.text.toString().trim().takeIf { it.isNotEmpty() }?.let { posStr ->
                    try {
                        UserPosition.valueOf(posStr.uppercase().replace(" ", "_"))
                    } catch (e: Exception) {
                        UserPosition.UNKNOWN
                    }
                },
                dateOfBirth = etDateOfBirth.text.toString().trim().takeIf { it.isNotEmpty() }?.let { dateStr ->
                    try {
                        LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                    } catch (e: Exception) {
                        null
                    }
                },
                role = when {
                    rbFullTimePlayer.isChecked -> UserRole.FULL_TIME_PLAYER
                    rbPartTimePlayer.isChecked -> UserRole.PART_TIME_PLAYER
                    rbCoach.isChecked -> UserRole.COACH
                    else -> user.role
                },
                status = when {
                    rbActive.isChecked -> UserStatus.ACTIVE
                    rbInactive.isChecked -> UserStatus.INJURY
                    else -> user.status
                }
            )
            
            // Validate required fields
            if (updatedUser.name.isBlank()) {
                etFirstName.error = "First name is required"
                return
            }
            
            if (updatedUser.surname.isBlank()) {
                etLastName.error = "Last name is required"
                return
            }
            
            if (updatedUser.email.isNullOrBlank()) {
                etEmail.error = "Email is required"
                return
            }
            
            onUserUpdated?.invoke(updatedUser)
            dismiss()
        }
    }
    
    private fun showDeleteAccountConfirmation() {
        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Delete Account")
            .setMessage("Are you sure you want to delete your account? This action cannot be undone.")
            .setPositiveButton("Delete") { _, _ ->
                profileViewModel.deleteUserAccount()
                Snackbar.make(requireView(), "Account deletion requested", Snackbar.LENGTH_SHORT).show()
                dismiss()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

