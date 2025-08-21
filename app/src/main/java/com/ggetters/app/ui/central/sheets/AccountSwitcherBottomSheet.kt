package com.ggetters.app.ui.central.sheets

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton
import com.ggetters.app.R
import com.ggetters.app.data.model.Team
import com.ggetters.app.ui.central.models.UserAccount
import com.ggetters.app.ui.management.viewmodels.TeamViewerViewModel
import kotlinx.coroutines.flow.combine
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class AccountSwitcherBottomSheet : BottomSheetDialogFragment() {

    // Keep the old callback type to avoid touching call-sites; we’ll pass a minimal UserAccount.
    private var onAccountSelected: ((UserAccount) -> Unit)? = null

    // ► Use the same VM as the host (it exposes teams + switchTo)
    private val model: TeamViewerViewModel by activityViewModels()

    companion object {
        private const val ARG_ACCOUNTS = "accounts" // ignored now (back-compat)

        fun newInstance(
            accounts: List<UserAccount>,                 // ignored
            onAccountSelected: (UserAccount) -> Unit
        ): AccountSwitcherBottomSheet {
            return AccountSwitcherBottomSheet().apply {
                this.onAccountSelected = onAccountSelected
                // keep arg for back-compat; we won’t read it anymore
                val args = Bundle().apply { putParcelableArray(ARG_ACCOUNTS, accounts.toTypedArray()) }
                arguments = args
            }
        }
    }

    private lateinit var recycler: RecyclerView
    private lateinit var manageBtn: MaterialButton
    private val adapter = TeamAdapter(
        onClick = { team -> onTeamClicked(team) }
    )

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.bottom_sheet_account_switcher, container, false)



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recycler = view.findViewById(R.id.accountsRecyclerView)
        recycler.layoutManager = LinearLayoutManager(context)
        recycler.adapter = adapter

        manageBtn = view.findViewById(R.id.manageTeamsButton)
        manageBtn.setOnClickListener {
            startActivity(android.content.Intent(requireContext(), com.ggetters.app.ui.management.views.TeamViewerActivity::class.java))
            dismiss()
        }

        // Render REAL data: teams list drives both items and active selection
        viewLifecycleOwner.lifecycleScope.launch {
            model.teams.collectLatest { teams ->
                val activeId = teams.firstOrNull { it.isActive }?.id
                adapter.submit(teams, activeId)
            }
        }
    }


    private fun onTeamClicked(team: Team) {
        // Switch locally (Room-first) + VM sync
        model.switchTo(team)
        Toast.makeText(requireContext(), "Switched to ${team.name}", Toast.LENGTH_SHORT).show()

        // Back-compat callback: emit a minimal UserAccount derived from the team
        onAccountSelected?.invoke(
            UserAccount(
                id = "N/A",
                name = "N/A",
                email = "N/A",
                avatar = null,
                teamName = team.name,
                role = "N/A",
                isActive = true
            )
        )
        dismiss()
    }

    // ---------- Adapter showing Teams using your existing item_account layout ----------
    private class TeamAdapter(
        private val onClick: (Team) -> Unit
    ) : RecyclerView.Adapter<TeamAdapter.VH>() {

        private var items: List<Team> = emptyList()
        private var activeId: String? = null

        fun submit(newItems: List<Team>, activeId: String?) {
            this.items = newItems
            this.activeId = activeId
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val v = LayoutInflater.from(parent.context).inflate(R.layout.item_account, parent, false)
            return VH(v, onClick)
        }

        override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(items[position], activeId)
        override fun getItemCount(): Int = items.size

        class VH(itemView: View, private val onClick: (Team) -> Unit) : RecyclerView.ViewHolder(itemView) {
            private val avatarImage: ImageView = itemView.findViewById(R.id.accountAvatar)
            private val nameText: TextView   = itemView.findViewById(R.id.accountName)
            private val emailText: TextView  = itemView.findViewById(R.id.accountEmail)
            private val teamText: TextView   = itemView.findViewById(R.id.accountTeam)
            private val roleText: TextView   = itemView.findViewById(R.id.accountRole)
            private val activeIndicator: View = itemView.findViewById(R.id.activeIndicator)

            fun bind(team: Team, activeId: String?) {
                // Map Team → your existing row fields
                nameText.text = team.name
                emailText.text = team.code ?: ""                // secondary line
                teamText.text  = team.alias ?: ""               // third line (club/alias)
                roleText.text  = team.composition.name.replace('_', ' ').lowercase().replaceFirstChar { it.uppercase() }

                activeIndicator.visibility = if (team.id == activeId) View.VISIBLE else View.GONE

                // Optional: team crest / color if you have it
                avatarImage.setImageResource(R.drawable.ic_unicons_soccer_24)

                itemView.setOnClickListener { onClick(team) }
            }
        }
    }
}
