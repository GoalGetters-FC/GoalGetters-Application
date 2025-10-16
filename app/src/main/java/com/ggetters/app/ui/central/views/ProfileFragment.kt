// app/src/main/java/com/ggetters/app/ui/central/views/ProfileFragment.kt
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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.ggetters.app.R
import com.ggetters.app.core.utils.Clogger
import com.ggetters.app.data.model.User
import com.ggetters.app.data.model.UserRole
import com.ggetters.app.data.model.UserStatus
import com.ggetters.app.ui.central.models.AppbarTheme
import com.ggetters.app.ui.central.models.HomeUiConfiguration
import com.ggetters.app.ui.central.models.UserAccount
import com.ggetters.app.ui.central.sheets.AccountEditBottomSheet
import com.ggetters.app.ui.central.sheets.AccountSwitcherBottomSheet
import com.ggetters.app.ui.central.sheets.ContactDevelopersBottomSheet
import com.ggetters.app.ui.central.sheets.HelpAndFAQBottomSheet
import com.ggetters.app.ui.central.sheets.NotificationSettingsBottomSheet
import com.ggetters.app.ui.central.sheets.TeamProfileBottomSheet
import com.ggetters.app.ui.central.viewmodels.HomeViewModel
import com.ggetters.app.ui.central.viewmodels.ProfileViewModel
import com.ggetters.app.ui.management.sheets.TeamSwitcherBottomSheet
import com.ggetters.app.ui.management.views.TeamViewerActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate

@AndroidEntryPoint
class ProfileFragment : Fragment() {
    companion object {
        private const val TAG = "ProfileFragment"
    }

    private val activeModel: ProfileViewModel by viewModels()
    private val sharedModel: HomeViewModel by activityViewModels()

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
        observeActiveTeam()   // ← keep team name live
        loadUserProfile()

