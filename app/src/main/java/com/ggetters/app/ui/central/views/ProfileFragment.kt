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
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.ggetters.app.core.extensions.navigateTo
import com.ggetters.app.core.extensions.navigateToActivity
import com.ggetters.app.R
import com.ggetters.app.data.model.User
import com.ggetters.app.data.model.UserRole
import com.ggetters.app.data.model.UserStatus
import com.ggetters.app.ui.central.models.UserAccount
import com.ggetters.app.ui.central.sheets.AccountSwitcherBottomSheet
import com.ggetters.app.ui.central.viewmodels.ProfileViewModel
import com.ggetters.app.ui.management.sheets.TeamSwitcherBottomSheet
import com.ggetters.app.ui.management.views.TeamViewerActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import java.time.Instant
import java.time.LocalDate
import kotlinx.coroutines.launch

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
        observeActiveTeam()   // ← keep team name live
        loadUserProfile()
        setupToolbarVisibility(view)
    }

    private fun setupToolbarVisibility(view: View) {
        // Hide toolbar when hosted by UserProfileActivity to avoid double toolbars
        if (requireActivity() is UserProfileActivity) {
            // No toolbar in this fragment layout, but we could hide other elements if needed
        }
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
        contactDevelopersItem.setOnClickListener { contactDevelopers() }
        helpFaqItem.setOnClickListener { showHelpFaq() }
        logoutButton.setOnClickListener { showLogoutConfirmation() }
    }

    /** Keep the team name in the header in sync with the active team. */
    private fun observeActiveTeam() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    model.activeTeam.collect { team ->
                        teamNameText.text = team?.name ?: getString(R.string.no_active_team)
                    }
                }
            }
        }
    }

    private fun loadUserProfile() {
        // TODO: Replace sample with real user from repo/auth profile
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
        val name = user.fullName().ifBlank { user.alias.ifBlank { "Player" } }
        userNameText.text = name
        userHandleText.text = user.email.orEmpty()
        // teamNameText is driven by observeActiveTeam()
        // TODO: Load avatar with Coil/Glide when you have user.avatarUrl
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
        val intent = android.content.Intent(requireContext(), UserProfileActivity::class.java).apply {
            putExtra(UserProfileActivity.EXTRA_PROFILE_TYPE, UserProfileActivity.PROFILE_TYPE_TEAM)
        }
        requireActivity().navigateToActivity(intent)
    }

    private fun navigateToTeamManagement() {
        Snackbar.make(requireView(), "Team management coming soon", Snackbar.LENGTH_SHORT).show()
    }

    private fun showNotificationSettings() {
        Snackbar.make(requireView(), "Notification settings coming soon", Snackbar.LENGTH_SHORT).show()
    }

    private fun showPrivacyPolicy() {
        Snackbar.make(requireView(), "Privacy policy coming soon", Snackbar.LENGTH_SHORT).show()
    }

    private fun contactDevelopers() {
        Snackbar.make(requireView(), "Contact developers coming soon", Snackbar.LENGTH_SHORT).show()
    }

    private fun showHelpFaq() {
        Snackbar.make(requireView(), "Help & FAQ coming soon", Snackbar.LENGTH_SHORT).show()
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
            model.logout()
        } catch (e: Exception) {
            Log.e("ProfileFragment", "Error during logout", e)
            Snackbar.make(requireView(), "Error during logout. Please try again.", Snackbar.LENGTH_LONG).show()
        }
    }

    /** Local helper in case your data model doesn’t implement this. */
    private fun User.fullName(): String = "$name $surname".trim()
}
