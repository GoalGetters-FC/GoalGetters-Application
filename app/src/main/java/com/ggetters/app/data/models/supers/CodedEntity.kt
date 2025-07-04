package com.ggetters.app.data.models.supers

import com.ggetters.app.core.utils.Clogger

interface CodedEntity {

    var code: String

    // --- Functions

    fun generateCode(): String = if (this is KeyedEntity) {
        // TODO: Validate expected behaviour against UUID generation
        id.substring(0, id.indexOf("-", id.indexOf("-") + 1)).uppercase()
    } else {
        Clogger.e(
            "CodedEntity", "Unique code can only be generated for valid database entities."
        )
        throw UnsupportedOperationException()
    }
}