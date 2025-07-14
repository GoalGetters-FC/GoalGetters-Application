package com.ggetters.app.ui.shared.models

import android.view.View

/**
 * Interface definition for a [View] derivative class that supports manual-touch
 * interactions.
 * 
 * **Note:** This contract should never be directly implemented. It is intended
 * to be used as a base interface for other contracts that require manual-touch
 * interactions between the user and [View] elements.
 * 
 * @see Clickable
 * @see Holdable
 * @see View
 */
interface Interactable {

    /**
     * Function to register touch listeners for [View] elements.
     *
     * **Note:** The order in which this function is called is important.
     * It must be called after the all of the bindings have been initialised and
     * the [View] elements have been created, otherwise a null-pointer exception
     * will be thrown at compile-time.
     *
     * **Usage:**
     *
     * ```
     * override fun onCreate(savedInstanceState: Bundle?) {
     *     ...
     *     
     *     setupTouchListeners()
     * }
     * 
     * override fun setupTouchListeners() {
     *     binding.buttonA.setOnClickListener(this)
     *     binding.buttonB.setOnClickListener(this)
     *     binding.buttonA.setOnLongClickListener(this)
     *     binding.buttonB.setOnLongClickListener(this)
     * }
     * ```
     */
    fun setupTouchListeners()
}