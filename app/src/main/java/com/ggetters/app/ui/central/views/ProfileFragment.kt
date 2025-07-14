package com.ggetters.app.ui.central.views

import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.ggetters.app.R
import com.ggetters.app.ui.central.sheets.AccountSwitcherBottomSheet
import com.ggetters.app.data.model.User
import java.util.Date

class ProfileFragment : Fragment() {
    
    private lateinit var profileAvatar: ImageView
    private lateinit var userNameText: TextView
    private lateinit var userEmailText: TextView
    private lateinit var teamNameText: TextView
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupViews(view)
        setupClickListeners()
        loadUserProfile()
    }
    
    private fun setupViews(view: View) {
        profileAvatar = view.findViewById(R.id.profileAvatar)
        userNameText = view.findViewById(R.id.userNameText)
        userEmailText = view.findViewById(R.id.userEmailText)
        teamNameText = view.findViewById(R.id.teamNameText)
    }
    
    private fun setupClickListeners() {
        // Tap to open profile details
        profileAvatar.setOnClickListener {
            Log.d("ProfileFragment", "Profile avatar clicked")
            Toast.makeText(context, "Profile clicked", Toast.LENGTH_SHORT).show()
            // TODO: Navigate to profile details/edit screen
        }
        
        // Long press to open account switcher
        profileAvatar.setOnLongClickListener {
            Log.d("ProfileFragment", "Profile avatar long pressed")
            Toast.makeText(context, "Opening account switcher...", Toast.LENGTH_SHORT).show()
            showAccountSwitcher()
            true
        }
    }
    
    private fun loadUserProfile() {
        // TODO: Backend - Fetch current user profile
        // Endpoint: GET /api/users/profile
        // Request: { userId: String }
        // Response: { user: User, team: Team }
        // Error handling: { message: String, code: String }
        
        // Sample data for now
        val sampleUser = User(
            authId = "auth123",
            teamId = "1",
            role = 1, // Player role
            name = "John",
            surname = "Doe",
            alias = "JohnDoe",
            dateOfBirth = Date()
        )
        
        displayUserInfo(sampleUser)
    }
    
    private fun displayUserInfo(user: User) {
        userNameText.text = user.getFullName()
        userEmailText.text = "${user.alias}@example.com" // TODO: Get actual email
        teamNameText.text = "Goal Getters FC" // TODO: Get from team data
        
        // TODO: Load user avatar using Glide or similar
        // if (user.avatar != null) {
        //     Glide.with(this)
        //         .load(user.avatar)
        //         .placeholder(R.drawable.default_avatar)
        //         .into(profileAvatar)
        // } else {
        //     profileAvatar.setImageResource(R.drawable.default_avatar)
        // }
    }
    
    fun showAccountSwitcher() {
        Log.d("ProfileFragment", "Showing account switcher")
        
        // TODO: Backend - Fetch available accounts
        // Endpoint: GET /api/users/available-accounts
        // Request: { userId: String }
        // Response: { accounts: UserAccount[] }
        // Error handling: { message: String, code: String }
        
        val availableAccounts = listOf(
            UserAccount(
                id = "1",
                name = "John Doe",
                email = "john.doe@example.com",
                avatar = null,
                teamName = "Goal Getters FC",
                role = "Player",
                isActive = true
            ),
            UserAccount(
                id = "2",
                name = "Jane Smith",
                email = "jane.smith@example.com",
                avatar = null,
                teamName = "City FC",
                role = "Coach",
                isActive = false
            )
        )
        
        try {
            AccountSwitcherBottomSheet.newInstance(availableAccounts) { selectedAccount ->
                // TODO: Backend - Switch to selected account
                // Endpoint: POST /api/users/switch-account
                // Request: { userId: String, targetAccountId: String }
                // Response: { success: boolean, newToken: string }
                // Error handling: { message: String, code: String }
                
                // Update UI with new account
                val newUser = User(
                    authId = selectedAccount.id,
                    teamId = selectedAccount.id,
                    role = if (selectedAccount.role == "Player") 1 else 2, // 1=Player, 2=Coach
                    name = selectedAccount.name.split(" ").firstOrNull() ?: "",
                    surname = selectedAccount.name.split(" ").lastOrNull() ?: "",
                    alias = selectedAccount.name.replace(" ", ""),
                    dateOfBirth = Date()
                )
                displayUserInfo(newUser)
            }.show(childFragmentManager, "AccountSwitcher")
        } catch (e: Exception) {
            Log.e("ProfileFragment", "Error showing account switcher", e)
            Toast.makeText(context, "Error showing account switcher: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}

data class UserAccount(
    val id: String,
    val name: String,
    val email: String,
    val avatar: String?,
    val teamName: String,
    val role: String,
    val isActive: Boolean
) : Parcelable {
    
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString(),
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readByte() != 0.toByte()
    )
    
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(name)
        parcel.writeString(email)
        parcel.writeString(avatar)
        parcel.writeString(teamName)
        parcel.writeString(role)
        parcel.writeByte(if (isActive) 1 else 0)
    }
    
    override fun describeContents(): Int {
        return 0
    }
    
    companion object CREATOR : Parcelable.Creator<UserAccount> {
        override fun createFromParcel(parcel: Parcel): UserAccount {
            return UserAccount(parcel)
        }
        
        override fun newArray(size: Int): Array<UserAccount?> {
            return arrayOfNulls(size)
        }
    }
}