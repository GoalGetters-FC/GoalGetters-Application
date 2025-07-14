package com.ggetters.app.ui.shared.models

import android.view.View

/**
 * Interface definition for a view that supports [View.OnLongClickListener].
 *
 * **Note:** This contract is deliberately left empty. It serves as a union for
 * inheritance to ensure that standardised code style conventions are followed.
 * See usage and [Interactable] for implementation details.
 *
 * **Usage:**
 * 
 * *The following example assumes that view binding is enabled.*
 * 
 * The `else` condition will only be reached if an element has registered itself
 * for touch events, but its implementation has not yet been accounted for. This
 * should be monitored to ensure that logic errors are avoided when debugging.
 *
 * ```
 * override fun onLongClick(view: View?): Boolean = when (view?.id) {
 *     binding.buttonA.id -> ...
 *     binding.buttonB.id -> ...
 *     else -> {
 *         Clogger.w(
 *             TAG, "Unhandled on-long-click for: ${view?.id}"
 *         )
 *         
 *         return false
 *     }
 *     
 *     return true
 * }
 * ```
 */
interface Holdable : Interactable, View.OnLongClickListener