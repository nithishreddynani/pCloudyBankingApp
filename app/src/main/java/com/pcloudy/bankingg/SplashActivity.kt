package com.pcloudy.bankingg

import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.DecelerateInterpolator
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.pcloudy.bankingg.databinding.ActivitySplashBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Add animation to the logo and title
        binding.apply {
            splashIcon.alpha = 0f
            splashTitle.alpha = 0f

            splashIcon.animate()
                .alpha(1f)
                .setDuration(1000)
                .setInterpolator(DecelerateInterpolator())
                .start()

            splashTitle.animate()
                .alpha(1f)
                .setDuration(1000)
                .setInterpolator(DecelerateInterpolator())
                .start()
        }

        // Navigate to MainActivity after delay
        Handler(Looper.getMainLooper()).postDelayed({
            startMainActivity()
        }, 2500) // 2.5 seconds delay
    }

    private fun startMainActivity() {
        val intent = Intent(this@SplashActivity, MainActivity::class.java)
        val options = ActivityOptions.makeCustomAnimation(
            this,
            android.R.anim.fade_in,
            android.R.anim.fade_out
        )
        startActivity(intent, options.toBundle())
        finish()
    }
}