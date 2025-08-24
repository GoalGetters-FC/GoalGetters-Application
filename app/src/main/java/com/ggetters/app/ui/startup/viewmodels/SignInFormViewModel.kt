package com.ggetters.app.ui.startup.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ggetters.app.core.extensions.kotlin.emptyString
import com.ggetters.app.core.models.FormField
import com.ggetters.app.core.validation.statute.string.AsEmailAddress
import com.ggetters.app.core.validation.statute.string.ExcludeWhitespace
import com.ggetters.app.core.validation.statute.string.NotEmpty
import com.ggetters.app.ui.shared.models.ValidatableForm
import com.ggetters.app.ui.startup.models.SignInFormState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class SignInFormViewModel @Inject constructor() : ViewModel(), ValidatableForm {
    companion object {
        private const val TAG = "SignInFormViewModel"
    }


// --- Internals


    private val schema = SignInFormState(
        identity = FormField(
            value = emptyString(), //
            rules = listOf(
                NotEmpty(), ExcludeWhitespace(), AsEmailAddress()
            )
        ), //
        password = FormField(
            value = emptyString(), //
            rules = listOf(
                NotEmpty(), ExcludeWhitespace()
            )
        )
    )


    private val _formState = MutableStateFlow(schema)
    val formState = _formState.asStateFlow()


// --- Functions


    fun onIdentityChanged(
        value: String
    ) {
        _formState.update { current ->
            current.copy(
                identity = current.identity.copy(value = value).applyValidation()
            )
        }
    }


    fun onPasswordChanged(
        value: String
    ) {
        _formState.update { current ->
            current.copy(
                password = current.password.copy(value = value).applyValidation()
            )
        }
    }


// --- Validation


    override val isFormValid = formState.map { field ->
        listOf(
            field.identity, //
            field.password, //
        ).all {
            it.isValid
        }
    }.stateIn(
        viewModelScope, SharingStarted.Eagerly, false
    )


    override fun validateForm() {
        _formState.update { current ->
            current.copy(
                identity = current.identity.applyValidation(),
                password = current.password.applyValidation(),
            )
        }
    }
}