package com.ggetters.app.ui.central.views

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ggetters.app.R
import com.ggetters.app.ui.central.adapters.PlayerAdapter
import com.ggetters.app.ui.central.models.Player
import com.ggetters.app.ui.central.models.PlayerStats

class HomePlayersFragment : Fragment() {
    
    private lateinit var playerAdapter: PlayerAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyStateText: TextView
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_players, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupViews(view)
        setupRecyclerView()
        loadPlayers()
    }
    
    private fun setupViews(view: View) {
        recyclerView = view.findViewById(R.id.playersRecyclerView)
        emptyStateText = view.findViewById(R.id.emptyStateText)
    }
    
    private fun setupRecyclerView() {
        playerAdapter = PlayerAdapter(
            onPlayerClick = { player ->
                // TODO: Navigate to player details
            },
            onPlayerLongClick = { player ->
                // TODO: Show player options menu
            }
        )
        
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = playerAdapter
    }
    
    private fun loadPlayers() {
        // TODO: Backend - Fetch players from API
        // Endpoint: GET /api/teams/{teamId}/players
        // Request: { teamId: String }
        // Response: { players: Player[], total: number }
        // Error handling: { message: String, code: String }
        
        // Sample data for now
        val samplePlayers = listOf(
            Player(
                id = "1",
                name = "John Doe",
                position = "Forward",
                jerseyNumber = "10",
                avatar = null,
                isActive = true,
                stats = PlayerStats(
                    goals = 15,
                    assists = 8,
                    matches = 25
                )
            ),
            Player(
                id = "2",
                name = "Jane Smith",
                position = "Midfielder",
                jerseyNumber = "8",
                avatar = null,
                isActive = true,
                stats = PlayerStats(
                    goals = 5,
                    assists = 12,
                    matches = 22
                )
            ),
            Player(
                id = "3",
                name = "Mike Johnson",
                position = "Defender",
                jerseyNumber = "4",
                avatar = null,
                isActive = false,
                stats = PlayerStats(
                    goals = 1,
                    assists = 3,
                    matches = 18
                )
            )
        )
        
        playerAdapter.updatePlayers(samplePlayers)
        updateEmptyState(samplePlayers.isEmpty())
    }
    
    private fun updateEmptyState(isEmpty: Boolean) {
        if (isEmpty) {
            recyclerView.visibility = View.GONE
            emptyStateText.visibility = View.VISIBLE
        } else {
            recyclerView.visibility = View.VISIBLE
            emptyStateText.visibility = View.GONE
        }
    }
} 