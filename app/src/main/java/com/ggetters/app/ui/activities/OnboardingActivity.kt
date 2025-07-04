package com.ggetters.app.ui.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.ggetters.app.R

class OnboardingActivity : AppCompatActivity() {
    private lateinit var viewPager: ViewPager2
    private lateinit var skipButton: Button
    private lateinit var nextButton: Button
    private lateinit var onboardingAdapter: OnboardingAdapter
    private lateinit var paginationDots: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding)

        viewPager = findViewById(R.id.onboardingViewPager)
        skipButton = findViewById(R.id.skipButton)
        nextButton = findViewById(R.id.nextButton)
        paginationDots = findViewById(R.id.paginationDots)

        // Sample onboarding data
        val onboardingItems = listOf(
            OnboardingItem(R.drawable.ic_home, "Track games"),
            OnboardingItem(R.drawable.ic_person, "Manage lineups"),
            OnboardingItem(R.drawable.ic_settings, "Customize your experience")
        )
        onboardingAdapter = OnboardingAdapter(onboardingItems)
        viewPager.adapter = onboardingAdapter

        // TODO: Set up pagination dots
        // TODO: Log onboarding events with analytics

        skipButton.setOnClickListener {
            // TODO: Backend - Log onboarding skip event
            startActivity(Intent(this, UnifiedEntryActivity::class.java))
            finish()
        }
        nextButton.setOnClickListener {
            if (viewPager.currentItem < onboardingAdapter.itemCount - 1) {
                viewPager.currentItem = viewPager.currentItem + 1
            } else {
                startActivity(Intent(this, UnifiedEntryActivity::class.java))
                finish()
            }
        }
    }
} 