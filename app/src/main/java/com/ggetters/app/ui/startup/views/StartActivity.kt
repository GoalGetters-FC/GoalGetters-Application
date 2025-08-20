package com.ggetters.app.ui.startup.views

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.ggetters.app.core.extensions.navigateToActivity
import com.ggetters.app.core.utils.Clogger
import com.ggetters.app.databinding.ActivityStartBinding
import com.ggetters.app.ui.central.views.HomeActivity
import com.ggetters.app.ui.shared.models.Clickable
import com.ggetters.app.ui.startup.models.StartUiState.Authenticated
import com.ggetters.app.ui.startup.models.StartUiState.SignedOut
import com.ggetters.app.ui.startup.viewmodels.StartViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class StartActivity : AppCompatActivity(), Clickable {
    companion object {
        private const val TAG = "StartActivity"
    }


    private lateinit var binds: ActivityStartBinding
    private val model: StartViewModel by viewModels()


    private var authenticating: Boolean = true


// --- Lifecycle


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Clogger.d(
            TAG, "Created a new instance of the activity"
        )
        
        persistSplashScreenUntilAuthChecksComplete()

        setupBindings()
        setupLayoutUi()
        setupTouchListeners()
        observe()

        model.authenticate()
    }


// --- ViewModel


    private fun observe() = model.uiState.observe(this) { state ->
        when (state) {
            is Authenticated -> {
                authenticating = false
                navigateToActivity(Intent(this, HomeActivity::class.java), finishCurrent = true)
            }

            is SignedOut -> {
                authenticating = false
            }

            else -> {
                Clogger.w(
                    TAG, "Unhandled state: ${state.javaClass::class.java.simpleName}"
                )
            }
        }
    }


// --- Internals


    private fun persistSplashScreenUntilAuthChecksComplete() {
        val splashScreen = installSplashScreen()
        splashScreen.setKeepOnScreenCondition {
            authenticating
        }
    }


// --- Event Handlers


    override fun setupTouchListeners() {
        binds.btSignIn.setOnClickListener(this)
        binds.btSignUp.setOnClickListener(this)
    }


    override fun onClick(view: View?) = when (view?.id) {
        binds.btSignIn.id -> {
            navigateToActivity(Intent(this, SignInActivity::class.java))
        }

        binds.btSignUp.id -> {
            navigateToActivity(Intent(this, SignUpActivity::class.java))
        }

        else -> {
            Clogger.w(
                TAG, "Unhandled on-click for: ${view?.id}"
            )
        }
    }


// --- UI


    private fun setupBindings() {
        binds = ActivityStartBinding.inflate(layoutInflater)
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