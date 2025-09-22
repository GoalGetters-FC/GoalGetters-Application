package com.ggetters.app.ui.management.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import com.ggetters.app.core.utils.Clogger
import com.ggetters.app.data.model.Team
import com.ggetters.app.databinding.ItemTeamViewerAccountBinding
import com.ggetters.app.ui.shared.adapters.KeyedDiffCallback

class TeamViewerAccountAdapter(
    private val onSelectClicked: (Team) -> Unit,
    private val onDeleteClicked: (Team) -> Unit,
) : ListAdapter<Team, TeamViewerAccountViewHolder>(KeyedDiffCallback<Team>()) {
    companion object {
        private const val TAG = "TeamViewerAccountAdapter"
        private const val DEV_VERBOSE_LOGGER = false
    }


// --- Contracts


    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ): TeamViewerAccountViewHolder {
        Clogger.d(
            TAG, "Constructing the ViewHolder"
        )

        // Construct the binding and return the view holder
        return TeamViewerAccountViewHolder(
            binding = ItemTeamViewerAccountBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            ),
            onSelectClicked = onSelectClicked,
            onDeleteClicked = onDeleteClicked,
        )
    }


    override fun onBindViewHolder(
        holder: TeamViewerAccountViewHolder, position: Int
    ) {
        if (DEV_VERBOSE_LOGGER) Clogger.d(
            TAG, "<onBindViewHolder>: position=[$position]"
        )

        holder.bind(getItem(position))
    }


// --- Internals


    fun update(collection: List<Team>) {
        Clogger.d(
            TAG, "Updating the source collection"
        )

        // Update the recycler by name ascending
        submitList(collection.sortedBy {
            it.name
        })
    }
}