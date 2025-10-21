package com.ggetters.app.ui.central.views

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.ggetters.app.R
import com.ggetters.app.core.models.results.Final
import com.ggetters.app.core.utils.Clogger
import com.ggetters.app.data.model.User
import com.ggetters.app.databinding.FragmentHomeTeamBinding
import com.ggetters.app.ui.central.adapters.TeamUserListAdapter
import com.ggetters.app.ui.central.dialogs.InsertUserDialog
import com.ggetters.app.ui.central.models.AppbarTheme
import com.ggetters.app.ui.central.models.HomeUiConfiguration
import com.ggetters.app.ui.central.viewmodels.HomeTeamViewModel
import com.ggetters.app.ui.central.viewmodels.HomeViewModel
import com.ggetters.app.ui.shared.models.Clickable
import com.ggetters.app.ui.shared.viewmodels.AuthViewModel
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch


@AndroidEntryPoint
class HomeTeamFragment : Fragment(), Clickable {
    companion object {
        private const val TAG = "HomeTeamFragment"
    }


// --- Fields


    private val activeModel: HomeTeamViewModel by viewModels()
    private val sharedModel: HomeViewModel by activityViewModels()
    private val authViewModel: AuthViewModel by viewModels()


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

        sharedModel.useViewConfiguration(
            HomeUiConfiguration(
                appBarColor = AppbarTheme.NIGHT,
                appBarTitle = "",
                appBarShown = true,
            )
        )

        setupRecyclerView()
        setupTouchListeners()
        observe()
    }


// --- ViewModel


    private fun observe() = viewLifecycleOwner.lifecycleScope.launch {
        viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            // Active team
            launch {
                activeModel.activeTeam.collect { team ->
                    if (team == null) {
                        binds.widgetHeader.setHeadingText(getString(R.string.no_active_team))
                        binds.fab.isEnabled = false
                        adapter.update(emptyList())
                    } else {
                        binds.widgetHeader.setHeadingText(team.name)
                        binds.widgetHeader.setMessageText("Football (Soccer)")
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


// --- Internals


    private fun setupRecyclerView() {
        adapter = TeamUserListAdapter(
            withAdministrativeAuthorization = true,
            onClick = ::onListItemClickedCallback,
            activeUserAuthId = activeModel.getCurrentUserAuthId()
        )

        binds.rvUsers.layoutManager = LinearLayoutManager(context)
        binds.rvUsers.adapter = adapter
    }


// --- Delegates


    private fun onListItemClickedCallback(
        selected: User
    ) {
        val navigationIntent = PlayerProfileFragment.newInstance(selected.id)
        parentFragmentManager.beginTransaction().apply {
            replace(R.id.fragmentContainer, navigationIntent)
            addToBackStack("players_to_player_profile")
        }.commit()
    }


// --- Event Handlers


    override fun setupTouchListeners() {
        binds.fab.setOnClickListener(this)
    }


    override fun onClick(view: View?) = when (view?.id) {
        binds.fab.id -> {
            val t = activeModel.activeTeam.value
            if (t == null) {
                Snackbar.make(
                    requireView(), "Select an active team first", Snackbar.LENGTH_SHORT
                ).show()
            } else {
                InsertUserDialog.newInstance { result ->
                    if (result is Final.Success) {
                        activeModel.insertUser(result.product)
                    }
                }.show(
                    parentFragmentManager, InsertUserDialog.TAG
                )
            }
        }

        else -> {
            Clogger.w(
                TAG, "Unhandled on-click for: ${view?.id}"
            )
        }
    }


// --- UI


    private fun createBindings(
        inflater: LayoutInflater, container: ViewGroup?
    ): View {
        binds = FragmentHomeTeamBinding.inflate(inflater, container, false)
        binds.lifecycleOwner = viewLifecycleOwner
        binds.authSource = authViewModel
        return binds.root
    }
}
