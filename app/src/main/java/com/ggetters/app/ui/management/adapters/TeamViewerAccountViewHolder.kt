package com.ggetters.app.ui.management.adapters

import android.view.View
import android.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.ggetters.app.R
import com.ggetters.app.core.utils.Clogger
import com.ggetters.app.data.model.Team
import com.ggetters.app.databinding.ItemTeamViewerTeamBinding

/**
 * TODO: Conditionally render that a team is currently selected
 * TODO: Bind the role of the user viewing this in the displayed team
 * TODO: Bind the number of users in the team
 */
class TeamViewerAccountViewHolder(
    private val binding: ItemTeamViewerTeamBinding,
    private val onSelectClicked: (Team) -> Unit,
    private val onDeleteClicked: (Team) -> Unit,
    private val onClick: (Team) -> Unit,
) : RecyclerView.ViewHolder(binding.root) {
    companion object {
        private const val TAG = "TeamViewerAccountViewHolder"
        private const val DEV_VERBOSE_LOGGER = false
    }

    // --- Internals

    /**
     * Binds the data to the view.
     */
    fun bind(item: Team) {
        if (DEV_VERBOSE_LOGGER) Clogger.d(TAG, "<bind>: id=[${item.id}]")

        // Apply the objects information to the view
        binding.apply {
            cvContainer.setOnClickListener {
                onClick(item)
            }

            ivOptions.setOnClickListener {
                showPopupMenu(it, item)
            }

            tvTeamName.text = item.name

            // Set role (Coach/Player) - for now showing Coach for first team, Player for second
            tvTeamRole.text = if (item.name.contains("U15a")) "Coach" else "Full-time Player"

            // Set member count - for now showing 15 for first team, 8 for second
            //tvTeamCount.text = if (item.name.contains("U15a")) "15 members" else "8 members"
        }
    }

    private fun showPopupMenu(view: View, item: Team) = PopupMenu(view.context, view).apply {
        inflate(R.menu.menu_team_viewer_account)
        setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_item_team_viewer_account_select -> onSelectClicked(item)
                R.id.nav_item_team_viewer_account_delete -> onDeleteClicked(item)
                else -> {
                    Clogger.w(TAG, "Unhandled menu-item-on-click for: ${menuItem.itemId}")
                    false
                }
            }
            true
        }
    }.show()
}