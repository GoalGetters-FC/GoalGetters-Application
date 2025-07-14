package com.ggetters.app.ui.central.adapters

import androidx.recyclerview.widget.DiffUtil
import com.ggetters.app.core.utils.Clogger
import com.ggetters.app.ui.central.models.Event

class EventDiffCallback : DiffUtil.ItemCallback<Event>() {
    companion object {
        private const val TAG = "EventDiffCallback"
        private const val DEV_VERBOSE_LOGGER = true
    }


    // --- Functions


    override fun areContentsTheSame(
        oldItem: Event, newItem: Event
    ): Boolean {
        val result = (oldItem.id == newItem.id)
        if (DEV_VERBOSE_LOGGER) Clogger.d(
            TAG,
            "<areItemsTheSame>: oldItem.id=[${oldItem.id}], newItem.id=[${newItem.id}], result=[${result}]"
        )

        return result
    }


    override fun areItemsTheSame(
        oldItem: Event, newItem: Event
    ): Boolean {
        val result = (oldItem == newItem)
        if (DEV_VERBOSE_LOGGER) Clogger.d(
            TAG,
            "<areContentsTheSame>: oldItem.id=[${oldItem.id}], newItem.id=[${newItem.id}], result=[${result}]"
        )

        return result
    }
}