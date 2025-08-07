package com.ggetters.app.ui.central.views

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.ggetters.app.R
import com.ggetters.app.core.utils.Clogger
import com.ggetters.app.databinding.FragmentPlayerDetailsBinding

class PlayerDetailsFragment : Fragment() {
    
    private var _binding: FragmentPlayerDetailsBinding? = null
    private val binding get() = _binding!!
    
    companion object {
        private const val TAG = "PlayerDetailsFragment"
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlayerDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Clogger.d(TAG, "PlayerDetailsFragment created")
        
        setupViews()
        loadPlayerData()
    }
    
    private fun setupViews() {
        // Setup player avatar with long press for account switcher
        binding.playerAvatar.setOnLongClickListener {
            Clogger.d(TAG, "Player avatar long-press detected")
            showAccountSwitcher()
            true
        }
        
        // Setup other UI elements
        binding.editPlayerButton.setOnClickListener {
            // TODO: Navigate to edit player screen
            Clogger.d(TAG, "Edit player clicked")
        }
        
        binding.playerStatsButton.setOnClickListener {
            // TODO: Show player statistics
            Clogger.d(TAG, "Player stats clicked")
        }
    }
    
    private fun loadPlayerData() {
        // TODO: Backend - Load real player data from backend
        // For now, show placeholder data
        binding.playerName.text = "John Doe"
        binding.playerPosition.text = "Forward"
        binding.playerNumber.text = "#10"
        binding.playerAge.text = "25 years"
        binding.playerHeight.text = "180 cm"
        binding.playerWeight.text = "75 kg"
        
        // Load player statistics
        binding.gamesPlayed.text = "15"
        binding.goalsScored.text = "8"
        binding.assists.text = "5"
        binding.yellowCards.text = "2"
        binding.redCards.text = "0"
    }
    
    private fun showAccountSwitcher() {
        // Get the parent activity to show account switcher
        val activity = activity as? HomeActivity
        activity?.showAccountSwitcher()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 