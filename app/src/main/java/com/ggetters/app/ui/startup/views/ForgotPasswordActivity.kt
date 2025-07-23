package com.ggetters.app.ui.startup.views

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.ggetters.app.core.utils.Clogger
import com.ggetters.app.databinding.ForgotPasswordActivityBinding
import com.ggetters.app.ui.shared.models.Clickable

class ForgotPasswordActivity : AppCompatActivity(), Clickable {
    companion object {
        private const val TAG = "ForgotPasswordActivity"
    }


    private lateinit var binds: ForgotPasswordActivityBinding


// --- Lifecycle


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Clogger.d(
            TAG, "Created a new instance of the activity"
        )

        setupBindings()
        setupLayoutUi()
    }


// --- Event Handlers


    override fun setupTouchListeners() {
        binds.btSubmit.setOnClickListener(this)
    }


    override fun onClick(view: View?) = when (view?.id) {
        binds.btSubmit.id -> {}
        else -> {
            Clogger.w(
                TAG, "Unhandled on-click for: ${view?.id}"
            )
        }
    }


// --- UI 


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