package com.ggetters.app.ui.central.views

import android.content.Context
import android.util.AttributeSet
import android.view.View
import com.google.android.material.bottomnavigation.BottomNavigationView

class BadgeBottomNavigationView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : BottomNavigationView(context, attrs, defStyleAttr) {

    private var notificationBadge: View? = null

    override fun onFinishInflate() {
        super.onFinishInflate()
        // TODO: Implement badge functionality for other tabs if needed
    }

    fun showNotificationBadge(show: Boolean) {
        // TODO: Implement badge functionality for profile tab or other tabs
        // For now, this is a placeholder for future badge implementation
    }
}