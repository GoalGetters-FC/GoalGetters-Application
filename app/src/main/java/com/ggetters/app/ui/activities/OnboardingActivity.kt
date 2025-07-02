package com.ggetters.app.ui.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.ggetters.app.R

class OnboardingActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding)

        val viewPager = findViewById<ViewPager2>(R.id.onboardingViewPager)
        val skipButton = findViewById<Button>(R.id.skipButton)
        val nextButton = findViewById<Button>(R.id.nextButton)

        // TODO: Set up onboarding carousel adapter with benefit screens
        // TODO: Log onboarding events with analytics

        skipButton.setOnClickListener {
            // TODO: Backend - Log onboarding skip event
            startActivity(Intent(this, WelcomeActivity::class.java)) // Go to team onboarding
            finish()
        }
        nextButton.setOnClickListener {
            // TODO: Advance to next onboarding screen or finish
            // If last screen:
            startActivity(Intent(this, WelcomeActivity::class.java)) // Go to team onboarding
            finish()
        }
    }
} 