package com.ggetters.app.ui.central.views

import android.app.AlertDialog
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
import com.google.android.material.chip.Chip
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import android.widget.AutoCompleteTextView

class PlayerProfileFragment : Fragment() {
    
    companion object {
        private const val ARG_PLAYER_ID = "player_id"
        
        fun newInstance(playerId: String): PlayerProfileFragment {
            val fragment = PlayerProfileFragment()
            val args = Bundle()
            args.putString(ARG_PLAYER_ID, playerId)
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
    
    // TODO: Backend - Get user role from backend/UserRepository
    private val userRole = "coach"
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            playerId = it.getString(ARG_PLAYER_ID)
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
        
        displayPlayerInfo(player)
    }
    
    private fun displayPlayerInfo(player: Player) {
        playerName.text = player.name
        playerAge.text = "16 yo" // Calculate age from dateOfBirth
        
        // Populate form fields
        playerNameInput.setText(player.name)
        playerNumberInput.setText(player.jerseyNumber)
        playerEmailInput.setText(player.email ?: "")
        playerDateOfBirthInput.setText(player.dateOfBirth ?: "")
        playerContactInput.setText(player.phone ?: "")
        
        // Set dropdown values
        playerStatusDropdown.setText("Fulltime Player")
        playerRoleDropdown.setText("Center Striker")
        
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
            showEditProfileDialog()
        }
        
        btnSendMessage.setOnClickListener {
            showSendMessageDialog()
        }
        
        btnViewHistory.setOnClickListener {
            showPlayerHistory()
        }
    }
    
    private fun showEditProfileDialog() {
        // TODO: Backend - Show edit profile dialog/screen
        Snackbar.make(requireView(), "Edit profile functionality coming soon", Snackbar.LENGTH_SHORT).show()
    }
    
    private fun showSendMessageDialog() {
        // TODO: Backend - Implement messaging functionality
        Snackbar.make(requireView(), "Messaging functionality coming soon", Snackbar.LENGTH_SHORT).show()
    }
    
    private fun showPlayerHistory() {
        // TODO: Backend - Show player performance history
        Snackbar.make(requireView(), "Player history functionality coming soon", Snackbar.LENGTH_SHORT).show()
    }
} 