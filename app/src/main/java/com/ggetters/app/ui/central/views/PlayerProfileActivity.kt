package com.ggetters.app.ui.central.views

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowCompat
import com.ggetters.app.R
import com.ggetters.app.core.extensions.navigateBack

class PlayerProfileActivity : AppCompatActivity() {

	companion object {
		const val EXTRA_PLAYER_ID = "extra_player_id"
		const val EXTRA_START_EDITING = "extra_start_editing"
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_player_profile)

		// Edge-to-edge with proper insets so toolbar/content aren't under status bar
		enableEdgeToEdge()
		val toolbar = findViewById<com.google.android.material.appbar.MaterialToolbar>(R.id.activityToolbar)
		val container = findViewById<android.view.View>(R.id.fragmentContainer)
		WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = true
		window.statusBarColor = getColor(R.color.white)
		ViewCompat.setOnApplyWindowInsetsListener(toolbar) { v, insets ->
			val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
			v.setPadding(v.paddingLeft, systemBars.top, v.paddingRight, v.paddingBottom)
			insets
		}
		ViewCompat.setOnApplyWindowInsetsListener(container) { v, insets ->
			val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
			v.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom)
			insets
		}

		// Wire activity toolbar back
		toolbar.setNavigationIcon(R.drawable.ic_unicons_arrow_left)
		toolbar.setNavigationOnClickListener { navigateBack() }

		if (savedInstanceState == null) {
			val playerId = intent.getStringExtra(EXTRA_PLAYER_ID) ?: ""
			val startEditing = intent.getBooleanExtra(EXTRA_START_EDITING, false)
			supportFragmentManager
				.beginTransaction()
				.setReorderingAllowed(true)
				.setCustomAnimations(
					R.anim.slide_in_right,
					R.anim.slide_out_left,
					R.anim.slide_in_left,
					R.anim.slide_out_right
				)
				.replace(
					R.id.fragmentContainer,
					PlayerProfileFragment.newInstance(playerId, startEditing)
				)
				.commit()
		}
	}

	override fun onBackPressed() {
		navigateBack()
	}
}


