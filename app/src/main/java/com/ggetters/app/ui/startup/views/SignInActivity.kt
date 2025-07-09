package com.ggetters.app.ui.startup.views

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.ggetters.app.core.utils.Clogger
import com.ggetters.app.databinding.SignInActivityBinding
import com.ggetters.app.ui.shared.models.Clickable
import com.ggetters.app.ui.startup.models.SignInUiState.Failure
import com.ggetters.app.ui.startup.models.SignInUiState.Loading
import com.ggetters.app.ui.startup.models.SignInUiState.Success
import com.ggetters.app.ui.startup.viewmodels.SignInViewModel

class SignInActivity : AppCompatActivity(), Clickable {
    companion object {
        private const val TAG = "SignInActivity"
    }


    private lateinit var binds: SignInActivityBinding
    private lateinit var model: SignInViewModel


    // --- Lifecycle


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Clogger.d(
            TAG, "Created a new instance of the activity"
        )

        setupBindings()
        setupLayoutUi()
        setupTouchListeners()

        model = ViewModelProvider(this)[SignInViewModel::class.java]

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

                // TODO: ...
            }

            is Success -> {
                cast()
                Clogger.d(
                    TAG, "Success..."
                )

                // TODO: ...
            }

            is Failure -> {
                cast()
                Clogger.d(
                    TAG, "Failure..."
                )

                // TODO: ...
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


    private fun tryAuthenticateGoogleLogin() {
        // TODO: ...
    }


    private fun load() {
        // TODO: Display loading UI
    }


    private fun cast() {
        // TODO: Hide loading UI
    }


    // --- Event Handlers


    override fun setupTouchListeners() {
        binds.btSignIn.setOnClickListener(this)
        binds.tvForgotPassword.setOnClickListener(this)
    }


    override fun onClick(view: View?) = when (view?.id) {
        binds.btSignIn.id -> {
            startActivity(Intent(this, WelcomeBackActivity::class.java))
            finishAffinity()
        }

        binds.btGoogle.id -> {
            startActivity(Intent(this, WelcomeBackActivity::class.java))
            finishAffinity()
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


    private fun setupBindings() {
        binds = SignInActivityBinding.inflate(layoutInflater)
    }


    private fun setupLayoutUi() {
        setContentView(binds.root)
        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(binds.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
} 