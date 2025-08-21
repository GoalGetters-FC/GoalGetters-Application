package com.ggetters.app.ui.central.views

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AutoCompleteTextView
import androidx.fragment.app.Fragment
import android.content.Intent
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.ggetters.app.R
import com.ggetters.app.core.extensions.navigateTo
import com.ggetters.app.core.extensions.navigateToActivity
import com.ggetters.app.core.utils.Clogger
import com.ggetters.app.data.model.User
import com.ggetters.app.data.model.UserPosition
import com.ggetters.app.data.model.UserRole
import com.ggetters.app.data.model.UserStatus
import com.ggetters.app.databinding.FragmentHomeTeamBinding
import com.ggetters.app.ui.central.adapters.TeamUserListAdapter
import com.ggetters.app.ui.central.viewmodels.HomeTeamViewModel
import com.ggetters.app.ui.central.viewmodels.HomeViewModel
import com.ggetters.app.ui.shared.models.Clickable
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import java.time.Instant
import java.time.LocalDate
import java.time.Month
import java.util.UUID

@AndroidEntryPoint
class HomeTeamFragment : Fragment(), Clickable {
    companion object {
        private const val TAG = "HomeTeamFragment"
    }


// --- Fields


    private val activeModel: HomeTeamViewModel by viewModels()
    private val sharedModel: HomeViewModel by activityViewModels()


    private lateinit var binds: FragmentHomeTeamBinding
    private lateinit var adapter: TeamUserListAdapter


// --- Lifecycle


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? = createBindings(inflater, container)


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Clogger.d(
            TAG, "Created a new instance of HomeTeamFragment"
        )

        val seed = listOf(
            User(
                id = UUID.randomUUID().toString(),
                authId = activeModel.getCurrentUserAuthId(),
                teamId = UUID.randomUUID().toString(),
                joinedAt = Instant.parse("2023-01-15T10:00:00Z"),
                name = "Jane",
                surname = "Abigail Doe",
                alias = "JD",
                dateOfBirth = LocalDate.of(1995, Month.MARCH, 22),
                email = "jane.doe@example.com",
                position = UserPosition.GOALKEEPER,
                role = UserRole.COACH,
                number = 9,
                status = UserStatus.ACTIVE,
            ), User(
                id = UUID.randomUUID().toString(),
                authId = UUID.randomUUID().toString(),
                teamId = UUID.randomUUID().toString(),
                name = "Fortune",
                surname = "Martinez",
                alias = "JD",
                dateOfBirth = LocalDate.of(1995, Month.MARCH, 22),
                email = "jane.doe@example.com",
                position = UserPosition.GOALKEEPER,
                number = 9,
                status = UserStatus.ACTIVE,
            )
        )

        adapter = TeamUserListAdapter(
            withAdministrativeAuthorization = true,
            onClick = ::onListItemClickedCallback,
            activeUserAuthId = activeModel.getCurrentUserAuthId()
        )

        binds.rvUsers.layoutManager = LinearLayoutManager(context)
        binds.rvUsers.adapter = adapter

        adapter.update(seed)

        setupTouchListeners()
    }


// --- Delegates


    private fun onListItemClickedCallback(selected: User) {
        val intent = Intent(requireContext(), UserProfileActivity::class.java).apply {
            putExtra(UserProfileActivity.EXTRA_PROFILE_TYPE, UserProfileActivity.PROFILE_TYPE_PLAYER)
            putExtra(UserProfileActivity.EXTRA_PROFILE_ID, selected.id)
        }
        requireActivity().navigateToActivity(intent)
    }
    
    
// --- Modal GUI
    
    
    private fun showInsertDialog() {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_add_player, null)
        
        val dialog = AlertDialog.Builder(requireContext(), R.style.Theme_GoalGetters_Dialog)
            .setView(dialogView)
            .create()

        // Position dropdown
        val positionInput = dialogView.findViewById<AutoCompleteTextView>(R.id.playerPositionInput)
        val positions = arrayOf(
            "Striker", "Forward", "Midfielder", "Defender",
            "Goalkeeper", "Winger", "Center Back", "Full Back"
        )
        
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

            // Basic validation
            if (firstName.isBlank()) {
                dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.playerFirstNameInput).error =
                    "First name is required"
                return@setOnClickListener
            }
            if (lastName.isBlank()) {
                dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.playerLastNameInput).error =
                    "Last name is required"
                return@setOnClickListener
            }
            if (email.isBlank()) {
                dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.playerEmailInput).error =
                    "Email is required"
                return@setOnClickListener
            }
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.playerEmailInput).error =
                    "Please enter a valid email"
                return@setOnClickListener
            }
            if (position.isBlank()) {
                dialogView.findViewById<AutoCompleteTextView>(R.id.playerPositionInput).error =
                    "Position is required"
                return@setOnClickListener
            }

            // TODO: Backend - Save player to repository
            // For now, just show success message
            Snackbar.make(
                requireView(),
                "Player ${firstName} ${lastName} added",
                Snackbar.LENGTH_LONG
            ).show()

            dialog.dismiss()
        }

        dialog.show()
    }


// --- Event Handlers


    override fun setupTouchListeners() {
        binds.fab.setOnClickListener(this)
    }


    override fun onClick(view: View?) = when (view?.id) {
        binds.fab.id -> {
            showInsertDialog()
        }

        else -> {
            Clogger.w(
                TAG, "Unhandled on-click for: ${view?.id}"
            )
        }
    }


// --- UI


    /**
     * Construct the view binding for this fragment.
     *
     * @return the root [View] of this fragment within the same context as every
     *         other invocation of the binding instance. This is crucial because
     *         otherwise they would exist in different contexts.
     */
    private fun createBindings(
        inflater: LayoutInflater, container: ViewGroup?
    ): View {
        binds = FragmentHomeTeamBinding.inflate(inflater, container, false)
        return binds.root
    }
}