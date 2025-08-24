package com.ggetters.app.ui.startup.views

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.ggetters.app.core.extensions.android.onTextUpdated
import com.ggetters.app.core.extensions.android.setLayoutError
import com.ggetters.app.core.utils.Clogger
import com.ggetters.app.databinding.ActivitySignInBinding
import com.ggetters.app.ui.shared.models.Clickable
import com.ggetters.app.ui.shared.models.UiState.Failure
import com.ggetters.app.ui.shared.models.UiState.Loading
import com.ggetters.app.ui.shared.models.UiState.Success
import com.ggetters.app.ui.startup.viewmodels.SignInViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SignInActivity : AppCompatActivity(), Clickable {
    companion object {
        private const val TAG = "SignInActivity"
    }


    private lateinit var binds: ActivitySignInBinding
    private val model: SignInViewModel by viewModels()


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


    private fun tryAuthenticateCredentials() {
        val email = binds.etIdentity.text.toString().trim()
        val password = binds.etPassword.text.toString().trim()
        model.signIn(
            email, password
        )
    }


    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    private fun tryAuthenticateGoogleLogin() {
        model.googleSignIn()
    }


    private fun load() {
        // Display loading UI
    }


    private fun cast() {
        // Hide loading UI
    }


// --- Event Handlers


    override fun setupTouchListeners() {
        binds.btSignIn.setOnClickListener(this)
        binds.btGoogle.setOnClickListener(this)
        binds.tvForgotPassword.setOnClickListener(this)
    }


    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    override fun onClick(view: View?) = when (view?.id) {
        binds.btSignIn.id -> {
            tryAuthenticateCredentials()
        }

        binds.btGoogle.id -> {
            Clogger.d(
                TAG, "Clicked Google SSO"
            )

            tryAuthenticateGoogleLogin()
        }

        binds.tvForgotPassword.id -> {
            startActivity(Intent(this, ForgotPasswordActivity::class.java))
        }

        else -> {
            Clogger.w(
                TAG, "Unhandled on-click for: ${view?.id}"
            )
        }
    }


// --- UI


    private fun setupForm() {
        binds.etIdentity.onTextUpdated { text ->
            model.form.onIdentityChanged(text)
        }

        binds.etPassword.onTextUpdated { text ->
            model.form.onPasswordChanged(text)
        }

        // Error UI
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                model.form.formState.collect { state ->
                    binds.etIdentity.setLayoutError(state.identity.error?.toString())
                    binds.etPassword.setLayoutError(state.password.error?.toString())
                }
            }
        }
    }


    private fun setupBindings() {
        binds = ActivitySignInBinding.inflate(layoutInflater)
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