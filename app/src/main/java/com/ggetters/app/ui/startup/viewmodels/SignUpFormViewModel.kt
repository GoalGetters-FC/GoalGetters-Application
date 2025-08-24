package com.ggetters.app.ui.startup.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ggetters.app.core.extensions.empty
import com.ggetters.app.core.models.FormField
import com.ggetters.app.core.validation.constitution.string.AsEmailAddress
import com.ggetters.app.core.validation.constitution.string.AsFirebaseCredential
import com.ggetters.app.core.validation.constitution.string.NotEmpty
import com.ggetters.app.ui.shared.models.ValidatableForm
import com.ggetters.app.ui.startup.models.SignUpFormState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class SignUpFormViewModel @Inject constructor() : ViewModel(), ValidatableForm {
    companion object {
        private const val TAG = "SignUpFormViewModel"
    }


// --- Internals


    private val formSchema = SignUpFormState(
        identity = FormField(
            value = String.empty(), //
            rules = listOf(
                NotEmpty(), AsEmailAddress()
            )
        ), //
        passwordDefault = FormField(
            value = String.empty(), //
            rules = listOf(
                NotEmpty(), AsFirebaseCredential()
            )
        ), //
        passwordConfirm = FormField(
            value = String.empty(), //
            rules = listOf(
                NotEmpty(), AsFirebaseCredential()
            )
        )
    )


    private val _formState = MutableStateFlow(formSchema)
    val formState = _formState.asStateFlow()


// --- Functions


    fun onIdentityChanged(
        value: String
    ) {
        _formState.update { currentState ->
            currentState.copy(
                identity = currentState.identity.copy(value = value).applyValidation()
            )
        }
    }


    fun onPasswordDefaultChanged(
        value: String
    ) {
        _formState.update { currentState ->
            currentState.copy(
                passwordDefault = currentState.passwordDefault.copy(value = value).applyValidation()
            )
        }
    }


    fun onPasswordConfirmChanged(
        value: String
    ) {
        _formState.update { currentState ->
            currentState.copy(
                passwordConfirm = currentState.passwordConfirm.copy(value = value).applyValidation()
            )
        }
    }


// --- Validation


    override val isFormValid = validateForm().run {
        formState.map { field ->
            listOf(
                field.identity, field.passwordDefault, field.passwordConfirm
            ).all { it.isValid }
        }.stateIn(
            viewModelScope, SharingStarted.Eagerly, false
        )
    }


    override fun validateForm() {
        _formState.update { currentState ->
            currentState.copy(
                identity = currentState.identity.applyValidation(),
                passwordDefault = currentState.passwordDefault.applyValidation(),
                passwordConfirm = currentState.passwordConfirm.applyValidation(),
            )
        }
    }
}