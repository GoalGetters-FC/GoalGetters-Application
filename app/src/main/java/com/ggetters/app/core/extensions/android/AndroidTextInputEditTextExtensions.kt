package com.ggetters.app.core.extensions.android

import android.view.ViewParent
import com.ggetters.app.core.extensions.kotlin.emptyString
import com.ggetters.app.core.models.results.Final
import com.ggetters.app.core.validation.ValidationError
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout


// --- Extensions


    fun TextInputEditText.addInlineValidation(
        validator: (String) -> Final<Unit, ValidationError>
    ) {
        val textInputLayout = findTextInputLayout()
        onTextUpdated { text -> 
            validator(text).apply { 
                onSuccess { 
                    textInputLayout?.error = emptyString()
                }
                
                onFailure { error ->
                    textInputLayout?.error = error.toString()
                }
            }
        }
    }


    fun TextInputEditText.setLayoutError(notice: String?) {
        val layout = this.findTextInputLayout()
        layout?.error = notice ?: emptyString()
    }


// --- Internal


/**
 * Find the [TextInputLayout] that contains this [TextInputEditText].
 * 
 * **Note:** This extension method will recursively search the parent views of
 * this [TextInputEditText] until it finds a [TextInputLayout], even when it is
 * not a direct child of it.
 */
private fun TextInputEditText.findTextInputLayout(): TextInputLayout? {
    var parentView: ViewParent? = this.parent
    while (parentView != null) {
        if (parentView is TextInputLayout) {
            return parentView
        }
        
        parentView = parent.parent
    }

    return null
}