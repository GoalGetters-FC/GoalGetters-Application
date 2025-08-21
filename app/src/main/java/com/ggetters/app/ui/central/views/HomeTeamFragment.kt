package com.ggetters.app.ui.central.views

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AutoCompleteTextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.ggetters.app.R
import com.ggetters.app.core.utils.Clogger
import com.ggetters.app.data.model.User
import com.ggetters.app.databinding.FragmentHomeTeamBinding
import com.ggetters.app.ui.central.adapters.TeamUserListAdapter
import com.ggetters.app.ui.central.models.AppbarTheme
import com.ggetters.app.ui.central.models.HomeUiConfiguration
import com.ggetters.app.ui.central.viewmodels.HomeTeamViewModel
import com.ggetters.app.ui.central.viewmodels.HomeViewModel
import com.ggetters.app.ui.shared.models.Clickable
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HomeTeamFragment : Fragment(), Clickable {
    companion object {
        private const val TAG = "HomeTeamFragment"
    }

    private val activeModel: HomeTeamViewModel by viewModels()
    private val sharedModel: HomeViewModel by activityViewModels()

    private lateinit var binds: FragmentHomeTeamBinding
    private lateinit var adapter: TeamUserListAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View = createBindings(inflater, container)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Clogger.d(TAG, "Created a new instance of HomeTeamFragment")

        sharedModel.useViewConfiguration(
            HomeUiConfiguration(
                appBarColor = AppbarTheme.NIGHT,
                appBarTitle = "",
                appBarShown = true,
            )
        )

        setupRecyclerView()
        setupTouchListeners()
        observeViewModel()
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Active team
                launch {
                    activeModel.activeTeam.collect { team ->
                        if (team == null) {
                            binds.tvTeamAlias.text = getString(R.string.no_active_team)
                            binds.tvTeamSport.text = ""
                            binds.fab.isEnabled = false
                            adapter.update(emptyList())
                        } else {
                            binds.tvTeamAlias.text = team.name
                            binds.tvTeamSport.text = "Football (Soccer)" // TODO: derive from team.sport
                            binds.fab.isEnabled = true
                        }
                    }
                }

                // Users
                launch {
                    activeModel.teamUsers.collect { users ->
                        adapter.update(users)
                    }
                }
            }
        }
    }

    private fun setupRecyclerView() {
        adapter = TeamUserListAdapter(
            withAdministrativeAuthorization = true,
            onClick = ::onListItemClickedCallback,
            activeUserAuthId = activeModel.getCurrentUserAuthId()
        )
        binds.rvUsers.layoutManager = LinearLayoutManager(context)
        binds.rvUsers.adapter = adapter
    }

    private fun onListItemClickedCallback(selected: User) {
        val navigationIntent = PlayerProfileFragment.newInstance(selected.id)
        parentFragmentManager.beginTransaction().apply {
            replace(R.id.fragmentContainer, navigationIntent)
            addToBackStack("players_to_player_profile")
        }.commit()
    }

    private fun showInsertDialog() {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_add_player, null)

        val dialog = AlertDialog.Builder(requireContext(), R.style.Theme_GoalGetters_Dialog)
            .setView(dialogView)
            .create()

        val positionInput = dialogView.findViewById<AutoCompleteTextView>(R.id.playerPositionInput)
        val positions = arrayOf("Striker", "Forward", "Midfielder", "Defender",
            "Goalkeeper", "Winger", "Center Back", "Full Back")

        val positionAdapter = android.widget.ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            positions
        )
        positionInput.setAdapter(positionAdapter)

        dialogView.findViewById<MaterialButton>(R.id.cancelButton).setOnClickListener {
            dialog.dismiss()
        }

        dialogView.findViewById<MaterialButton>(R.id.addPlayerButton).setOnClickListener {
            val firstName =
                dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.playerFirstNameInput).text.toString().trim()
            val lastName =
                dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.playerLastNameInput).text.toString().trim()
            val email =
                dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.playerEmailInput).text.toString().trim()
            val position =
                dialogView.findViewById<AutoCompleteTextView>(R.id.playerPositionInput).text.toString().trim()
            val jerseyNumber =
                dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.playerJerseyNumberInput).text.toString().trim()
            val dateOfBirth =
                dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.playerDateOfBirthInput).text.toString().trim()

            if (firstName.isBlank()) {
                dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.playerFirstNameInput).error =
                    "First name is required"; return@setOnClickListener
            }
            if (lastName.isBlank()) {
                dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.playerLastNameInput).error =
                    "Last name is required"; return@setOnClickListener
            }
            if (email.isBlank()) {
                dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.playerEmailInput).error =
                    "Email is required"; return@setOnClickListener
            }
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.playerEmailInput).error =
                    "Please enter a valid email"; return@setOnClickListener
            }
            if (position.isBlank()) {
                dialogView.findViewById<AutoCompleteTextView>(R.id.playerPositionInput).error =
                    "Position is required"; return@setOnClickListener
            }

            val jerseyNum = jerseyNumber.toIntOrNull()

            activeModel.insertUser(
                firstName, lastName, email, position, jerseyNum,
                dateOfBirth.ifBlank { null }
            )

            Snackbar.make(requireView(),
                "Player $firstName $lastName added",
                Snackbar.LENGTH_LONG
            ).show()

            dialog.dismiss()
        }

        dialog.show()
    }

    override fun setupTouchListeners() {
        binds.fab.setOnClickListener(this)
    }

    override fun onClick(view: View?) = when (view?.id) {
        binds.fab.id -> {
            val t = activeModel.activeTeam.value
            if (t == null) {
                Snackbar.make(requireView(), "Select an active team first", Snackbar.LENGTH_SHORT).show()
            } else {
                showInsertDialog()
            }
        }
        else -> Clogger.w(TAG, "Unhandled on-click for: ${view?.id}")
    }

    private fun createBindings(
        inflater: LayoutInflater, container: ViewGroup?
    ): View {
        binds = FragmentHomeTeamBinding.inflate(inflater, container, false)
        return binds.root
    }
}
