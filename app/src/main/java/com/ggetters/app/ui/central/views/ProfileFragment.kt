package com.ggetters.app.ui.central.views

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.ggetters.app.R
import com.ggetters.app.data.model.User
import com.ggetters.app.data.model.UserRole
import com.ggetters.app.data.model.UserStatus
import com.ggetters.app.ui.central.models.UserAccount
import com.ggetters.app.ui.central.sheets.AccountSwitcherBottomSheet
import com.ggetters.app.ui.central.sheets.TeamSwitcherBottomSheet
import com.ggetters.app.ui.central.viewmodels.HomePlayersViewModel
import com.ggetters.app.ui.central.viewmodels.HomeViewModel
import com.ggetters.app.ui.central.viewmodels.ProfileViewModel
import com.ggetters.app.ui.startup.views.SignInActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import java.time.Instant
import java.time.LocalDate
import kotlin.getValue

// TODO: Backend - Implement user profile management and photo upload
// TODO: Backend - Add account switching with proper authentication
// TODO: Backend - Implement team profile management and settings
// TODO: Backend - Add notification preferences and user settings
// TODO: Backend - Implement privacy policy and terms of service
// TODO: Backend - Add user analytics and engagement tracking
// TODO: Backend - Implement data export and account deletion
// TODO: Backend - Add multi-team support and team switching
// TODO: Backend - Implement user feedback and support system

@AndroidEntryPoint
class ProfileFragment : Fragment() {

    private val model: ProfileViewModel by viewModels()

    private lateinit var profileAvatar: ImageView
    private lateinit var userNameText: TextView
    private lateinit var userHandleText: TextView
    private lateinit var teamNameText: TextView
    
