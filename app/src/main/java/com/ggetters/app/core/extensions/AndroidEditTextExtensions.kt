package com.ggetters.app.core.extensions

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText


// --- Extensions


/**
 * Hijack the onTextChanged event from an EditText.
 * 
 * **Usage:**
 * 
 * ```
 * editText.onTextChanged { input ->
 *     ...
 * }
 * ```
 */
fun EditText.onTextChanged(
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
        @Deprecated(
            message = "This method should not be implemented for performance.",
            replaceWith = ReplaceWith("afterTextChanged"),
            level = DeprecationLevel.ERROR
        )
        override fun beforeTextChanged(
            s: CharSequence?, start: Int, count: Int, after: Int
        ) {
        }

        /**
         * Called while the text is being changed.
         *
         * **Note:** The method body of this function has been intentionally
         * left blank as it is not needed and may reduce performance with
         * unnecessary implementations.
         */
        @Deprecated(
            message = "This method should not be implemented for performance.",
            replaceWith = ReplaceWith("afterTextChanged"),
            level = DeprecationLevel.ERROR
        )
        override fun onTextChanged(
            s: CharSequence?, start: Int, before: Int, count: Int
        ) {
        }

        /**
         * Called after the text has been changed.
         */
        override fun afterTextChanged(editable: Editable?) {
            afterTextChanged.invoke(editable.toString())
        }
    })
}