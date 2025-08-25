package com.ggetters.app.ui.central.views

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AutoCompleteTextView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.ggetters.app.R
import com.ggetters.app.core.utils.Clogger
import com.ggetters.app.data.model.User
import com.ggetters.app.data.model.UserRole
import com.ggetters.app.data.model.UserStatus
import com.ggetters.app.databinding.FragmentPlayerDetailsBinding
import com.ggetters.app.ui.central.viewmodels.PlayerDetailsViewModel
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter

@AndroidEntryPoint
class PlayerDetailsFragment : Fragment() {

    private var _binding: FragmentPlayerDetailsBinding? = null
    private val binding get() = _binding!!

    private lateinit var ddStatus: AutoCompleteTextView
    private lateinit var ddRole: AutoCompleteTextView
    private lateinit var inpName: TextInputEditText
    private lateinit var inpNumber: TextInputEditText
    private lateinit var inpEmail: TextInputEditText
    private lateinit var inpDob: TextInputEditText
    private lateinit var inpContact: TextInputEditText

    private lateinit var tvEmail: TextView
    private lateinit var tvPhone: TextView
    private lateinit var tvJoined: TextView

    private lateinit var tvGoals: TextView
    private lateinit var tvAssists: TextView
    private lateinit var tvMatches: TextView
    private lateinit var tvYellows: TextView
    private lateinit var tvReds: TextView
    private lateinit var tvCleanSheets: TextView

    private lateinit var btnEdit: MaterialButton
    private lateinit var btnMsg: MaterialButton
    private lateinit var btnHistory: MaterialButton

    private val viewModel: PlayerDetailsViewModel by viewModels()

    companion object {
        private const val ARG_PLAYER_ID = "player_id"

        fun newInstance(playerId: String) = PlayerDetailsFragment().apply {
            arguments = Bundle().apply { putString(ARG_PLAYER_ID, playerId) }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlayerDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bindViews(view)                     // << initialize all findViewById refs

        val playerId = arguments?.getString(ARG_PLAYER_ID) ?: return
        viewModel.loadPlayer(playerId)

        observePlayer()
        setupListeners()
    }


    private fun observePlayer() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.player.collect { player ->
                    if (player != null) renderPlayer(player)
                }
            }
        }
    }

    private fun bindViews(root: View) {
        // IDs from your XML
        ddStatus      = root.findViewById(R.id.playerStatusDropdown)
        ddRole        = root.findViewById(R.id.playerRoleDropdown)
        inpName       = root.findViewById(R.id.playerNameInput)
        inpNumber     = root.findViewById(R.id.playerNumberInput)
        inpEmail      = root.findViewById(R.id.playerEmailInput)
        inpDob        = root.findViewById(R.id.playerDateOfBirthInput)
        inpContact    = root.findViewById(R.id.playerContactInput)

        tvEmail       = root.findViewById(R.id.playerEmail)
        tvPhone       = root.findViewById(R.id.playerPhone)
        tvJoined      = root.findViewById(R.id.playerJoinedDate)

        tvGoals       = root.findViewById(R.id.statsGoals)
        tvAssists     = root.findViewById(R.id.statsAssists)
        tvMatches     = root.findViewById(R.id.statsMatches)
        tvYellows     = root.findViewById(R.id.statsYellowCards)
        tvReds        = root.findViewById(R.id.statsRedCards)
        tvCleanSheets = root.findViewById(R.id.statsCleanSheets)

        btnEdit       = root.findViewById(R.id.btnEditProfile)
        btnMsg        = root.findViewById(R.id.btnSendMessage)
        btnHistory    = root.findViewById(R.id.btnViewHistory)
    }

    private fun renderPlayer(player: User) {
        // Header still via binding (these exist in all variants)
        binding.playerName.text = player.fullName()
        binding.playerAge.text = "--"
        binding.playerPosition.text = player.position?.name ?: "Unknown"
        binding.playerNumber.text = "#${player.number ?: "--"}"

        // General section (dropdowns)
        ddStatus.setText(player.status?.name ?: UserStatus.ACTIVE.name, false)
        ddRole.setText(player.role.name, false)

        // Player details
        inpName.setText(player.fullName())
        inpNumber.setText(player.number?.toString() ?: "--")
        inpEmail.setText(player.email ?: "N/A")
        inpDob.setText(player.dateOfBirth?.toString() ?: "N/A")
        inpContact.setText("N/A") // no contact field in model

        // Contact card
        tvEmail.text = "Email: ${player.email ?: "-"}"
        tvPhone.text = "Phone: N/A"
        tvJoined.text = "Joined: ${player.joinedAt?.toString() ?: "-"}"

        // Stats (placeholders)
        tvGoals.text = "0"
        tvAssists.text = "0"
        tvMatches.text = "0"
        tvYellows.text = "0"
        tvReds.text = "0"
        tvCleanSheets.text = "0"
    }

    private fun setupListeners() {
        btnEdit.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(
                    R.id.fragmentContainer,
                    PlayerProfileFragment.newInstance(
                        arguments?.getString(ARG_PLAYER_ID) ?: return@setOnClickListener,
                        startEditing = true
                    )

                )
                .addToBackStack("player_details_to_profile_edit")
                .commit()
        }
        btnMsg.setOnClickListener { Snackbar.make(requireView(), "Send Message clicked", Snackbar.LENGTH_SHORT).show() }
        btnHistory.setOnClickListener { Snackbar.make(requireView(), "View History clicked", Snackbar.LENGTH_SHORT).show() }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
