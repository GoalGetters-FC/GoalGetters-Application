package com.ggetters.app.ui.startup.views

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.ggetters.app.R
import com.ggetters.app.core.extensions.android.onTextUpdated
import com.ggetters.app.core.extensions.android.setLayoutError
import com.ggetters.app.core.extensions.kotlin.openBrowserTo
import com.ggetters.app.core.utils.Clogger
import com.ggetters.app.databinding.ActivitySignUpBinding
import com.ggetters.app.ui.shared.models.Clickable
import com.ggetters.app.ui.shared.models.UiState.Failure
import com.ggetters.app.ui.shared.models.UiState.Loading
import com.ggetters.app.ui.shared.models.UiState.Success
import com.ggetters.app.ui.startup.dialogs.AgeVerificationBottomSheet
import com.ggetters.app.ui.startup.viewmodels.SignUpViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SignUpActivity : AppCompatActivity(), Clickable {
    companion object {
        private const val TAG = "SignUpActivity"
    }


    private lateinit var binds: ActivitySignUpBinding
    private val model: SignUpViewModel by viewModels()


// --- Lifecycle


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Clogger.d(
            TAG, "Created a new instance of the activity"
        )

        setupBindings()
        setupLayoutUi()
        setupTouchListeners()
        setupForm()
        observe()
    }


// --- ViewModel


    private fun observe() = model.uiState.observe(this) { state ->
        when (state) {
            is Loading -> {
                load()
                Clogger.d(
                    TAG, "Loading..."
                )
            }

            is Success -> {
                cast()
                Clogger.d(
                    TAG, "Success..."
                )

                startActivity(Intent(this, OnboardingActivity::class.java))
                overridePendingTransition(R.anim.slide_in_right, R.anim.fade_out)
                finishAffinity()
            }

            is Failure -> {
                cast()
                Clogger.d(
                    TAG, "Failure..."
                )

                Toast.makeText(
                    this, state.message, Toast.LENGTH_SHORT
                ).show()
            }

            else -> {
                Clogger.w(
                    TAG, "Unhandled state: ${state.javaClass::class.java.simpleName}"
                )
            }
        }
    }


// --- Internals


    private fun load() {
        // Display loading UI
    }


    private fun cast() {
        // Hide loading UI
    }


// --- Event Handlers


    override fun setupTouchListeners() {
        binds.tvSignIn.setOnClickListener(this)
        binds.btSignUp.setOnClickListener(this)
        binds.tvFaq.setOnClickListener(this)
    }


    override fun onClick(view: View?) {
        when (view?.id) {
            binds.tvFaq.id -> {
                openBrowserTo("https://help.goalgettersfc.co.za/policy")
            }
            
            binds.tvSignIn.id -> {
                startActivity(Intent(this, SignInActivity::class.java))
                overridePendingTransition(R.anim.slide_in_left, R.anim.fade_out)
                finish()
            }

            binds.btSignUp.id -> {
                // First validate form
                model.form.validateForm()
                
                // Check if form is valid before proceeding
                if (!model.form.isFormValid.value) {
                    Toast.makeText(this, "Please fill in all required fields correctly", Toast.LENGTH_SHORT).show()
                    return
                }
                
                // Show age verification dialog
                val ageVerificationDialog = AgeVerificationBottomSheet().apply {
                    onAgeVerified = { isVerified ->
                        if (isVerified) {
                            // Only proceed with sign-up after age verification
                            model.signUp()
                        }
                    }
                }
                ageVerificationDialog.show(supportFragmentManager, "AgeVerificationBottomSheet")
            }

            else -> {
                Clogger.w(
                    TAG, "Unhandled on-click for: ${view?.id}"
                )
            }
        }
    }


// --- UI


    private fun setupForm() {
        binds.etIdentity.onTextUpdated { text ->
            model.form.onIdentityChanged(text)
        }

        binds.etPasswordDefault.onTextUpdated { text ->
            model.form.onPasswordDefaultChanged(text)
        }

        binds.etPasswordConfirm.onTextUpdated { text ->
            model.form.onPasswordConfirmChanged(text)
        }

        // Error UI and Button State
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                model.form.formState.collect { state ->
                    binds.etIdentity.setLayoutError(state.identity.error?.toString())
                    binds.etPasswordDefault.setLayoutError(state.passwordDefault.error?.toString())
                    binds.etPasswordConfirm.setLayoutError(state.passwordConfirm.error?.toString())
                }
            }
        }
        
        // Button state management
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                model.form.isFormValid.collect { isValid ->
                    binds.btSignUp.isEnabled = isValid
                    binds.btSignUp.alpha = if (isValid) 1.0f else 0.6f
                }
            }
        }
    }


    private fun setupBindings() {
        binds = ActivitySignUpBinding.inflate(layoutInflater)
    }


    private fun setupLayoutUi() {
        setContentView(binds.root)
        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(binds.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }
    }
} 