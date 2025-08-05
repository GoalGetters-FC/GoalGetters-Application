package com.ggetters.app.ui.central.views

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ggetters.app.R
import com.ggetters.app.ui.central.adapters.PlayerAdapter
import com.ggetters.app.ui.central.models.Player
import com.ggetters.app.ui.central.viewmodels.HomePlayersViewModel
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PlayerListFragment : Fragment() {

    private val viewModel: HomePlayersViewModel by viewModels()
    
    private lateinit var toolbar: MaterialToolbar
    private lateinit var playersRecyclerView: RecyclerView
    private lateinit var playerAdapter: PlayerAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_players, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews(view)
        setupRecyclerView()
        loadPlayers()
    }

    private fun setupViews(view: View) {
        toolbar = view.findViewById(R.id.toolbar)
        playersRecyclerView = view.findViewById(R.id.playersRecyclerView)
        
        toolbar.setNavigationOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    private fun setupRecyclerView() {
        playerAdapter = PlayerAdapter(
            onPlayerClick = { player ->
                navigateToPlayerProfile(player.id)
            }
        )
        
        playersRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = playerAdapter
        }
    }

    private fun loadPlayers() {
        // TODO: Backend - Fetch players from backend
        // val players = playerRepo.getPlayersForTeam(currentTeamId)
        
        // Sample data for demo
        val samplePlayers = listOf(
            Player(
                id = "1",
                firstName = "John",
                lastName = "Doe",
                position = "Forward",
                jerseyNumber = "10",
                isActive = true
            ),
            Player(
                id = "2",
                firstName = "Jane",
                lastName = "Smith",
                position = "Midfielder",
                jerseyNumber = "8",
                isActive = false
            ),
            Player(
                id = "3",
                firstName = "Mike",
                lastName = "Johnson",
                position = "Defender",
                jerseyNumber = "4",
                isActive = false
            )
        )
        
        playerAdapter.updatePlayers(samplePlayers)
        Snackbar.make(requireView(), "Players loaded", Snackbar.LENGTH_SHORT).show()
    }

    private fun navigateToPlayerProfile(playerId: String) {
        // TODO: Backend - Navigate to player profile with player data
        val playerProfileFragment = PlayerProfileFragment()
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, playerProfileFragment)
            .addToBackStack("player_list_to_player_profile")
            .commit()
    }
} 