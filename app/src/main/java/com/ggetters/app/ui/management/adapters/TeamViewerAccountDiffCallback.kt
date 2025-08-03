package com.ggetters.app.ui.management.adapters

import androidx.recyclerview.widget.DiffUtil
import com.ggetters.app.core.utils.Clogger
import com.ggetters.app.data.model.Team

class TeamViewerAccountDiffCallback : DiffUtil.ItemCallback<Team>() {
    companion object {
        private const val TAG = "TeamViewerAccountDiffCallback"
        private const val DEV_VERBOSE_LOGGER = false
    }


// --- Contracts


    override fun areItemsTheSame(
        oldItem: Team, newItem: Team
    ): Boolean {
        val result = (oldItem.id == newItem.id)
        if (DEV_VERBOSE_LOGGER) Clogger.d(
            TAG,
            "<areItemsTheSame>: oldItem.id=[${oldItem.id}], newItem.id=[${newItem.id}], result=[${result}]"
        )

        return result
    }


    override fun areContentsTheSame(
        oldItem: Team, newItem: Team
    ): Boolean {
        val result = (oldItem == newItem)
        if (DEV_VERBOSE_LOGGER) Clogger.d(
            TAG,
            "<areContentsTheSame>: oldItem.id=[${oldItem.id}], newItem.id=[${newItem.id}], result=[${result}]"
        )

        return result
    }
}