    // Settings items
    private lateinit var accountItem: View
    private lateinit var teamProfileItem: View
    private lateinit var notificationsItem: View
    private lateinit var privacyPolicyItem: View
    private lateinit var contactDevelopersItem: View
    private lateinit var helpFaqItem: View
    private lateinit var logoutButton: MaterialButton

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_profile, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews(view)
        setupClickListeners()
        loadUserProfile()
    }

    private fun setupViews(view: View) {
        profileAvatar = view.findViewById(R.id.profileAvatar)
        userNameText = view.findViewById(R.id.userNameText)
        userHandleText = view.findViewById(R.id.userHandleText)
        teamNameText = view.findViewById(R.id.teamNameText)
        
        // Settings items
        accountItem = view.findViewById(R.id.accountItem)
        teamProfileItem = view.findViewById(R.id.teamProfileItem)
        notificationsItem = view.findViewById(R.id.notificationsItem)
        privacyPolicyItem = view.findViewById(R.id.privacyPolicyItem)
        contactDevelopersItem = view.findViewById(R.id.contactDevelopersItem)
        helpFaqItem = view.findViewById(R.id.helpFaqItem)
        logoutButton = view.findViewById(R.id.logoutButton)
    }

    private fun setupClickListeners() {
        // Long press avatar to switch teams (based on design template)
        profileAvatar.setOnLongClickListener {
            showTeamSwitcher()
            true
        }
        
        // Settings item clicks
        accountItem.setOnClickListener {
            showAccountSettings()
        }
        
        teamProfileItem.setOnClickListener {
            navigateToTeamProfile()
        }
        
        notificationsItem.setOnClickListener {
            showNotificationSettings()
        }
        
        privacyPolicyItem.setOnClickListener {
            showPrivacyPolicy()
        }
        
        contactDevelopersItem.setOnClickListener {
            contactDevelopers()
        }
        
        helpFaqItem.setOnClickListener {
            showHelpFaq()
        }
        
        logoutButton.setOnClickListener {
            showLogoutConfirmation()
        }
    }

    private fun loadUserProfile() {
        // TODO: Backend - Fetch current user data from backend
        // TODO: Backend - Implement user data caching for offline access
        // TODO: Backend - Add user data synchronization across devices
        // TODO: Backend - Implement user profile photo upload and management
        // TODO: Backend - Add user preferences and settings persistence
        
        // Sample user data for demo
        val sampleUser = User(
            id = "1",
            authId = "auth123",
            teamId = "team1",
            name = "Matthew",
            surname = "Pieterse",
            alias = "matthew_pieterse",
            email = "ST10257002@domain.com",
            dateOfBirth = LocalDate.of(1990, 5, 15),
            role = UserRole.FULL_TIME_PLAYER,
            status = UserStatus.ACTIVE,
            createdAt = Instant.now(),
            updatedAt = Instant.now()
        )

        displayUserInfo(sampleUser)
    }

    private fun displayUserInfo(user: User) {
        userNameText.text = user.getFullName()
        userHandleText.text = user.email
        teamNameText.text = "U15a Football" // TODO: Fetch current team name from backend

        // TODO: Load avatar with Glide/Coil
        // Glide.with(this).load(user.avatar).into(profileAvatar)
    }

    fun showAccountSwitcher() {
        Log.d("ProfileFragment", "Showing account switcher")

        // TODO: Backend - Fetch available accounts from backend/local
        // TODO: Backend - Implement secure account switching with proper authentication
        // TODO: Backend - Add account switching analytics and audit logging
        // TODO: Backend - Implement account switching notifications
        // TODO: Backend - Add account switching validation and permissions
        val availableAccounts = listOf(
            UserAccount(
                "1",
                "Matthew Pieterse",
                "matthew@example.com",
                null,
                "U15a Football",
                "Player",
                true
            ),
            UserAccount(
                "2", 
                "Matthew Pieterse", 
                "matthew@example.com",
                null, 
                "City FC", 
                "Coach", 
                false
            )
        )

        AccountSwitcherBottomSheet
            .newInstance(availableAccounts) { selectedAccount ->
                // TODO: Backend - Call backend to switch active team
                // teamRepo.switchActiveTeam(selectedAccount.id)
                Snackbar.make(requireView(), "Switched to ${selectedAccount.teamName}", Snackbar.LENGTH_SHORT).show()
                loadUserProfile() // Refresh profile with new team
            }
            .show(childFragmentManager, "AccountSwitcher")
    }

    private fun showTeamSwitcher() {
        // TODO: Backend - Implement team switching with proper authentication
        // TODO: Backend - Add team switching analytics and tracking
        // TODO: Backend - Implement team switching notifications and confirmations
        // TODO: Backend - Add team switching validation and permissions
        // TODO: Backend - Implement team switching data synchronization

        TeamSwitcherBottomSheet.newInstance(
            onTeamSelected = { selectedTeam ->
                // Handle team selection
                Snackbar.make(requireView(), "Switched to ${selectedTeam.teamName}", Snackbar.LENGTH_SHORT).show()
                loadUserProfile() // Refresh profile with new team
            },
            onManageTeams = {
                // Navigate to team management
                navigateToTeamManagement()
            }
        ).show(childFragmentManager, "TeamSwitcher")
    }
    
    private fun showAccountSettings() {
        // TODO: Backend - Navigate to account settings screen
        // TODO: Backend - Implement account settings management
        // TODO: Backend - Add account security and privacy settings
        // TODO: Backend - Implement account data export and backup
        // TODO: Backend - Add account deletion and deactivation
        Snackbar.make(requireView(), "Account settings coming soon", Snackbar.LENGTH_SHORT).show()
    }
    
    private fun navigateToTeamProfile() {
        // TODO: Backend - Navigate to current team profile
        // TODO: Backend - Implement team profile management
        // TODO: Backend - Add team settings and configuration
        // TODO: Backend - Implement team analytics and reporting
        // TODO: Backend - Add team member management and permissions
        val teamProfileFragment = TeamProfileFragment()
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, teamProfileFragment)
            .addToBackStack("profile_to_team_profile")
            .commit()
    }

    private fun navigateToTeamManagement() {
        // TODO: Backend - Navigate to team management screen
        // TODO: Backend - Add team management analytics and tracking
        // TODO: Backend - Implement team management permissions and validation
        // TODO: Backend - Add team management audit logging
        // TODO: Backend - Implement team management data synchronization
        Snackbar.make(requireView(), "Team management coming soon", Snackbar.LENGTH_SHORT).show()
    }
    
    private fun showNotificationSettings() {
        // TODO: Backend - Show notification preferences dialog/screen
        // TODO: Backend - Implement notification preferences management
        // TODO: Backend - Add notification categories and filtering
        // TODO: Backend - Implement notification scheduling and quiet hours
        // TODO: Backend - Add notification delivery preferences
        Snackbar.make(requireView(), "Notification settings coming soon", Snackbar.LENGTH_SHORT).show()
    }
    
    private fun showPrivacyPolicy() {
        // TODO: Backend - Show privacy policy web view or dialog
        // TODO: Backend - Implement privacy policy version management
        // TODO: Backend - Add privacy policy acceptance tracking
        // TODO: Backend - Implement privacy settings and data controls
        // TODO: Backend - Add GDPR compliance and data portability
        Snackbar.make(requireView(), "Privacy policy coming soon", Snackbar.LENGTH_SHORT).show()
    }
    
    private fun contactDevelopers() {
        // TODO: Backend - Open email intent or contact form
        // TODO: Backend - Implement in-app feedback system
        // TODO: Backend - Add bug reporting and feature requests
        // TODO: Backend - Implement support ticket management
        // TODO: Backend - Add user feedback analytics and tracking
        Snackbar.make(requireView(), "Contact developers coming soon", Snackbar.LENGTH_SHORT).show()
    }
    
    private fun showHelpFaq() {
        // TODO: Backend - Show help and FAQ screen
        // TODO: Backend - Implement help content management
        // TODO: Backend - Add searchable help documentation
        // TODO: Backend - Implement contextual help and tooltips
        // TODO: Backend - Add help analytics and usage tracking
        Snackbar.make(requireView(), "Help & FAQ coming soon", Snackbar.LENGTH_SHORT).show()
    }
    
    private fun showLogoutConfirmation() {
        AlertDialog.Builder(requireContext(), R.style.Theme_GoalGetters_Dialog)
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Logout") { _, _ ->
                performLogout()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun performLogout() {
        // TODO: Backend - Call logout API and clear local data
        // TODO: Backend - Implement secure logout with token invalidation
        // TODO: Backend - Add logout analytics and session tracking
        // TODO: Backend - Implement logout notifications and cleanup
        // TODO: Backend - Add logout confirmation and data backup
        
        try {
            // Clear local storage and preferences
            // TODO: Clear user session data
            // TODO: Clear cached data
            // TODO: Clear preferences
            
            // Navigate to login screen
            val intent = Intent(requireContext(), SignInActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
            
            // Close the current activity
            requireActivity().finish()
            
        } catch (e: Exception) {
            Log.e("ProfileFragment", "Error during logout", e)
            Snackbar.make(requireView(), "Error during logout. Please try again.", Snackbar.LENGTH_LONG).show()
        }
    }
}
