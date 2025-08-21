package com.ggetters.app.ui.central.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import com.ggetters.app.core.utils.Clogger
import com.ggetters.app.data.model.User
import com.ggetters.app.databinding.ItemUserBinding
import com.ggetters.app.ui.shared.adapters.KeyedDiffCallback

class TeamUserListAdapter(
    private val onClick: (User) -> Unit,
    private val withAdministrativeAuthorization: Boolean,
    private val activeUserAuthId: String,
) : ListAdapter<User, TeamUserListViewHolder>(KeyedDiffCallback<User>()) {
    companion object {
        private const val TAG = "TeamUserListAdapter"
    }


// --- Functions (Contract)


    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ): TeamUserListViewHolder {
        Clogger.d(
            TAG, "Creating the ViewHolder"
        )

        return TeamUserListViewHolder(
            binding = ItemUserBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            ),
            asAdmin = withAdministrativeAuthorization,
            activeUserAuthId = activeUserAuthId,
            onClick = onClick
        )
    }


    override fun onBindViewHolder(
        holder: TeamUserListViewHolder, position: Int
    ) {
        holder.bind(getItem(position))
    }


// --- Functions (Helper)


    fun update(collection: List<User>) {
        Clogger.d(
            TAG, "Updating the source collection"
        )

        submitList(collection)
    }
}