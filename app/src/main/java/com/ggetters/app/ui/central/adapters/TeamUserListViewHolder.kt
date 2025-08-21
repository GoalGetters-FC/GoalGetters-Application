package com.ggetters.app.ui.central.adapters

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.ggetters.app.R
import com.ggetters.app.core.utils.Clogger
import com.ggetters.app.data.model.User
import com.ggetters.app.data.model.UserRole
import com.ggetters.app.data.model.UserStatus
import com.ggetters.app.databinding.ItemUserBinding

class TeamUserListViewHolder(
    private val binding: ItemUserBinding,
    private val asAdmin: Boolean,
    private val activeUserAuthId: String,
    private val onClick: (User) -> Unit
) : RecyclerView.ViewHolder(binding.root) {
    companion object {
        private const val TAG = "TeamUserListViewHolder"
    }


// --- Functions


    fun bind(
        item: User
    ) {
        setupOnClicked(item)

        binding.tvUserName.text = item.fullName()
        binding.tvUserRole.text = mapUserRole(item.role)

        renderUserChip(item)
        renderDefaultIcon(item)
        renderAnnexedIcon(item)
        renderOptionsMenu()
    }


// --- Event Handlers


    private fun setupOnClicked(item: User) {
        binding.container.setOnClickListener {
            onClick(item)
        }
    }


// --- Internals


    private fun mapUserRole(
        role: UserRole
    ): String = when (role) {
        UserRole.COACH -> "Team Coach"
        UserRole.COACH_PLAYER -> "In-Team Coach"
        UserRole.PART_TIME_PLAYER -> "Hobbyist Member"
        UserRole.FULL_TIME_PLAYER -> "Full-Time Player"
        else -> "Other"
    }
    
    
    private fun renderOptionsMenu() = with(binding.cvIconMenu) {
        visibility = when (asAdmin) {
            true -> View.VISIBLE
            else -> View.GONE
        }
    }


    private fun renderUserChip(item: User) = with(binding.cvChip) {
        visibility = when (item.authId) {
            activeUserAuthId -> View.VISIBLE
            else -> View.GONE
        }
    }


    private fun renderDefaultIcon(item: User) = with(binding.ivIconDefault) {
        setImageResource(
            when (item.status) {
                UserStatus.ACTIVE -> {
                    val administrativeUsers = listOf(
                        UserRole.COACH, UserRole.COACH_PLAYER
                    )

                    when (administrativeUsers.contains(item.role)) {
                        true -> R.drawable.xic_x36_uic_line_user_md
                        else -> R.drawable.xic_x36_uic_line_user
                    }
                }

                UserStatus.INJURY -> {
                    R.drawable.xic_x36_uic_line_band_aid
                }

                else -> {
                    Clogger.w(TAG, "Illegal user status found for user: ${item.id}")
                    R.drawable.xic_x36_uic_line_question_circle
                }
            }
        )
    }


    private fun renderAnnexedIcon(item: User) = with(binding.cvIconAnnexed) {
        visibility = when (item.joinedAt) {
            null -> View.GONE
            else -> View.VISIBLE
        }
    }
}