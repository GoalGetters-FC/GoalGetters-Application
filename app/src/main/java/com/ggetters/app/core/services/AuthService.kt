package com.ggetters.app.core.services

import com.ggetters.app.core.utils.Clogger
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

/**
 * Service to handle authentication operations in the [FirebaseAuth] server.
 * 
 * **Note:** All operations conducted by functions within this service require a
 * stable connection to the internet. Checks should be conducted beforehand, and
 * such edge-cases should be accounted for in failure conditions.
 *
 * TODO: Implement functionality for SSO accounts.
 *
 * @see FirebaseAuth
 * @see AuthService.signUpAsync
 * @see AuthService.signInAsync
 * @see AuthService.sendPasswordResetEmailAsync
 * @see AuthService.deleteCurrentUserAsync
 * @see AuthService.isUserSignedIn
 * @see AuthService.getCurrentUser
 * @see AuthService.logout
 */
class AuthService @Inject constructor(
    private val server: FirebaseAuth
) {
    companion object {
        private const val TAG = "AuthService"
    }


    // --- Functions


    /**
     * Suspend function to attempt to sign-up a [FirebaseUser] with the provided
     * email and password credentials.
     *
     * **Note:** The password complexity rules for a new user are determined by
     * the rules set within the server console. Ensure that all domain-level
     * validations comply with these rules, with no unnecessary additions.
     *
     * **Usage:**
     *
     * ```
     * viewModelScope.launch {
     *     runCatching {
     *         withTimeout(3_000) {
     *             authService.signUpAsync(email, password)
     *         }
     *     }.apply {
     *         onSuccess { user ->
     *             ...
     *         }
     *
     *         onFailure { exception ->
     *             ...
     *         }
     *     }
     * }
     * ```
     *
     * @see FirebaseAuth.createUserWithEmailAndPassword
     * @see await
     *
     * @throws IllegalStateException when the transaction cannot be completed.
     *         This may commonly occur if a user is not authenticated or when
     *         Firebase requires re-authentication to verify authenticity.
     */
    suspend fun signUpAsync(
        email: String, password: String,
    ): FirebaseUser {
        Clogger.d(
            TAG, "Attempting to sign-up a user using their credentials."
        )

        val result = server.createUserWithEmailAndPassword(email, password).await()
        when (result.user) {
            null -> {
                Clogger.d(
                    TAG, "Failed to sign-up a user using their credentials."
                )

                throw IllegalStateException("User is null after sign-up.")
            }

            else -> {
                Clogger.d(
                    TAG, "Successfully completed sign-up transaction."
                )

                return result.user!!
            }
        }
    }


    /**
     * Suspend function to attempt to sign-in a [FirebaseUser] with the provided
     * email and password credentials.
     *
     * **Usage:**
     *
     * ```
     * viewModelScope.launch {
     *     runCatching {
     *         withTimeout(3_000) {
     *             authService.signInAsync(email, password)
     *         }
     *     }.apply {
     *         onSuccess { user ->
     *             ...
     *         }
     *
     *         onFailure { exception ->
     *             ...
     *         }
     *     }
     * }
     * ```
     *
     * @see FirebaseAuth.signInWithEmailAndPassword
     * @see await
     *
     * @throws IllegalStateException when the transaction cannot be completed.
     *         This may commonly occur if a user is not authenticated or when
     *         Firebase requires re-authentication to verify authenticity.
     */
    suspend fun signInAsync(
        email: String, password: String
    ): FirebaseUser {
        Clogger.d(
            TAG, "Attempting to sign-in a user using their credentials."
        )

        val result = server.signInWithEmailAndPassword(email, password).await()
        when (result.user) {
            null -> {
                Clogger.d(
                    TAG, "Failed to sign-in a user using their credentials."
                )

                throw IllegalStateException("User is null after sign-in.")
            }

            else -> {
                Clogger.d(
                    TAG, "Successfully completed sign-in transaction."
                )

                return result.user!!
            }
        }
    }


    // --- Accessory


    /**
     * Attempts to send a password reset email to the provided email address.
     *
     * @see FirebaseAuth.sendPasswordResetEmail
     * @see await
     *
     * @throws IllegalStateException when the transaction cannot be completed.
     *         This may commonly occur if a user is not authenticated or when
     *         Firebase requires re-authentication to verify its validity.
     */
    suspend fun sendPasswordResetEmailAsync(email: String) {
        try {
            Clogger.d(
                TAG, "Sending a password reset request to the server."
            )

            server.sendPasswordResetEmail(email).await()
        } catch (e: Exception) {
            throw IllegalStateException(
                "Failed to send password reset email", e
            )
        }
    }


    /**
     * Attempts to delete the currently authenticated [FirebaseUser] account.
     * 
     * **Note:** This function uses the [FirebaseAuth.getCurrentUser] to get the
     * user account instance inline. Should this retrieval operation except, the
     * function will throw an exception that may be difficult to debug.
     * 
     * @see FirebaseUser.delete
     * @see await
     *
     * @throws IllegalStateException when the transaction cannot be completed.
     *         This may commonly occur if a user is not authenticated or when
     *         Firebase requires re-authentication to verify its validity.
     */
    suspend fun deleteCurrentUserAsync() {
        val user = server.currentUser
        when (user) {
            null -> throw IllegalStateException("Could not find authenticated user")
            else -> {
                Clogger.d(
                    TAG, "Sending an account deletion request to the server."
                )

                user.delete().await()
            }
        }
    }


    /**
     * Convenience method to determine whether a [FirebaseUser] is authenticated.
     *
     * **Usage:**
     *
     * ```
     * when (authService.isUserSignedIn()) {
     *     true -> { ... }
     *     else -> { ... }
     * }
     * ```
     *
     * @return [Boolean]
     */
    fun isUserSignedIn(): Boolean = (server.currentUser != null)


    /**
     * Convenience method to retrieve the currently authenticated [FirebaseUser].
     *
     * **Usage:**
     *
     * ```
     * var user = authService.getCurrentUser()
     * when (user) {
     *     null -> { ... }
     *     else -> { ... }
     * }
     * ```
     *
     * @return [FirebaseUser] or null if no user is currently signed in.
     */
    fun getCurrentUser(): FirebaseUser? = server.currentUser


    /**
     * Signs out the current [FirebaseUser] on this device by clearing their
     * credentials from the device cache. After invocation, the current user
     * will be null.
     *
     * **Note:** This method will not automatically refresh authentication and
     * authorization guards. It is the responsibility of the caller to enforce
     * manual rerouting and re-authentication if required.
     *
     * TODO: Implement functionality for SSO accounts.
     *
     * **Usage:**
     *
     * ```
     * authService.logout()
     *
     * // Then:
     * // 1. Navigate the user to the login screen.
     * // 2. Clear previously navigated-to activities from the stack.
     * // 3. Secure sensitive stored user data on the device.
     * ```
     */
    fun logout() {
        server.signOut()
        Clogger.i(
            TAG, "Logged out the currently authorized user on this device."
        )
    }
}

