package com.ggetters.app.ui.shared.models

import com.ggetters.app.core.models.results.FinalError

enum class ModalError : FinalError {

    /**
     * Called when the user cancels the modal.
     */
    DISMISSED_CLICKED,

    /**
     * Called when the user clicks outside of the modal and dismisses it.
     */
    DISMISSED_OUTSIDE
}