        sharedModel.useViewConfiguration(
            HomeUiConfiguration(
                appBarColor = AppbarTheme.WHITE,
                appBarTitle = "Settings",
                appBarShown = true,
            )
        )
    }

    private fun setupViews(view: View) {
        profileAvatar = view.findViewById(R.id.profileAvatar)
        userNameText = view.findViewById(R.id.userNameText)
        userHandleText = view.findViewById(R.id.userHandleText)
        teamNameText = view.findViewById(R.id.teamNameText)

        accountItem = view.findViewById(R.id.accountItem)
        teamProfileItem = view.findViewById(R.id.teamProfileItem)
        notificationsItem = view.findViewById(R.id.notificationsItem)
        privacyPolicyItem = view.findViewById(R.id.privacyPolicyItem)
        contactDevelopersItem = view.findViewById(R.id.contactDevelopersItem)
        helpFaqItem = view.findViewById(R.id.helpFaqItem)
        logoutButton = view.findViewById(R.id.logoutButton)
        
        // Set up account-specific UI elements
        setupAccountSpecificViews(view)
    }
    
    private fun setupAccountSpecificViews(view: View) {
        // Set up profile avatar with account-specific styling
        profileAvatar.setOnClickListener {
            showAccountEditDialog()
        }
    }

    private fun setupClickListeners() {
        profileAvatar.setOnLongClickListener {
            Log.d("ProfileFragment", "Avatar long-press detected")
            showAccountSwitcher()
            true
        }

        // Temporary dev shortcut
        logoutButton.setOnLongClickListener {
            startActivity(Intent(requireContext(), TeamViewerActivity::class.java))
            true
        }

        accountItem.setOnClickListener { showAccountSettings() }
        teamProfileItem.setOnClickListener { navigateToTeamProfile() }
        notificationsItem.setOnClickListener { showNotificationSettings() }
        privacyPolicyItem.setOnClickListener { showPrivacyPolicy() }
        contactDevelopersItem.setOnClickListener { showContactDevelopers() }
        helpFaqItem.setOnClickListener { showHelpAndFAQ() }
        logoutButton.setOnClickListener { showLogoutConfirmation() }
    }

    /** Keep the team name in the header in sync with the active team. */
    private fun observeActiveTeam() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    activeModel.activeTeam.collect { team ->
                        teamNameText.text = team?.name ?: getString(R.string.no_active_team)
                    }
                }
            }
        }
    }

    private fun loadUserProfile() {
        // Load the actual logged-in user from the repository
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                try {
                    activeModel.currentUser.collect { user ->
                        Clogger.d(TAG, "User data received: ${user?.let { "User: ${it.fullName()}, Email: ${it.email}" } ?: "null"}")
                        if (user != null) {
                            displayUserInfo(user)
                        } else {
                            Clogger.w(TAG, "No user found, showing placeholder")
                            // Show placeholder if no user found
                            displayUserInfo(getPlaceholderUser())
                        }
                    }
                } catch (e: Exception) {
                    Clogger.e(TAG, "Error loading user profile: ${e.message}", e)
                    displayUserInfo(getPlaceholderUser())
                }
            }
        }
    }
    
    private fun getPlaceholderUser(): User {
        return User(
            id = "placeholder",
            authId = "placeholder",
            teamId = null,
            name = "User",
            surname = "",
            alias = "user",
            email = "user@example.com",
            role = UserRole.FULL_TIME_PLAYER,
            status = UserStatus.ACTIVE,
            createdAt = Instant.now(),
            updatedAt = Instant.now()
        )
    }

    private fun displayUserInfo(user: User) {
        // Display user's full name or fallback to alias/email
        val displayName = when {
            user.fullName().isNotBlank() -> user.fullName()
            user.alias.isNotBlank() -> user.alias
            !user.email.isNullOrBlank() -> user.email!!.split("@").first()
            else -> "User"
        }
        userNameText.text = displayName
        
        // Display email or fallback
        userHandleText.text = user.email?.ifBlank { "No email set" } ?: "No email set"
        
        // teamNameText is driven by observeActiveTeam()
        // TODO: Load avatar with Coil/Glide when you have user.avatarUrl
    }

    private fun showAccountEditDialog() {
        val currentUser = activeModel.currentUser.value
        if (currentUser != null) {
            AccountEditBottomSheet.newInstance(currentUser.id) { updatedUser ->
                activeModel.updateUserProfile(updatedUser)
                Snackbar.make(requireView(), "Account updated successfully", Snackbar.LENGTH_SHORT).show()
            }.show(parentFragmentManager, "AccountEditBottomSheet")
        }
    }
    
    private fun showTeamProfileDialog() {
        val currentTeam = activeModel.activeTeam.value
        if (currentTeam != null) {
            TeamProfileBottomSheet.newInstance(currentTeam.id).show(parentFragmentManager, "TeamProfileBottomSheet")
        } else {
            Snackbar.make(requireView(), "No active team found", Snackbar.LENGTH_SHORT).show()
        }
    }
    
    private fun showNotificationSettings() {
        NotificationSettingsBottomSheet().show(parentFragmentManager, "NotificationSettingsBottomSheet")
    }
    
    private fun showPrivacyPolicy() {
        // TODO: Open privacy policy web page or dialog
        Snackbar.make(requireView(), "Privacy Policy - Coming Soon", Snackbar.LENGTH_SHORT).show()
    }
    
    private fun showContactDevelopers() {
        ContactDevelopersBottomSheet().show(parentFragmentManager, "ContactDevelopersBottomSheet")
    }
    
    private fun showHelpAndFAQ() {
        HelpAndFAQBottomSheet().show(parentFragmentManager, "HelpAndFAQBottomSheet")
    }

    fun showAccountSwitcher() {
        val availableAccounts = listOf(
            UserAccount("1", "Matthew Pieterse", "matthew@example.com", null, "U15a Football", "Coach", true),
            UserAccount("2", "Matthew Pieterse", "matthew@example.com", null, "City FC", "Coach", false)
        )

        AccountSwitcherBottomSheet
            .newInstance(availableAccounts) { selectedAccount ->
                // TODO: Switch active team via TeamRepository
                Snackbar.make(requireView(), "Switched to ${selectedAccount.teamName}", Snackbar.LENGTH_SHORT).show()
                loadUserProfile()
            }
            .show(childFragmentManager, "AccountSwitcher")
    }

    private fun showTeamSwitcher() {
        TeamSwitcherBottomSheet.newInstance(
            onTeamSelected = { selectedTeam ->
                Snackbar.make(requireView(), "Switched to ${selectedTeam.teamName}", Snackbar.LENGTH_SHORT).show()
                loadUserProfile()
            },
            onManageTeams = { navigateToTeamManagement() },
            onSetDefaultTeam = { teamId -> setDefaultTeam(teamId) }
        ).show(childFragmentManager, "TeamSwitcher")
    }

    private fun setDefaultTeam(teamId: String) {
        Snackbar.make(requireView(), "Default team updated", Snackbar.LENGTH_SHORT).show()
    }

    private fun showAccountSettings() {
        Snackbar.make(requireView(), "Account settings coming soon", Snackbar.LENGTH_SHORT).show()
    }

    private fun navigateToTeamProfile() {
        val teamProfileFragment = com.ggetters.app.ui.management.views.TeamProfileFragment()
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, teamProfileFragment)
            .addToBackStack("profile_to_team_profile")
            .commit()
    }

    private fun navigateToTeamManagement() {
        Snackbar.make(requireView(), "Team management coming soon", Snackbar.LENGTH_SHORT).show()
    }


    private fun showLogoutConfirmation() {
        AlertDialog.Builder(requireContext(), R.style.Theme_GoalGetters_Dialog)
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Logout") { _, _ -> performLogout() }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun performLogout() {
        try {
            activeModel.logout()
        } catch (e: Exception) {
            Log.e("ProfileFragment", "Error during logout", e)
            Snackbar.make(requireView(), "Error during logout. Please try again.", Snackbar.LENGTH_LONG).show()
        }
    }

}
