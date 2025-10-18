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
import com.ggetters.app.core.utils.Clogger
import com.ggetters.app.databinding.ActivityOnboardingBinding
import com.ggetters.app.ui.central.views.HomeActivity
import com.ggetters.app.ui.shared.modals.FormTeamBottomSheet
import com.ggetters.app.ui.shared.modals.JoinTeamBottomSheet
import com.ggetters.app.ui.shared.models.Clickable
import com.ggetters.app.ui.startup.viewmodels.OnboardingViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

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
        Clogger.d(TAG, "Created a new instance of the activity")

        setupBindings()
        setupLayoutUi()
        setupTouchListeners()
        bindViewModel()
    }

    // --- Internals

    private fun navigateToHome() {
        startActivity(Intent(this, HomeActivity::class.java))
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        finishAffinity()
    }

    private fun setBusy(isBusy: Boolean) {
        binds.root.alpha = if (isBusy) 0.7f else 1f
        binds.btMakeTeam.isEnabled = !isBusy
        binds.btJoinTeam.isEnabled = !isBusy
        // If you have a ProgressBar, toggle here (e.g., binds.pbLoading.isVisible = isBusy)
    }

    private fun bindViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    model.state.collect { state ->
                        setBusy(state.isBusy)
                        // If you have an inline error label, bind state.errorMessage here.
                    }
                }
                launch {
                    model.events.collect { event ->
                        when (event) {
                            is OnboardingViewModel.UiEvent.NavigateHome -> navigateToHome()
                            is OnboardingViewModel.UiEvent.Toast ->
                                Toast.makeText(this@OnboardingActivity, event.message, Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
        }
    }

    // --- Delegates

    private fun onCreateTeamSheetSubmitted(teamName: String) {
        model.createTeam(teamName)
    }

    private fun onJoinTeamSheetSubmitted(teamCode: String, userCode: String) {
        model.joinTeam(teamCode, userCode)
    }

    // --- Event Handlers

    override fun setupTouchListeners() {
        binds.btMakeTeam.setOnClickListener(this)
        binds.btJoinTeam.setOnClickListener(this)
    }

    override fun onClick(view: View?) = when (view?.id) {
        binds.btMakeTeam.id -> {
            FormTeamBottomSheet(this::onCreateTeamSheetSubmitted)
                .show(supportFragmentManager, FormTeamBottomSheet.TAG)
        }
        binds.btJoinTeam.id -> {
            JoinTeamBottomSheet(this::onJoinTeamSheetSubmitted)
                .show(supportFragmentManager, JoinTeamBottomSheet.TAG)
        }
        else -> {
            Clogger.w(TAG, "Unhandled on-click for: ${view?.id}")
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
