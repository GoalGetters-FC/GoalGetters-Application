package com.ggetters.app.ui.central.adapters

import androidx.recyclerview.widget.DiffUtil
import com.ggetters.app.core.utils.Clogger
import com.ggetters.app.data.model.User

class TeamUserListDiffCallback : DiffUtil.ItemCallback<User>() {
    companion object {
        private const val TAG = "TeamUserListDiffCallback"
        private const val DEV_VERBOSE_LOGGER = false
    }


// --- Functions


    override fun areItemsTheSame(
        oldItem: User, newItem: User
    ): Boolean {
        val result = (oldItem.id == newItem.id)
        if (DEV_VERBOSE_LOGGER) Clogger.d(
            TAG,
            "<areItemsTheSame>: oldItem.id=[${oldItem.id}], newItem.id=[${newItem.id}], result=[${result}]"
        )

        return result
    }


    override fun areContentsTheSame(
        oldItem: User, newItem: User
    ): Boolean {
        val result = (oldItem == newItem)
        if (DEV_VERBOSE_LOGGER) Clogger.d(
            TAG,
            "<areContentsTheSame>: oldItem.id=[${oldItem.id}], newItem.id=[${newItem.id}], result=[${result}]"
        )

        return result
    }
}