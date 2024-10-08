package com.example.tripsetgo
import HomeFragment
import com.example.tripsetgo.ItineraryFragment


import GalleryFragment
import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val carditinerary=findViewById<ImageView>(R.id.imageItinerary)
        val carddocs=findViewById<ImageView>(R.id.imageDocs)
        val cardphotos=findViewById<ImageView>(R.id.imagePhotos)
        val cardexpense=findViewById<ImageView>(R.id.imageExpenses)
        carditinerary.setOnClickListener {
            loadFragment(ItineraryFragment())

        }
        carddocs.setOnClickListener {
            loadFragment(DocsFragment())

        }
        cardphotos.setOnClickListener {
            loadFragment(GalleryFragment())

        }
        cardexpense.setOnClickListener {
            loadFragment(ExpenseFragment())

        }



        // Inflate custom action bar
        val inflater = layoutInflater
        val customActionBarView = inflater.inflate(R.layout.custom_action_bar, null)
        supportActionBar?.setDisplayShowCustomEnabled(true)
        supportActionBar?.customView = customActionBarView

        // Initialize Firebase Auth
        firebaseAuth = FirebaseAuth.getInstance()

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        drawerLayout = findViewById(R.id.drawer_layout)
        val navigationView: NavigationView = findViewById(R.id.navigation_view)
        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottom_navigation)

        // Set up the drawer menu item click listener
        navigationView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    loadFragment(HomeFragment())
                }

                R.id.nav_logout -> {
                    firebaseAuth.signOut()
                    val intent = Intent(this, SignInActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                }
            }
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }

        // Set up the bottom navigation item click listener
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    loadFragment(HomeFragment())
                    true
                }
                R.id.nav_itinerary -> {
                    loadFragment(ItineraryFragment())
                    true
                }
                R.id.nav_docs -> {
                    loadFragment(DocsFragment())
                    true
                }
                R.id.nav_expense -> {
                    loadFragment(ExpenseFragment())
                    true
                }
                R.id.nav_photo -> {
                    loadFragment(GalleryFragment())
                    true
                }
                else -> false
            }
        }

        // Load the default fragment when the activity starts
        if (savedInstanceState == null) {
            bottomNavigationView.selectedItemId = R.id.nav_home
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction().replace(R.id.fragment_container, fragment).commit()
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}
