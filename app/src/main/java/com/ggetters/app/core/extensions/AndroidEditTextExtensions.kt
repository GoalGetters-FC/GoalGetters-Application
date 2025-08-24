package com.ggetters.app.core.extensions

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText


// --- Extensions


/**
 * Hijack the `afterTextChanged()` event from an `EditText`.
 * 
 * **Usage:**
 * 
 * ```
 * editText.onTextUpdated { text ->
 *     ...
 * }
 * ```
 */
fun EditText.onTextUpdated(
    afterTextChanged: (String) -> Unit
) {
    this.addTextChangedListener(object : TextWatcher {

        /**
         * Called before the text is changed.
         *
         * **Note:** The method body of this function has been intentionally 
         * left blank as it is not needed and may reduce performance with 
         * unnecessary implementations.
         */
        override fun beforeTextChanged(
            s: CharSequence?, start: Int, count: Int, after: Int
        ) {
            // Intentionally blank for performance.
        }

        
        /**
         * Called while the text is being changed.
         *
         * **Note:** The method body of this function has been intentionally
         * left blank as it is not needed and may reduce performance with
         * unnecessary implementations.
         */
        override fun onTextChanged(
            s: CharSequence?, start: Int, before: Int, count: Int
        ) {
            // Intentionally blank for performance.
        }

        
        /**
         * Called after the text has been changed.
         */
        override fun afterTextChanged(
            editable: Editable?
        ) {
            afterTextChanged.invoke(editable.toString())
        }
    })
}