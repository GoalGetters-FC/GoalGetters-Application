package com.ggetters.app.ui.central.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.ggetters.app.R
import com.ggetters.app.data.model.Notification
import com.ggetters.app.data.model.NotificationType
import com.ggetters.app.data.model.LinkedEventType
import com.ggetters.app.core.utils.Clogger
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

class NotificationCardAdapter(
    private val onNotificationClick: (Notification) -> Unit = {},
    private val onMarkAsSeen: (Notification) -> Unit = {},
    private val onDelete: (Notification) -> Unit = {}
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var notifications = listOf<Notification>()
    
    companion object {
        private const val VIEW_TYPE_REMINDER = 0
        private const val VIEW_TYPE_MATCH_RESULT = 1
        private const val VIEW_TYPE_SCHEDULE = 2
        private const val VIEW_TYPE_ANNOUNCEMENT = 3
        private const val VIEW_TYPE_DEFAULT = 4
    }

    fun updateNotifications(newNotifications: List<Notification>) {
        val diffCallback = NotificationDiffCallback(notifications, newNotifications)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        notifications = newNotifications
        diffResult.dispatchUpdatesTo(this)
    }

    override fun getItemViewType(position: Int): Int {
        val notification = notifications[position]
        return when (notification.type) {
            NotificationType.GAME_REMINDER, NotificationType.PRACTICE_REMINDER -> VIEW_TYPE_REMINDER
            NotificationType.POST_MATCH_SUMMARY -> VIEW_TYPE_MATCH_RESULT
            NotificationType.GAME_NOTIFICATION, NotificationType.PRACTICE_NOTIFICATION -> VIEW_TYPE_SCHEDULE
            NotificationType.ANNOUNCEMENT, NotificationType.ADMIN_MESSAGE -> VIEW_TYPE_ANNOUNCEMENT
            else -> VIEW_TYPE_DEFAULT
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_REMINDER -> {
                val view = inflater.inflate(R.layout.item_notification_reminder, parent, false)
                ReminderViewHolder(view)
            }
            VIEW_TYPE_MATCH_RESULT -> {
                val view = inflater.inflate(R.layout.item_notification_match_result, parent, false)
                MatchResultViewHolder(view)
            }
            VIEW_TYPE_SCHEDULE -> {
                val view = inflater.inflate(R.layout.item_notification_schedule, parent, false)
                ScheduleViewHolder(view)
            }
            VIEW_TYPE_ANNOUNCEMENT -> {
                val view = inflater.inflate(R.layout.item_notification_announcement, parent, false)
                AnnouncementViewHolder(view)
            }
            else -> {
                val view = inflater.inflate(R.layout.item_notification_reminder, parent, false)
                DefaultViewHolder(view)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val notification = notifications[position]
        when (holder) {
            is ReminderViewHolder -> holder.bind(notification)
            is MatchResultViewHolder -> holder.bind(notification)
            is ScheduleViewHolder -> holder.bind(notification)
            is AnnouncementViewHolder -> holder.bind(notification)
            is DefaultViewHolder -> holder.bind(notification)
        }
    }

    override fun getItemCount(): Int = notifications.size

    // Base ViewHolder with common functionality
    abstract inner class BaseNotificationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        protected val unreadIndicator: View = itemView.findViewById(R.id.unreadIndicator)
        protected val notificationIcon: ImageView = itemView.findViewById(R.id.notificationIcon)
        protected val notificationTitle: TextView = itemView.findViewById(R.id.notificationTitle)
        protected val notificationTime: TextView = itemView.findViewById(R.id.notificationTime)
        protected val optionsButton: ImageButton = itemView.findViewById(R.id.optionsButton)

        protected fun setupCommonElements(notification: Notification) {
            // Set unread indicator visibility
            if (notification.isSeen) {
                unreadIndicator.visibility = View.GONE
                unreadIndicator.contentDescription = null
            } else {
                unreadIndicator.visibility = View.VISIBLE
                unreadIndicator.contentDescription = "Unread notification"
            }

            // Set notification icon based on type
            notificationIcon.setImageResource(getIconForType(notification.type))
            notificationIcon.setColorFilter(itemView.context.getColor(getColorForType(notification.type)))

            // Set title and time
            notificationTitle.text = notification.title
            notificationTime.text = formatTime(notification.createdAt)

            // Setup options menu
            setupOptionsMenu(notification)

            // Setup click listener
            itemView.setOnClickListener {
                onNotificationClick(notification)
            }
        }

        private fun setupOptionsMenu(notification: Notification) {
            optionsButton.setOnClickListener { view ->
                val popup = PopupMenu(view.context, view)
                popup.menuInflater.inflate(R.menu.notification_options, popup.menu)
                
                popup.setOnMenuItemClickListener { item ->
                    when (item.itemId) {
                        R.id.markAsSeen -> {
                            onMarkAsSeen(notification)
                            true
                        }
                        R.id.delete -> {
                            onDelete(notification)
                            true
                        }
                        else -> false
                    }
                }
                popup.show()
            }
        }

        private fun getIconForType(type: NotificationType): Int {
            return when (type) {
                NotificationType.GAME_REMINDER, NotificationType.PRACTICE_REMINDER -> R.drawable.ic_unicons_bell_24
                NotificationType.POST_MATCH_SUMMARY -> R.drawable.ic_unicons_trophy_24
                NotificationType.GAME_NOTIFICATION, NotificationType.PRACTICE_NOTIFICATION -> R.drawable.ic_unicons_calendar_24
                NotificationType.ANNOUNCEMENT, NotificationType.ADMIN_MESSAGE -> R.drawable.ic_unicons_megaphone_24
                NotificationType.SCHEDULE_CHANGE -> R.drawable.ic_unicons_clock_24
                NotificationType.PLAYER_UPDATE -> R.drawable.ic_unicons_user_24
                else -> R.drawable.ic_unicons_bell_24
            }
        }

        private fun getColorForType(type: NotificationType): Int {
            return when (type) {
                NotificationType.GAME_REMINDER, NotificationType.PRACTICE_REMINDER -> R.color.primary
                NotificationType.POST_MATCH_SUMMARY -> R.color.primary
                NotificationType.GAME_NOTIFICATION, NotificationType.PRACTICE_NOTIFICATION -> R.color.primary
                NotificationType.ANNOUNCEMENT, NotificationType.ADMIN_MESSAGE -> R.color.primary
                NotificationType.SCHEDULE_CHANGE -> R.color.primary
                NotificationType.PLAYER_UPDATE -> R.color.primary
                else -> R.color.text_secondary
            }
        }

        private fun formatTime(createdAt: Instant): String {
            val now = Instant.now()
            val days = ChronoUnit.DAYS.between(createdAt, now)
            
            return when {
                days == 0L -> "Today"
                days == 1L -> "Yesterday"
                days < 7L -> "$days days ago"
                else -> {
                    val formatter = DateTimeFormatter.ofPattern("dd MMM yyyy")
                    createdAt.atZone(ZoneId.systemDefault()).format(formatter)
                }
            }
        }
    }

    inner class ReminderViewHolder(itemView: View) : BaseNotificationViewHolder(itemView) {
        fun bind(notification: Notification) {
            setupCommonElements(notification)
        }
    }

    inner class MatchResultViewHolder(itemView: View) : BaseNotificationViewHolder(itemView) {
        private val scoreCard: LinearLayout = itemView.findViewById(R.id.scoreCard)
        private val homeScore: TextView = itemView.findViewById(R.id.homeScore)
        private val awayScore: TextView = itemView.findViewById(R.id.awayScore)
        private val homeLabel: TextView = itemView.findViewById(R.id.homeLabel)
        private val awayLabel: TextView = itemView.findViewById(R.id.awayLabel)

        fun bind(notification: Notification) {
            setupCommonElements(notification)
            
            // Parse score data from notification data
            try {
                val json = org.json.JSONObject(notification.data)
                val homeScoreValue = json.optInt("homeScore", 0)
                val awayScoreValue = json.optInt("awayScore", 0)
                val homeTeam = json.optString("homeTeam", "Home")
                val awayTeam = json.optString("awayTeam", "Away")
                
                homeScore.text = homeScoreValue.toString()
                awayScore.text = awayScoreValue.toString()
                homeLabel.text = homeTeam
                awayLabel.text = awayTeam
                
                // Change background based on win/lose
                val isWin = homeScoreValue > awayScoreValue
                scoreCard.setBackgroundResource(
                    if (isWin) R.drawable.score_card_background 
                    else R.drawable.score_card_background_lose
                )
                
            } catch (e: Exception) {
                Clogger.e("NotificationCardAdapter", "Failed to parse score data", e)
                // Hide score card if parsing fails and clear sensitive data
                scoreCard.visibility = View.GONE
                homeLabel.text = ""
                awayLabel.text = ""
                homeScore.text = ""
                awayScore.text = ""
            }
        }
    }

    inner class ScheduleViewHolder(itemView: View) : BaseNotificationViewHolder(itemView) {
        private val scheduleCard: LinearLayout = itemView.findViewById(R.id.scheduleCard)
        private val scheduleDateTime: TextView = itemView.findViewById(R.id.scheduleDateTime)

        fun bind(notification: Notification) {
            setupCommonElements(notification)
            
            // Parse schedule data
            try {
                val json = org.json.JSONObject(notification.data)
                val eventDate = json.optString("eventDate", "")
                val eventTime = json.optString("eventTime", "")
                
                scheduleDateTime.text = "$eventDate $eventTime"
                
            } catch (e: Exception) {
                Clogger.e("NotificationCardAdapter", "Failed to parse schedule data", e)
                scheduleCard.visibility = View.GONE
                scheduleDateTime.text = ""
            }
        }
    }

    inner class AnnouncementViewHolder(itemView: View) : BaseNotificationViewHolder(itemView) {
        fun bind(notification: Notification) {
            setupCommonElements(notification)
        }
    }

    inner class DefaultViewHolder(itemView: View) : BaseNotificationViewHolder(itemView) {
        fun bind(notification: Notification) {
            setupCommonElements(notification)
        }
    }

    private class NotificationDiffCallback(
        private val oldList: List<Notification>,
        private val newList: List<Notification>
    ) : DiffUtil.Callback() {

        override fun getOldListSize(): Int = oldList.size
        override fun getNewListSize(): Int = newList.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition].id == newList[newItemPosition].id
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            val old = oldList[oldItemPosition]
            val new = newList[newItemPosition]
            return old.title == new.title &&
                    old.message == new.message &&
                    old.isSeen == new.isSeen &&
                    old.isPinned == new.isPinned
        }
    }
}
