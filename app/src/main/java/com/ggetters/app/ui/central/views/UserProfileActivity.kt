package com.ggetters.app.ui.central.views

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowCompat
import com.ggetters.app.R
import com.ggetters.app.core.extensions.navigateBack
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class UserProfileActivity : AppCompatActivity() {

	companion object {
		const val EXTRA_PROFILE_TYPE = "extra_profile_type"
		const val EXTRA_PROFILE_ID = "extra_profile_id"
		const val EXTRA_START_EDITING = "extra_start_editing"
		
		const val PROFILE_TYPE_USER = "user"
		const val PROFILE_TYPE_TEAM = "team"
		const val PROFILE_TYPE_PLAYER = "player"
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_user_profile)

		// Edge-to-edge with proper insets
		enableEdgeToEdge()
		val toolbar = findViewById<com.google.android.material.appbar.MaterialToolbar>(R.id.activityToolbar)
		val container = findViewById<android.view.View>(R.id.fragmentContainer)
		
		// Light status bar for visibility
		WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = true
		window.statusBarColor = getColor(R.color.white)
		
		// Apply insets
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

		if (savedInstanceState == null) {
			val profileType = intent.getStringExtra(EXTRA_PROFILE_TYPE) ?: PROFILE_TYPE_USER
			val profileId = intent.getStringExtra(EXTRA_PROFILE_ID) ?: ""
			val startEditing = intent.getBooleanExtra(EXTRA_START_EDITING, false)
			
			// Setup toolbar based on profile type
			setupToolbar(profileType)
			
			// Load appropriate fragment
			loadProfileFragment(profileType, profileId, startEditing)
		}
	}

	private fun setupToolbar(profileType: String) {
		val toolbar = findViewById<com.google.android.material.appbar.MaterialToolbar>(R.id.activityToolbar)
		
		// Set title based on profile type
		val title = when (profileType) {
			PROFILE_TYPE_USER -> "Profile"
			PROFILE_TYPE_TEAM -> "Team Profile"
			PROFILE_TYPE_PLAYER -> "Player Profile"
			else -> "Profile"
		}
		toolbar.title = title
		
		// Setup navigation
		toolbar.setNavigationIcon(R.drawable.ic_unicons_arrow_left)
		toolbar.setNavigationOnClickListener { navigateBack() }
	}

	private fun loadProfileFragment(profileType: String, profileId: String, startEditing: Boolean) {
		val fragment = when (profileType) {
			PROFILE_TYPE_USER -> ProfileFragment()
			PROFILE_TYPE_TEAM -> com.ggetters.app.ui.management.views.TeamProfileFragment()
			PROFILE_TYPE_PLAYER -> PlayerProfileFragment.newInstance(profileId, startEditing)
			else -> ProfileFragment()
		}
		
		supportFragmentManager
			.beginTransaction()
			.setReorderingAllowed(true)
			.setCustomAnimations(
				R.anim.slide_in_right,
				R.anim.slide_out_left,
				R.anim.slide_in_left,
				R.anim.slide_out_right
			)
			.replace(R.id.fragmentContainer, fragment)
			.commit()
	}

	override fun onBackPressed() {
		navigateBack()
	}
}
