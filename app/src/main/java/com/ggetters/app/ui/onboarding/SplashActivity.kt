package com.ggetters.app.ui.onboarding

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AlphaAnimation
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.ggetters.app.R
import com.ggetters.app.ui.auth.LoginActivity

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.splash_activity)

        val logoImageView = findViewById<ImageView>(R.id.logoImageView)
        val fadeIn = AlphaAnimation(0f, 1f)
        fadeIn.duration = 1200
        logoImageView.startAnimation(fadeIn)

        Handler(Looper.getMainLooper()).postDelayed({
            // TODO: Add analytics, check login state, and navigate accordingly
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }, 1800)
    }
}