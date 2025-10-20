package com.ggetters.app.core.utils

import com.ggetters.app.data.model.UserRole

object AuthorizedAccessUtil {

    /** Returns true if the user can perform write/edit actions. */
    fun canEdit(role: UserRole?): Boolean {
        return role == UserRole.COACH || role == UserRole.COACH_PLAYER
    }

    /** Returns true if the user can only view (non-editable). */
    fun canViewOnly(role: UserRole?): Boolean {
        return !canEdit(role)
    }

    /** General helper for visibility logic in UI. */
    fun shouldShowEditControls(role: UserRole?): Boolean = canEdit(role)

    /** Optional helper to describe user capability. */
    fun accessLabel(role: UserRole?): String =
        if (canEdit(role)) "Edit Access" else "View Only"
}
