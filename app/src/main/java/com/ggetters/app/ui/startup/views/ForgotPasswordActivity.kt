package com.ggetters.app.ui.startup.views

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
import com.ggetters.app.core.utils.Clogger
import com.ggetters.app.databinding.ForgotPasswordActivityBinding
import com.ggetters.app.ui.shared.models.Clickable
import com.ggetters.app.ui.shared.models.UiState.Failure
import com.ggetters.app.ui.shared.models.UiState.Success
import com.ggetters.app.ui.startup.viewmodels.ForgotPasswordViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ForgotPasswordActivity : AppCompatActivity(), Clickable {
    companion object {
        private const val TAG = "ForgotPasswordActivity"
    }


    private lateinit var binds: ForgotPasswordActivityBinding
    private val model: ForgotPasswordViewModel by viewModels()


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
            is Success -> {
                Clogger.d(
                    TAG, "Success..."
                )

                Toast.makeText(
                    this, "Sent!", Toast.LENGTH_SHORT
                ).show()
            }

            is Failure -> {
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


// --- Event Handlers


    override fun setupTouchListeners() {
        binds.btSubmit.setOnClickListener(this)
    }


    override fun onClick(view: View?) = when (view?.id) {
        binds.btSubmit.id -> model.sendEmail()
        else -> {
            Clogger.w(
                TAG, "Unhandled on-click for: ${view?.id}"
            )
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }


// --- UI 


    private fun setupForm() {
        binds.etIdentity.onTextUpdated { text ->
            model.form.onIdentityChanged(text)
        }

        // Error UI
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                model.form.formState.collect { state ->
                    binds.etIdentity.setLayoutError(state.identity.error?.toString())
                }
            }
        }
    }


    private fun setupBindings() {
        binds = ForgotPasswordActivityBinding.inflate(layoutInflater)
    }


    private fun setupLayoutUi() {
        setContentView(binds.root)
        enableEdgeToEdge()

        // Apply system-bar insets to the root view
        ViewCompat.setOnApplyWindowInsetsListener(binds.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
} 