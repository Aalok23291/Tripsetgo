package com.example.tripsetgo
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.example.tripsetgo.R
import com.example.tripsetgo.SignInActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.tbuonomo.viewpagerdotsindicator.SpringDotsIndicator
import android.widget.ImageButton

class OnboardingActivity : AppCompatActivity() {


    private lateinit var viewPager: ViewPager2
    private lateinit var skipButton: MaterialButton
    private lateinit var nextButton: ImageButton
    private lateinit var dotsIndicator: SpringDotsIndicator


    private val images = listOf(
        R.drawable.onboardingpage1,
        R.drawable.onboardingpage2,
        R.drawable.onbo3,
        R.drawable.onbo4,
        R.drawable.onbo5,
        R.drawable.onbo6
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.onboardingactivity)

        viewPager = findViewById(R.id.viewPager)
        dotsIndicator = findViewById(R.id.dotsIndicator)
        skipButton = findViewById(R.id.skipButton)
        nextButton = findViewById(R.id.nextButton)

        val adapter = OnboardingAdapter(images)
        viewPager.adapter = adapter
        dotsIndicator.setViewPager2(viewPager)


        skipButton.setOnClickListener {
            navigateToLogin()
        }

        nextButton.setOnClickListener {
            if (viewPager.currentItem < images.size - 1) {
                viewPager.currentItem = viewPager.currentItem + 1
            } else {
                navigateToLogin()
            }
        }
    }

    private fun navigateToLogin() {
        val intent = Intent(this, SignInActivity::class.java)
        startActivity(intent)
        finish()
    }
}
