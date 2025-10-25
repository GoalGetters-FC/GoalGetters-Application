package com.ggetters.app.core.services

import com.ggetters.app.core.utils.AuthorizedAccessUtil
import com.ggetters.app.core.utils.Clogger
import com.ggetters.app.data.repository.user.CombinedUserRepository
import javax.inject.Inject

class AuthorizationService
@Inject constructor(
    private val auth: AuthenticationService,
    private val data: CombinedUserRepository
) {
    companion object {
        const val TAG = "AuthorizationService"
    }


// --- Variables


    val userCollection = data.allForActiveTeam()


// --- Functions


    /**
     * Checks whether the currently authenticated user has elevated access.
     */
    suspend fun isCurrentUserElevated(): Boolean {
        Clogger.d(
            TAG, "Checking authorized action privileges for the current user"
        )

        val authUser = auth.getCurrentUser() ?: return false
        val dataUser = data.getById(authUser.uid) ?: return false
        return AuthorizedAccessUtil
            .canEdit(dataUser.role)
    }
}