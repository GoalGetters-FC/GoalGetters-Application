package com.ggetters.app.ui.startup.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ggetters.app.core.extensions.kotlin.emptyString
import com.ggetters.app.core.models.FormField
import com.ggetters.app.core.validation.statute.string.AsEmailAddress
import com.ggetters.app.core.validation.statute.string.AsFirebaseCredential
import com.ggetters.app.core.validation.statute.string.ExcludeWhitespace
import com.ggetters.app.core.validation.statute.string.NotEmpty
import com.ggetters.app.core.validation.statute.string.PasswordMatchStringValidationLaw
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


    private val schema = SignUpFormState(
        identity = FormField(
            value = emptyString(), //
            rules = listOf(
                NotEmpty(), ExcludeWhitespace(), AsEmailAddress()
            )
        ), //
        passwordDefault = FormField(
            value = emptyString(), //
            rules = listOf(
                NotEmpty(), AsFirebaseCredential()
            )
        ), //
        passwordConfirm = FormField(
            value = emptyString(), //
            rules = listOf(
                NotEmpty(), AsFirebaseCredential()
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


    fun onPasswordDefaultChanged(
        value: String
    ) {
        _formState.update { current ->
            current.copy(
                passwordDefault = current.passwordDefault.copy(value = value).applyValidation()
            )
        }
    }


    fun onPasswordConfirmChanged(
        value: String
    ) {
        _formState.update { current ->
            val updatedConfirm = current.passwordConfirm.copy(value = value)
            // Apply password matching validation
            val passwordMatchRule = PasswordMatchStringValidationLaw(current.passwordDefault.value)
            val confirmField = updatedConfirm.copy(
                rules = listOf(
                    NotEmpty(), 
                    AsFirebaseCredential(),
                    passwordMatchRule
                )
            ).applyValidation()
            
            current.copy(
                passwordConfirm = confirmField
            )
        }
    }


// --- Validation


    override val isFormValid = formState.map { field ->
        val passwordsMatch = field.passwordDefault.value.trim() == field.passwordConfirm.value.trim()
        val confirmPasswordValid = field.passwordConfirm.value.isNotEmpty() && passwordsMatch
        
        listOf(
            field.identity, //
            field.passwordDefault, //
        ).all { it.isValid } && confirmPasswordValid
    }.stateIn(
        viewModelScope, SharingStarted.Eagerly, false
    )


    override fun validateForm() {
        _formState.update { current ->
            current.copy(
                identity = current.identity.applyValidation(),
                passwordDefault = current.passwordDefault.applyValidation(),
                passwordConfirm = current.passwordConfirm.applyValidation(),
            )
        }
    }
}