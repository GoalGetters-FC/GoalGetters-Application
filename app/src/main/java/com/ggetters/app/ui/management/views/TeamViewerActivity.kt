package com.ggetters.app.ui.management.views

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.ggetters.app.databinding.ActivityTeamViewerBinding
import com.ggetters.app.ui.management.viewmodels.TeamViewerViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TeamViewerActivity : AppCompatActivity() {
    companion object {
        private const val TAG = "TeamViewerActivity"
    }


    private lateinit var binds: ActivityTeamViewerBinding
    private val model: TeamViewerViewModel by viewModels()


// --- Lifecycle


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupBindings()
        setupLayoutUi()
    }


// --- UI


    private fun setupBindings() {
        binds = ActivityTeamViewerBinding.inflate(layoutInflater)
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