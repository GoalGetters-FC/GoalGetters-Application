package com.ggetters.app.ui.startup.views

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.ggetters.app.core.utils.Clogger
import com.ggetters.app.databinding.SignInActivityBinding
import com.ggetters.app.ui.shared.models.Clickable

class SignInActivity : AppCompatActivity(), Clickable {
    companion object {
        private const val TAG = "SignInActivity"
    }


    private lateinit var binds: SignInActivityBinding


    // --- Lifecycle


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Clogger.d(
            TAG, "Created a new instance of the activity"
        )

        setupBindings()
        setupLayoutUi()
        setupTouchListeners()
    }


    // --- Internals


    private fun tryAuthenticateCredentials() {
        // TODO: ...
    }


    private fun tryAuthenticateGoogleLogin() {
        // TODO: ...
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