package com.example.tripsetgo


import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {
    private lateinit var loginLogoutButton: Button
    private var isLoggedIn: Boolean
        get() = sharedPreferences.getBoolean("isLoggedIn", false)
        set(value) = sharedPreferences.edit().putBoolean("isLoggedIn", value).apply()
    private val sharedPreferences by lazy {
        getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        loginLogoutButton = findViewById(R.id.loginLogoutButton)

        updateButtonText()

        loginLogoutButton.setOnClickListener {
            if (isLoggedIn) {
                // Handle logout
                isLoggedIn = false
                updateButtonText()
                // Clear user session, etc.
            } else {
                // Navigate to SignInActivity
                val intent = Intent(this, SignInActivity::class.java)
                startActivity(intent)
            }
        }
    }
    private fun updateButtonText() {
        if (isLoggedIn) {
            loginLogoutButton.text = "Logout"
        } else {
            loginLogoutButton.text = "Login"
        }
    }

    override fun onResume() {
        super.onResume()
        // Check if the user is logged in, update the isLoggedIn variable accordingly
        // For example, check from SharedPreferences
        // isLoggedIn = checkIfUserIsLoggedIn()

        updateButtonText()
    }
}