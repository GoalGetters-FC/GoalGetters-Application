package com.ggetters.app.ui.startup.views

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.ggetters.app.core.utils.Clogger
import com.ggetters.app.databinding.ActivityStartBinding
import com.ggetters.app.ui.shared.models.Clickable

class StartActivity : AppCompatActivity(), Clickable {
    companion object {
        private const val TAG = "StartActivity"
    }


    private lateinit var binds: ActivityStartBinding


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


// --- Event Handlers


    override fun setupTouchListeners() {
        binds.btSignIn.setOnClickListener(this)
        binds.btSignUp.setOnClickListener(this)
    }


    override fun onClick(view: View?) = when (view?.id) {
        binds.btSignIn.id -> {
            startActivity(Intent(this, SignInActivity::class.java))
            finish()
        }

        binds.btSignUp.id -> {
            startActivity(Intent(this, SignInActivity::class.java))
            finish()
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