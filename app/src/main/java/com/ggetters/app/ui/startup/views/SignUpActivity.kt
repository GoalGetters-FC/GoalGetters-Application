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
import androidx.lifecycle.ViewModelProvider
import com.ggetters.app.core.utils.Clogger
import com.ggetters.app.databinding.ActivitySignUpBinding
import com.ggetters.app.ui.central.views.HomeActivity
import com.ggetters.app.ui.shared.models.Clickable
import com.ggetters.app.ui.shared.models.UiState.Failure
import com.ggetters.app.ui.shared.models.UiState.Loading
import com.ggetters.app.ui.shared.models.UiState.Success
import com.ggetters.app.ui.startup.dialogs.AgeVerificationBottomSheet
import com.ggetters.app.ui.startup.viewmodels.SignUpViewModel
import dagger.hilt.android.AndroidEntryPoint

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

                startActivity(Intent(this, WelcomeBackActivity::class.java))
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
        val defaultPassword = binds.etPasswordDefault.text.toString().trim()
        val confirmPassword = binds.etPasswordConfirm.text.toString().trim()
        model.signUp(
            email, defaultPassword, confirmPassword
        )
    }


    private fun load() {
        // TODO: Display loading UI
    }


    private fun cast() {
        // TODO: Hide loading UI
    }


// --- Event Handlers


    override fun setupTouchListeners() {
        binds.tvSignIn.setOnClickListener(this)
        binds.btSignUp.setOnClickListener(this)
    }


    override fun onClick(view: View?) = when (view?.id) {
        binds.tvSignIn.id -> startActivity(Intent(this, SignInActivity::class.java))
        binds.btSignUp.id -> {
            AgeVerificationBottomSheet().show(
                supportFragmentManager, "AgeVerificationBottomSheet"
            )
            
            tryAuthenticateCredentials()
        }

        else -> {
            Clogger.w(
                TAG, "Unhandled on-click for: ${view?.id}"
            )
        }
    }


// --- UI


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