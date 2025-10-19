package com.example.wellnesstracker

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {

    private val SPLASH_DELAY: Long = 2000 // 2 seconds

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // Check if user has completed onboarding-explicit intent
        val prefs = getSharedPreferences("WellnessData", MODE_PRIVATE)
        val isOnboardingComplete = prefs.getBoolean("onboarding_complete", false)

        // Navigate after delay
        Handler(Looper.getMainLooper()).postDelayed({
            val intent = if (isOnboardingComplete) {
                Intent(this, MainActivity::class.java)
            } else {
                Intent(this, OnboardingActivity::class.java)
            }
            startActivity(intent)
            finish()
        }, SPLASH_DELAY)
    }
}