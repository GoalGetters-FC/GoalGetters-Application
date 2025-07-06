package com.ggetters.app.ui.central.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import com.ggetters.app.R
import com.google.android.material.bottomnavigation.BottomNavigationView

class BadgeBottomNavigationView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : BottomNavigationView(context, attrs, defStyleAttr) {

    private var notificationBadge: View? = null

    override fun onFinishInflate() {
        super.onFinishInflate()
        setupNotificationBadge()
    }

    private fun setupNotificationBadge() {
        // Find the notification tab and add badge
        val menu = menu
        val notificationItem = menu.findItem(R.id.nav_notifications)
        
        // Create a custom view for the notification tab
        val customView = LayoutInflater.from(context).inflate(R.layout.bottom_nav_notification_item, null)
        notificationBadge = customView.findViewById(R.id.notificationBadge)
        
        notificationItem.setActionView(customView)
    }

    fun showNotificationBadge(show: Boolean) {
        notificationBadge?.visibility = if (show) VISIBLE else GONE
    }
}