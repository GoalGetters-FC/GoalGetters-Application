package com.ggetters.app.ui.startup.models

import com.ggetters.app.core.models.FormField

data class SignInFormState(
    val identity: FormField<String>,
    val password: FormField<String>,
)