package com.ggetters.app.ui.startup.models

import com.ggetters.app.core.models.FormField

data class SignUpFormState(
    val identity: FormField<String>,
    val passwordDefault: FormField<String>,
    val passwordConfirm: FormField<String>,
)