package com.ggetters.app.ui.central.views

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.ggetters.app.R
import com.ggetters.app.data.model.User
import com.ggetters.app.data.model.UserRole
import com.ggetters.app.data.model.UserStatus
import com.ggetters.app.ui.central.models.UserAccount
import com.ggetters.app.ui.central.sheets.AccountSwitcherBottomSheet
import java.time.Instant
import java.time.LocalDate

class ProfileFragment : Fragment() {

    private lateinit var profileAvatar: ImageView
    private lateinit var userNameText: TextView
    private lateinit var userEmailText: TextView
    private lateinit var teamNameText: TextView

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
        userNameText  = view.findViewById(R.id.userNameText)
        userEmailText = view.findViewById(R.id.userEmailText)
        teamNameText  = view.findViewById(R.id.teamNameText)
    }

    private fun setupClickListeners() {
        profileAvatar.setOnClickListener {
            Log.d("ProfileFragment", "Profile avatar clicked")
            Toast.makeText(context, "Profile clicked", Toast.LENGTH_SHORT).show()
            // TODO: navigate to real profile/edit screen
        }

        profileAvatar.setOnLongClickListener {
            Log.d("ProfileFragment", "Profile avatar long pressed")
            Toast.makeText(context, "Opening account switcher...", Toast.LENGTH_SHORT).show()
            showAccountSwitcher()
            true
        }
    }

    private fun loadUserProfile() {
        // TODO: Replace with real UserRepository call
        val sampleUser = User(
            id           = "fake-id-123",
            createdAt    = Instant.now(),
            updatedAt    = Instant.now(),
            stainedAt    = null,
            code         = null,
            authId       = "auth123",
            teamId       = "team1",
            annexedAt    = null,
            role         = UserRole.FULL_TIME_PLAYER,
            name         = "John",
            surname      = "Doe",
            alias        = "JohnDoe",
            dateOfBirth  = LocalDate.of(1990, 1, 1),
            email        = "john.doe@example.com",
            position     = null,
            number       = null,
            status       = UserStatus.ACTIVE,
            healthWeight = null,
            healthHeight = null
        )

        displayUserInfo(sampleUser)
    }

    private fun displayUserInfo(user: User) {
        userNameText .text = user.getFullName()
        userEmailText.text = user.email ?: "${user.alias}@example.com"
        teamNameText .text = "Goal Getters FC"  // TODO: fetch real team name later

        // TODO: load avatar with Glide/Coil
    }

    fun showAccountSwitcher() {
        Log.d("ProfileFragment", "Showing account switcher")

        // TODO: fetch available accounts from backend/local
        val availableAccounts = listOf(
            UserAccount(
                "1",
                "John Doe",
                "john.doe@example.com",
                null,
                "Goal Getters FC",
                "Player",
                true
            ),
            UserAccount("2", "Jane Smith","jane.smith@example.com",null, "City FC",           "Coach",  false)
        )

        AccountSwitcherBottomSheet
            .newInstance(availableAccounts) { selectedAccount ->
                // TODO: call backend to switch, then re-load profile
                loadUserProfile()
            }
            .show(childFragmentManager, "AccountSwitcher")
    }
}
