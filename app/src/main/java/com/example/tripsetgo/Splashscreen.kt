package com.example.tripsetgo

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.example.tripsetgo.databinding.SplashBinding

class Splashscreen : AppCompatActivity() {

    private lateinit var binding: SplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = SplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Load and play the video
        val videoUri = Uri.parse("android.resource://" + packageName + "/" + R.raw.gif)
        binding.videoView.setVideoURI(videoUri)
        binding.videoView.setOnCompletionListener {
            // After the video is complete, start the next activity
            val intent = Intent(this, OnboardingActivity::class.java)
            startActivity(intent)
            finish()
        }
        binding.videoView.start()

        // Alternative: Use a Handler to move to the next activity after the video duration
        // Handler(Looper.getMainLooper()).postDelayed({
        //     val intent = Intent(this, OnboardingActivity::class.java)
        //     startActivity(intent)
        //     finish()
        // }, 3000) // Change the delay time as needed
    }
}
