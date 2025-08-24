package com.ggetters.app.core.extensions

import android.text.Editable
import android.text.TextWatcher
import android.view.ViewParent
import android.widget.EditText
import com.ggetters.app.core.models.results.Final
import com.ggetters.app.core.validation.ValidationError
import com.google.android.material.textfield.TextInputLayout


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


fun EditText.addValidation(validator: (String) -> Final<Unit, ValidationError>) {
    val textInputLayout = findTextInputLayout()

    onTextUpdated { text ->
        val result = validator(text)
        when (result) {
            is Final.Success -> {
                textInputLayout?.isErrorEnabled = false
                textInputLayout?.error = null
            }

            is Final.Failure -> {
                textInputLayout?.isErrorEnabled = true
                textInputLayout?.error = result.problem.toString()
            }
        }
    }
}


fun EditText.setLayoutError(error: String) {
    val textInputLayout = findTextInputLayout()
    textInputLayout?.error = error
}


private fun EditText.findTextInputLayout(): TextInputLayout? {
    var parent: ViewParent? = this.parent
    while (parent != null) {
        if (parent is TextInputLayout) {
            return parent
        }
        parent = parent.parent
    }
    return null
}