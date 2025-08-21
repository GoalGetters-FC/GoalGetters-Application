package com.ggetters.app.ui.startup.views

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.ggetters.app.core.extensions.navigateToActivity
import com.ggetters.app.core.utils.Clogger
import com.ggetters.app.databinding.ActivityOnboardingBinding
import com.ggetters.app.ui.central.views.HomeActivity
import com.ggetters.app.ui.shared.modals.CreateTeamBottomSheet
import com.ggetters.app.ui.shared.modals.JoinTeamBottomSheet
import com.ggetters.app.ui.shared.models.Clickable
import com.ggetters.app.ui.startup.viewmodels.OnboardingViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class OnboardingActivity : AppCompatActivity(), Clickable {
    companion object {
        private const val TAG = "OnboardingActivity"
    }


    private lateinit var binds: ActivityOnboardingBinding
    private val model: OnboardingViewModel by viewModels()


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
    
    
    private fun navigateToHome() {
        navigateToActivity(Intent(this, HomeActivity::class.java), clearTask = true)
    }


// --- Delegates


    private fun onCreateTeamSheetSubmitted(teamName: String) {
        // TODO
        
        navigateToHome()
    }


    private fun onJoinTeamSheetSubmitted(
        teamCode: String, userCode: String
    ) {
        // TODO
        
        navigateToHome()
    }


// --- Event Handlers


    override fun setupTouchListeners() {
        binds.btMakeTeam.setOnClickListener(this)
        binds.btJoinTeam.setOnClickListener(this)
    }


    override fun onClick(view: View?) = when (view?.id) {
        binds.btMakeTeam.id -> {
            CreateTeamBottomSheet(
                this::onCreateTeamSheetSubmitted
            ).show(
                supportFragmentManager, CreateTeamBottomSheet.TAG
            )
        }

        binds.btJoinTeam.id -> {
            JoinTeamBottomSheet(
                this::onJoinTeamSheetSubmitted
            ).show(
                supportFragmentManager, JoinTeamBottomSheet.TAG
            )
        }

        else -> {
            Clogger.w(
                TAG, "Unhandled on-click for: ${view?.id}"
            )
        }
    }


// --- UI


    private fun setupBindings() {
        binds = ActivityOnboardingBinding.inflate(layoutInflater)
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